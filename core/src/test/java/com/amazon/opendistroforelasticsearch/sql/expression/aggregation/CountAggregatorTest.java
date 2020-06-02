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

package com.amazon.opendistroforelasticsearch.sql.expression.aggregation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import org.junit.jupiter.api.Test;

class CountAggregatorTest extends AggregationTest {

  @Test
  public void count_integer_field_expression() {
    ExprValue result = aggregation(dsl.count(typeEnv, DSL.ref("integer_value")), tuples);
    assertEquals(4, result.value());
  }

  @Test
  public void count_long_field_expression() {
    ExprValue result = aggregation(dsl.count(typeEnv, DSL.ref("long_value")), tuples);
    assertEquals(4, result.value());
  }

  @Test
  public void count_float_field_expression() {
    ExprValue result = aggregation(dsl.count(typeEnv, DSL.ref("float_value")), tuples);
    assertEquals(4, result.value());
  }

  @Test
  public void count_double_field_expression() {
    ExprValue result = aggregation(dsl.count(typeEnv, DSL.ref("double_value")), tuples);
    assertEquals(4, result.value());
  }

  @Test
  public void count_arithmetic_expression() {
    ExprValue result = aggregation(dsl.count(typeEnv,
        dsl.multiply(typeEnv, DSL.ref("integer_value"),
            DSL.literal(ExprValueUtils.integerValue(10)))), tuples);
    assertEquals(4, result.value());
  }

  @Test
  public void count_string_field_expression() {
    ExprValue result = aggregation(dsl.count(typeEnv, DSL.ref("string_value")), tuples);
    assertEquals(4, result.value());
  }

  @Test
  public void count_boolean_field_expression() {
    ExprValue result = aggregation(dsl.count(typeEnv, DSL.ref("boolean_value")), tuples);
    assertEquals(1, result.value());
  }

  @Test
  public void count_struct_field_expression() {
    ExprValue result = aggregation(dsl.count(typeEnv, DSL.ref("struct_value")), tuples);
    assertEquals(1, result.value());
  }

  @Test
  public void count_array_field_expression() {
    ExprValue result = aggregation(dsl.count(typeEnv, DSL.ref("array_value")), tuples);
    assertEquals(1, result.value());
  }

  @Test
  public void count_with_missing() {
    ExprValue result = aggregation(dsl.count(typeEnv, DSL.ref("integer_value")),
        tuples_with_null_and_missing);
    assertEquals(2, result.value());
  }

  @Test
  public void count_with_null() {
    ExprValue result = aggregation(dsl.count(typeEnv, DSL.ref("double_value")),
        tuples_with_null_and_missing);
    assertEquals(2, result.value());
  }

  @Test
  public void valueOf() {
    ExpressionEvaluationException exception = assertThrows(ExpressionEvaluationException.class,
        () -> dsl.count(typeEnv, DSL.ref("double_value")).valueOf(valueEnv()));
    assertEquals("can't evaluate on aggregator: count", exception.getMessage());
  }

  @Test
  public void test_to_string() {
    Aggregator countAggregator = dsl.count(typeEnv, DSL.ref("integer_value"));
    assertEquals("count(integer_value)", countAggregator.toString());
  }
}