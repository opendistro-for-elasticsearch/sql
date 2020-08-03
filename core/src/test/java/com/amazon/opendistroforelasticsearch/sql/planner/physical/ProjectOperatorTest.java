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

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlanDSL.project;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectOperatorTest extends PhysicalPlanTestBase {

  @Mock
  private PhysicalPlan inputPlan;

  @Test
  public void project_one_field() {
    when(inputPlan.hasNext()).thenReturn(true, false);
    when(inputPlan.next())
        .thenReturn(ExprValueUtils.tupleValue(ImmutableMap.of("action", "GET", "response", 200)));
    PhysicalPlan plan = project(inputPlan, DSL.named("action", DSL.ref("action", STRING)));
    List<ExprValue> result = execute(plan);

    assertThat(
        result,
        allOf(
            iterableWithSize(1),
            hasItems(ExprValueUtils.tupleValue(ImmutableMap.of("action", "GET")))));
  }

  @Test
  public void project_two_field_follow_the_project_order() {
    when(inputPlan.hasNext()).thenReturn(true, false);
    when(inputPlan.next())
        .thenReturn(ExprValueUtils.tupleValue(ImmutableMap.of("action", "GET", "response", 200)));
    PhysicalPlan plan = project(inputPlan,
        DSL.named("response", DSL.ref("response", INTEGER)),
        DSL.named("action", DSL.ref("action", STRING)));
    List<ExprValue> result = execute(plan);

    assertThat(
        result,
        allOf(
            iterableWithSize(1),
            hasItems(
                ExprValueUtils.tupleValue(ImmutableMap.of("response", 200, "action", "GET")))));
  }

  @Test
  public void project_ignore_missing_value() {
    when(inputPlan.hasNext()).thenReturn(true, true, false);
    when(inputPlan.next())
        .thenReturn(ExprValueUtils.tupleValue(ImmutableMap.of("action", "GET", "response", 200)))
        .thenReturn(ExprValueUtils.tupleValue(ImmutableMap.of("action", "POST")));
    PhysicalPlan plan = project(inputPlan,
        DSL.named("response", DSL.ref("response", INTEGER)),
        DSL.named("action", DSL.ref("action", STRING)));
    List<ExprValue> result = execute(plan);

    assertThat(
        result,
        allOf(
            iterableWithSize(2),
            hasItems(
                ExprValueUtils.tupleValue(ImmutableMap.of("response", 200, "action", "GET")),
                ExprValueUtils.tupleValue(ImmutableMap.of("action", "POST")))));
  }
}
