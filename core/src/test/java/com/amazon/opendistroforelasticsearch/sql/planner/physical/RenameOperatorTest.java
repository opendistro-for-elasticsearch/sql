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

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RenameOperatorTest extends PhysicalPlanTestBase {
  @Mock
  private PhysicalPlan inputPlan;

  @Test
  public void avg_aggregation_rename() {
    PhysicalPlan plan = new RenameOperator(
        new AggregationOperator(new TestScan(),
            Collections
                .singletonList(DSL.named("avg(response)", dsl.avg(DSL.ref("response", INTEGER)))),
            Collections.singletonList(DSL.named("action", DSL.ref("action", STRING)))),
        ImmutableMap.of(DSL.ref("avg(response)", DOUBLE), DSL.ref("avg", DOUBLE))
    );
    List<ExprValue> result = execute(plan);
    assertEquals(2, result.size());
    assertThat(result, containsInAnyOrder(
        ExprValueUtils.tupleValue(ImmutableMap.of("action", "GET", "avg", 268d)),
        ExprValueUtils.tupleValue(ImmutableMap.of("action", "POST", "avg", 350d))
    ));
  }

  @Test
  public void rename_int_value() {
    when(inputPlan.hasNext()).thenReturn(true, false);
    when(inputPlan.next()).thenReturn(ExprValueUtils.integerValue(1));
    PhysicalPlan plan = new RenameOperator(
        inputPlan,
        ImmutableMap.of(DSL.ref("avg(response)", DOUBLE), DSL.ref("avg", DOUBLE))
    );
    List<ExprValue> result = execute(plan);
    assertEquals(1, result.size());
    assertThat(result, containsInAnyOrder(ExprValueUtils.integerValue(1)));
  }
}
