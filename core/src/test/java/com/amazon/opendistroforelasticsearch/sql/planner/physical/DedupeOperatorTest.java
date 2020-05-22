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

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.tupleValue;
import static com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlanDSL.dedupe;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DedupeOperatorTest extends PhysicalPlanTestBase {
  @Mock private PhysicalPlan inputPlan;

  /**
   * construct the map which contain null value, because {@link ImmutableMap} doesn't support null
   * value.
   */
  private Map<String, Object> NULL_MAP =
      new HashMap<String, Object>() {
        {
          put("region", null);
          put("action", "GET");
        }
      };

  @Test
  public void dedupe_one_field() {
    when(inputPlan.hasNext()).thenReturn(true, true, true, false);
    when(inputPlan.next())
        .thenReturn(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "GET", "response", 200)))
        .thenReturn(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "POST", "response", 200)))
        .thenReturn(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "PUT", "response", 200)));

    assertThat(
        execute(dedupe(inputPlan, DSL.ref("region"))),
        contains(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "GET", "response", 200))));
  }

  @Test
  public void dedupe_one_field_no_duplication() {
    when(inputPlan.hasNext()).thenReturn(true, true, true, false);
    when(inputPlan.next())
        .thenReturn(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "GET", "response", 200)))
        .thenReturn(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "POST", "response", 200)))
        .thenReturn(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "PUT", "response", 200)));
    PhysicalPlan plan = dedupe(inputPlan, DSL.ref("action"));

    assertThat(
        execute(plan),
        contains(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "GET", "response", 200)),
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "POST", "response", 200)),
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "PUT", "response", 200))));
  }

  @Test
  public void dedupe_one_field_allow_2_duplication() {
    when(inputPlan.hasNext()).thenReturn(true, true, true, false);
    when(inputPlan.next())
        .thenReturn(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "GET", "response", 200)))
        .thenReturn(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "POST", "response", 200)))
        .thenReturn(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "PUT", "response", 200)));

    assertThat(
        execute(dedupe(inputPlan, 2, false, false, DSL.ref("region"))),
        contains(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "GET", "response", 200)),
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "POST", "response", 200))));
  }

  @Test
  public void dedupe_one_field_in_consecutive_mode() {
    when(inputPlan.hasNext()).thenReturn(true, true, true, true, false);
    when(inputPlan.next())
        .thenReturn(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "GET", "response", 200)))
        .thenReturn(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "POST", "response", 200)))
        .thenReturn(
            tupleValue(ImmutableMap.of("region", "us-west-2", "action", "POST", "response", 200)))
        .thenReturn(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "PUT", "response", 200)));

    assertThat(
        execute(dedupe(inputPlan, 1, false, true, DSL.ref("region"))),
        contains(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "GET", "response", 200)),
            tupleValue(ImmutableMap.of("region", "us-west-2", "action", "POST", "response", 200)),
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "PUT", "response", 200))));
  }

  @Test
  public void dedupe_one_field_in_consecutive_mode_all_consecutive() {
    when(inputPlan.hasNext()).thenReturn(true, true, true, false);
    when(inputPlan.next())
        .thenReturn(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "GET", "response", 200)))
        .thenReturn(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "POST", "response", 200)))
        .thenReturn(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "PUT", "response", 200)));

    assertThat(
        execute(dedupe(inputPlan, 1, false, true, DSL.ref("region"))),
        contains(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "GET", "response", 200))));
  }

  @Test
  public void dedupe_one_field_in_consecutive_mode_allow_2_duplication() {
    when(inputPlan.hasNext()).thenReturn(true, true, true, true, false);
    when(inputPlan.next())
        .thenReturn(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "GET", "response", 200)))
        .thenReturn(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "POST", "response", 200)))
        .thenReturn(
            tupleValue(ImmutableMap.of("region", "us-west-2", "action", "POST", "response", 200)))
        .thenReturn(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "PUT", "response", 200)));

    assertThat(
        execute(dedupe(inputPlan, 2, false, true, DSL.ref("region"))),
        contains(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "GET", "response", 200)),
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "POST", "response", 200)),
            tupleValue(ImmutableMap.of("region", "us-west-2", "action", "POST", "response", 200)),
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "PUT", "response", 200))));
  }

  @Test
  public void dedupe_two_field() {
    when(inputPlan.hasNext()).thenReturn(true, true, true, false);
    when(inputPlan.next())
        .thenReturn(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "GET", "response", 200)))
        .thenReturn(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "POST", "response", 200)))
        .thenReturn(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "GET", "response", 200)));

    assertThat(
        execute(dedupe(inputPlan, DSL.ref("region"), DSL.ref("action"))),
        contains(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "GET", "response", 200)),
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "POST", "response", 200))));
  }

  @Test
  public void dedupe_one_field_with_missing_value() {
    when(inputPlan.hasNext()).thenReturn(true, true, true, false);
    when(inputPlan.next())
        .thenReturn(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "GET", "response", 200)))
        .thenReturn(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "POST", "response", 200)))
        .thenReturn(
            tupleValue(ImmutableMap.of( "action", "POST", "response", 200)))
        .thenReturn(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "GET", "response", 200)));

    assertThat(
        execute(dedupe(inputPlan, DSL.ref("region"))),
        contains(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "GET", "response", 200))));
  }

  @Test
  public void dedupe_one_field_with_missing_value_keep_empty() {
    when(inputPlan.hasNext()).thenReturn(true, true, true, false);
    when(inputPlan.next())
        .thenReturn(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "GET", "response", 200)))
        .thenReturn(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "POST", "response", 200)))
        .thenReturn(
            tupleValue(ImmutableMap.of( "action", "POST", "response", 200)))
        .thenReturn(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "GET", "response", 200)));

    assertThat(
        execute(dedupe(inputPlan, 1, true, false, DSL.ref("region"))),
        contains(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "GET", "response", 200)),
            tupleValue(ImmutableMap.of( "action", "POST", "response", 200))));
  }

  @Test
  public void dedupe_one_field_with_null_value() {
    when(inputPlan.hasNext()).thenReturn(true, true, true, false);
    when(inputPlan.next())
        .thenReturn(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "GET", "response", 200)))
        .thenReturn(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "POST", "response", 200)))
        .thenReturn(
            tupleValue(NULL_MAP))
        .thenReturn(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "GET", "response", 200)));

    assertThat(
        execute(dedupe(inputPlan, DSL.ref("region"))),
        contains(
            tupleValue(ImmutableMap.of("region", "us-east-1", "action", "GET", "response", 200))));
  }
}
