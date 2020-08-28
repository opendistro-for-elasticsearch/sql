package com.amazon.opendistroforelasticsearch.sql.planner.physical;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTupleValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.storage.bindingtuple.BindingTuple;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Group the all the input {@link BindingTuple} by {@link RareTopNOperator#groupByExprList},
 * Calculate the rare result by using the {@link RareTopNOperator#fieldExprList}.
 */
@ToString
@EqualsAndHashCode
public class RareTopNOperator extends PhysicalPlan {
  @Getter
  private final PhysicalPlan input;
  @Getter
  private final Boolean rareTopFlag;
  @Getter
  private final Integer noOfResults;
  @Getter
  private final List<Expression> fieldExprList;
  @Getter
  private final List<Expression> groupByExprList;

  @EqualsAndHashCode.Exclude
  private final Group group;
  @EqualsAndHashCode.Exclude
  private Iterator<ExprValue> iterator;

  private static final Integer DEFAULT_NO_OF_RESULTS = 10;


  public RareTopNOperator(PhysicalPlan input, Boolean rareTopFlag, List<Expression> fieldExprList,
      List<Expression> groupByExprList) {
    this(input, rareTopFlag, DEFAULT_NO_OF_RESULTS, fieldExprList, groupByExprList);
  }

  /**
   * RareTopNOperator Constructor.
   *
   * @param input           Input {@link PhysicalPlan}
   * @param rareTopFlag     Flag for Rare/TopN command.
   * @param noOfResults     Number of results
   * @param fieldExprList   List of {@link Expression}
   * @param groupByExprList List of group by {@link Expression}
   */
  public RareTopNOperator(PhysicalPlan input, Boolean rareTopFlag, int noOfResults,
      List<Expression> fieldExprList,
      List<Expression> groupByExprList) {
    this.input = input;
    this.rareTopFlag = rareTopFlag;
    this.noOfResults = noOfResults;
    this.fieldExprList = fieldExprList;
    this.groupByExprList = groupByExprList;
    this.group = new Group();
  }

  @Override
  public <R, C> R accept(PhysicalPlanNodeVisitor<R, C> visitor, C context) {
    return visitor.visitRareTopN(this, context);
  }

  @Override
  public List<PhysicalPlan> getChild() {
    return Collections.singletonList(input);
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public ExprValue next() {
    return iterator.next();
  }

  @Override
  public void open() {
    super.open();
    while (input.hasNext()) {
      group.push(input.next());
    }
    iterator = group.result().iterator();
  }

  @VisibleForTesting
  @RequiredArgsConstructor
  public class Group {

    private final Map<GroupKey, List<HashMap<FieldKey, Integer>>> groupListMap = new HashMap<>();

    /**
     * Push the BindingTuple to Group. Two functions will be applied to each BindingTuple to
     * generate the {@link GroupKey} and {@link FieldKey} GroupKey = GroupKey(bindingTuple),
     * FieldKey = FieldKey(bindingTuple)
     */
    public void push(ExprValue inputValue) {
      GroupKey groupKey = new GroupKey(inputValue);
      FieldKey fieldKey = new FieldKey(inputValue);
      groupListMap.computeIfAbsent(groupKey, k -> {
        List<HashMap<FieldKey, Integer>> list = new ArrayList<>();
        HashMap<FieldKey, Integer> map = new HashMap<>();
        map.put(fieldKey, 1);
        list.add(map);
        return list;
      });
      groupListMap.computeIfPresent(groupKey, (key, fieldList) -> {
        fieldList.forEach(map -> {
          map.computeIfAbsent(fieldKey, f -> 1);
          map.computeIfPresent(fieldKey, (field, count) -> {
            return count + 1;
          });
        });
        return fieldList;
      });
    }

    /**
     * Get the list of {@link BindingTuple} for each group.
     */
    public List<ExprValue> result() {
      ImmutableList.Builder<ExprValue> resultBuilder = new ImmutableList.Builder<>();

      groupListMap.forEach((groups, list) -> {
        LinkedHashMap<String, ExprValue> map = new LinkedHashMap<>();
        list.forEach(fieldMap -> {

          List<FieldKey> resultlist = new ArrayList<>();
          if (rareTopFlag) {
            resultlist = findTop(fieldMap);
          } else {
            resultlist = findRare(fieldMap);
          }
          resultlist.forEach(field -> {
            map.putAll(groups.groupKeyMap());
            map.putAll(field.fieldKeyMap());
            resultBuilder.add(ExprTupleValue.fromExprValueMap(map));
          });
        });
      });

      return resultBuilder.build();
    }

    /**
     * Get a list of top values.
     */
    public List<FieldKey> findTop(HashMap<FieldKey, Integer> map) {
      PriorityQueue<FieldKey> topQueue = new PriorityQueue<>(new Comparator<FieldKey>() {
        @Override
        public int compare(FieldKey e1, FieldKey e2) {
          return map.get(e1) - map.get(e2);
        }
      });

      return getList(map, topQueue, noOfResults);
    }

    /**
     * Get a list of rare values.
     */
    public List<FieldKey> findRare(HashMap<FieldKey, Integer> map) {
      PriorityQueue<FieldKey> rareQueue = new PriorityQueue<>(new Comparator<FieldKey>() {
        @Override
        public int compare(FieldKey e1, FieldKey e2) {
          return map.get(e2) - map.get(e1);
        }
      });

      return getList(map, rareQueue, noOfResults);
    }

    /**
     * Get a list of result.
     */
    public List<FieldKey> getList(HashMap<FieldKey, Integer> map, PriorityQueue<FieldKey> queue,
        Integer size) {
      for (Map.Entry<FieldKey, Integer> entry : map.entrySet()) {
        queue.add(entry.getKey());
        if (queue.size() > size) {
          queue.poll();
        }
      }

      List<FieldKey> list = new ArrayList<>();
      while (!queue.isEmpty()) {
        list.add(queue.poll());
      }

      Collections.reverse(list);
      return list;
    }

  }

  /**
   * Field Key.
   */
  @EqualsAndHashCode
  @VisibleForTesting
  public class FieldKey {

    private final List<ExprValue> fieldByValueList;

    /**
     * FieldKey constructor.
     */
    public FieldKey(ExprValue value) {
      this.fieldByValueList = new ArrayList<>();
      for (Expression fieldExpr : fieldExprList) {
        this.fieldByValueList.add(fieldExpr.valueOf(value.bindingTuples()));
      }
    }

    /**
     * Return the Map of field and field value.
     */
    public LinkedHashMap<String, ExprValue> fieldKeyMap() {
      LinkedHashMap<String, ExprValue> map = new LinkedHashMap<>();
      for (int i = 0; i < fieldExprList.size(); i++) {
        map.put(fieldExprList.get(i).toString(), fieldByValueList.get(i));
      }
      return map;
    }
  }

  /**
   * Group Key.
   */
  @EqualsAndHashCode
  @VisibleForTesting
  public class GroupKey {

    private final List<ExprValue> groupByValueList;

    /**
     * GroupKey constructor.
     */
    public GroupKey(ExprValue value) {
      this.groupByValueList = new ArrayList<>();
      for (Expression groupExpr : groupByExprList) {
        this.groupByValueList.add(groupExpr.valueOf(value.bindingTuples()));
      }
    }

    /**
     * Return the Map of group field and group field value.
     */
    public LinkedHashMap<String, ExprValue> groupKeyMap() {
      LinkedHashMap<String, ExprValue> map = new LinkedHashMap<>();
      for (int i = 0; i < groupByExprList.size(); i++) {
        map.put(groupByExprList.get(i).toString(), groupByValueList.get(i));
      }
      return map;
    }
  }

}
