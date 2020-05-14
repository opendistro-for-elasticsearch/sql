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

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.AggregationState;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.Aggregator;
import com.amazon.opendistroforelasticsearch.sql.storage.bindingtuple.BindingTuple;
import com.amazon.opendistroforelasticsearch.sql.storage.bindingtuple.MapBasedBindingTuple;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Group the all the input {@link BindingTuple} by {@link AggregationOperator#groupByExprList}, calculate the
 * aggregation result by using {@link AggregationOperator#aggregatorList}
 */
@EqualsAndHashCode
@ToString
public class AggregationOperator extends PhysicalPlan {

    private final PhysicalPlan input;
    private final List<Aggregator> aggregatorList;
    private final List<Expression> groupByExprList;
    @EqualsAndHashCode.Exclude
    private final Group group;
    @EqualsAndHashCode.Exclude
    private Iterator<BindingTuple> iterator;

    public AggregationOperator(PhysicalPlan input, List<Aggregator> aggregatorList,
            List<Expression> groupByExprList) {
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
    public BindingTuple next() {
        return iterator.next();
    }

    @Override
    public void open() {
        while (input.hasNext()) {
            group.push(input.next());
        }
        iterator = group.result().iterator();
    }

    @VisibleForTesting
    @RequiredArgsConstructor
    public class Group {

        private final Map<GroupKey, List<Map.Entry<Aggregator, AggregationState>>> groupListMap = new HashMap<>();

        /**
         * Push the BindingTuple to Group. Two functions will be applied to each BindingTuple to generate the
         * {@link GroupKey} and {@link AggregationState}
         * Key = GroupKey(bindingTuple), State = Aggregator(bindingTuple)
         */
        public void push(BindingTuple bindingTuple) {
            GroupKey groupKey = new GroupKey(bindingTuple);
            groupListMap.computeIfAbsent(groupKey, k ->
                    aggregatorList.stream()
                            .map(aggregator -> new AbstractMap.SimpleEntry<>(aggregator,
                                    aggregator.create()))
                            .collect(Collectors.toList())
            );
            groupListMap.computeIfPresent(groupKey, (key, aggregatorList) -> {
                aggregatorList
                        .forEach(entry -> entry.getKey().iterate(bindingTuple, entry.getValue()));
                return aggregatorList;
            });
        }

        /**
         * Get the list of {@link BindingTuple} for each group.
         */
        public List<BindingTuple> result() {
            ImmutableList.Builder<BindingTuple> resultBuilder = new ImmutableList.Builder<>();
            for (Map.Entry<GroupKey, List<Map.Entry<Aggregator, AggregationState>>> entry : groupListMap
                    .entrySet()) {
                MapBasedBindingTuple.MapBasedBindingTupleBuilder tupleBuilder = MapBasedBindingTuple
                        .builder();
                tupleBuilder.bindingMap(entry.getKey().groupKeyMap());
                for (Map.Entry<Aggregator, AggregationState> stateEntry : entry.getValue()) {
                    tupleBuilder.binding(stateEntry.getKey().toString(),
                            stateEntry.getValue().result());
                }
                resultBuilder.add(tupleBuilder.build());
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

        public GroupKey(BindingTuple bindingTuple) {
            this.groupByValueList = new ArrayList<>();
            for (Expression groupExpr : groupByExprList) {
                this.groupByValueList.add(groupExpr.valueOf(bindingTuple));
            }
        }

        /**
         * Return the Map of group field and group field value.
         */
        public Map<String, ExprValue> groupKeyMap() {
            Map<String, ExprValue> map = new HashMap<>();
            for (int i = 0; i < groupByExprList.size(); i++) {
                map.put(groupByExprList.get(i).toString(), groupByValueList.get(i));
            }
            return map;
        }
    }
}
