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
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import org.junit.jupiter.api.Test;

class AvgAggregatorTest extends AggregationTest {

  @Test
  public void avg_field_expression() {
    ExprValue result = aggregation(dsl.avg(DSL.ref("integer_value", INTEGER)), tuples);
    assertEquals(2.5, result.value());
  }

  @Test
  public void avg_arithmetic_expression() {
    ExprValue result = aggregation(dsl.avg(
        dsl.multiply(DSL.ref("integer_value", INTEGER),
            DSL.literal(ExprValueUtils.integerValue(10)))), tuples);
    assertEquals(25.0, result.value());
  }

  @Test
  public void avg_with_missing() {
    ExprValue result =
        aggregation(dsl.avg(DSL.ref("integer_value", INTEGER)), tuples_with_null_and_missing);
    assertTrue(result.isNull());
  }

  @Test
  public void avg_with_null() {
    ExprValue result =
        aggregation(dsl.avg(DSL.ref("double_value", DOUBLE)), tuples_with_null_and_missing);
    assertTrue(result.isNull());
  }

  @Test
  public void valueOf() {
    ExpressionEvaluationException exception = assertThrows(ExpressionEvaluationException.class,
        () -> dsl.avg(DSL.ref("double_value", DOUBLE)).valueOf(valueEnv()));
    assertEquals("can't evaluate on aggregator: avg", exception.getMessage());
  }

  @Test
  public void test_to_string() {
    Aggregator avgAggregator = dsl.avg(DSL.ref("integer_value", INTEGER));
    assertEquals("avg(integer_value)", avgAggregator.toString());
  }

  @Test
  public void test_nested_to_string() {
    Aggregator avgAggregator = dsl.avg(dsl.multiply(DSL.ref("integer_value", INTEGER),
        DSL.literal(ExprValueUtils.integerValue(10))));
    assertEquals(String.format("avg(*(%s, %d))", DSL.ref("integer_value", INTEGER), 10),
        avgAggregator.toString());
  }
}