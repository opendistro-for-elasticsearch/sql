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
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.getDoubleValue;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.FLOAT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.LONG;
import static com.amazon.opendistroforelasticsearch.sql.utils.MatcherUtils.hasType;
import static com.amazon.opendistroforelasticsearch.sql.utils.MatcherUtils.hasValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionTestBase;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
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
  private static Stream<Arguments> testLogIntegerArguments() {
    Stream.Builder<Arguments> builder = Stream.builder();
    return builder.add(Arguments.of(2, 2)).build();
  }

  private static Stream<Arguments> testLogLongArguments() {
    Stream.Builder<Arguments> builder = Stream.builder();
    return builder.add(Arguments.of(2L, 2L)).build();
  }

  private static Stream<Arguments> testLogFloatArguments() {
    Stream.Builder<Arguments> builder = Stream.builder();
    return builder.add(Arguments.of(2F, 2F)).build();
  }

  private static Stream<Arguments> testLogDoubleArguments() {
    Stream.Builder<Arguments> builder = Stream.builder();
    return builder.add(Arguments.of(2D, 2D)).build();
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
   * Test ceil/ceiling with integer value.
   */
  @ParameterizedTest(name = "ceil({0})")
  @ValueSource(ints = {2, -2})
  public void ceil_int_value(Integer value) {
    FunctionExpression ceil = dsl.ceil(DSL.literal(value));
    assertThat(
        ceil.valueOf(valueEnv()),
        allOf(hasType(INTEGER), hasValue((int) Math.ceil(value))));
    assertEquals(String.format("ceil(%s)", value.toString()), ceil.toString());

    FunctionExpression ceiling = dsl.ceiling(DSL.literal(value));
    assertThat(
        ceiling.valueOf(valueEnv()), allOf(hasType(INTEGER), hasValue((int) Math.ceil(value))));
    assertEquals(String.format("ceiling(%s)", value.toString()), ceiling.toString());
  }

  /**
   * Test ceil/ceiling with long value.
   */
  @ParameterizedTest(name = "ceil({0})")
  @ValueSource(longs = {2L, -2L})
  public void ceil_long_value(Long value) {
    FunctionExpression ceil = dsl.ceil(DSL.literal(value));
    assertThat(
        ceil.valueOf(valueEnv()), allOf(hasType(INTEGER), hasValue((int) Math.ceil(value))));
    assertEquals(String.format("ceil(%s)", value.toString()), ceil.toString());

    FunctionExpression ceiling = dsl.ceiling(DSL.literal(value));
    assertThat(
        ceiling.valueOf(valueEnv()), allOf(hasType(INTEGER), hasValue((int) Math.ceil(value))));
    assertEquals(String.format("ceiling(%s)", value.toString()), ceiling.toString());
  }

  /**
   * Test ceil/ceiling with float value.
   */
  @ParameterizedTest(name = "ceil({0})")
  @ValueSource(floats = {2F, -2F})
  public void ceil_float_value(Float value) {
    FunctionExpression ceil = dsl.ceil(DSL.literal(value));
    assertThat(
        ceil.valueOf(valueEnv()), allOf(hasType(INTEGER), hasValue((int) Math.ceil(value))));
    assertEquals(String.format("ceil(%s)", value.toString()), ceil.toString());

    FunctionExpression ceiling = dsl.ceiling(DSL.literal(value));
    assertThat(
        ceiling.valueOf(valueEnv()), allOf(hasType(INTEGER), hasValue((int) Math.ceil(value))));
    assertEquals(String.format("ceiling(%s)", value.toString()), ceiling.toString());
  }

  /**
   * Test ceil/ceiling with double value.
   */
  @ParameterizedTest(name = "ceil({0})")
  @ValueSource(doubles = {-2L, 2L})
  public void ceil_double_value(Double value) {
    FunctionExpression ceil = dsl.ceil(DSL.literal(value));
    assertThat(
        ceil.valueOf(valueEnv()), allOf(hasType(INTEGER), hasValue((int) Math.ceil(value))));
    assertEquals(String.format("ceil(%s)", value.toString()), ceil.toString());

    FunctionExpression ceiling = dsl.ceiling(DSL.literal(value));
    assertThat(
        ceiling.valueOf(valueEnv()), allOf(hasType(INTEGER), hasValue((int) Math.ceil(value))));
    assertEquals(String.format("ceiling(%s)", value.toString()), ceiling.toString());
  }

  /**
   * Test ceil/ceiling with null value.
   */
  @Test
  public void ceil_null_value() {
    FunctionExpression ceil = dsl.ceil(DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE));
    assertEquals(INTEGER, ceil.type());
    assertTrue(ceil.valueOf(valueEnv()).isNull());

    FunctionExpression ceiling = dsl.ceiling(DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE));
    assertEquals(INTEGER, ceiling.type());
    assertTrue(ceiling.valueOf(valueEnv()).isNull());
  }

  /**
   * Test ceil/ceiling with missing value.
   */
  @Test
  public void ceil_missing_value() {
    FunctionExpression ceil = dsl.ceil(DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE));
    assertEquals(INTEGER, ceil.type());
    assertTrue(ceil.valueOf(valueEnv()).isMissing());

    FunctionExpression ceiling = dsl.ceiling(DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE));
    assertEquals(INTEGER, ceiling.type());
    assertTrue(ceiling.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test exp with integer value.
   */
  @ParameterizedTest(name = "exp({0})")
  @ValueSource(ints = {-2, 2})
  public void exp_int_value(Integer value) {
    FunctionExpression exp = dsl.exp(DSL.literal(value));
    assertThat(
        exp.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(Math.exp(value))));
    assertEquals(String.format("exp(%s)", value.toString()), exp.toString());
  }

  /**
   * Test exp with long value.
   */
  @ParameterizedTest(name = "exp({0})")
  @ValueSource(longs = {-2L, 2L})
  public void exp_long_value(Long value) {
    FunctionExpression exp = dsl.exp(DSL.literal(value));
    assertThat(
        exp.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(Math.exp(value))));
    assertEquals(String.format("exp(%s)", value.toString()), exp.toString());
  }

  /**
   * Test exp with float value.
   */
  @ParameterizedTest(name = "exp({0})")
  @ValueSource(floats = {-2F, 2F})
  public void exp_float_value(Float value) {
    FunctionExpression exp = dsl.exp(DSL.literal(value));
    assertThat(
        exp.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(Math.exp(value))));
    assertEquals(String.format("exp(%s)", value.toString()), exp.toString());
  }

  /**
   * Test exp with double value.
   */
  @ParameterizedTest(name = "exp({0})")
  @ValueSource(doubles = {-2D, 2D})
  public void exp_double_value(Double value) {
    FunctionExpression exp = dsl.exp(DSL.literal(value));
    assertThat(
        exp.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(Math.exp(value))));
    assertEquals(String.format("exp(%s)", value.toString()), exp.toString());
  }

  /**
   * Test exp with null value.
   */
  @Test
  public void exp_null_value() {
    FunctionExpression exp =  dsl.exp(DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, exp.type());
    assertTrue(exp.valueOf(valueEnv()).isNull());
  }

  /**
   * Test exp with missing value.
   */
  @Test
  public void exp_missing_value() {
    FunctionExpression exp = dsl.exp(DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, exp.type());
    assertTrue(exp.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test floor with integer value.
   */
  @ParameterizedTest(name = "floor({0})")
  @ValueSource(ints = {-2, 2})
  public void floor_int_value(Integer value) {
    FunctionExpression floor = dsl.floor(DSL.literal(value));
    assertThat(
        floor.valueOf(valueEnv()),
        allOf(hasType(INTEGER), hasValue((int) Math.floor(value))));
    assertEquals(String.format("floor(%s)", value.toString()), floor.toString());
  }

  /**
   * Test floor with long value.
   */
  @ParameterizedTest(name = "floor({0})")
  @ValueSource(longs = {-2L, 2L})
  public void floor_long_value(Long value) {
    FunctionExpression floor = dsl.floor(DSL.literal(value));
    assertThat(
        floor.valueOf(valueEnv()),
        allOf(hasType(INTEGER), hasValue((int) Math.floor(value))));
    assertEquals(String.format("floor(%s)", value.toString()), floor.toString());
  }

  /**
   * Test floor with float value.
   */
  @ParameterizedTest(name = "floor({0})")
  @ValueSource(floats = {-2F, 2F})
  public void floor_float_value(Float value) {
    FunctionExpression floor = dsl.floor(DSL.literal(value));
    assertThat(
        floor.valueOf(valueEnv()),
        allOf(hasType(INTEGER), hasValue((int) Math.floor(value))));
    assertEquals(String.format("floor(%s)", value.toString()), floor.toString());
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
        allOf(hasType(INTEGER), hasValue((int) Math.floor(value))));
    assertEquals(String.format("floor(%s)", value.toString()), floor.toString());
  }

  /**
   * Test floor with null value.
   */
  @Test
  public void floor_null_value() {
    FunctionExpression floor = dsl.floor(DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE));
    assertEquals(INTEGER, floor.type());
    assertTrue(floor.valueOf(valueEnv()).isNull());
  }

  /**
   * Test floor with missing value.
   */
  @Test
  public void floor_missing_value() {
    FunctionExpression floor = dsl.floor(DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE));
    assertEquals(INTEGER, floor.type());
    assertTrue(floor.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test ln with integer value.
   */
  @ParameterizedTest(name = "ln({0})")
  @ValueSource(ints = {2, -2})
  public void ln_int_value(Integer value) {
    FunctionExpression ln = dsl.ln(DSL.literal(value));
    assertThat(
        ln.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(Math.log(value))));
    assertEquals(String.format("ln(%s)", value.toString()), ln.toString());
  }

  /**
   * Test ln with long value.
   */
  @ParameterizedTest(name = "ln({0})")
  @ValueSource(longs = {2L, -2L})
  public void ln_long_value(Long value) {
    FunctionExpression ln = dsl.ln(DSL.literal(value));
    assertThat(
        ln.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(Math.log(value))));
    assertEquals(String.format("ln(%s)", value.toString()), ln.toString());
  }

  /**
   * Test ln with float value.
   */
  @ParameterizedTest(name = "ln({0})")
  @ValueSource(floats = {2F, -2F})
  public void ln_float_value(Float value) {
    FunctionExpression ln = dsl.ln(DSL.literal(value));
    assertThat(
        ln.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(Math.log(value))));
    assertEquals(String.format("ln(%s)", value.toString()), ln.toString());
  }

  /**
   * Test ln with double value.
   */
  @ParameterizedTest(name = "ln({0})")
  @ValueSource(doubles = {2D, -2D})
  public void ln_double_value(Double value) {
    FunctionExpression ln = dsl.ln(DSL.literal(value));
    assertThat(
        ln.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(Math.log(value))));
    assertEquals(String.format("ln(%s)", value.toString()), ln.toString());
  }

  /**
   * Test ln with null value.
   */
  @Test
  public void ln_null_value() {
    FunctionExpression ln = dsl.ln(DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, ln.type());
    assertTrue(ln.valueOf(valueEnv()).isNull());
  }

  /**
   * Test ln with missing value.
   */
  @Test
  public void ln_missing_value() {
    FunctionExpression ln = dsl.ln(DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, ln.type());
    assertTrue(ln.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test log with 1 int argument.
   */
  @ParameterizedTest(name = "log({0})")
  @ValueSource(ints = {2, 3})
  public void log_int_value(Integer v) {
    FunctionExpression log = dsl.log(DSL.literal(v));
    assertEquals(log.type(), DOUBLE);
    assertThat(
        getDoubleValue(log.valueOf(valueEnv())),
        closeTo(Math.log(v), 0.0001)
    );
    assertEquals(String.format("log(%s)", v.toString()), log.toString());
  }

  /**
   * Test log with 1 long argument.
   */
  @ParameterizedTest(name = "log({0})")
  @ValueSource(longs = {2L, 3L})
  public void log_int_value(Long v) {
    FunctionExpression log = dsl.log(DSL.literal(v));
    assertEquals(log.type(), DOUBLE);
    assertThat(
        getDoubleValue(log.valueOf(valueEnv())),
        closeTo(Math.log(v), 0.0001)
    );
    assertEquals(String.format("log(%s)", v.toString()), log.toString());
  }

  /**
   * Test log with 1 float argument.
   */
  @ParameterizedTest(name = "log({0})")
  @ValueSource(floats = {2F, 3F})
  public void log_float_value(Float v) {
    FunctionExpression log = dsl.log(DSL.literal(v));
    assertEquals(log.type(), DOUBLE);
    assertThat(
        getDoubleValue(log.valueOf(valueEnv())),
        closeTo(Math.log(v), 0.0001)
    );
    assertEquals(String.format("log(%s)", v.toString()), log.toString());
  }

  /**
   * Test log with 1 double argument.
   */
  @ParameterizedTest(name = "log({0})")
  @ValueSource(doubles = {2D, 3D})
  public void log_double_value(Double v) {
    FunctionExpression log = dsl.log(DSL.literal(v));
    assertEquals(log.type(), DOUBLE);
    assertThat(
        getDoubleValue(log.valueOf(valueEnv())),
        closeTo(Math.log(v), 0.0001)
    );
    assertEquals(String.format("log(%s)", v.toString()), log.toString());
  }

  /**
   * Test log with 1 null value argument.
   */
  @Test
  public void log_null_value() {
    FunctionExpression log = dsl.log(
        DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, log.type());
    assertTrue(log.valueOf(valueEnv()).isNull());
  }

  /**
   * Test log with 1 missing value argument.
   */
  @Test
  public void log_missing_value() {
    FunctionExpression log = dsl.log(
        DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, log.type());
    assertTrue(log.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test log with 2 int arguments.
   */
  @ParameterizedTest(name = "log({0}, {1})")
  @MethodSource("testLogIntegerArguments")
  public void log_two_int_value(Integer v1, Integer v2) {
    FunctionExpression log = dsl.log(DSL.literal(v1), DSL.literal(v2));
    assertEquals(log.type(), DOUBLE);
    assertThat(
        getDoubleValue(log.valueOf(valueEnv())),
        closeTo(Math.log(v2) / Math.log(v1), 0.0001));
    assertEquals(String.format("log(%s, %s)", v1.toString(), v2.toString()), log.toString());
  }

  /**
   * Test log with 2 long arguments.
   */
  @ParameterizedTest(name = "log({0}, {1})")
  @MethodSource("testLogLongArguments")
  public void log_two_long_value(Long v1, Long v2) {
    FunctionExpression log = dsl.log(DSL.literal(v1), DSL.literal(v2));
    assertEquals(log.type(), DOUBLE);
    assertThat(
        getDoubleValue(log.valueOf(valueEnv())),
        closeTo(Math.log(v2) / Math.log(v1), 0.0001));
    assertEquals(String.format("log(%s, %s)", v1.toString(), v2.toString()), log.toString());
  }

  /**
   * Test log with 2 float arguments.
   */
  @ParameterizedTest(name = "log({0}, {1})")
  @MethodSource("testLogFloatArguments")
  public void log_two_double_value(Float v1, Float v2) {
    FunctionExpression log = dsl.log(DSL.literal(v1), DSL.literal(v2));
    assertEquals(log.type(), DOUBLE);
    assertThat(
        getDoubleValue(log.valueOf(valueEnv())),
        closeTo(Math.log(v2) / Math.log(v1), 0.0001));
    assertEquals(String.format("log(%s, %s)", v1.toString(), v2.toString()), log.toString());
  }

  /**
   * Test log with 2 double arguments.
   */
  @ParameterizedTest(name = "log({0}, {1})")
  @MethodSource("testLogDoubleArguments")
  public void log_two_double_value(Double v1, Double v2) {
    FunctionExpression log = dsl.log(DSL.literal(v1), DSL.literal(v2));
    assertEquals(log.type(), DOUBLE);
    assertThat(
        getDoubleValue(log.valueOf(valueEnv())),
        closeTo(Math.log(v2) / Math.log(v1), 0.0001));
    assertEquals(String.format("log(%s, %s)", v1.toString(), v2.toString()), log.toString());
  }

  /**
   * Test log with 2 null value arguments.
   */
  @Test
  public void log_two_null_value() {
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

  /**
   * Test log with 2 missing value arguments.
   */
  @Test
  public void log_two_missing_value() {
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

  /**
   * Test log with null and missing value arguments.
   */
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

  /**
   * Test log10 with int value.
   */
  @ParameterizedTest(name = "log10({0})")
  @ValueSource(ints = {2, 3})
  public void log10_int_value(Integer v) {
    FunctionExpression log = dsl.log10(DSL.literal(v));
    assertEquals(log.type(), DOUBLE);
    assertThat(
        getDoubleValue(log.valueOf(valueEnv())),
        closeTo(Math.log10(v), 0.0001)
    );
    assertEquals(String.format("log10(%s)", v.toString()), log.toString());
  }

  /**
   * Test log10 with long value.
   */
  @ParameterizedTest(name = "log10({0})")
  @ValueSource(longs = {2L, 3L})
  public void log10_long_value(Long v) {
    FunctionExpression log = dsl.log10(DSL.literal(v));
    assertEquals(log.type(), DOUBLE);
    assertThat(
        getDoubleValue(log.valueOf(valueEnv())),
        closeTo(Math.log10(v), 0.0001)
    );
    assertEquals(String.format("log10(%s)", v.toString()), log.toString());
  }

  /**
   * Test log10 with float value.
   */
  @ParameterizedTest(name = "log10({0})")
  @ValueSource(floats = {2F, 3F})
  public void log10_float_value(Float v) {
    FunctionExpression log = dsl.log10(DSL.literal(v));
    assertEquals(log.type(), DOUBLE);
    assertThat(
        getDoubleValue(log.valueOf(valueEnv())),
        closeTo(Math.log10(v), 0.0001)
    );
    assertEquals(String.format("log10(%s)", v.toString()), log.toString());
  }

  /**
   * Test log10 with int value.
   */
  @ParameterizedTest(name = "log10({0})")
  @ValueSource(doubles = {2D, 3D})
  public void log10_double_value(Double v) {
    FunctionExpression log = dsl.log10(DSL.literal(v));
    assertEquals(log.type(), DOUBLE);
    assertThat(
        getDoubleValue(log.valueOf(valueEnv())),
        closeTo(Math.log10(v), 0.0001)
    );
    assertEquals(String.format("log10(%s)", v.toString()), log.toString());
  }

  /**
   * Test log10 with null value.
   */
  @Test
  public void log10_null_value() {
    FunctionExpression log = dsl.log10(
        DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, log.type());
    assertTrue(log.valueOf(valueEnv()).isNull());
  }

  /**
   * Test log10 with missing value.
   */
  @Test
  public void log10_missing_value() {
    FunctionExpression log = dsl.log10(
        DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, log.type());
    assertTrue(log.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test log2 with int value.
   */
  @ParameterizedTest(name = "log10({0})")
  @ValueSource(ints = {2, 3})
  public void log2_int_value(Integer v) {
    FunctionExpression log = dsl.log2(DSL.literal(v));
    assertEquals(log.type(), DOUBLE);
    assertThat(
        getDoubleValue(log.valueOf(valueEnv())),
        closeTo(Math.log(v) / Math.log(2), 0.0001)
    );
    assertEquals(String.format("log2(%s)", v.toString()), log.toString());
  }

  /**
   * Test log2 with long value.
   */
  @ParameterizedTest(name = "log10({0})")
  @ValueSource(longs = {2L, 3L})
  public void log2_long_value(Long v) {
    FunctionExpression log = dsl.log2(DSL.literal(v));
    assertEquals(log.type(), DOUBLE);
    assertThat(
        getDoubleValue(log.valueOf(valueEnv())),
        closeTo(Math.log(v) / Math.log(2), 0.0001)
    );
    assertEquals(String.format("log2(%s)", v.toString()), log.toString());
  }

  /**
   * Test log2 with float value.
   */
  @ParameterizedTest(name = "log10({0})")
  @ValueSource(floats = {2F, 3F})
  public void log2_float_value(Float v) {
    FunctionExpression log = dsl.log2(DSL.literal(v));
    assertEquals(log.type(), DOUBLE);
    assertThat(
        getDoubleValue(log.valueOf(valueEnv())),
        closeTo(Math.log(v) / Math.log(2), 0.0001)
    );
    assertEquals(String.format("log2(%s)", v.toString()), log.toString());
  }

  /**
   * Test log2 with double value.
   */
  @ParameterizedTest(name = "log10({0})")
  @ValueSource(doubles = {2D, 3D})
  public void log2_double_value(Double v) {
    FunctionExpression log = dsl.log2(DSL.literal(v));
    assertEquals(log.type(), DOUBLE);
    assertThat(
        getDoubleValue(log.valueOf(valueEnv())),
        closeTo(Math.log(v) / Math.log(2), 0.0001)
    );
    assertEquals(String.format("log2(%s)", v.toString()), log.toString());
  }

  /**
   * Test log2 with null value.
   */
  @Test
  public void log2_null_value() {
    FunctionExpression log = dsl.log2(
        DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, log.type());
    assertTrue(log.valueOf(valueEnv()).isNull());
  }

  /**
   * Test log2 with missing value.
   */
  @Test
  public void log2_missing_value() {
    FunctionExpression log = dsl.log2(
        DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, log.type());
    assertTrue(log.valueOf(valueEnv()).isMissing());
  }
}
