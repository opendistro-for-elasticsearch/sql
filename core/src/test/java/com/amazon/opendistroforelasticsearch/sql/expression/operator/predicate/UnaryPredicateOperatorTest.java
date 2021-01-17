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
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_FALSE;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_MISSING;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_NULL;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_TRUE;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.booleanValue;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.BOOLEAN;
import static java.lang.Enum.valueOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprNullValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionTestBase;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.google.common.collect.Lists;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class UnaryPredicateOperatorTest extends ExpressionTestBase {
  @ParameterizedTest(name = "not({0})")
  @ValueSource(booleans = {true, false})
  public void test_not(Boolean v) {
    FunctionExpression not = dsl.not(DSL.literal(booleanValue(v)));
    assertEquals(BOOLEAN, not.type());
    assertEquals(!v, ExprValueUtils.getBooleanValue(not.valueOf(valueEnv())));
    assertEquals(String.format("not(%s)", v.toString()), not.toString());
  }

  private static Stream<Arguments> isNullArguments() {
    ArrayList<Expression> expressions = new ArrayList<>();
    expressions.add(DSL.literal("test"));
    expressions.add(DSL.literal(100));
    expressions.add(DSL.literal(""));
    expressions.add(DSL.literal(LITERAL_NULL));

    return Lists.cartesianProduct(expressions, expressions).stream()
        .map(list -> {
          Expression e1 = list.get(0);
          if (e1.valueOf(valueEnv()).isNull()
                  || e1.valueOf(valueEnv()).isMissing()) {
            return Arguments.of(e1, DSL.literal(LITERAL_TRUE));
          } else {
            return Arguments.of(e1, DSL.literal(LITERAL_FALSE));
          }
        });
  }

  private static Stream<Arguments> ifNullArguments() {
    ArrayList<Expression> exprValueArrayList = new ArrayList<>();
    exprValueArrayList.add(DSL.literal(123));
    exprValueArrayList.add(DSL.literal(LITERAL_NULL));
    exprValueArrayList.add(DSL.literal(321));
    exprValueArrayList.add(DSL.literal(LITERAL_NULL));

    return Lists.cartesianProduct(exprValueArrayList, exprValueArrayList).stream()
        .map(list -> {
          Expression e1 = list.get(0);
          Expression e2 = list.get(1);
          if (e1.valueOf(valueEnv()).value() == LITERAL_NULL.value()
                  || e1.valueOf(valueEnv()).value() == LITERAL_MISSING) {
            return Arguments.of(e1, e2, e2);
          } else {
            return Arguments.of(e1, e2, e1);
          }
        });
  }

  private static Stream<Arguments> nullIfArguments() {
    ArrayList<Expression> exprValueArrayList = new ArrayList<>();
    exprValueArrayList.add(DSL.literal(123));
    exprValueArrayList.add(DSL.literal(321));

    return Lists.cartesianProduct(exprValueArrayList, exprValueArrayList).stream()
        .map(list -> {
          Expression e1 = list.get(0);
          Expression e2 = list.get(1);

          if (e1.equals(e2)) {
            return Arguments.of(e1, e2, DSL.literal(LITERAL_NULL));
          } else {
            return Arguments.of(e1, e2, e1);
          }
        });
  }

  private static Stream<Arguments> ifArguments() {
    ArrayList<Expression> exprValueArrayList = new ArrayList<>();
    exprValueArrayList.add(DSL.literal(LITERAL_TRUE));
    exprValueArrayList.add(DSL.literal(LITERAL_FALSE));
    exprValueArrayList.add(DSL.literal(LITERAL_NULL));
    exprValueArrayList.add(DSL.literal(LITERAL_MISSING));

    return Lists.cartesianProduct(exprValueArrayList, exprValueArrayList).stream()
        .map(list -> {
          Expression e1 = list.get(0);
          if (e1.valueOf(valueEnv()).value() == LITERAL_TRUE.value()) {
            return Arguments.of(e1, DSL.literal("123"), DSL.literal("321"), DSL.literal("123"));
          } else {
            return Arguments.of(e1, DSL.literal("123"), DSL.literal("321"), DSL.literal("321"));
          }
        });
  }

  private static Stream<Arguments> exprIfNullArguments() {
    ArrayList<ExprValue> exprValues = new ArrayList<>();
    exprValues.add(LITERAL_NULL);
    exprValues.add(LITERAL_MISSING);
    exprValues.add(ExprValueUtils.integerValue(123));
    exprValues.add(ExprValueUtils.integerValue(456));

    return Lists.cartesianProduct(exprValues, exprValues).stream()
        .map(list -> {
          ExprValue e1 = list.get(0);
          ExprValue e2 = list.get(1);
          if (e1.isNull() || e1.isMissing()) {
            return Arguments.of(e1, e2, e2);
          } else {
            return Arguments.of(e1, e2, e1);
          }
        });
  }

  private static Stream<Arguments> exprNullIfArguments() {
    ArrayList<ExprValue> exprValues = new ArrayList<>();
    exprValues.add(LITERAL_NULL);
    exprValues.add(LITERAL_MISSING);
    exprValues.add(ExprValueUtils.integerValue(123));

    return Lists.cartesianProduct(exprValues, exprValues).stream()
        .map(list -> {
          ExprValue e1 = list.get(0);
          ExprValue e2 = list.get(1);
          if (e1.equals(e2)) {
            return Arguments.of(e1, e2, LITERAL_NULL);
          } else {
            return Arguments.of(e1, e2, e1);
          }
        });
  }

  @Test
  public void test_not_null() {
    FunctionExpression expression = dsl.not(DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD, BOOLEAN));
    assertEquals(BOOLEAN, expression.type());
    assertEquals(LITERAL_NULL, expression.valueOf(valueEnv()));
  }

  @Test
  public void test_not_missing() {
    FunctionExpression expression = dsl.not(DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD, BOOLEAN));
    assertEquals(BOOLEAN, expression.type());
    assertEquals(LITERAL_MISSING, expression.valueOf(valueEnv()));
  }

  @Test
  public void test_is_null_predicate() {
    FunctionExpression expression = dsl.is_null(DSL.literal(1));
    assertEquals(BOOLEAN, expression.type());
    assertEquals(LITERAL_FALSE, expression.valueOf(valueEnv()));

    expression = dsl.is_null(DSL.literal(ExprNullValue.of()));
    assertEquals(BOOLEAN, expression.type());
    assertEquals(LITERAL_TRUE, expression.valueOf(valueEnv()));
  }

  @Test
  public void test_is_not_null_predicate() {
    FunctionExpression expression = dsl.isnotnull(DSL.literal(1));
    assertEquals(BOOLEAN, expression.type());
    assertEquals(LITERAL_TRUE, expression.valueOf(valueEnv()));

    expression = dsl.isnotnull(DSL.literal(ExprNullValue.of()));
    assertEquals(BOOLEAN, expression.type());
    assertEquals(LITERAL_FALSE, expression.valueOf(valueEnv()));
  }

  @ParameterizedTest
  @MethodSource("isNullArguments")
  public void test_isnull_predicate(Expression v1, Expression expected) {
    assertEquals(expected.valueOf(valueEnv()), dsl.isnull(v1).valueOf(valueEnv()));
  }

  @ParameterizedTest
  @MethodSource("ifNullArguments")
  public void test_ifnull_predicate(Expression v1, Expression v2, Expression expected) {
    assertEquals(expected.valueOf(valueEnv()), dsl.ifnull(v1, v2).valueOf(valueEnv()));
  }

  @ParameterizedTest
  @MethodSource("nullIfArguments")
  public void test_nullif_predicate(Expression v1, Expression v2, Expression expected) {
    assertEquals(expected.valueOf(valueEnv()), dsl.nullif(v1, v2).valueOf(valueEnv()));
  }

  @ParameterizedTest
  @MethodSource("ifArguments")
  public void test_if_predicate(Expression v1, Expression v2, Expression v3, Expression expected) {
    assertEquals(expected.valueOf(valueEnv()), dsl.iffunction(v1, v2, v3).valueOf(valueEnv()));
  }

  @ParameterizedTest
  @MethodSource("exprIfNullArguments")
  public void test_exprIfNull_predicate(ExprValue v1, ExprValue v2, ExprValue expected) {
    assertEquals(expected.value(), UnaryPredicateOperator.exprIfNull(v1, v2).value());
  }

  @ParameterizedTest
  @MethodSource("exprNullIfArguments")
  public void test_exprNullIf_predicate(ExprValue v1, ExprValue v2, ExprValue expected) {
    assertEquals(expected.value(), UnaryPredicateOperator.exprNullIf(v1, v2).value());
  }

}