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

package com.amazon.opendistroforelasticsearch.sql.expression.operator.arthmetic;

import static com.amazon.opendistroforelasticsearch.sql.config.TestConfig.DOUBLE_TYPE_MISSING_VALUE_FIELD;
import static com.amazon.opendistroforelasticsearch.sql.config.TestConfig.DOUBLE_TYPE_NULL_VALUE_FIELD;
import static com.amazon.opendistroforelasticsearch.sql.config.TestConfig.INT_TYPE_MISSING_VALUE_FIELD;
import static com.amazon.opendistroforelasticsearch.sql.config.TestConfig.INT_TYPE_NULL_VALUE_FIELD;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.FLOAT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.LONG;
import static com.amazon.opendistroforelasticsearch.sql.utils.MatcherUtils.hasType;
import static com.amazon.opendistroforelasticsearch.sql.utils.MatcherUtils.hasValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionTestBase;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class MathematicalFunctionTest extends ExpressionTestBase {
  private static Stream<Arguments> testLogArguments() {
    List<List<Double>> arguments = Arrays.asList(
        Arrays.asList(2D, 2D), Arrays.asList(3D, 9D)
    );
    Stream.Builder<Arguments> builder = Stream.builder();
    for (List<Double> argPair : arguments) {
      builder.add(Arguments.of(argPair.get(0), argPair.get(1)));
    }
    return builder.build();
  }

  /**
   * Test abs with integer value.
   */
  @ParameterizedTest(name = "abs({0})")
  @ValueSource(ints = {-2, 2})
  public void abs_int_value(Integer value) {
    FunctionExpression abs = dsl.abs(DSL.literal(value));
    assertThat(
        abs.valueOf(valueEnv()),
        allOf(hasType(INTEGER), hasValue(Math.abs(value))));
    assertEquals(String.format("abs(%s)", value.toString()), abs.toString());
  }

  /**
   * Test abs with long value.
   */
  @ParameterizedTest(name = "abs({0})")
  @ValueSource(longs = {-2L, 2L})
  public void abs_long_value(Long value) {
    FunctionExpression abs = dsl.abs(DSL.literal(value));
    assertThat(
        abs.valueOf(valueEnv()),
        allOf(hasType(LONG), hasValue(Math.abs(value))));
    assertEquals(String.format("abs(%s)", value.toString()), abs.toString());
  }

  /**
   * Test abs with float value.
   */
  @ParameterizedTest(name = "abs({0})")
  @ValueSource(floats = {-2f, 2f})
  public void abs_float_value(Float value) {
    FunctionExpression abs = dsl.abs(DSL.literal(value));
    assertThat(
        abs.valueOf(valueEnv()),
        allOf(hasType(FLOAT), hasValue(Math.abs(value))));
    assertEquals(String.format("abs(%s)", value.toString()), abs.toString());
  }

  /**
   * Test abs with double value.
   */
  @ParameterizedTest(name = "abs({0})")
  @ValueSource(doubles = {-2L, 2L})
  public void abs_double_value(Double value) {
    FunctionExpression abs = dsl.abs(DSL.literal(value));
    assertThat(
        abs.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(Math.abs(value))));
    assertEquals(String.format("abs(%s)", value.toString()), abs.toString());
  }

  @Test
  public void abs_null_value() {
    assertTrue(dsl.abs(DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER)).valueOf(valueEnv()).isNull());
  }

  @Test
  public void abs_missing_value() {
    assertTrue(
        dsl.abs(DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER)).valueOf(valueEnv()).isMissing());
  }

  /**
   * Test ceil/ceiling with double value.
   */
  @ParameterizedTest(name = "ceil({0})")
  @ValueSource(doubles = {-2L, 2L})
  public void ceil_double_value(Double value) {
    FunctionExpression ceil = dsl.ceil(DSL.literal(value));
    assertThat(
        ceil.valueOf(valueEnv()), allOf(hasType(LONG), hasValue((long) Math.ceil(value))));
    assertEquals(String.format("ceil(%s)", value.toString()), ceil.toString());

    FunctionExpression ceiling = dsl.ceiling(DSL.literal(value));
    assertThat(
        ceiling.valueOf(valueEnv()),
        allOf(hasType(LONG), hasValue((long) Math.ceil(value))));
    assertEquals(String.format("ceiling(%s)", value.toString()), ceiling.toString());
  }

  @Test
  public void ceil_null_value() {
    FunctionExpression ceil = dsl.ceil(DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE));
    assertEquals(LONG, ceil.type());
    assertTrue(ceil.valueOf(valueEnv()).isNull());

    FunctionExpression ceiling = dsl.ceiling(DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE));
    assertEquals(LONG, ceiling.type());
    assertTrue(ceiling.valueOf(valueEnv()).isNull());
  }

  @Test
  public void ceil_missing_value() {
    FunctionExpression ceil = dsl.ceil(DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE));
    assertEquals(LONG, ceil.type());
    assertTrue(ceil.valueOf(valueEnv()).isMissing());

    ceil = dsl.ceiling(DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE));
    assertEquals(LONG, ceil.type());
    assertTrue(ceil.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test exp with double value.
   */
  @ParameterizedTest(name = "exp({0})")
  @ValueSource(doubles = {-2L, 2L})
  public void exp_double_value(Double value) {
    FunctionExpression exp = dsl.exp(DSL.literal(value));
    assertThat(
        exp.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(Math.exp(value))));
    assertEquals(String.format("exp(%s)", value.toString()), exp.toString());
  }

  @Test
  public void exp_null_value() {
    FunctionExpression exp =  dsl.exp(DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, exp.type());
    assertTrue(exp.valueOf(valueEnv()).isNull());
  }

  @Test
  public void exp_missing_value() {
    FunctionExpression exp = dsl.exp(DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, exp.type());
    assertTrue(exp.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test floor with double value.
   */
  @ParameterizedTest(name = "floor({0})")
  @ValueSource(doubles = {-2D, 2D})
  public void floor_double_value(Double value) {
    FunctionExpression floor = dsl.floor(DSL.literal(value));
    assertThat(
        floor.valueOf(valueEnv()),
        allOf(hasType(LONG), hasValue(((long) Math.floor(value)))));
    assertEquals(String.format("floor(%s)", value.toString()), floor.toString());
  }

  @Test
  public void floor_null_value() {
    FunctionExpression floor = dsl.floor(DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE));
    assertEquals(LONG, floor.type());
    assertTrue(floor.valueOf(valueEnv()).isNull());
  }

  @Test
  public void floor_missing_value() {
    FunctionExpression floor = dsl.floor(DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE));
    assertEquals(LONG, floor.type());
    assertTrue(floor.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test ln with double value.
   */
  @ParameterizedTest(name = "ln({0})")
  @ValueSource(doubles = {2D})
  public void ln_double_value(Double value) {
    FunctionExpression ln = dsl.ln(DSL.literal(value));
    assertThat(
        ln.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(Math.log(value))));
    assertEquals(String.format("ln(%s)", value.toString()), ln.toString());
  }

  @Test
  public void ln_null_value() {
    FunctionExpression ln = dsl.ln(DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, ln.type());
    assertTrue(ln.valueOf(valueEnv()).isNull());
  }

  @Test
  public void ln_missing_value() {
    FunctionExpression ln = dsl.ln(DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, ln.type());
    assertTrue(ln.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test log with double value.
   */
  @ParameterizedTest(name = "log({0}, {1})")
  @MethodSource("testLogArguments")
  public void log_double_value(Double v1, Double v2) {
    FunctionExpression log = dsl.log(DSL.literal(v1), DSL.literal(v2));
    assertThat(
        log.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(Math.log(v2) / Math.log(v1))));
    assertEquals(String.format("log(%s, %s)", v1.toString(), v2.toString()), log.toString());
  }

  @Test
  public void log_null_value() {
    FunctionExpression log = dsl.log(
        DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE), DSL.literal(2D));
    assertEquals(DOUBLE, log.type());
    assertTrue(log.valueOf(valueEnv()).isNull());

    log = dsl.log(DSL.literal(2D), DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, log.type());
    assertTrue(log.valueOf(valueEnv()).isNull());

    log = dsl.log(
        DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE),
        DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, log.type());
    assertTrue(log.valueOf(valueEnv()).isNull());
  }

  @Test
  public void log_missing_value() {
    FunctionExpression log = dsl.log(
        DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE), DSL.literal(2D));
    assertEquals(DOUBLE, log.type());
    assertTrue(log.valueOf(valueEnv()).isMissing());

    log = dsl.log(DSL.literal(2D), DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, log.type());
    assertTrue(log.valueOf(valueEnv()).isMissing());

    log = dsl.log(
        DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE),
        DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, log.type());
    assertTrue(log.valueOf(valueEnv()).isMissing());
  }

  @Test
  public void log_null_missing() {
    FunctionExpression log = dsl.log(
        DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE),
        DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, log.type());
    assertTrue(log.valueOf(valueEnv()).isMissing());

    log = dsl.log(
        DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE),
        DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, log.type());
    assertTrue(log.valueOf(valueEnv()).isMissing());
  }
}
