/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

@ExtendWith(MockitoExtension.class)
class TruncateOperatorTest extends PhysicalPlanTestBase {

  @Mock
  private PhysicalPlan inputPlan;

  @Test
  public void headTest_InputEnd() {
    TruncateOperator plan = new TruncateOperator(new CountTestScan(),
        false, DSL.literal(true));
    List<ExprValue> result = execute(plan);
    assertEquals(11, result.size());
    assertThat(result, containsInAnyOrder(
        ExprValueUtils.tupleValue(ImmutableMap.of("id", 1, "testString", "asdf")),
        ExprValueUtils.tupleValue(ImmutableMap.of("id", 2, "testString", "asdf")),
        ExprValueUtils.tupleValue(ImmutableMap.of("id", 3, "testString", "asdf")),
        ExprValueUtils.tupleValue(ImmutableMap.of("id", 4, "testString", "asdf")),
        ExprValueUtils.tupleValue(ImmutableMap.of("id", 5, "testString", "asdf")),
        ExprValueUtils.tupleValue(ImmutableMap.of("id", 6, "testString", "asdf")),
        ExprValueUtils.tupleValue(ImmutableMap.of("id", 7, "testString", "asdf")),
        ExprValueUtils.tupleValue(ImmutableMap.of("id", 8, "testString", "asdf")),
        ExprValueUtils.tupleValue(ImmutableMap.of("id", 9, "testString", "asdf")),
        ExprValueUtils.tupleValue(ImmutableMap.of("id", 10, "testString", "asdf")),
        ExprValueUtils.tupleValue(ImmutableMap.of("id", 11, "testString", "asdf"))
    ));
  }

  @Test
  public void headTest_keepLastTrue() {
    TruncateOperator plan = new TruncateOperator(new CountTestScan(),
        true, dsl.less(DSL.ref("id", INTEGER), DSL.literal(5)));
    List<ExprValue> result = execute(plan);
    assertEquals(5, result.size());
    assertThat(result, containsInAnyOrder(
        ExprValueUtils.tupleValue(ImmutableMap.of("id", 1, "testString", "asdf")),
        ExprValueUtils.tupleValue(ImmutableMap.of("id", 2, "testString", "asdf")),
        ExprValueUtils.tupleValue(ImmutableMap.of("id", 3, "testString", "asdf")),
        ExprValueUtils.tupleValue(ImmutableMap.of("id", 4, "testString", "asdf")),
        ExprValueUtils.tupleValue(ImmutableMap.of("id", 5, "testString", "asdf"))
    ));
  }

  @Test
  public void headTest_keepLastFalse() {
    TruncateOperator plan = new TruncateOperator(new CountTestScan(),
        false, dsl.less(DSL.ref("id", INTEGER), DSL.literal(5)));
    List<ExprValue> result = execute(plan);
    assertEquals(4, result.size());
    assertThat(result, containsInAnyOrder(
        ExprValueUtils.tupleValue(ImmutableMap.of("id", 1, "testString", "asdf")),
        ExprValueUtils.tupleValue(ImmutableMap.of("id", 2, "testString", "asdf")),
        ExprValueUtils.tupleValue(ImmutableMap.of("id", 3, "testString", "asdf")),
        ExprValueUtils.tupleValue(ImmutableMap.of("id", 4, "testString", "asdf"))
    ));
  }

  @Test
  public void nullValueShouldBeenIgnored() {
    LinkedHashMap<String, ExprValue> value = new LinkedHashMap<>();
    value.put("id", LITERAL_NULL);
    when(inputPlan.hasNext()).thenReturn(true, false);
    when(inputPlan.next()).thenReturn(new ExprTupleValue(value));

    TruncateOperator plan = new TruncateOperator(inputPlan,
        false, dsl.less(DSL.ref("id", INTEGER), DSL.literal(5)));
    List<ExprValue> result = execute(plan);
    assertEquals(0, result.size());
  }

  @Test
  public void headTest_missingValueShouldBeenIgnored() {
    LinkedHashMap<String, ExprValue> value = new LinkedHashMap<>();
    value.put("id", LITERAL_MISSING);
    when(inputPlan.hasNext()).thenReturn(true, false);
    when(inputPlan.next()).thenReturn(new ExprTupleValue(value));

    TruncateOperator plan = new TruncateOperator(inputPlan,
        false, dsl.less(DSL.ref("id", INTEGER), DSL.literal(5)));
    List<ExprValue> result = execute(plan);
    assertEquals(0, result.size());
  }
}