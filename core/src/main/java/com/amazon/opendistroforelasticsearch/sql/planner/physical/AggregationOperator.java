/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.planner.physical;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTupleValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.NamedExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.AggregationState;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.Aggregator;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.NamedAggregator;
import com.amazon.opendistroforelasticsearch.sql.storage.bindingtuple.BindingTuple;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Group the all the input {@link BindingTuple} by {@link AggregationOperator#groupByExprList},
 * calculate the aggregation result by using {@link AggregationOperator#aggregatorList}.
 */
@EqualsAndHashCode
@ToString
public class AggregationOperator extends PhysicalPlan {
  @Getter
  private final PhysicalPlan input;
  @Getter
  private final List<NamedAggregator> aggregatorList;
  @Getter
  private final List<NamedExpression> groupByExprList;
  @EqualsAndHashCode.Exclude
  private final Group group;
  @EqualsAndHashCode.Exclude
  private Iterator<ExprValue> iterator;

  /**
   * AggregationOperator Constructor.
   *
   * @param input           Input {@link PhysicalPlan}
   * @param aggregatorList  List of {@link Aggregator}
   * @param groupByExprList List of group by {@link Expression}
   */
  public AggregationOperator(PhysicalPlan input, List<NamedAggregator> aggregatorList,
                             List<NamedExpression> groupByExprList) {
    this.input = input;
    this.aggregatorList = aggregatorList;
    this.groupByExprList = groupByExprList;
    this.group = new Group();
  }

  @Override
  public <R, C> R accept(PhysicalPlanNodeVisitor<R, C> visitor, C context) {
    return visitor.visitAggregation(this, context);
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

    private final Map<GroupKey, List<Map.Entry<NamedAggregator, AggregationState>>> groupListMap =
        new HashMap<>();

    /**
     * Push the BindingTuple to Group. Two functions will be applied to each BindingTuple to
     * generate the {@link GroupKey} and {@link AggregationState}
     * Key = GroupKey(bindingTuple), State = Aggregator(bindingTuple)
     */
    public void push(ExprValue inputValue) {
      GroupKey groupKey = new GroupKey(inputValue);
      groupListMap.computeIfAbsent(groupKey, k ->
          aggregatorList.stream()
              .map(aggregator -> new AbstractMap.SimpleEntry<>(aggregator,
                  aggregator.create()))
              .collect(Collectors.toList())
      );
      groupListMap.computeIfPresent(groupKey, (key, aggregatorList) -> {
        aggregatorList
            .forEach(entry -> entry.getKey().iterate(inputValue.bindingTuples(), entry.getValue()));
        return aggregatorList;
      });
    }

    /**
     * Get the list of {@link BindingTuple} for each group.
     */
    public List<ExprValue> result() {
      ImmutableList.Builder<ExprValue> resultBuilder = new ImmutableList.Builder<>();
      for (Map.Entry<GroupKey, List<Map.Entry<NamedAggregator, AggregationState>>>
          entry : groupListMap.entrySet()) {
        LinkedHashMap<String, ExprValue> map = new LinkedHashMap<>();
        map.putAll(entry.getKey().groupKeyMap());
        for (Map.Entry<NamedAggregator, AggregationState> stateEntry : entry.getValue()) {
          map.put(stateEntry.getKey().getName(), stateEntry.getValue().result());
        }
        resultBuilder.add(ExprTupleValue.fromExprValueMap(map));
      }
      return resultBuilder.build();
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
        map.put(groupByExprList.get(i).getName(), groupByValueList.get(i));
      }
      return map;
    }
  }
}
