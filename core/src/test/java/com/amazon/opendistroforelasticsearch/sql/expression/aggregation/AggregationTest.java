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

package com.amazon.opendistroforelasticsearch.sql.expression.aggregation;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.booleanValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.collectionValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.tupleValue;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionTestBase;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AggregationTest extends ExpressionTestBase {

  protected static List<ExprValue> tuples =
      Arrays.asList(
          ExprValueUtils.tupleValue(
              new ImmutableMap.Builder<String, Object>()
                  .put("integer_value", 2)
                  .put("long_value", 2L)
                  .put("string_value", "m")
                  .put("double_value", 2d)
                  .put("float_value", 2f)
                  .put("boolean_value", true)
                  .put("struct_value", ImmutableMap.of("str", 1))
                  .put("array_value", ImmutableList.of(1))
                  .build()),
          ExprValueUtils.tupleValue(
              ImmutableMap.of(
                  "integer_value",
                  1,
                  "long_value",
                  1L,
                  "string_value",
                  "f",
                  "double_value",
                  1d,
                  "float_value",
                  1f)),
          ExprValueUtils.tupleValue(
              ImmutableMap.of(
                  "integer_value",
                  3,
                  "long_value",
                  3L,
                  "string_value",
                  "m",
                  "double_value",
                  3d,
                  "float_value",
                  3f)),
          ExprValueUtils.tupleValue(
              ImmutableMap.of(
                  "integer_value",
                  4,
                  "long_value",
                  4L,
                  "string_value",
                  "f",
                  "double_value",
                  4d,
                  "float_value",
                  4f)));

  protected static List<ExprValue> tuples_with_null_and_missing =
      Arrays.asList(
          ExprValueUtils.tupleValue(
              ImmutableMap.of("integer_value", 2, "string_value", "m", "double_value", 3d)),
          ExprValueUtils.tupleValue(
              ImmutableMap.of("integer_value", 1, "string_value", "f", "double_value", 4d)),
          ExprValueUtils.tupleValue(Collections.singletonMap("double_value", null)));

  protected ExprValue aggregation(Aggregator aggregator, List<ExprValue> tuples) {
    AggregationState state = aggregator.create();
    for (ExprValue tuple : tuples) {
      aggregator.iterate(tuple.bindingTuples(), state);
    }
    return state.result();
  }
}
