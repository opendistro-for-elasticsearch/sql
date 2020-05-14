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

package com.amazon.opendistroforelasticsearch.sql.planner;

import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalAggregation;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalFilter;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalRelation;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalRename;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.AggregationOperator;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.FilterOperator;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlanDSL;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlanTestBase;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.RenameOperator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlannerTest extends PhysicalPlanTestBase {
    @Mock
    private PhysicalPlan scan;

    @Test
    public void planner_test() {
        assertPhysicalPlan(
                PhysicalPlanDSL.rename(
                        PhysicalPlanDSL.agg(
                                PhysicalPlanDSL.filter(
                                        scan,
                                        dsl.equal(typeEnv(), DSL.ref("response"), DSL.literal(10))
                                ),
                                ImmutableList.of(dsl.avg(typeEnv(), DSL.ref("response"))),
                                ImmutableList.of()
                        ),
                        ImmutableMap.of(DSL.ref("ivalue"), DSL.ref("avg(response)"))
                ),
                LogicalPlanDSL.rename(
                        LogicalPlanDSL.aggregation(
                                LogicalPlanDSL.filter(
                                        LogicalPlanDSL.relation("schema"),
                                        dsl.equal(typeEnv(), DSL.ref("response"), DSL.literal(10))
                                ),
                                ImmutableList.of(dsl.avg(typeEnv(), DSL.ref("response"))),
                                ImmutableList.of()
                        ),
                        ImmutableMap.of(DSL.ref("ivalue"), DSL.ref("avg(response)"))
                )
        );
    }

    protected void assertPhysicalPlan(PhysicalPlan expected, LogicalPlan logicalPlan) {
        assertEquals(expected, analyze(logicalPlan));
    }

    protected PhysicalPlan analyze(LogicalPlan logicalPlan) {
        return new TestPlanner().plan(logicalPlan, ImmutableMap.of());
    }

    /**
     * Todo, For test coverage only. Remove it when Planner is implemented.
     */
    protected class TestPlanner extends LogicalPlanNodeVisitor<PhysicalPlan, Object> {
        public PhysicalPlan plan(LogicalPlan plan, Object o) {
            return plan.accept(this, o);
        }

        @Override
        public PhysicalPlan visitRelation(LogicalRelation plan, Object context) {
            return scan;
        }

        @Override
        public PhysicalPlan visitFilter(LogicalFilter plan, Object context) {
            return new FilterOperator(plan.getChild().get(0).accept(this, context), plan.getCondition());
        }

        @Override
        public PhysicalPlan visitAggregation(LogicalAggregation plan, Object context) {
            return new AggregationOperator(plan.getChild().get(0).accept(this, context),
                    plan.getAggregatorList(), plan.getGroupByList()
            );
        }

        @Override
        public PhysicalPlan visitRename(LogicalRename plan, Object context) {
            return new RenameOperator(plan.getChild().get(0).accept(this, context),
                    plan.getRenameMap());
        }
    }
}
