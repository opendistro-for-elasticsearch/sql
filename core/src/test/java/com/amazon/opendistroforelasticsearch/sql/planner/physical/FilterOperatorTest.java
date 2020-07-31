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

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_MISSING;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_NULL;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTupleValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.google.common.collect.ImmutableMap;
import java.util.LinkedHashMap;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;

@ExtendWith(MockitoExtension.class)
class FilterOperatorTest extends PhysicalPlanTestBase {
  @Mock
  private PhysicalPlan inputPlan;

  @Test
  public void filterTest() {
    FilterOperator plan = new FilterOperator(new TestScan(),
        dsl.equal(DSL.ref("response", INTEGER), DSL.literal(404)));
    List<ExprValue> result = execute(plan);
    assertEquals(1, result.size());
    assertThat(result, containsInAnyOrder(ExprValueUtils
        .tupleValue(ImmutableMap
            .of("ip", "209.160.24.63", "action", "GET", "response", 404, "referer",
                "www.amazon.com"))));
  }

  @Test
  public void nullValueShouldBeenIgnored() {
    LinkedHashMap<String, ExprValue> value = new LinkedHashMap<>();
    value.put("response", LITERAL_NULL);
    when(inputPlan.hasNext()).thenReturn(true, false);
    when(inputPlan.next()).thenReturn(new ExprTupleValue(value));

    FilterOperator plan = new FilterOperator(inputPlan,
        dsl.equal(DSL.ref("response", INTEGER), DSL.literal(404)));
    List<ExprValue> result = execute(plan);
    assertEquals(0, result.size());
  }

  @Test
  public void missingValueShouldBeenIgnored() {
    LinkedHashMap<String, ExprValue> value = new LinkedHashMap<>();
    value.put("response", LITERAL_MISSING);
    when(inputPlan.hasNext()).thenReturn(true, false);
    when(inputPlan.next()).thenReturn(new ExprTupleValue(value));

    FilterOperator plan = new FilterOperator(inputPlan,
        dsl.equal(DSL.ref("response", INTEGER), DSL.literal(404)));
    List<ExprValue> result = execute(plan);
    assertEquals(0, result.size());
  }
}