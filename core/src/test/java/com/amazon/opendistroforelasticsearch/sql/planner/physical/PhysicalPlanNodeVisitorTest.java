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

import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Todo, testing purpose, delete later.
 */
class PhysicalPlanNodeVisitorTest extends PhysicalPlanTestBase {
    @Test
    public void print_physical_plan() {
        PhysicalPlan plan = PhysicalPlanDSL.rename(
                PhysicalPlanDSL.agg(
                        PhysicalPlanDSL.filter(
                                new TestScan(),
                                dsl.equal(typeEnv(), DSL.ref("response"), DSL.literal(10))
                        ),
                        ImmutableList.of(dsl.avg(typeEnv(), DSL.ref("response"))),
                        ImmutableList.of()
                ),
                ImmutableMap.of(DSL.ref("ivalue"), DSL.ref("avg(response)"))
        );

        PhysicalPlanPrinter printer = new PhysicalPlanPrinter();
        assertEquals("Rename->\n" +
                "\tAggregation->\n" +
                "\t\tFilter->", printer.print(plan));
    }

    @Test
    public void test_PhysicalPlanVisitor_should_return_null() {
        PhysicalPlan filter = PhysicalPlanDSL.filter(
                new TestScan(),
                dsl.equal(typeEnv(), DSL.ref("response"), DSL.literal(10))
        );
        PhysicalPlan aggregation = PhysicalPlanDSL.agg(
                filter,
                ImmutableList.of(dsl.avg(typeEnv(), DSL.ref("response"))),
                ImmutableList.of()
        );
        PhysicalPlan rename = PhysicalPlanDSL.rename(
                aggregation,
                ImmutableMap.of(DSL.ref("ivalue"), DSL.ref("avg(response)"))
        );
        assertNull(filter.accept(new PhysicalPlanNodeVisitor<Integer, Object>() {
        }, null));
        assertNull(aggregation.accept(new PhysicalPlanNodeVisitor<Integer, Object>() {
        }, null));
        assertNull(rename.accept(new PhysicalPlanNodeVisitor<Integer, Object>() {
        }, null));
    }

    public static class PhysicalPlanPrinter extends PhysicalPlanNodeVisitor<String, Integer> {

        public String print(PhysicalPlan node) {
            return node.accept(this, 0);
        }

        @Override
        public String visitFilter(FilterOperator node, Integer tabs) {
            String child = node.getChild().get(0).accept(this, tabs + 1);
            StringBuilder sb = new StringBuilder();
            for (Integer i = 0; i < tabs; i++) {
                sb.append("\t");
            }
            sb.append("Filter->");
            if (!Strings.isNullOrEmpty(child)) {
                sb.append("\n");
                sb.append(child);
            }
            return sb.toString();
        }

        @Override
        public String visitAggregation(AggregationOperator node, Integer tabs) {
            String child = node.getChild().get(0).accept(this, tabs + 1);
            StringBuilder sb = new StringBuilder();
            for (Integer i = 0; i < tabs; i++) {
                sb.append("\t");
            }
            sb.append("Aggregation->");
            if (!Strings.isNullOrEmpty(child)) {
                sb.append("\n");
                sb.append(child);
            }
            return sb.toString();
        }

        @Override
        public String visitRename(RenameOperator node, Integer tabs) {
            String child = node.getChild().get(0).accept(this, tabs + 1);
            StringBuilder sb = new StringBuilder();
            for (Integer i = 0; i < tabs; i++) {
                sb.append("\t");
            }
            sb.append("Rename->");
            if (!Strings.isNullOrEmpty(child)) {
                sb.append("\n");
                sb.append(child);
            }
            return sb.toString();
        }
    }
}