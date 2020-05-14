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
import com.amazon.opendistroforelasticsearch.sql.storage.bindingtuple.BindingTuple;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static com.amazon.opendistroforelasticsearch.sql.utils.MatcherUtils.tuple;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AggregationOperatorTest extends PhysicalPlanTestBase {
    @Test
    public void avg_aggregation() {
        PhysicalPlan plan = new AggregationOperator(new TestScan(),
                Arrays.asList(dsl.avg(typeEnv(), DSL.ref("response"))),
                Arrays.asList(DSL.ref("action")));
        List<BindingTuple> result = execute(plan);
        assertEquals(2, result.size());
        assertThat(result, containsInAnyOrder(
                tuple(ImmutableMap.of("action", "GET", "avg(response)", 268d)),
                tuple(ImmutableMap.of("action", "POST", "avg(response)", 350d))
        ));
    }

    @Test
    public void avg_aggregation_rename() {
        PhysicalPlan plan = new RenameOperator(
                new AggregationOperator(new TestScan(),
                        Arrays.asList(dsl.avg(typeEnv(), DSL.ref("response"))),
                        Arrays.asList(DSL.ref("action"))),
                ImmutableMap.of(DSL.ref("avg"), DSL.ref("avg(response)"))
        );
        List<BindingTuple> result = execute(plan);
        assertEquals(2, result.size());
        assertThat(result, containsInAnyOrder(
                tuple(ImmutableMap.of("action", "GET", "avg", 268d)),
                tuple(ImmutableMap.of("action", "POST", "avg", 350d))
        ));
    }
}