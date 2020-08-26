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
 * Group the all the input {@link BindingTuple} by {@link RareOperator#groupByExprList}, Calculate
 * the rare result by using the {@link RareOperator#fieldExprList}.
 */
@EqualsAndHashCode
@ToString
public class RareOperator extends PhysicalPlan {

  @Getter
  private final PhysicalPlan input;
  @Getter
  private final List<Expression> fieldExprList;
  @Getter
  private final List<Expression> groupByExprList;

  @EqualsAndHashCode.Exclude
  private final Group group;
  @EqualsAndHashCode.Exclude
  private Iterator<ExprValue> iterator;

  private static final Integer DEFAULT_NO_OF_RESULTS = 10;

  /**
   * RareOperator Constructor.
   *
   * @param input           Input {@link PhysicalPlan}
   * @param fieldExprList   List of {@link Expression}
   * @param groupByExprList List of group by {@link Expression}
   */
  public RareOperator(PhysicalPlan input, List<Expression> fieldExprList,
      List<Expression> groupByExprList) {
    this.input = input;
    this.fieldExprList = fieldExprList;
    this.groupByExprList = groupByExprList;
    this.group = new Group();
  }

  @Override
  public <R, C> R accept(PhysicalPlanNodeVisitor<R, C> visitor, C context) {
    return visitor.visitRare(this, context);
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
          List<FieldKey> rareList = findRare(fieldMap);
          rareList.forEach(rareField -> {
            map.putAll(groups.groupKeyMap());
            map.putAll(rareField.fieldKeyMap());
            resultBuilder.add(ExprTupleValue.fromExprValueMap(map));
          });
        });
      });

      return resultBuilder.build();
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

      for (Map.Entry<FieldKey, Integer> entry : map.entrySet()) {
        rareQueue.add(entry.getKey());
        if (rareQueue.size() > DEFAULT_NO_OF_RESULTS) {
          rareQueue.poll();
        }
      }

      List<FieldKey> rareList = new ArrayList<>();
      while (!rareQueue.isEmpty()) {
        rareList.add(rareQueue.poll());
      }

      Collections.reverse(rareList);
      return rareList;
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