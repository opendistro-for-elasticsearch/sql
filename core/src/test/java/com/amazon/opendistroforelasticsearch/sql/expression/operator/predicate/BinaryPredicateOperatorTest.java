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

package com.amazon.opendistroforelasticsearch.sql.expression.operator.predicate;

import static com.amazon.opendistroforelasticsearch.sql.config.TestConfig.BOOL_TYPE_MISSING_VALUE_FIELD;
import static com.amazon.opendistroforelasticsearch.sql.config.TestConfig.BOOL_TYPE_NULL_VALUE_FIELD;
import static com.amazon.opendistroforelasticsearch.sql.config.TestConfig.INT_TYPE_MISSING_VALUE_FIELD;
import static com.amazon.opendistroforelasticsearch.sql.config.TestConfig.INT_TYPE_NULL_VALUE_FIELD;
import static com.amazon.opendistroforelasticsearch.sql.config.TestConfig.STRING_TYPE_MISSING_VALUE_FILED;
import static com.amazon.opendistroforelasticsearch.sql.config.TestConfig.STRING_TYPE_NULL_VALUE_FILED;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_FALSE;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_MISSING;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_NULL;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_TRUE;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.booleanValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.fromObjectValue;
import static com.amazon.opendistroforelasticsearch.sql.expression.operator.OperatorUtils.matches;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionTestBase;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BinaryPredicateOperatorTest extends ExpressionTestBase {

  private static Stream<Arguments> binaryPredicateArguments() {
    List<Boolean> booleans = Arrays.asList(true, false);
    return Lists.cartesianProduct(booleans, booleans).stream()
        .map(list -> Arguments.of(list.get(0), list.get(1)));
  }

  private static Stream<Arguments> testEqualArguments() {
    List<Object> arguments = Arrays.asList(1, 1L, 1F, 1D, "str", true, ImmutableList.of(1),
        ImmutableMap.of("str", 1));
    Stream.Builder<Arguments> builder = Stream.builder();
    for (Object argument : arguments) {
      builder.add(Arguments.of(fromObjectValue(argument), fromObjectValue(argument)));
    }
    return builder.build();
  }

  private static Stream<Arguments> testNotEqualArguments() {
    List<List<Object>> arguments = Arrays.asList(
        Arrays.asList(1, 2), Arrays.asList(1L, 2L), Arrays.asList(1F, 2F), Arrays.asList(1D, 2D),
        Arrays.asList("str0", "str1"), Arrays.asList(true, false),
        Arrays.asList(ImmutableList.of(1), ImmutableList.of(2)),
        Arrays.asList(ImmutableMap.of("str", 1), ImmutableMap.of("str", 2))
    );
    Stream.Builder<Arguments> builder = Stream.builder();
    for (List<Object> argPair : arguments) {
      builder.add(Arguments.of(fromObjectValue(argPair.get(0)), fromObjectValue(argPair.get(1))));
    }
    return builder.build();
  }

  private static Stream<Arguments> testCompareValueArguments() {
    List<List<Object>> arguments = Arrays.asList(
        Arrays.asList(1, 1), Arrays.asList(1, 2), Arrays.asList(2, 1),
        Arrays.asList(1L, 1L), Arrays.asList(1L, 2L), Arrays.asList(2L, 1L),
        Arrays.asList(1F, 1F), Arrays.asList(1F, 2F), Arrays.asList(2F, 1F),
        Arrays.asList(1D, 1D), Arrays.asList(1D, 2D), Arrays.asList(2D, 1D),
        Arrays.asList("str", "str"), Arrays.asList("str", "str0"), Arrays.asList("str0", "str")
    );
    Stream.Builder<Arguments> builder = Stream.builder();
    for (List<Object> argPair : arguments) {
      builder.add(Arguments.of(fromObjectValue(argPair.get(0)), fromObjectValue(argPair.get(1))));
    }
    return builder.build();
  }

  private static Stream<Arguments> testLikeArguments() {
    List<List<String>> arguments = Arrays.asList(
        Arrays.asList("foo", "foo"), Arrays.asList("notFoo", "foo"),
        Arrays.asList("foobar", "%bar"), Arrays.asList("bar", "%bar"),
        Arrays.asList("foo", "fo_"), Arrays.asList("foo", "foo_"),
        Arrays.asList("foorbar", "%o_ar"), Arrays.asList("foobar", "%o_a%"),
        Arrays.asList("fooba%_\\^$.*[]()|+r", "%\\%\\_\\\\\\^\\$\\.\\*\\[\\]\\(\\)\\|\\+_")
    );
    Stream.Builder<Arguments> builder = Stream.builder();
    for (List<String> argPair : arguments) {
      builder.add(Arguments.of(fromObjectValue(argPair.get(0)), fromObjectValue(argPair.get(1))));
    }
    return builder.build();
  }

  @ParameterizedTest(name = "and({0}, {1})")
  @MethodSource("binaryPredicateArguments")
  public void test_and(Boolean v1, Boolean v2) {
    FunctionExpression and =
        dsl.and(typeEnv(), DSL.literal(booleanValue(v1)), DSL.literal(booleanValue(v2)));
    assertEquals(ExprType.BOOLEAN, and.type(typeEnv()));
    assertEquals(v1 && v2, ExprValueUtils.getBooleanValue(and.valueOf(valueEnv())));
    assertEquals(String.format("%s and %s", v1.toString(), v2.toString()), and.toString());
  }

  @Test
  public void test_boolean_and_null() {
    FunctionExpression and =
        dsl.and(typeEnv(), DSL.literal(LITERAL_TRUE), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, and.type(typeEnv()));
    assertEquals(LITERAL_NULL, and.valueOf(valueEnv()));

    and = dsl.and(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD), DSL.literal(LITERAL_TRUE));
    assertEquals(ExprType.BOOLEAN, and.type(typeEnv()));
    assertEquals(LITERAL_NULL, and.valueOf(valueEnv()));

    and = dsl.and(typeEnv(), DSL.literal(LITERAL_FALSE), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, and.type(typeEnv()));
    assertEquals(LITERAL_FALSE, and.valueOf(valueEnv()));

    and = dsl.and(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD), DSL.literal(LITERAL_FALSE));
    assertEquals(ExprType.BOOLEAN, and.type(typeEnv()));
    assertEquals(LITERAL_FALSE, and.valueOf(valueEnv()));
  }

  @Test
  public void test_boolean_and_missing() {
    FunctionExpression and =
        dsl.and(typeEnv(), DSL.literal(LITERAL_TRUE), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, and.type(typeEnv()));
    assertEquals(LITERAL_MISSING, and.valueOf(valueEnv()));

    and = dsl.and(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD), DSL.literal(LITERAL_TRUE));
    assertEquals(ExprType.BOOLEAN, and.type(typeEnv()));
    assertEquals(LITERAL_MISSING, and.valueOf(valueEnv()));

    and = dsl.and(typeEnv(), DSL.literal(LITERAL_FALSE), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, and.type(typeEnv()));
    assertEquals(LITERAL_FALSE, and.valueOf(valueEnv()));

    and = dsl.and(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD), DSL.literal(LITERAL_FALSE));
    assertEquals(ExprType.BOOLEAN, and.type(typeEnv()));
    assertEquals(LITERAL_FALSE, and.valueOf(valueEnv()));
  }

  @Test
  public void test_null_and_missing() {
    FunctionExpression and = dsl.and(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD),
        DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, and.type(typeEnv()));
    assertEquals(LITERAL_MISSING, and.valueOf(valueEnv()));

    and = dsl.and(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD),
        DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, and.type(typeEnv()));
    assertEquals(LITERAL_NULL, and.valueOf(valueEnv()));

    and = dsl.and(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD),
        DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, and.type(typeEnv()));
    assertEquals(LITERAL_MISSING, and.valueOf(valueEnv()));

    and = dsl.and(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD),
        DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, and.type(typeEnv()));
    assertEquals(LITERAL_MISSING, and.valueOf(valueEnv()));
  }

  @ParameterizedTest(name = "or({0}, {1})")
  @MethodSource("binaryPredicateArguments")
  public void test_or(Boolean v1, Boolean v2) {
    FunctionExpression or =
        dsl.or(typeEnv(), DSL.literal(booleanValue(v1)), DSL.literal(booleanValue(v2)));
    assertEquals(ExprType.BOOLEAN, or.type(typeEnv()));
    assertEquals(v1 || v2, ExprValueUtils.getBooleanValue(or.valueOf(valueEnv())));
    assertEquals(String.format("%s or %s", v1.toString(), v2.toString()), or.toString());
  }

  @Test
  public void test_boolean_or_null() {
    FunctionExpression or =
        dsl.or(typeEnv(), DSL.literal(LITERAL_TRUE), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, or.type(typeEnv()));
    assertEquals(LITERAL_TRUE, or.valueOf(valueEnv()));

    or = dsl.or(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD), DSL.literal(LITERAL_TRUE));
    assertEquals(ExprType.BOOLEAN, or.type(typeEnv()));
    assertEquals(LITERAL_TRUE, or.valueOf(valueEnv()));

    or = dsl.or(typeEnv(), DSL.literal(LITERAL_FALSE), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, or.type(typeEnv()));
    assertEquals(LITERAL_NULL, or.valueOf(valueEnv()));

    or = dsl.or(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD), DSL.literal(LITERAL_FALSE));
    assertEquals(ExprType.BOOLEAN, or.type(typeEnv()));
    assertEquals(LITERAL_NULL, or.valueOf(valueEnv()));
  }

  @Test
  public void test_boolean_or_missing() {
    FunctionExpression or =
        dsl.or(typeEnv(), DSL.literal(LITERAL_TRUE), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, or.type(typeEnv()));
    assertEquals(LITERAL_TRUE, or.valueOf(valueEnv()));

    or = dsl.or(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD), DSL.literal(LITERAL_TRUE));
    assertEquals(ExprType.BOOLEAN, or.type(typeEnv()));
    assertEquals(LITERAL_TRUE, or.valueOf(valueEnv()));

    or = dsl.or(typeEnv(), DSL.literal(LITERAL_FALSE), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, or.type(typeEnv()));
    assertEquals(LITERAL_MISSING, or.valueOf(valueEnv()));

    or = dsl.or(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD), DSL.literal(LITERAL_FALSE));
    assertEquals(ExprType.BOOLEAN, or.type(typeEnv()));
    assertEquals(LITERAL_MISSING, or.valueOf(valueEnv()));
  }

  @Test
  public void test_null_or_missing() {
    FunctionExpression or = dsl.or(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD),
        DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, or.type(typeEnv()));
    assertEquals(LITERAL_MISSING, or.valueOf(valueEnv()));

    or =
        dsl.or(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, or.type(typeEnv()));
    assertEquals(LITERAL_NULL, or.valueOf(valueEnv()));

    or = dsl.or(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD),
        DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, or.type(typeEnv()));
    assertEquals(LITERAL_NULL, or.valueOf(valueEnv()));

    or = dsl.or(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD),
        DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, or.type(typeEnv()));
    assertEquals(LITERAL_NULL, or.valueOf(valueEnv()));
  }


  @ParameterizedTest(name = "xor({0}, {1})")
  @MethodSource("binaryPredicateArguments")
  public void test_xor(Boolean v1, Boolean v2) {
    FunctionExpression xor =
        dsl.xor(typeEnv(), DSL.literal(booleanValue(v1)), DSL.literal(booleanValue(v2)));
    assertEquals(ExprType.BOOLEAN, xor.type(typeEnv()));
    assertEquals(v1 ^ v2, ExprValueUtils.getBooleanValue(xor.valueOf(valueEnv())));
    assertEquals(String.format("%s xor %s", v1.toString(), v2.toString()), xor.toString());
  }

  @Test
  public void test_boolean_xor_null() {
    FunctionExpression xor =
        dsl.xor(typeEnv(), DSL.literal(LITERAL_TRUE), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, xor.type(typeEnv()));
    assertEquals(LITERAL_TRUE, xor.valueOf(valueEnv()));

    xor = dsl.xor(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD), DSL.literal(LITERAL_TRUE));
    assertEquals(ExprType.BOOLEAN, xor.type(typeEnv()));
    assertEquals(LITERAL_TRUE, xor.valueOf(valueEnv()));

    xor = dsl.xor(typeEnv(), DSL.literal(LITERAL_FALSE), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, xor.type(typeEnv()));
    assertEquals(LITERAL_NULL, xor.valueOf(valueEnv()));

    xor = dsl.xor(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD), DSL.literal(LITERAL_FALSE));
    assertEquals(ExprType.BOOLEAN, xor.type(typeEnv()));
    assertEquals(LITERAL_NULL, xor.valueOf(valueEnv()));
  }

  @Test
  public void test_boolean_xor_missing() {
    FunctionExpression xor =
        dsl.xor(typeEnv(), DSL.literal(LITERAL_TRUE), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, xor.type(typeEnv()));
    assertEquals(LITERAL_TRUE, xor.valueOf(valueEnv()));

    xor = dsl.xor(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD), DSL.literal(LITERAL_TRUE));
    assertEquals(ExprType.BOOLEAN, xor.type(typeEnv()));
    assertEquals(LITERAL_TRUE, xor.valueOf(valueEnv()));

    xor = dsl.xor(typeEnv(), DSL.literal(LITERAL_FALSE), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, xor.type(typeEnv()));
    assertEquals(LITERAL_MISSING, xor.valueOf(valueEnv()));

    xor = dsl.xor(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD), DSL.literal(LITERAL_FALSE));
    assertEquals(ExprType.BOOLEAN, xor.type(typeEnv()));
    assertEquals(LITERAL_MISSING, xor.valueOf(valueEnv()));
  }

  @Test
  public void test_null_xor_missing() {
    FunctionExpression xor = dsl.xor(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD),
        DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, xor.type(typeEnv()));
    assertEquals(LITERAL_MISSING, xor.valueOf(valueEnv()));

    xor = dsl.xor(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD),
        DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, xor.type(typeEnv()));
    assertEquals(LITERAL_NULL, xor.valueOf(valueEnv()));

    xor = dsl.xor(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD),
        DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, xor.type(typeEnv()));
    assertEquals(LITERAL_NULL, xor.valueOf(valueEnv()));

    xor = dsl.xor(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD),
        DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, xor.type(typeEnv()));
    assertEquals(LITERAL_NULL, xor.valueOf(valueEnv()));
  }

  @ParameterizedTest(name = "equal({0}, {1})")
  @MethodSource("testEqualArguments")
  public void test_equal(ExprValue v1, ExprValue v2) {
    FunctionExpression equal = dsl.equal(typeEnv(), DSL.literal(v1), DSL.literal(v2));
    assertEquals(ExprType.BOOLEAN, equal.type(typeEnv()));
    assertEquals(v1.value().equals(v2.value()),
        ExprValueUtils.getBooleanValue(equal.valueOf(valueEnv())));
    assertEquals(String.format("%s = %s", v1.toString(), v2.toString()), equal.toString());
  }

  @Test
  public void test_null_equal_missing() {
    FunctionExpression equal = dsl.equal(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD),
        DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, equal.type(typeEnv()));
    assertEquals(LITERAL_TRUE, equal.valueOf(valueEnv()));

    equal = dsl.equal(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD),
        DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, equal.type(typeEnv()));
    assertEquals(LITERAL_TRUE, equal.valueOf(valueEnv()));

    equal = dsl.equal(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD),
        DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, equal.type(typeEnv()));
    assertEquals(LITERAL_FALSE, equal.valueOf(valueEnv()));

    equal = dsl.equal(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD),
        DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, equal.type(typeEnv()));
    assertEquals(LITERAL_FALSE, equal.valueOf(valueEnv()));

    equal = dsl.equal(typeEnv(), DSL.literal(LITERAL_TRUE), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, equal.type(typeEnv()));
    assertEquals(LITERAL_FALSE, equal.valueOf(valueEnv()));

    equal = dsl.equal(typeEnv(), DSL.literal(LITERAL_TRUE), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, equal.type(typeEnv()));
    assertEquals(LITERAL_FALSE, equal.valueOf(valueEnv()));

    equal = dsl.equal(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD), DSL.literal(LITERAL_TRUE));
    assertEquals(ExprType.BOOLEAN, equal.type(typeEnv()));
    assertEquals(LITERAL_FALSE, equal.valueOf(valueEnv()));

    equal = dsl.equal(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD), DSL.literal(LITERAL_TRUE));
    assertEquals(ExprType.BOOLEAN, equal.type(typeEnv()));
    assertEquals(LITERAL_FALSE, equal.valueOf(valueEnv()));
  }

  @ParameterizedTest(name = "equal({0}, {1})")
  @MethodSource({"testEqualArguments", "testNotEqualArguments"})
  public void test_notequal(ExprValue v1, ExprValue v2) {
    FunctionExpression notequal = dsl.notequal(typeEnv(), DSL.literal(v1), DSL.literal(v2));
    assertEquals(ExprType.BOOLEAN, notequal.type(typeEnv()));
    assertEquals(!v1.value().equals(v2.value()),
        ExprValueUtils.getBooleanValue(notequal.valueOf(valueEnv())));
    assertEquals(String.format("%s != %s", v1.toString(), v2.toString()), notequal.toString());
  }

  @Test
  public void test_null_notequal_missing() {
    FunctionExpression notequal = dsl.notequal(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD),
        DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, notequal.type(typeEnv()));
    assertEquals(LITERAL_FALSE, notequal.valueOf(valueEnv()));

    notequal = dsl.notequal(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD),
        DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, notequal.type(typeEnv()));
    assertEquals(LITERAL_FALSE, notequal.valueOf(valueEnv()));

    notequal = dsl.notequal(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD),
        DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, notequal.type(typeEnv()));
    assertEquals(LITERAL_TRUE, notequal.valueOf(valueEnv()));

    notequal = dsl.notequal(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD),
        DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, notequal.type(typeEnv()));
    assertEquals(LITERAL_TRUE, notequal.valueOf(valueEnv()));

    notequal = dsl.notequal(typeEnv(), DSL.literal(LITERAL_TRUE),
        DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, notequal.type(typeEnv()));
    assertEquals(LITERAL_TRUE, notequal.valueOf(valueEnv()));

    notequal = dsl.notequal(typeEnv(), DSL.literal(LITERAL_TRUE),
        DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, notequal.type(typeEnv()));
    assertEquals(LITERAL_TRUE, notequal.valueOf(valueEnv()));

    notequal = dsl.notequal(typeEnv(), DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD),
        DSL.literal(LITERAL_TRUE));
    assertEquals(ExprType.BOOLEAN, notequal.type(typeEnv()));
    assertEquals(LITERAL_TRUE, notequal.valueOf(valueEnv()));

    notequal = dsl.notequal(typeEnv(), DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD),
        DSL.literal(LITERAL_TRUE));
    assertEquals(ExprType.BOOLEAN, notequal.type(typeEnv()));
    assertEquals(LITERAL_TRUE, notequal.valueOf(valueEnv()));
  }

  @ParameterizedTest(name = "less({0}, {1})")
  @MethodSource("testCompareValueArguments")
  public void test_less(ExprValue v1, ExprValue v2) {
    FunctionExpression less = dsl.less(typeEnv(), DSL.literal(v1), DSL.literal(v2));
    assertEquals(ExprType.BOOLEAN, less.type(typeEnv()));
    assertEquals(v1.compareTo(v2) < 0,
        ExprValueUtils.getBooleanValue(less.valueOf(valueEnv())));
    assertEquals(String.format("%s < %s", v1.toString(), v2.toString()), less.toString());
  }

  @Test
  public void test_less_null() {
    FunctionExpression less = dsl.less(typeEnv(), DSL.literal(1),
        DSL.ref(INT_TYPE_NULL_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, less.type(typeEnv()));
    assertEquals(LITERAL_NULL, less.valueOf(valueEnv()));

    less = dsl.less(typeEnv(), DSL.ref(INT_TYPE_NULL_VALUE_FIELD), DSL.literal(1));
    assertEquals(ExprType.BOOLEAN, less.type(typeEnv()));
    assertEquals(LITERAL_NULL, less.valueOf(valueEnv()));

    less = dsl.less(typeEnv(), DSL.ref(INT_TYPE_NULL_VALUE_FIELD),
        DSL.ref(INT_TYPE_NULL_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, less.type(typeEnv()));
    assertEquals(LITERAL_NULL, less.valueOf(valueEnv()));
  }

  @Test
  public void test_less_missing() {
    FunctionExpression less = dsl.less(typeEnv(), DSL.literal(1),
        DSL.ref(INT_TYPE_MISSING_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, less.type(typeEnv()));
    assertEquals(LITERAL_MISSING, less.valueOf(valueEnv()));

    less = dsl.less(typeEnv(), DSL.ref(INT_TYPE_MISSING_VALUE_FIELD), DSL.literal(1));
    assertEquals(ExprType.BOOLEAN, less.type(typeEnv()));
    assertEquals(LITERAL_MISSING, less.valueOf(valueEnv()));

    less = dsl.less(typeEnv(), DSL.ref(INT_TYPE_MISSING_VALUE_FIELD),
        DSL.ref(INT_TYPE_MISSING_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, less.type(typeEnv()));
    assertEquals(LITERAL_MISSING, less.valueOf(valueEnv()));
  }

  @Test
  public void test_null_less_missing() {
    FunctionExpression less = dsl.less(typeEnv(), DSL.ref(INT_TYPE_NULL_VALUE_FIELD),
        DSL.ref(INT_TYPE_MISSING_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, less.type(typeEnv()));
    assertEquals(LITERAL_NULL, less.valueOf(valueEnv()));

    less = dsl.less(typeEnv(), DSL.ref(INT_TYPE_MISSING_VALUE_FIELD),
        DSL.ref(INT_TYPE_NULL_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, less.type(typeEnv()));
    assertEquals(LITERAL_NULL, less.valueOf(valueEnv()));
  }

  @ParameterizedTest(name = "lte({0}, {1})")
  @MethodSource("testCompareValueArguments")
  public void test_lte(ExprValue v1, ExprValue v2) {
    FunctionExpression lte = dsl.lte(typeEnv(), DSL.literal(v1), DSL.literal(v2));
    assertEquals(ExprType.BOOLEAN, lte.type(typeEnv()));
    assertEquals(v1.compareTo(v2) <= 0,
        ExprValueUtils.getBooleanValue(lte.valueOf(valueEnv())));
    assertEquals(String.format("%s <= %s", v1.toString(), v2.toString()), lte.toString());
  }

  @Test
  public void test_lte_null() {
    FunctionExpression lte = dsl.lte(typeEnv(), DSL.literal(1),
        DSL.ref(INT_TYPE_NULL_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, lte.type(typeEnv()));
    assertEquals(LITERAL_NULL, lte.valueOf(valueEnv()));

    lte = dsl.lte(typeEnv(), DSL.ref(INT_TYPE_NULL_VALUE_FIELD), DSL.literal(1));
    assertEquals(ExprType.BOOLEAN, lte.type(typeEnv()));
    assertEquals(LITERAL_NULL, lte.valueOf(valueEnv()));

    lte = dsl.lte(typeEnv(), DSL.ref(INT_TYPE_NULL_VALUE_FIELD),
        DSL.ref(INT_TYPE_NULL_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, lte.type(typeEnv()));
    assertEquals(LITERAL_NULL, lte.valueOf(valueEnv()));
  }

  @Test
  public void test_lte_missing() {
    FunctionExpression lte = dsl.lte(typeEnv(), DSL.literal(1),
        DSL.ref(INT_TYPE_MISSING_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, lte.type(typeEnv()));
    assertEquals(LITERAL_MISSING, lte.valueOf(valueEnv()));

    lte = dsl.lte(typeEnv(), DSL.ref(INT_TYPE_MISSING_VALUE_FIELD), DSL.literal(1));
    assertEquals(ExprType.BOOLEAN, lte.type(typeEnv()));
    assertEquals(LITERAL_MISSING, lte.valueOf(valueEnv()));

    lte = dsl.lte(typeEnv(), DSL.ref(INT_TYPE_MISSING_VALUE_FIELD),
        DSL.ref(INT_TYPE_MISSING_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, lte.type(typeEnv()));
    assertEquals(LITERAL_MISSING, lte.valueOf(valueEnv()));
  }

  @Test
  public void test_null_lte_missing() {
    FunctionExpression lte = dsl.lte(typeEnv(), DSL.ref(INT_TYPE_NULL_VALUE_FIELD),
        DSL.ref(INT_TYPE_MISSING_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, lte.type(typeEnv()));
    assertEquals(LITERAL_NULL, lte.valueOf(valueEnv()));

    lte = dsl.lte(typeEnv(), DSL.ref(INT_TYPE_MISSING_VALUE_FIELD),
        DSL.ref(INT_TYPE_NULL_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, lte.type(typeEnv()));
    assertEquals(LITERAL_NULL, lte.valueOf(valueEnv()));
  }

  @ParameterizedTest(name = "greater({0}, {1})")
  @MethodSource("testCompareValueArguments")
  public void test_greater(ExprValue v1, ExprValue v2) {
    FunctionExpression greater = dsl.greater(typeEnv(), DSL.literal(v1), DSL.literal(v2));
    assertEquals(ExprType.BOOLEAN, greater.type(typeEnv()));
    assertEquals(v1.compareTo(v2) > 0,
        ExprValueUtils.getBooleanValue(greater.valueOf(valueEnv())));
    assertEquals(String.format("%s > %s", v1.toString(), v2.toString()), greater.toString());
  }

  @Test
  public void test_greater_null() {
    FunctionExpression greater = dsl.greater(typeEnv(), DSL.literal(1),
        DSL.ref(INT_TYPE_NULL_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, greater.type(typeEnv()));
    assertEquals(LITERAL_NULL, greater.valueOf(valueEnv()));

    greater = dsl.greater(typeEnv(), DSL.ref(INT_TYPE_NULL_VALUE_FIELD), DSL.literal(1));
    assertEquals(ExprType.BOOLEAN, greater.type(typeEnv()));
    assertEquals(LITERAL_NULL, greater.valueOf(valueEnv()));

    greater = dsl.greater(typeEnv(), DSL.ref(INT_TYPE_NULL_VALUE_FIELD),
        DSL.ref(INT_TYPE_NULL_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, greater.type(typeEnv()));
    assertEquals(LITERAL_NULL, greater.valueOf(valueEnv()));
  }

  @Test
  public void test_greater_missing() {
    FunctionExpression greater = dsl.greater(typeEnv(), DSL.literal(1),
        DSL.ref(INT_TYPE_MISSING_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, greater.type(typeEnv()));
    assertEquals(LITERAL_MISSING, greater.valueOf(valueEnv()));

    greater = dsl.greater(typeEnv(), DSL.ref(INT_TYPE_MISSING_VALUE_FIELD), DSL.literal(1));
    assertEquals(ExprType.BOOLEAN, greater.type(typeEnv()));
    assertEquals(LITERAL_MISSING, greater.valueOf(valueEnv()));

    greater = dsl.greater(typeEnv(), DSL.ref(INT_TYPE_MISSING_VALUE_FIELD),
        DSL.ref(INT_TYPE_MISSING_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, greater.type(typeEnv()));
    assertEquals(LITERAL_MISSING, greater.valueOf(valueEnv()));
  }

  @Test
  public void test_null_greater_missing() {
    FunctionExpression greater = dsl.greater(typeEnv(), DSL.ref(INT_TYPE_NULL_VALUE_FIELD),
        DSL.ref(INT_TYPE_MISSING_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, greater.type(typeEnv()));
    assertEquals(LITERAL_NULL, greater.valueOf(valueEnv()));

    greater = dsl.greater(typeEnv(), DSL.ref(INT_TYPE_MISSING_VALUE_FIELD),
        DSL.ref(INT_TYPE_NULL_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, greater.type(typeEnv()));
    assertEquals(LITERAL_NULL, greater.valueOf(valueEnv()));
  }

  @ParameterizedTest(name = "gte({0}, {1})")
  @MethodSource("testCompareValueArguments")
  public void test_gte(ExprValue v1, ExprValue v2) {
    FunctionExpression gte = dsl.gte(typeEnv(), DSL.literal(v1), DSL.literal(v2));
    assertEquals(ExprType.BOOLEAN, gte.type(typeEnv()));
    assertEquals(v1.compareTo(v2) >= 0,
        ExprValueUtils.getBooleanValue(gte.valueOf(valueEnv())));
    assertEquals(String.format("%s >= %s", v1.toString(), v2.toString()), gte.toString());
  }

  @Test
  public void test_gte_null() {
    FunctionExpression gte = dsl.gte(typeEnv(), DSL.literal(1),
        DSL.ref(INT_TYPE_NULL_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, gte.type(typeEnv()));
    assertEquals(LITERAL_NULL, gte.valueOf(valueEnv()));

    gte = dsl.gte(typeEnv(), DSL.ref(INT_TYPE_NULL_VALUE_FIELD), DSL.literal(1));
    assertEquals(ExprType.BOOLEAN, gte.type(typeEnv()));
    assertEquals(LITERAL_NULL, gte.valueOf(valueEnv()));

    gte = dsl.gte(typeEnv(), DSL.ref(INT_TYPE_NULL_VALUE_FIELD),
        DSL.ref(INT_TYPE_NULL_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, gte.type(typeEnv()));
    assertEquals(LITERAL_NULL, gte.valueOf(valueEnv()));
  }

  @Test
  public void test_gte_missing() {
    FunctionExpression gte = dsl.gte(typeEnv(), DSL.literal(1),
        DSL.ref(INT_TYPE_MISSING_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, gte.type(typeEnv()));
    assertEquals(LITERAL_MISSING, gte.valueOf(valueEnv()));

    gte = dsl.gte(typeEnv(), DSL.ref(INT_TYPE_MISSING_VALUE_FIELD), DSL.literal(1));
    assertEquals(ExprType.BOOLEAN, gte.type(typeEnv()));
    assertEquals(LITERAL_MISSING, gte.valueOf(valueEnv()));

    gte = dsl.gte(typeEnv(), DSL.ref(INT_TYPE_MISSING_VALUE_FIELD),
        DSL.ref(INT_TYPE_MISSING_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, gte.type(typeEnv()));
    assertEquals(LITERAL_MISSING, gte.valueOf(valueEnv()));
  }

  @Test
  public void test_null_gte_missing() {
    FunctionExpression gte = dsl.gte(typeEnv(), DSL.ref(INT_TYPE_NULL_VALUE_FIELD),
        DSL.ref(INT_TYPE_MISSING_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, gte.type(typeEnv()));
    assertEquals(LITERAL_NULL, gte.valueOf(valueEnv()));

    gte = dsl.gte(typeEnv(), DSL.ref(INT_TYPE_MISSING_VALUE_FIELD),
        DSL.ref(INT_TYPE_NULL_VALUE_FIELD));
    assertEquals(ExprType.BOOLEAN, gte.type(typeEnv()));
    assertEquals(LITERAL_NULL, gte.valueOf(valueEnv()));
  }

  @ParameterizedTest(name = "like({0}, {1})")
  @MethodSource("testLikeArguments")
  public void test_like(ExprValue v1, ExprValue v2) {
    FunctionExpression like = dsl.like(typeEnv(), DSL.literal(v1), DSL.literal(v2));
    assertEquals(ExprType.BOOLEAN, like.type(typeEnv()));
    assertEquals(matches(((String) v1.value()), (String) v2.value()),
        ExprValueUtils.getBooleanValue(like.valueOf(valueEnv())));
    assertEquals(String.format("%s like %s", v1.toString(), v2.toString()), like.toString());
  }

  @Test
  public void test_null_like_missing() {
    FunctionExpression like = dsl.like(typeEnv(), DSL.ref(STRING_TYPE_NULL_VALUE_FILED),
        DSL.ref(STRING_TYPE_NULL_VALUE_FILED));
    assertEquals(ExprType.BOOLEAN, like.type(typeEnv()));
    assertEquals(LITERAL_TRUE, like.valueOf(valueEnv()));

    like = dsl.like(typeEnv(), DSL.ref(STRING_TYPE_MISSING_VALUE_FILED),
        DSL.ref(STRING_TYPE_MISSING_VALUE_FILED));
    assertEquals(ExprType.BOOLEAN, like.type(typeEnv()));
    assertEquals(LITERAL_TRUE, like.valueOf(valueEnv()));

    like = dsl.like(typeEnv(), DSL.ref(STRING_TYPE_NULL_VALUE_FILED),
        DSL.ref(STRING_TYPE_MISSING_VALUE_FILED));
    assertEquals(ExprType.BOOLEAN, like.type(typeEnv()));
    assertEquals(LITERAL_FALSE, like.valueOf(valueEnv()));

    like = dsl.like(typeEnv(), DSL.ref(STRING_TYPE_MISSING_VALUE_FILED),
        DSL.ref(STRING_TYPE_NULL_VALUE_FILED));
    assertEquals(ExprType.BOOLEAN, like.type(typeEnv()));
    assertEquals(LITERAL_FALSE, like.valueOf(valueEnv()));
  }
}