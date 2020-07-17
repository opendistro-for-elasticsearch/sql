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

package com.amazon.opendistroforelasticsearch.sql.expression.aggregation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

public class MinAggregatorTest extends AggregationTest {

  @Test
  public void test_min_integer() {
    ExprValue result = aggregation(dsl.min(typeEnv(), DSL.ref("integer_value")), tuples);
    assertEquals(1, result.value());
  }

  @Test
  public void test_min_long() {
    ExprValue result = aggregation(dsl.min(typeEnv(), DSL.ref("long_value")), tuples);
    assertEquals(1L, result.value());
  }

  @Test
  public void test_min_float() {
    ExprValue result = aggregation(dsl.min(typeEnv(), DSL.ref("float_value")), tuples);
    assertEquals(1F, result.value());
  }

  @Test
  public void test_min_double() {
    ExprValue result = aggregation(dsl.min(typeEnv(), DSL.ref("double_value")), tuples);
    assertEquals(1D, result.value());
  }

  @Test
  public void test_min_string() {
    ExprValue result = aggregation(dsl.min(typeEnv(), DSL.ref("string_value")), tuples);
    assertEquals("f", result.value());
  }

  @Test
  public void test_min_arithmetic_expression() {
    ExprValue result = aggregation(
        dsl.min(typeEnv(), dsl.add(typeEnv(), DSL.ref("integer_value"),
            DSL.literal(ExprValueUtils.integerValue(0)))), tuples);
    assertEquals(1, result.value());
  }

  @Test
  public void test_min_unsupported_type_field() {
    MinAggregator minAggregator =
        new MinAggregator(ImmutableList.of(DSL.ref("string_value")), ExprType.STRING);
    MinAggregator.MinState minState = minAggregator.create();
    ExpressionEvaluationException exception = assertThrows(ExpressionEvaluationException.class,
        () -> minAggregator
            .iterate(
                ExprValueUtils.tupleValue(ImmutableMap.of("string_value", "m")).bindingTuples(),
                minState)
    );
    assertEquals("unexpected type [STRING] in min aggregation", exception.getMessage());
  }

  @Test
  public void test_min_null() {
    ExprValue result =
        aggregation(dsl.min(typeEnv(), DSL.ref("double_value")), tuples_with_null_and_missing);
    assertEquals(3.0, result.value());
  }

  @Test
  public void test_min_missing() {
    ExprValue result =
        aggregation(dsl.min(typeEnv(), DSL.ref("integer_value")), tuples_with_null_and_missing);
    assertEquals(1, result.value());
  }

  @Test
  public void test_min_all_missing_or_null() {
    ExprValue result =
        aggregation(dsl.min(typeEnv(), DSL.ref("integer_value")), tuples_with_all_null_or_missing);
    assertTrue(result.isNull());
  }

  @Test
  public void test_value_of() {
    ExpressionEvaluationException exception = assertThrows(ExpressionEvaluationException.class,
        () -> dsl.min(typeEnv, DSL.ref("double_value")).valueOf(valueEnv()));
    assertEquals("can't evaluate on aggregator: min", exception.getMessage());
  }

  @Test
  public void test_to_string() {
    Aggregator minAggregator = dsl.min(typeEnv(), DSL.ref("integer_value"));
    assertEquals("min(integer_value)", minAggregator.toString());
  }

  @Test
  public void test_nested_to_string() {
    Aggregator minAggregator = dsl.min(typeEnv(), dsl.add(typeEnv, DSL.ref("integer_value"),
        DSL.literal(ExprValueUtils.integerValue(10))));
    assertEquals(String.format("min(%s + %d)", DSL.ref("integer_value"), 10),
        minAggregator.toString());
  }
}
