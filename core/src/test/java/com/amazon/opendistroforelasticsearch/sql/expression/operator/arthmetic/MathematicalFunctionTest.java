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

  private static Stream<Arguments> trigonometricArguments() {
    Stream.Builder<Arguments> builder = Stream.builder();
    return builder
        .add(Arguments.of(1)).add(Arguments.of(1L)).add(Arguments.of(1F)).add(Arguments.of(1D))
        .build();
  }

  private static Stream<Arguments> trigonometricDoubleArguments() {
    Stream.Builder<Arguments> builder = Stream.builder();
    return builder
        .add(Arguments.of(1, 2)).add(Arguments.of(1L, 2L)).add(Arguments.of(1F, 2F))
        .add(Arguments.of(1D, 2D)).build();
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

  /**
   * Test acos with integer, long, float, double values.
   */
  @ParameterizedTest(name = "acos({0})")
  @MethodSource("trigonometricArguments")
  public void test_acos(Number value) {
    FunctionExpression acos = dsl.acos(DSL.literal(value));
    assertThat(
        acos.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(Math.acos(value.doubleValue()))));
    assertEquals(String.format("acos(%s)", value), acos.toString());
  }

  /**
   * Test acos with null value.
   */
  @Test
  public void acos_null_value() {
    FunctionExpression acos = dsl.acos(DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, acos.type());
    assertTrue(acos.valueOf(valueEnv()).isNull());
  }

  /**
   * Test acos with missing value.
   */
  @Test
  public void acos_missing_value() {
    FunctionExpression acos = dsl.acos(DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, acos.type());
    assertTrue(acos.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test asin with integer, long, float, double values.
   */
  @ParameterizedTest(name = "asin({0})")
  @MethodSource("trigonometricArguments")
  public void test_asin(Number value) {
    FunctionExpression asin = dsl.asin(DSL.literal(value));
    assertThat(
        asin.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(Math.asin(value.doubleValue()))));
    assertEquals(String.format("asin(%s)", value), asin.toString());
  }

  /**
   * Test asin with null value.
   */
  @Test
  public void asin_null_value() {
    FunctionExpression asin = dsl.asin(DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, asin.type());
    assertTrue(asin.valueOf(valueEnv()).isNull());
  }

  /**
   * Test asin with missing value.
   */
  @Test
  public void asin_missing_value() {
    FunctionExpression asin = dsl.asin(DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, asin.type());
    assertTrue(asin.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test atan with one argument integer, long, float, double values.
   */
  @ParameterizedTest(name = "atan({0})")
  @MethodSource("trigonometricArguments")
  public void atan_one_arg(Number value) {
    FunctionExpression atan = dsl.atan(DSL.literal(value));
    assertThat(
        atan.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(Math.atan(value.doubleValue()))));
    assertEquals(String.format("atan(%s)", value), atan.toString());
  }

  /**
   * Test atan with two arguments of integer, long, float, double values.
   */
  @ParameterizedTest(name = "atan({0}, {1})")
  @MethodSource("trigonometricDoubleArguments")
  public void atan_two_args(Number v1, Number v2) {
    FunctionExpression atan =
        dsl.atan(DSL.literal(v1), DSL.literal(v2));
    assertThat(
        atan.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(Math.atan2(v1.doubleValue(), v2.doubleValue()))));
    assertEquals(String.format("atan(%s, %s)", v1, v2), atan.toString());
  }

  /**
   * Test atan with null value.
   */
  @Test
  public void atan_null_value() {
    FunctionExpression atan = dsl.atan(DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, atan.type());
    assertTrue(atan.valueOf(valueEnv()).isNull());

    atan = dsl.atan(DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE), DSL.literal(1));
    assertEquals(DOUBLE, atan.type());
    assertTrue(atan.valueOf(valueEnv()).isNull());

    atan = dsl.atan(DSL.literal(1), DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, atan.type());
    assertTrue(atan.valueOf(valueEnv()).isNull());

    atan = dsl.atan(DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE),
        DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, atan.type());
    assertTrue(atan.valueOf(valueEnv()).isNull());
  }

  /**
   * Test atan with missing value.
   */
  @Test
  public void atan_missing_value() {
    FunctionExpression atan = dsl.atan(DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, atan.type());
    assertTrue(atan.valueOf(valueEnv()).isMissing());

    atan = dsl.atan(DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE), DSL.literal(1));
    assertEquals(DOUBLE, atan.type());
    assertTrue(atan.valueOf(valueEnv()).isMissing());

    atan = dsl.atan(DSL.literal(1), DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, atan.type());
    assertTrue(atan.valueOf(valueEnv()).isMissing());

    atan = dsl.atan(DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE),
        DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, atan.type());
    assertTrue(atan.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test atan with missing value.
   */
  @Test
  public void atan_null_missing() {
    FunctionExpression atan = dsl.atan(
        DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE),
        DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, atan.type());
    assertTrue(atan.valueOf(valueEnv()).isMissing());

    atan = dsl.atan(DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE),
        DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, atan.type());
    assertTrue(atan.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test atan2 with integer, long, float, double values.
   */
  @ParameterizedTest(name = "atan2({0}, {1})")
  @MethodSource("trigonometricDoubleArguments")
  public void test_atan2(Number v1, Number v2) {
    FunctionExpression atan2 = dsl.atan2(DSL.literal(v1), DSL.literal(v2));
    assertThat(
        atan2.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(Math.atan2(v1.doubleValue(), v2.doubleValue()))));
    assertEquals(String.format("atan2(%s, %s)", v1, v2), atan2.toString());
  }

  /**
   * Test atan2 with null value.
   */
  @Test
  public void atan2_null_value() {
    FunctionExpression atan2 = dsl.atan2(
        DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE), DSL.literal(1));
    assertEquals(DOUBLE, atan2.type());
    assertTrue(atan2.valueOf(valueEnv()).isNull());

    atan2 = dsl.atan2(DSL.literal(1), DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, atan2.type());
    assertTrue(atan2.valueOf(valueEnv()).isNull());

    atan2 = dsl.atan2(DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE),
        DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, atan2.type());
    assertTrue(atan2.valueOf(valueEnv()).isNull());
  }

  /**
   * Test atan2 with missing value.
   */
  @Test
  public void atan2_missing_value() {
    FunctionExpression atan2 = dsl.atan2(
        DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE), DSL.literal(1));
    assertEquals(DOUBLE, atan2.type());
    assertTrue(atan2.valueOf(valueEnv()).isMissing());

    atan2 = dsl.atan2(DSL.literal(1), DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, atan2.type());
    assertTrue(atan2.valueOf(valueEnv()).isMissing());

    atan2 = dsl.atan2(DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE),
        DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, atan2.type());
    assertTrue(atan2.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test atan2 with missing value.
   */
  @Test
  public void atan2_null_missing() {
    FunctionExpression atan2 = dsl.atan2(
        DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE),
        DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, atan2.type());
    assertTrue(atan2.valueOf(valueEnv()).isMissing());

    atan2 = dsl.atan2(DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE),
        DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, atan2.type());
    assertTrue(atan2.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test cos with integer, long, float, double values.
   */
  @ParameterizedTest(name = "cos({0})")
  @MethodSource("trigonometricArguments")
  public void test_cos(Number value) {
    FunctionExpression cos = dsl.cos(DSL.literal(value));
    assertThat(
        cos.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(Math.cos(value.doubleValue()))));
    assertEquals(String.format("cos(%s)", value), cos.toString());
  }

  /**
   * Test cos with null value.
   */
  @Test
  public void cos_null_value() {
    FunctionExpression cos = dsl.cos(DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, cos.type());
    assertTrue(cos.valueOf(valueEnv()).isNull());
  }

  /**
   * Test cos with missing value.
   */
  @Test
  public void cos_missing_value() {
    FunctionExpression cos = dsl.cos(DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, cos.type());
    assertTrue(cos.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test cot with integer, long, float, double values.
   */
  @ParameterizedTest(name = "cot({0})")
  @MethodSource("trigonometricArguments")
  public void test_cot(Number value) {
    FunctionExpression cot = dsl.cot(DSL.literal(value));
    assertThat(
        cot.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(1 / Math.tan(value.doubleValue()))));
    assertEquals(String.format("cot(%s)", value), cot.toString());
  }

  /**
   * Test cot with null value.
   */
  @Test
  public void cot_null_value() {
    FunctionExpression cot = dsl.cot(DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, cot.type());
    assertTrue(cot.valueOf(valueEnv()).isNull());
  }

  /**
   * Test cot with missing value.
   */
  @Test
  public void cot_missing_value() {
    FunctionExpression cot = dsl.cot(DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, cot.type());
    assertTrue(cot.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test degrees with integer, long, float, double values.
   */
  @ParameterizedTest(name = "degrees({0})")
  @MethodSource("trigonometricArguments")
  public void test_degrees(Number value) {
    FunctionExpression degrees = dsl.degrees(DSL.literal(value));
    assertThat(
        degrees.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(Math.toDegrees(value.doubleValue()))));
    assertEquals(String.format("degrees(%s)", value), degrees.toString());
  }

  /**
   * Test degrees with null value.
   */
  @Test
  public void degrees_null_value() {
    FunctionExpression degrees = dsl.degrees(DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, degrees.type());
    assertTrue(degrees.valueOf(valueEnv()).isNull());
  }

  /**
   * Test degrees with missing value.
   */
  @Test
  public void degrees_missing_value() {
    FunctionExpression degrees = dsl.degrees(DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, degrees.type());
    assertTrue(degrees.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test radians with integer, long, float, double values.
   */
  @ParameterizedTest(name = "radians({0})")
  @MethodSource("trigonometricArguments")
  public void test_radians(Number value) {
    FunctionExpression radians = dsl.radians(DSL.literal(value));
    assertThat(
        radians.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(Math.toRadians(value.doubleValue()))));
    assertEquals(String.format("radians(%s)", value), radians.toString());
  }

  /**
   * Test radians with null value.
   */
  @Test
  public void radians_null_value() {
    FunctionExpression radians = dsl.radians(DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, radians.type());
    assertTrue(radians.valueOf(valueEnv()).isNull());
  }

  /**
   * Test radians with missing value.
   */
  @Test
  public void radians_missing_value() {
    FunctionExpression radians = dsl.radians(DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, radians.type());
    assertTrue(radians.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test sin with integer, long, float, double values.
   */
  @ParameterizedTest(name = "sin({0})")
  @MethodSource("trigonometricArguments")
  public void test_sin(Number value) {
    FunctionExpression sin = dsl.sin(DSL.literal(value));
    assertThat(
        sin.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(Math.sin(value.doubleValue()))));
    assertEquals(String.format("sin(%s)", value), sin.toString());
  }

  /**
   * Test sin with null value.
   */
  @Test
  public void sin_null_value() {
    FunctionExpression sin = dsl.sin(DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, sin.type());
    assertTrue(sin.valueOf(valueEnv()).isNull());
  }

  /**
   * Test sin with missing value.
   */
  @Test
  public void sin_missing_value() {
    FunctionExpression sin = dsl.sin(DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, sin.type());
    assertTrue(sin.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test tan with integer, long, float, double values.
   */
  @ParameterizedTest(name = "tan({0})")
  @MethodSource("trigonometricArguments")
  public void test_tan(Number value) {
    FunctionExpression tan = dsl.tan(DSL.literal(value));
    assertThat(
        tan.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(Math.tan(value.doubleValue()))));
    assertEquals(String.format("tan(%s)", value), tan.toString());
  }

  /**
   * Test tan with null value.
   */
  @Test
  public void tan_null_value() {
    FunctionExpression tan = dsl.tan(DSL.ref(DOUBLE_TYPE_NULL_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, tan.type());
    assertTrue(tan.valueOf(valueEnv()).isNull());
  }

  /**
   * Test tan with missing value.
   */
  @Test
  public void tan_missing_value() {
    FunctionExpression tan = dsl.tan(DSL.ref(DOUBLE_TYPE_MISSING_VALUE_FIELD, DOUBLE));
    assertEquals(DOUBLE, tan.type());
    assertTrue(tan.valueOf(valueEnv()).isMissing());
  }
}
