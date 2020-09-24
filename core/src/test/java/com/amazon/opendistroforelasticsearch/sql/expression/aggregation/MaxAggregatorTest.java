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

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DATE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DATETIME;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.FLOAT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.LONG;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRUCT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.TIME;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.TIMESTAMP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

public class MaxAggregatorTest extends AggregationTest {

  @Test
  public void test_max_integer() {
    ExprValue result = aggregation(dsl.max(DSL.ref("integer_value", INTEGER)), tuples);
    assertEquals(4, result.value());
  }

  @Test
  public void test_max_long() {
    ExprValue result = aggregation(dsl.max(DSL.ref("long_value", LONG)), tuples);
    assertEquals(4L, result.value());
  }

  @Test
  public void test_max_float() {
    ExprValue result = aggregation(dsl.max(DSL.ref("float_value", FLOAT)), tuples);
    assertEquals(4F, result.value());
  }

  @Test
  public void test_max_double() {
    ExprValue result = aggregation(dsl.max(DSL.ref("double_value", DOUBLE)), tuples);
    assertEquals(4D, result.value());
  }

  @Test
  public void test_max_string() {
    ExprValue result = aggregation(dsl.max(DSL.ref("string_value", STRING)), tuples);
    assertEquals("n", result.value());
  }

  @Test
  public void test_max_date() {
    ExprValue result = aggregation(dsl.max(DSL.ref("date_value", DATE)), tuples);
    assertEquals("2040-01-01", result.value());
  }

  @Test
  public void test_max_datetime() {
    ExprValue result = aggregation(dsl.max(DSL.ref("datetime_value", DATETIME)), tuples);
    assertEquals("2040-01-01 07:00:00", result.value());
  }

  @Test
  public void test_max_time() {
    ExprValue result = aggregation(dsl.max(DSL.ref("time_value", TIME)), tuples);
    assertEquals("19:00:00", result.value());
  }

  @Test
  public void test_max_timestamp() {
    ExprValue result = aggregation(dsl.max(DSL.ref("timestamp_value", TIMESTAMP)), tuples);
    assertEquals("2040-01-01 07:00:00", result.value());
  }

  @Test
  public void test_max_arithmetic_expression() {
    ExprValue result = aggregation(
        dsl.max(dsl.add(DSL.ref("integer_value", INTEGER),
            DSL.literal(ExprValueUtils.integerValue(0)))), tuples);
    assertEquals(4, result.value());
  }

  @Test
  public void test_max_null() {
    ExprValue result =
        aggregation(dsl.max(DSL.ref("double_value", DOUBLE)), tuples_with_null_and_missing);
    assertEquals(4.0, result.value());
  }

  @Test
  public void test_max_missing() {
    ExprValue result =
        aggregation(dsl.max(DSL.ref("integer_value", INTEGER)), tuples_with_null_and_missing);
    assertEquals(2, result.value());
  }

  @Test
  public void test_max_all_missing_or_null() {
    ExprValue result =
        aggregation(dsl.max(DSL.ref("integer_value", INTEGER)), tuples_with_all_null_or_missing);
    assertTrue(result.isNull());
  }

  @Test
  public void test_value_of() {
    ExpressionEvaluationException exception = assertThrows(ExpressionEvaluationException.class,
        () -> dsl.max(DSL.ref("double_value", DOUBLE)).valueOf(valueEnv()));
    assertEquals("can't evaluate on aggregator: max", exception.getMessage());
  }

  @Test
  public void test_to_string() {
    Aggregator maxAggregator = dsl.max(DSL.ref("integer_value", INTEGER));
    assertEquals("max(integer_value)", maxAggregator.toString());
  }

  @Test
  public void test_nested_to_string() {
    Aggregator maxAggregator = dsl.max(dsl.add(DSL.ref("integer_value", INTEGER),
        DSL.literal(ExprValueUtils.integerValue(10))));
    assertEquals(String.format("max(+(%s, %d))", DSL.ref("integer_value", INTEGER), 10),
        maxAggregator.toString());
  }
}
