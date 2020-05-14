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

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;

class FilterOperatorTest extends PhysicalPlanTestBase {

    @Test
    public void filterTest() {
        FilterOperator plan = new FilterOperator(new TestScan(),
                dsl.equal(typeEnv(), DSL.ref("response"), DSL.literal(404)));
        List<BindingTuple> result = execute(plan);
        assertEquals(1, result.size());
        assertThat(result, containsInAnyOrder(BindingTuple.from(ImmutableMap.of("ip", "209.160.24.63", "action", "GET", "response", 404, "referer", "www.amazon.com"))));
    }
}