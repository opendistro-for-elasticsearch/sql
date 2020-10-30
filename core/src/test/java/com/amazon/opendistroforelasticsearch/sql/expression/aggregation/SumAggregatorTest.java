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

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.FLOAT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.LONG;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.SumAggregator.SumState;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

class SumAggregatorTest extends AggregationTest {

  @Test
  public void sum_integer_field_expression() {
    ExprValue result = aggregation(dsl.sum(DSL.ref("integer_value", INTEGER)), tuples);
    assertEquals(10, result.value());
  }

  @Test
  public void sum_long_field_expression() {
    ExprValue result = aggregation(dsl.sum(DSL.ref("long_value", LONG)), tuples);
    assertEquals(10L, result.value());
  }

  @Test
  public void sum_float_field_expression() {
    ExprValue result = aggregation(dsl.sum(DSL.ref("float_value", FLOAT)), tuples);
    assertEquals(10f, result.value());
  }

  @Test
  public void sum_double_field_expression() {
    ExprValue result = aggregation(dsl.sum(DSL.ref("double_value", DOUBLE)), tuples);
    assertEquals(10d, result.value());
  }

  @Test
  public void sum_arithmetic_expression() {
    ExprValue result = aggregation(dsl.sum(
        dsl.multiply(DSL.ref("integer_value", INTEGER),
            DSL.literal(ExprValueUtils.integerValue(10)))), tuples);
    assertEquals(100, result.value());
  }

  @Test
  public void sum_string_field_expression() {
    SumAggregator sumAggregator =
        new SumAggregator(ImmutableList.of(DSL.ref("string_value", STRING)), ExprCoreType.STRING);
    SumState sumState = sumAggregator.create();
    ExpressionEvaluationException exception = assertThrows(ExpressionEvaluationException.class,
        () -> sumAggregator
            .iterate(
                ExprValueUtils.tupleValue(ImmutableMap.of("string_value", "m")).bindingTuples(),
                sumState)
    );
    assertEquals("unexpected type [STRING] in sum aggregation", exception.getMessage());
  }

  @Test
  public void sum_with_missing() {
    ExprValue result =
        aggregation(dsl.sum(DSL.ref("integer_value", INTEGER)), tuples_with_null_and_missing);
    assertEquals(3, result.value());
  }

  @Test
  public void sum_with_null() {
    ExprValue result =
        aggregation(dsl.sum(DSL.ref("double_value", DOUBLE)), tuples_with_null_and_missing);
    assertEquals(7.0, result.value());
  }

  @Test
  public void sum_with_all_missing_or_null() {
    ExprValue result =
        aggregation(dsl.sum(DSL.ref("double_value", DOUBLE)), tuples_with_all_null_or_missing);
    assertTrue(result.isNull());
  }

  @Test
  public void valueOf() {
    ExpressionEvaluationException exception = assertThrows(ExpressionEvaluationException.class,
        () -> dsl.sum(DSL.ref("double_value", DOUBLE)).valueOf(valueEnv()));
    assertEquals("can't evaluate on aggregator: sum", exception.getMessage());
  }

  @Test
  public void test_to_string() {
    Aggregator sumAggregator = dsl.sum(DSL.ref("integer_value", INTEGER));
    assertEquals("sum(integer_value)", sumAggregator.toString());
  }

  @Test
  public void test_nested_to_string() {
    Aggregator sumAggregator = dsl.sum(dsl.multiply(DSL.ref("integer_value", INTEGER),
        DSL.literal(ExprValueUtils.integerValue(10))));
    assertEquals(String.format("sum(*(%s, %d))", DSL.ref("integer_value", INTEGER), 10),
        sumAggregator.toString());
  }
}