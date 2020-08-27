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
import static com.amazon.opendistroforelasticsearch.sql.config.TestConfig.STRING_TYPE_MISSING_VALUE_FILED;
import static com.amazon.opendistroforelasticsearch.sql.config.TestConfig.STRING_TYPE_NULL_VALUE_FILED;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.getDoubleValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.getFloatValue;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.FLOAT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.LONG;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.SHORT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.utils.MatcherUtils.hasType;
import static com.amazon.opendistroforelasticsearch.sql.utils.MatcherUtils.hasValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprShortValue;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionTestBase;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import java.util.stream.Stream;
import java.util.zip.CRC32;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class MathematicalFunctionTest extends ExpressionTestBase {

  private static Stream<Arguments> testLogShortArguments() {
    Stream.Builder<Arguments> builder = Stream.builder();
    return builder.add(Arguments.of((short) 2, (short) 2)).build();
  }

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

  /**
   * Test abs with short value.
   */
  @ParameterizedTest(name = "abs({0})")
  @ValueSource(shorts = {-2, 2})
  public void abs_short_value(Short value) {
    FunctionExpression abs = dsl.abs(DSL.literal(new ExprShortValue(value)));
    assertThat(
        abs.valueOf(valueEnv()),
        allOf(hasType(SHORT), hasValue(Integer.valueOf(Math.abs(value)).shortValue())));
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
   * Test conv from decimal base with string as a number.
   */
  @ParameterizedTest(name = "conv({0})")
  @ValueSource(strings = {"1", "0", "-1"})
  public void conv_from_decimal(String value) {
    FunctionExpression conv = dsl.conv(DSL.literal(value), DSL.literal(10), DSL.literal(2));
    assertThat(
        conv.valueOf(valueEnv()),
        allOf(hasType(STRING), hasValue(Integer.toString(Integer.parseInt(value), 2))));
    assertEquals(String.format("conv(\"%s\", 10, 2)", value), conv.toString());

    conv = dsl.conv(DSL.literal(value), DSL.literal(10), DSL.literal(8));
    assertThat(
        conv.valueOf(valueEnv()),
        allOf(hasType(STRING), hasValue(Integer.toString(Integer.parseInt(value), 8))));
    assertEquals(String.format("conv(\"%s\", 10, 8)", value), conv.toString());

    conv = dsl.conv(DSL.literal(value), DSL.literal(10), DSL.literal(16));
    assertThat(
        conv.valueOf(valueEnv()),
        allOf(hasType(STRING), hasValue(Integer.toString(Integer.parseInt(value), 16))));
    assertEquals(String.format("conv(\"%s\", 10, 16)", value), conv.toString());
  }

  /**
   * Test conv from decimal base with integer as a number.
   */
  @ParameterizedTest(name = "conv({0})")
  @ValueSource(ints = {1, 0, -1})
  public void conv_from_decimal(Integer value) {
    FunctionExpression conv = dsl.conv(DSL.literal(value), DSL.literal(10), DSL.literal(2));
    assertThat(
        conv.valueOf(valueEnv()),
        allOf(hasType(STRING), hasValue(Integer.toString(value, 2))));
    assertEquals(String.format("conv(%s, 10, 2)", value), conv.toString());

    conv = dsl.conv(DSL.literal(value), DSL.literal(10), DSL.literal(8));
    assertThat(
        conv.valueOf(valueEnv()),
        allOf(hasType(STRING), hasValue(Integer.toString(value, 8))));
    assertEquals(String.format("conv(%s, 10, 8)", value), conv.toString());

    conv = dsl.conv(DSL.literal(value), DSL.literal(10), DSL.literal(16));
    assertThat(
        conv.valueOf(valueEnv()),
        allOf(hasType(STRING), hasValue(Integer.toString(value, 16))));
    assertEquals(String.format("conv(%s, 10, 16)", value), conv.toString());
  }

  /**
   * Test conv to decimal base with string as a number.
   */
  @ParameterizedTest(name = "conv({0})")
  @ValueSource(strings = {"11", "0", "11111"})
  public void conv_to_decimal(String value) {
    FunctionExpression conv = dsl.conv(DSL.literal(value), DSL.literal(2), DSL.literal(10));
    assertThat(
        conv.valueOf(valueEnv()),
        allOf(hasType(STRING), hasValue(Integer.toString(Integer.parseInt(value, 2)))));
    assertEquals(String.format("conv(\"%s\", 2, 10)", value), conv.toString());

    conv = dsl.conv(DSL.literal(value), DSL.literal(8), DSL.literal(10));
    assertThat(
        conv.valueOf(valueEnv()),
        allOf(hasType(STRING), hasValue(Integer.toString(Integer.parseInt(value, 8)))));
    assertEquals(String.format("conv(\"%s\", 8, 10)", value), conv.toString());

    conv = dsl.conv(DSL.literal(value), DSL.literal(16), DSL.literal(10));
    assertThat(
        conv.valueOf(valueEnv()),
        allOf(hasType(STRING), hasValue(Integer.toString(Integer.parseInt(value, 16)))));
    assertEquals(String.format("conv(\"%s\", 16, 10)", value), conv.toString());
  }

  /**
   * Test conv to decimal base with integer as a number.
   */
  @ParameterizedTest(name = "conv({0})")
  @ValueSource(ints = {11, 0, 11111})
  public void conv_to_decimal(Integer value) {
    FunctionExpression conv = dsl.conv(DSL.literal(value), DSL.literal(2), DSL.literal(10));
    assertThat(
        conv.valueOf(valueEnv()),
        allOf(hasType(STRING), hasValue(Integer.toString(Integer.parseInt(value.toString(), 2)))));
    assertEquals(String.format("conv(%s, 2, 10)", value), conv.toString());

    conv = dsl.conv(DSL.literal(value), DSL.literal(8), DSL.literal(10));
    assertThat(
        conv.valueOf(valueEnv()),
        allOf(hasType(STRING), hasValue(Integer.toString(Integer.parseInt(value.toString(), 8)))));
    assertEquals(String.format("conv(%s, 8, 10)", value), conv.toString());

    conv = dsl.conv(DSL.literal(value), DSL.literal(16), DSL.literal(10));
    assertThat(
        conv.valueOf(valueEnv()),
        allOf(hasType(STRING), hasValue(Integer.toString(Integer.parseInt(value.toString(), 16)))));
    assertEquals(String.format("conv(%s, 16, 10)", value), conv.toString());
  }

  /**
   * Test conv with null value.
   */
  @Test
  public void conv_null_value() {
    FunctionExpression conv = dsl.conv(
        DSL.ref(STRING_TYPE_NULL_VALUE_FILED, STRING), DSL.literal(10), DSL.literal(2));
    assertEquals(STRING, conv.type());
    assertTrue(conv.valueOf(valueEnv()).isNull());

    conv = dsl.conv(
        DSL.literal("1"), DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER), DSL.literal(2));
    assertEquals(STRING, conv.type());
    assertTrue(conv.valueOf(valueEnv()).isNull());

    conv = dsl.conv(
        DSL.literal("1"), DSL.literal(10), DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER));
    assertEquals(STRING, conv.type());
    assertTrue(conv.valueOf(valueEnv()).isNull());
  }

  /**
   * Test conv with missing value.
   */
  @Test
  public void conv_missing_value() {
    FunctionExpression conv = dsl.conv(
        DSL.ref(STRING_TYPE_MISSING_VALUE_FILED, STRING), DSL.literal(10), DSL.literal(2));
    assertEquals(STRING, conv.type());
    assertTrue(conv.valueOf(valueEnv()).isMissing());

    conv = dsl.conv(
        DSL.literal("1"), DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER), DSL.literal(2));
    assertEquals(STRING, conv.type());
    assertTrue(conv.valueOf(valueEnv()).isMissing());

    conv = dsl.conv(
        DSL.literal("1"), DSL.literal(10), DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER));
    assertEquals(STRING, conv.type());
    assertTrue(conv.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test conv with null and missing values.
   */
  @Test
  public void conv_null_missing() {
    FunctionExpression conv = dsl.conv(DSL.ref(STRING_TYPE_MISSING_VALUE_FILED, STRING),
        DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER), DSL.literal(2));
    assertEquals(STRING, conv.type());
    assertTrue(conv.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test crc32 with string value.
   */
  @ParameterizedTest(name = "crc({0})")
  @ValueSource(strings = {"odfe", "sql"})
  public void crc32_string_value(String value) {
    FunctionExpression crc = dsl.crc32(DSL.literal(value));
    CRC32 crc32 = new CRC32();
    crc32.update(value.getBytes());
    assertThat(
        crc.valueOf(valueEnv()),
        allOf(hasType(LONG), hasValue(crc32.getValue())));
    assertEquals(String.format("crc32(\"%s\")", value), crc.toString());
  }

  /**
   * Test crc32 with null value.
   */
  @Test
  public void crc32_null_value() {
    FunctionExpression crc = dsl.crc32(DSL.ref(STRING_TYPE_NULL_VALUE_FILED, STRING));
    assertEquals(LONG, crc.type());
    assertTrue(crc.valueOf(valueEnv()).isNull());
  }

  /**
   * Test crc32 with missing value.
   */
  @Test
  public void crc32_missing_value() {
    FunctionExpression crc = dsl.crc32(DSL.ref(STRING_TYPE_MISSING_VALUE_FILED, STRING));
    assertEquals(LONG, crc.type());
    assertTrue(crc.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test constant e.
   */
  @Test
  public void test_e() {
    FunctionExpression e = dsl.euler();
    assertThat(e.valueOf(valueEnv()), allOf(hasType(DOUBLE), hasValue(Math.E)));
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
   * Test mod with short value.
   */
  @ParameterizedTest(name = "mod({0}, {1})")
  @MethodSource("testLogIntegerArguments")
  public void mod_short_value(Integer v1, Integer v2) {
    FunctionExpression mod = dsl.mod(DSL.literal(v1.shortValue()), DSL.literal(v2.shortValue()));

    assertThat(
        mod.valueOf(valueEnv()),
        allOf(hasType(SHORT),
            hasValue(Integer.valueOf(v1.shortValue() % v2.shortValue()).shortValue())));
    assertEquals(String.format("mod(%s, %s)", v1, v2), mod.toString());

    mod = dsl.mod(DSL.literal(v1.shortValue()), DSL.literal(new ExprShortValue(0)));
    assertEquals(SHORT, mod.type());
    assertTrue(mod.valueOf(valueEnv()).isNull());
  }

  /**
   * Test mod with integer value.
   */
  @ParameterizedTest(name = "mod({0}, {1})")
  @MethodSource("testLogIntegerArguments")
  public void mod_int_value(Integer v1, Integer v2) {
    FunctionExpression mod = dsl.mod(DSL.literal(v1), DSL.literal(v2));
    assertThat(
        mod.valueOf(valueEnv()),
        allOf(hasType(INTEGER), hasValue(v1 % v2)));
    assertEquals(String.format("mod(%s, %s)", v1, v2), mod.toString());

    mod = dsl.mod(DSL.literal(v1), DSL.literal(0));
    assertEquals(INTEGER, mod.type());
    assertTrue(mod.valueOf(valueEnv()).isNull());
  }

  /**
   * Test mod with long value.
   */
  @ParameterizedTest(name = "mod({0}, {1})")
  @MethodSource("testLogLongArguments")
  public void mod_long_value(Long v1, Long v2) {
    FunctionExpression mod = dsl.mod(DSL.literal(v1), DSL.literal(v2));
    assertThat(
        mod.valueOf(valueEnv()),
        allOf(hasType(LONG), hasValue(v1 % v2)));
    assertEquals(String.format("mod(%s, %s)", v1, v2), mod.toString());

    mod = dsl.mod(DSL.literal(v1), DSL.literal(0));
    assertEquals(LONG, mod.type());
    assertTrue(mod.valueOf(valueEnv()).isNull());
  }

  /**
   * Test mod with long value.
   */
  @ParameterizedTest(name = "mod({0}, {1})")
  @MethodSource("testLogFloatArguments")
  public void mod_float_value(Float v1, Float v2) {
    FunctionExpression mod = dsl.mod(DSL.literal(v1), DSL.literal(v2));
    assertThat(
        mod.valueOf(valueEnv()),
        allOf(hasType(FLOAT), hasValue(v1 % v2)));
    assertEquals(String.format("mod(%s, %s)", v1, v2), mod.toString());

    mod = dsl.mod(DSL.literal(v1), DSL.literal(0));
    assertEquals(FLOAT, mod.type());
    assertTrue(mod.valueOf(valueEnv()).isNull());
  }

  /**
   * Test mod with double value.
   */
  @ParameterizedTest(name = "mod({0}, {1})")
  @MethodSource("testLogDoubleArguments")
  public void mod_double_value(Double v1, Double v2) {
    FunctionExpression mod = dsl.mod(DSL.literal(v1), DSL.literal(v2));
    assertThat(
        mod.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(v1 % v2)));
    assertEquals(String.format("mod(%s, %s)", v1, v2), mod.toString());

    mod = dsl.mod(DSL.literal(v1), DSL.literal(0));
    assertEquals(DOUBLE, mod.type());
    assertTrue(mod.valueOf(valueEnv()).isNull());
  }

  /**
   * Test mod with null value.
   */
  @Test
  public void mod_null_value() {
    FunctionExpression mod = dsl.mod(DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER), DSL.literal(1));
    assertEquals(INTEGER, mod.type());
    assertTrue(mod.valueOf(valueEnv()).isNull());

    mod = dsl.mod(DSL.literal(1), DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER));
    assertEquals(INTEGER, mod.type());
    assertTrue(mod.valueOf(valueEnv()).isNull());

    mod = dsl.mod(
        DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER), DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER));
    assertEquals(INTEGER, mod.type());
    assertTrue(mod.valueOf(valueEnv()).isNull());
  }

  /**
   * Test mod with missing value.
   */
  @Test
  public void mod_missing_value() {
    FunctionExpression mod =
        dsl.mod(DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER), DSL.literal(1));
    assertEquals(INTEGER, mod.type());
    assertTrue(mod.valueOf(valueEnv()).isMissing());

    mod = dsl.mod(DSL.literal(1), DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER));
    assertEquals(INTEGER, mod.type());
    assertTrue(mod.valueOf(valueEnv()).isMissing());

    mod = dsl.mod(
        DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER),
        DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER));
    assertEquals(INTEGER, mod.type());
    assertTrue(mod.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test mod with null and missing values.
   */
  @Test
  public void mod_null_missing() {
    FunctionExpression mod = dsl.mod(DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER),
        DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER));
    assertEquals(INTEGER, mod.type());
    assertTrue(mod.valueOf(valueEnv()).isMissing());

    mod = dsl.mod(DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER),
        DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER));
    assertEquals(INTEGER, mod.type());
    assertTrue(mod.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test pow/power with short value.
   */
  @ParameterizedTest(name = "pow({0}, {1}")
  @MethodSource("testLogShortArguments")
  public void pow_short_value(Short v1, Short v2) {
    FunctionExpression pow = dsl.pow(DSL.literal(v1), DSL.literal(v2));
    assertThat(
        pow.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(Math.pow(v1, v2))));
    assertEquals(String.format("pow(%s, %s)", v1, v2), pow.toString());

    FunctionExpression power =
        dsl.power(DSL.literal(v1), DSL.literal(v2));
    assertThat(
        power.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(Math.pow(v1, v2))));
    assertEquals(String.format("pow(%s, %s)", v1, v2), pow.toString());
  }

  /**
   * Test pow/power with integer value.
   */
  @ParameterizedTest(name = "pow({0}, {1}")
  @MethodSource("testLogIntegerArguments")
  public void pow_int_value(Integer v1, Integer v2) {
    FunctionExpression pow = dsl.pow(DSL.literal(v1), DSL.literal(v2));
    assertThat(
        pow.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(Math.pow(v1, v2))));
    assertEquals(String.format("pow(%s, %s)", v1, v2), pow.toString());

    FunctionExpression power = dsl.power(DSL.literal(v1), DSL.literal(v2));
    assertThat(
        power.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(Math.pow(v1, v2))));
    assertEquals(String.format("pow(%s, %s)", v1, v2), pow.toString());
  }

  /**
   * Test pow/power with long value.
   */
  @ParameterizedTest(name = "pow({0}, {1}")
  @MethodSource("testLogLongArguments")
  public void pow_long_value(Long v1, Long v2) {
    FunctionExpression pow = dsl.pow(DSL.literal(v1), DSL.literal(v2));
    assertThat(
        pow.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(Math.pow(v1, v2))));
    assertEquals(String.format("pow(%s, %s)", v1, v2), pow.toString());

    FunctionExpression power = dsl.power(DSL.literal(v1), DSL.literal(v2));
    assertThat(
        power.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(Math.pow(v1, v2))));
    assertEquals(String.format("pow(%s, %s)", v1, v2), pow.toString());
  }

  /**
   * Test pow/power with float value.
   */
  @ParameterizedTest(name = "pow({0}, {1}")
  @MethodSource("testLogFloatArguments")
  public void pow_float_value(Float v1, Float v2) {
    FunctionExpression pow = dsl.pow(DSL.literal(v1), DSL.literal(v2));
    assertThat(
        pow.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(Math.pow(v1, v2))));
    assertEquals(String.format("pow(%s, %s)", v1, v2), pow.toString());

    FunctionExpression power = dsl.power(DSL.literal(v1), DSL.literal(v2));
    assertThat(
        power.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(Math.pow(v1, v2))));
    assertEquals(String.format("pow(%s, %s)", v1, v2), pow.toString());
  }

  /**
   * Test pow/power with double value.
   */
  @ParameterizedTest(name = "pow({0}, {1}")
  @MethodSource("testLogDoubleArguments")
  public void pow_double_value(Double v1, Double v2) {
    FunctionExpression pow = dsl.pow(DSL.literal(v1), DSL.literal(v2));
    assertThat(
        pow.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(Math.pow(v1, v2))));
    assertEquals(String.format("pow(%s, %s)", v1, v2), pow.toString());

    FunctionExpression power = dsl.power(DSL.literal(v1), DSL.literal(v2));
    assertThat(
        power.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(Math.pow(v1, v2))));
    assertEquals(String.format("pow(%s, %s)", v1, v2), pow.toString());
  }

  /**
   * Test pow/power with null value.
   */
  @Test
  public void pow_null_value() {
    FunctionExpression pow = dsl.pow(DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER), DSL.literal(1));
    assertEquals(DOUBLE, pow.type());
    assertTrue(pow.valueOf(valueEnv()).isNull());

    dsl.pow(DSL.literal(1), DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER));
    assertEquals(DOUBLE, pow.type());
    assertTrue(pow.valueOf(valueEnv()).isNull());

    dsl.pow(
        DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER), DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER));
    assertEquals(DOUBLE, pow.type());
    assertTrue(pow.valueOf(valueEnv()).isNull());

    FunctionExpression power =
        dsl.power(DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER), DSL.literal(1));
    assertEquals(DOUBLE, power.type());
    assertTrue(power.valueOf(valueEnv()).isNull());

    power = dsl.power(DSL.literal(1), DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER));
    assertEquals(DOUBLE, power.type());
    assertTrue(power.valueOf(valueEnv()).isNull());

    power = dsl.power(
        DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER), DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER));
    assertEquals(DOUBLE, power.type());
    assertTrue(power.valueOf(valueEnv()).isNull());
  }

  /**
   * Test pow/power with missing value.
   */
  @Test
  public void pow_missing_value() {
    FunctionExpression pow =
        dsl.pow(DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER), DSL.literal(1));
    assertEquals(DOUBLE, pow.type());
    assertTrue(pow.valueOf(valueEnv()).isMissing());

    dsl.pow(DSL.literal(1), DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER));
    assertEquals(DOUBLE, pow.type());
    assertTrue(pow.valueOf(valueEnv()).isMissing());

    dsl.pow(DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER),
        DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER));
    assertEquals(DOUBLE, pow.type());
    assertTrue(pow.valueOf(valueEnv()).isMissing());

    FunctionExpression power =
        dsl.power(DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER), DSL.literal(1));
    assertEquals(DOUBLE, power.type());
    assertTrue(power.valueOf(valueEnv()).isMissing());

    power = dsl.power(DSL.literal(1), DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER));
    assertEquals(DOUBLE, power.type());
    assertTrue(power.valueOf(valueEnv()).isMissing());

    power = dsl.power(DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER),
        DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER));
    assertEquals(DOUBLE, power.type());
    assertTrue(power.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test pow/power with null and missing values.
   */
  @Test
  public void pow_null_missing() {
    FunctionExpression pow = dsl.pow(
        DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER),
        DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER));
    assertEquals(DOUBLE, pow.type());
    assertTrue(pow.valueOf(valueEnv()).isMissing());

    pow = dsl.pow(
        DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER),
        DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER));
    assertEquals(DOUBLE, pow.type());
    assertTrue(pow.valueOf(valueEnv()).isMissing());

    FunctionExpression power = dsl.power(
        DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER),
        DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER));
    assertEquals(DOUBLE, power.type());
    assertTrue(power.valueOf(valueEnv()).isMissing());

    power = dsl.power(
        DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER),
        DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER));
    assertEquals(DOUBLE, power.type());
    assertTrue(power.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test round with integer value.
   */
  @ParameterizedTest(name = "round({0}")
  @ValueSource(ints = {21, -21})
  public void round_int_value(Integer value) {
    FunctionExpression round = dsl.round(DSL.literal(value));
    assertThat(
        round.valueOf(valueEnv()),
        allOf(hasType(LONG), hasValue((long) Math.round(value))));
    assertEquals(String.format("round(%s)", value), round.toString());

    round = dsl.round(DSL.literal(value), DSL.literal(1));
    assertThat(
        round.valueOf(valueEnv()),
        allOf(hasType(LONG), hasValue(
            new BigDecimal(value).setScale(1, RoundingMode.HALF_UP).longValue())));
    assertEquals(String.format("round(%s, 1)", value), round.toString());

    round = dsl.round(DSL.literal(value), DSL.literal(-1));
    assertThat(
        round.valueOf(valueEnv()),
        allOf(hasType(LONG), hasValue(
            new BigDecimal(value).setScale(-1, RoundingMode.HALF_UP).longValue())));
    assertEquals(String.format("round(%s, -1)", value), round.toString());
  }

  /**
   * Test round with long value.
   */
  @ParameterizedTest(name = "round({0}")
  @ValueSource(longs = {21L, -21L})
  public void round_long_value(Long value) {
    FunctionExpression round = dsl.round(DSL.literal(value));
    assertThat(
        round.valueOf(valueEnv()),
        allOf(hasType(LONG), hasValue((long) Math.round(value))));
    assertEquals(String.format("round(%s)", value), round.toString());

    round = dsl.round(DSL.literal(value), DSL.literal(1));
    assertThat(
        round.valueOf(valueEnv()),
        allOf(hasType(LONG), hasValue(
            new BigDecimal(value).setScale(1, RoundingMode.HALF_UP).longValue())));
    assertEquals(String.format("round(%s, 1)", value), round.toString());

    round = dsl.round(DSL.literal(value), DSL.literal(-1));
    assertThat(
        round.valueOf(valueEnv()),
        allOf(hasType(LONG), hasValue(
            new BigDecimal(value).setScale(-1, RoundingMode.HALF_UP).longValue())));
    assertEquals(String.format("round(%s, -1)", value), round.toString());
  }

  /**
   * Test round with float value.
   */
  @ParameterizedTest(name = "round({0}")
  @ValueSource(floats = {21F, -21F})
  public void round_float_value(Float value) {
    FunctionExpression round = dsl.round(DSL.literal(value));
    assertThat(
        round.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue((double) Math.round(value))));
    assertEquals(String.format("round(%s)", value), round.toString());

    round = dsl.round(DSL.literal(value), DSL.literal(1));
    assertThat(
        round.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(
            new BigDecimal(value).setScale(1, RoundingMode.HALF_UP).doubleValue())));
    assertEquals(String.format("round(%s, 1)", value), round.toString());

    round = dsl.round(DSL.literal(value), DSL.literal(-1));
    assertThat(
        round.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(
            new BigDecimal(value).setScale(-1, RoundingMode.HALF_UP).doubleValue())));
    assertEquals(String.format("round(%s, -1)", value), round.toString());
  }

  /**
   * Test round with double value.
   */
  @ParameterizedTest(name = "round({0}")
  @ValueSource(doubles = {21D, -21D})
  public void round_double_value(Double value) {
    FunctionExpression round = dsl.round(DSL.literal(value));
    assertThat(
        round.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue((double) Math.round(value))));
    assertEquals(String.format("round(%s)", value), round.toString());

    round = dsl.round(DSL.literal(value), DSL.literal(1));
    assertThat(
        round.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(
            new BigDecimal(value).setScale(1, RoundingMode.HALF_UP).doubleValue())));
    assertEquals(String.format("round(%s, 1)", value), round.toString());

    round = dsl.round(DSL.literal(value), DSL.literal(-1));
    assertThat(
        round.valueOf(valueEnv()),
        allOf(hasType(DOUBLE), hasValue(
            new BigDecimal(value).setScale(-1, RoundingMode.HALF_UP).doubleValue())));
    assertEquals(String.format("round(%s, -1)", value), round.toString());
  }

  /**
   * Test round with null value.
   */
  @Test
  public void round_null_value() {
    FunctionExpression round = dsl.round(DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER));
    assertEquals(LONG, round.type());
    assertTrue(round.valueOf(valueEnv()).isNull());

    round = dsl.round(DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER), DSL.literal(1));
    assertEquals(LONG, round.type());
    assertTrue(round.valueOf(valueEnv()).isNull());

    round = dsl.round(DSL.literal(1), DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER));
    assertEquals(LONG, round.type());
    assertTrue(round.valueOf(valueEnv()).isNull());
  }

  /**
   * Test round with null value.
   */
  @Test
  public void round_missing_value() {
    FunctionExpression round = dsl.round(DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER));
    assertEquals(LONG, round.type());
    assertTrue(round.valueOf(valueEnv()).isMissing());

    round = dsl.round(DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER), DSL.literal(1));
    assertEquals(LONG, round.type());
    assertTrue(round.valueOf(valueEnv()).isMissing());

    round = dsl.round(DSL.literal(1), DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER));
    assertEquals(LONG, round.type());
    assertTrue(round.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test round with null and missing values.
   */
  @Test
  public void round_null_missing() {
    FunctionExpression round = dsl.round(
        DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER),
        DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER));
    assertEquals(LONG, round.type());
    assertTrue(round.valueOf(valueEnv()).isMissing());

    round = dsl.round(
        DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER),
        DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER));
    assertEquals(LONG, round.type());
    assertTrue(round.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test sign with integer value.
   */
  @ParameterizedTest(name = "sign({0})")
  @ValueSource(ints = {2, -2})
  public void sign_int_value(Integer value) {
    FunctionExpression sign = dsl.sign(DSL.literal(value));
    assertThat(
        sign.valueOf(valueEnv()),
        allOf(hasType(INTEGER), hasValue((int) Math.signum(value))));
    assertEquals(String.format("sign(%s)", value), sign.toString());
  }

  /**
   * Test sign with long value.
   */
  @ParameterizedTest(name = "sign({0})")
  @ValueSource(longs = {2L, -2L})
  public void sign_long_value(Long value) {
    FunctionExpression sign = dsl.sign(DSL.literal(value));
    assertThat(
        sign.valueOf(valueEnv()),
        allOf(hasType(INTEGER), hasValue((int) Math.signum(value))));
    assertEquals(String.format("sign(%s)", value), sign.toString());
  }

  /**
   * Test sign with float value.
   */
  @ParameterizedTest(name = "sign({0})")
  @ValueSource(floats = {2F, -2F})
  public void sign_float_value(Float value) {
    FunctionExpression sign = dsl.sign(DSL.literal(value));
    assertThat(
        sign.valueOf(valueEnv()),
        allOf(hasType(INTEGER), hasValue((int) Math.signum(value))));
    assertEquals(String.format("sign(%s)", value), sign.toString());
  }

  /**
   * Test sign with double value.
   */
  @ParameterizedTest(name = "sign({0})")
  @ValueSource(doubles = {2, -2})
  public void sign_double_value(Double value) {
    FunctionExpression sign = dsl.sign(DSL.literal(value));
    assertThat(
        sign.valueOf(valueEnv()),
        allOf(hasType(INTEGER), hasValue((int) Math.signum(value))));
    assertEquals(String.format("sign(%s)", value), sign.toString());
  }

  /**
   * Test sign with null value.
   */
  @Test
  public void sign_null_value() {
    FunctionExpression sign = dsl.sign(DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER));
    assertEquals(INTEGER, sign.type());
    assertTrue(sign.valueOf(valueEnv()).isNull());
  }

  /**
   * Test sign with missing value.
   */
  @Test
  public void sign_missing_value() {
    FunctionExpression sign = dsl.sign(DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER));
    assertEquals(INTEGER, sign.type());
    assertTrue(sign.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test sqrt with int value.
   */
  @ParameterizedTest(name = "sqrt({0})")
  @ValueSource(ints = {1, 2})
  public void sqrt_int_value(Integer value) {
    FunctionExpression sqrt = dsl.sqrt(DSL.literal(value));
    assertThat(sqrt.valueOf(valueEnv()), allOf(hasType(DOUBLE), hasValue(Math.sqrt(value))));
    assertEquals(String.format("sqrt(%s)", value), sqrt.toString());
  }

  /**
   * Test sqrt with long value.
   */
  @ParameterizedTest(name = "sqrt({0})")
  @ValueSource(longs = {1L, 2L})
  public void sqrt_long_value(Long value) {
    FunctionExpression sqrt = dsl.sqrt(DSL.literal(value));
    assertThat(sqrt.valueOf(valueEnv()), allOf(hasType(DOUBLE), hasValue(Math.sqrt(value))));
    assertEquals(String.format("sqrt(%s)", value), sqrt.toString());
  }

  /**
   * Test sqrt with float value.
   */
  @ParameterizedTest(name = "sqrt({0})")
  @ValueSource(floats = {1F, 2F})
  public void sqrt_float_value(Float value) {
    FunctionExpression sqrt = dsl.sqrt(DSL.literal(value));
    assertThat(sqrt.valueOf(valueEnv()), allOf(hasType(DOUBLE), hasValue(Math.sqrt(value))));
    assertEquals(String.format("sqrt(%s)", value), sqrt.toString());
  }

  /**
   * Test sqrt with double value.
   */
  @ParameterizedTest(name = "sqrt({0})")
  @ValueSource(doubles = {1D, 2D})
  public void sqrt_double_value(Double value) {
    FunctionExpression sqrt = dsl.sqrt(DSL.literal(value));
    assertThat(sqrt.valueOf(valueEnv()), allOf(hasType(DOUBLE), hasValue(Math.sqrt(value))));
    assertEquals(String.format("sqrt(%s)", value), sqrt.toString());
  }

  /**
   * Test sqrt with negative value.
   */
  @ParameterizedTest(name = "sqrt({0})")
  @ValueSource(doubles = {-1D, -2D})
  public void sqrt_negative_value(Double value) {
    FunctionExpression sqrt = dsl.sqrt(DSL.literal(value));
    assertEquals(DOUBLE, sqrt.type());
    assertTrue(sqrt.valueOf(valueEnv()).isNull());
  }

  /**
   * Test sqrt with null value.
   */
  @Test
  public void sqrt_null_value() {
    FunctionExpression sqrt = dsl.sqrt(DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER));
    assertEquals(DOUBLE, sqrt.type());
    assertTrue(sqrt.valueOf(valueEnv()).isNull());
  }

  /**
   * Test sqrt with missing value.
   */
  @Test
  public void sqrt_missing_value() {
    FunctionExpression sqrt = dsl.sqrt(DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER));
    assertEquals(DOUBLE, sqrt.type());
    assertTrue(sqrt.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test truncate with integer value.
   */
  @ParameterizedTest(name = "truncate({0}, {1})")
  @ValueSource(ints = {2, -2})
  public void truncate_int_value(Integer value) {
    FunctionExpression truncate = dsl.truncate(DSL.literal(value), DSL.literal(1));
    assertThat(
        truncate.valueOf(valueEnv()), allOf(hasType(LONG),
            hasValue(new BigDecimal(value).setScale(1, RoundingMode.DOWN).longValue())));
    assertEquals(String.format("truncate(%s, 1)", value), truncate.toString());
  }

  /**
   * Test truncate with long value.
   */
  @ParameterizedTest(name = "truncate({0}, {1})")
  @ValueSource(longs = {2L, -2L})
  public void truncate_long_value(Long value) {
    FunctionExpression truncate = dsl.truncate(DSL.literal(value), DSL.literal(1));
    assertThat(
        truncate.valueOf(valueEnv()), allOf(hasType(LONG),
            hasValue(new BigDecimal(value).setScale(1, RoundingMode.DOWN).longValue())));
    assertEquals(String.format("truncate(%s, 1)", value), truncate.toString());
  }

  /**
   * Test truncate with float value.
   */
  @ParameterizedTest(name = "truncate({0}, {1})")
  @ValueSource(floats = {2F, -2F})
  public void truncate_float_value(Float value) {
    FunctionExpression truncate = dsl.truncate(DSL.literal(value), DSL.literal(1));
    assertThat(
        truncate.valueOf(valueEnv()), allOf(hasType(DOUBLE),
            hasValue(new BigDecimal(value).setScale(1, RoundingMode.DOWN).doubleValue())));
    assertEquals(String.format("truncate(%s, 1)", value), truncate.toString());
  }

  /**
   * Test truncate with double value.
   */
  @ParameterizedTest(name = "truncate({0}, {1})")
  @ValueSource(doubles = {2D, -2D})
  public void truncate_double_value(Double value) {
    FunctionExpression truncate = dsl.truncate(DSL.literal(value), DSL.literal(1));
    assertThat(
        truncate.valueOf(valueEnv()), allOf(hasType(DOUBLE),
            hasValue(new BigDecimal(value).setScale(1, RoundingMode.DOWN).doubleValue())));
    assertEquals(String.format("truncate(%s, 1)", value), truncate.toString());
  }

  /**
   * Test truncate with null value.
   */
  @Test
  public void truncate_null_value() {
    FunctionExpression truncate =
        dsl.truncate(DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER), DSL.literal(1));
    assertEquals(LONG, truncate.type());
    assertTrue(truncate.valueOf(valueEnv()).isNull());

    truncate = dsl.truncate(DSL.literal(1), DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER));
    assertEquals(LONG, truncate.type());
    assertTrue(truncate.valueOf(valueEnv()).isNull());

    truncate = dsl.truncate(
        DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER), DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER));
    assertEquals(LONG, truncate.type());
    assertTrue(truncate.valueOf(valueEnv()).isNull());
  }

  /**
   * Test truncate with missing value.
   */
  @Test
  public void truncate_missing_value() {
    FunctionExpression truncate =
        dsl.truncate(DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER), DSL.literal(1));
    assertEquals(LONG, truncate.type());
    assertTrue(truncate.valueOf(valueEnv()).isMissing());

    truncate = dsl.truncate(DSL.literal(1), DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER));
    assertEquals(LONG, truncate.type());
    assertTrue(truncate.valueOf(valueEnv()).isMissing());

    truncate = dsl.truncate(
        DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER),
        DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER));
    assertEquals(LONG, truncate.type());
    assertTrue(truncate.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test truncate with null and missing values.
   */
  @Test
  public void truncate_null_missing() {
    FunctionExpression truncate = dsl.truncate(DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER),
        DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER));
    assertEquals(LONG, truncate.type());
    assertTrue(truncate.valueOf(valueEnv()).isMissing());

    truncate = dsl.truncate(DSL.ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER),
        DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER));
    assertEquals(LONG, truncate.type());
    assertTrue(truncate.valueOf(valueEnv()).isMissing());
  }

  /**
   * Test constant pi.
   */
  @Test
  public void test_pi() {
    FunctionExpression pi = dsl.pi();
    assertThat(pi.valueOf(valueEnv()), allOf(hasType(DOUBLE), hasValue(Math.PI)));
  }

  /**
   * Test rand with no argument.
   */
  @Test
  public void rand_no_arg() {
    FunctionExpression rand = dsl.rand();
    assertEquals(FLOAT, rand.type());
    assertTrue(
        getFloatValue(rand.valueOf(valueEnv())) >= 0
            && getFloatValue(rand.valueOf(valueEnv())) < 1);
    assertEquals("rand()", rand.toString());
  }

  /**
   * Test rand with integer value.
   */
  @ParameterizedTest(name = "rand({0})")
  @ValueSource(ints = {2, 3})
  public void rand_int_value(Integer n) {
    FunctionExpression rand = dsl.rand(DSL.literal(n));
    assertEquals(FLOAT, rand.type());
    assertTrue(
        getFloatValue(rand.valueOf(valueEnv())) >= 0
        && getFloatValue(rand.valueOf(valueEnv())) < 1);
    assertEquals(getFloatValue(rand.valueOf(valueEnv())), new Random(n).nextFloat());
    assertEquals(String.format("rand(%s)", n), rand.toString());
  }

  @Test
  public void rand_null_value() {
    FunctionExpression rand = dsl.rand(DSL.ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER));
    assertEquals(FLOAT, rand.type());
    assertTrue(rand.valueOf(valueEnv()).isNull());
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
   * Test acos with illegal values.
   */
  @ParameterizedTest(name = "acos({0})")
  @ValueSource(doubles = {2D, -2D})
  public void acos_with_illegal_value(Number value) {
    FunctionExpression acos = dsl.acos(DSL.literal(value));
    assertEquals(DOUBLE, acos.type());
    assertTrue(acos.valueOf(valueEnv()).isNull());
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
   * Test asin with illegal value.
   */
  @ParameterizedTest(name = "asin({0})")
  @ValueSource(doubles = {2D, -2D})
  public void asin_with_illegal_value(Number value) {
    FunctionExpression asin = dsl.asin(DSL.literal(value));
    assertEquals(DOUBLE, asin.type());
    assertTrue(asin.valueOf(valueEnv()).isNull());
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
   * Test cot with out-of-range value 0.
   */
  @ParameterizedTest(name = "cot({0})")
  @ValueSource(doubles = {0})
  public void cot_with_zero(Number value) {
    FunctionExpression cot = dsl.cot(DSL.literal(value));
    assertThrows(
        ArithmeticException.class, () -> cot.valueOf(valueEnv()),
        String.format("Out of range value for cot(%s)", value));
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
