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

import static com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlanDSL.eval;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EvalOperatorTest extends PhysicalPlanTestBase {
  @Mock private PhysicalPlan inputPlan;
  @Mock private Environment<Expression, ExprType> environment;

  @Test
  public void create_new_field_that_contain_the_result_of_a_calculation() {
    when(inputPlan.hasNext()).thenReturn(true, false);
    when(inputPlan.next())
        .thenReturn(ExprValueUtils.tupleValue(ImmutableMap.of("distance", 100, "time", 10)));
    when(environment.resolve(DSL.ref("distance"))).thenReturn(ExprType.INTEGER);
    when(environment.resolve(DSL.ref("time"))).thenReturn(ExprType.INTEGER);

    PhysicalPlan plan =
        eval(
            inputPlan,
            ImmutablePair.of(
                DSL.ref("velocity"),
                dsl.divide(environment, DSL.ref("distance"), DSL.ref("time"))));
    assertThat(
        execute(plan),
        allOf(
            iterableWithSize(1),
            hasItems(
                ExprValueUtils.tupleValue(
                    ImmutableMap.of("distance", 100, "time", 10, "velocity", 10)))));
  }

  @Test
  public void create_multiple_field_using_field_defined_in_input_tuple() {
    when(inputPlan.hasNext()).thenReturn(true, false);
    when(inputPlan.next())
        .thenReturn(ExprValueUtils.tupleValue(ImmutableMap.of("distance", 100, "time", 10)));
    when(environment.resolve(DSL.ref("distance"))).thenReturn(ExprType.INTEGER);
    when(environment.resolve(DSL.ref("time"))).thenReturn(ExprType.INTEGER);

    PhysicalPlan plan =
        eval(
            inputPlan,
            ImmutablePair.of(
                DSL.ref("velocity"), dsl.divide(environment, DSL.ref("distance"), DSL.ref("time"))),
            ImmutablePair.of(
                DSL.ref("doubleDistance"),
                dsl.multiply(environment, DSL.ref("distance"), DSL.literal(2))));
    assertThat(
        execute(plan),
        allOf(
            iterableWithSize(1),
            hasItems(
                ExprValueUtils.tupleValue(
                    ImmutableMap.of(
                        "distance", 100, "time", 10, "velocity", 10, "doubleDistance", 200)))));
  }

  @Test
  public void create_multiple_filed_using_field_defined_in_eval_operator() {
    when(inputPlan.hasNext()).thenReturn(true, false);
    when(inputPlan.next())
        .thenReturn(ExprValueUtils.tupleValue(ImmutableMap.of("distance", 100, "time", 10)));
    when(environment.resolve(DSL.ref("distance"))).thenReturn(ExprType.INTEGER);
    when(environment.resolve(DSL.ref("time"))).thenReturn(ExprType.INTEGER);
    when(environment.resolve(DSL.ref("velocity"))).thenReturn(ExprType.INTEGER);

    PhysicalPlan plan =
        eval(
            inputPlan,
            ImmutablePair.of(
                DSL.ref("velocity"), dsl.divide(environment, DSL.ref("distance"), DSL.ref("time"))),
            ImmutablePair.of(
                DSL.ref("doubleVelocity"),
                dsl.multiply(environment, DSL.ref("velocity"), DSL.literal(2))));
    assertThat(
        execute(plan),
        allOf(
            iterableWithSize(1),
            hasItems(
                ExprValueUtils.tupleValue(
                    ImmutableMap.of(
                        "distance", 100, "time", 10, "velocity", 10, "doubleVelocity", 20)))));
  }

  @Test
  public void replace_existing_field() {
    when(inputPlan.hasNext()).thenReturn(true, false);
    when(inputPlan.next())
        .thenReturn(ExprValueUtils.tupleValue(ImmutableMap.of("distance", 100, "time", 10)));
    when(environment.resolve(DSL.ref("distance"))).thenReturn(ExprType.INTEGER);

    PhysicalPlan plan =
        eval(
            inputPlan,
            ImmutablePair.of(
                DSL.ref("distance"),
                dsl.multiply(environment, DSL.ref("distance"), DSL.literal(2))));
    assertThat(
        execute(plan),
        allOf(
            iterableWithSize(1),
            hasItems(ExprValueUtils.tupleValue(ImmutableMap.of("distance", 200, "time", 10)))));
  }

  @Test
  public void do_nothing_with_none_tuple_value() {
    when(inputPlan.hasNext()).thenReturn(true, false);
    when(inputPlan.next()).thenReturn(ExprValueUtils.integerValue(1));
    PhysicalPlan plan = eval(inputPlan, ImmutablePair.of(DSL.ref("response"), DSL.ref("referer")));
    List<ExprValue> result = execute(plan);

    assertThat(result, allOf(iterableWithSize(1), hasItems(ExprValueUtils.integerValue(1))));
  }
}
