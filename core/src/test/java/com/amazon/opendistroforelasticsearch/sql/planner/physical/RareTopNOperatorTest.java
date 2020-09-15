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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.opendistroforelasticsearch.sql.ast.tree.RareTopN.CommandType;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

public class RareTopNOperatorTest extends PhysicalPlanTestBase {

  @Test
  public void rare_without_group() {
    PhysicalPlan plan = new RareTopNOperator(new TestScan(),
        CommandType.RARE,
        Collections.singletonList(DSL.ref("action", ExprCoreType.STRING)),
        Collections.emptyList());
    List<ExprValue> result = execute(plan);
    assertEquals(2, result.size());
    assertThat(result, containsInAnyOrder(
        ExprValueUtils.tupleValue(ImmutableMap.of("action", "POST")),
        ExprValueUtils.tupleValue(ImmutableMap.of("action", "GET"))
    ));
  }

  @Test
  public void rare_with_group() {
    PhysicalPlan plan = new RareTopNOperator(new TestScan(),
        CommandType.RARE,
        Collections.singletonList(DSL.ref("response", ExprCoreType.INTEGER)),
        Collections.singletonList(DSL.ref("action", ExprCoreType.STRING)));
    List<ExprValue> result = execute(plan);
    assertEquals(4, result.size());
    assertThat(result, containsInAnyOrder(
        ExprValueUtils.tupleValue(ImmutableMap.of("action", "POST", "response", 200)),
        ExprValueUtils.tupleValue(ImmutableMap.of("action", "POST", "response", 500)),
        ExprValueUtils.tupleValue(ImmutableMap.of("action", "GET", "response", 404)),
        ExprValueUtils.tupleValue(ImmutableMap.of("action", "GET", "response", 200))
    ));
  }

  @Test
  public void top_without_group() {
    PhysicalPlan plan = new RareTopNOperator(new TestScan(),
        CommandType.TOP,
        Collections.singletonList(DSL.ref("action", ExprCoreType.STRING)),
        Collections.emptyList());
    List<ExprValue> result = execute(plan);
    assertEquals(2, result.size());
    assertThat(result, containsInAnyOrder(
        ExprValueUtils.tupleValue(ImmutableMap.of("action", "GET")),
        ExprValueUtils.tupleValue(ImmutableMap.of("action", "POST"))
    ));
  }

  @Test
  public void top_n_without_group() {
    PhysicalPlan plan = new RareTopNOperator(new TestScan(),
        CommandType.TOP,
        1,
        Collections.singletonList(DSL.ref("action", ExprCoreType.STRING)),
        Collections.emptyList());
    List<ExprValue> result = execute(plan);
    assertEquals(1, result.size());
    assertThat(result, containsInAnyOrder(
        ExprValueUtils.tupleValue(ImmutableMap.of("action", "GET"))
    ));
  }

  @Test
  public void top_n_with_group() {
    PhysicalPlan plan = new RareTopNOperator(new TestScan(),
        CommandType.TOP,
        1,
        Collections.singletonList(DSL.ref("response", ExprCoreType.INTEGER)),
        Collections.singletonList(DSL.ref("action", ExprCoreType.STRING)));
    List<ExprValue> result = execute(plan);
    assertEquals(2, result.size());
    assertThat(result, containsInAnyOrder(
        ExprValueUtils.tupleValue(ImmutableMap.of("action", "POST", "response", 200)),
        ExprValueUtils.tupleValue(ImmutableMap.of("action", "GET", "response", 200))
    ));
  }
}
