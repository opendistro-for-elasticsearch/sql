/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
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

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.junit.jupiter.api.Test;

public class LimitOperatorTest extends PhysicalPlanTestBase {

  @Test
  public void limit() {
    PhysicalPlan plan = new LimitOperator(new TestScan(), 1, 0);
    List<ExprValue> result = execute(plan);
    assertEquals(1, result.size());
    assertThat(result, containsInAnyOrder(
        ExprValueUtils.tupleValue(ImmutableMap.of(
            "ip", "209.160.24.63", "action", "GET", "response", 200, "referer", "www.amazon.com"))
    ));
  }

  @Test
  public void limit_and_offset() {
    PhysicalPlan plan = new LimitOperator(new TestScan(), 1, 1);
    List<ExprValue> result = execute(plan);
    assertEquals(1, result.size());
    assertThat(result, containsInAnyOrder(
        ExprValueUtils.tupleValue(ImmutableMap.of(
            "ip", "209.160.24.63", "action", "GET", "response", 404, "referer", "www.amazon.com"))
    ));
  }

  @Test
  public void offset_exceeds_row_number() {
    PhysicalPlan plan = new LimitOperator(new TestScan(),1, 6);
    List<ExprValue> result = execute(plan);
    assertEquals(0, result.size());
  }
}
