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

import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalFilter;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalRelation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Temporary added for UT coverage, Will be removed.
 */
@ExtendWith(MockitoExtension.class)
class AbstractPlanNodeVisitorTest {
    @Mock
    Expression expression;

    @Test
    public void logicalPlanShouldTraversable() {
        LogicalPlan logicalPlan = LogicalPlanDSL.filter(
                expression,
                LogicalPlanDSL.relation("schema")
        );

        Integer result = logicalPlan.accept(new NodesCount(), null);
        assertEquals(2, result);
    }

    @Test
    public void testAbstractPlanNodeVisitorShouldReturnNull() {
        LogicalPlan relation = LogicalPlanDSL.relation("schema");
        LogicalPlan filter = LogicalPlanDSL.filter(expression,relation);

        assertNull(relation.accept(new AbstractPlanNodeVisitor<Integer, Object>() {}, null));
        assertNull(filter.accept(new AbstractPlanNodeVisitor<Integer, Object>() {}, null));
    }

    private static class NodesCount extends AbstractPlanNodeVisitor<Integer, Object> {
        @Override
        public Integer visitRelation(LogicalRelation plan, Object context) {
            return 1;
        }

        @Override
        public Integer visitFilter(LogicalFilter plan, Object context) {
            return 1 + plan.getChild().stream()
                    .map(child -> child.accept(this, context)).collect(Collectors.summingInt(Integer::intValue));
        }
    }
}