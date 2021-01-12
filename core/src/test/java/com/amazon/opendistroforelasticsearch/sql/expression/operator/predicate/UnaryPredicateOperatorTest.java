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
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprNullValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionTestBase;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;

import java.lang.reflect.Array;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
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
  public void test_isnull_predicate() {
    ArrayList<Expression> exprValueArrayList = new ArrayList<>();
    exprValueArrayList.add(dsl.literal("test"));
    exprValueArrayList.add(dsl.literal(100));
    exprValueArrayList.add(dsl.literal(""));

    for (Expression expression : exprValueArrayList) {
      FunctionExpression functionExpression = dsl.isnull(expression);
      assertEquals(BOOLEAN, functionExpression.type());
      if (expression.valueOf(valueEnv()) == LITERAL_NULL
              || expression.valueOf(valueEnv()) == LITERAL_MISSING) {
        assertEquals(LITERAL_TRUE, functionExpression.valueOf(valueEnv()));
      } else {
        assertEquals(LITERAL_FALSE, functionExpression.valueOf(valueEnv()));
      }
    }
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

  @Test
  public void test_ifnull_predicate() {
    ArrayList<Expression> exprValueArrayList = new ArrayList<>();
    exprValueArrayList.add(dsl.literal(100));
    exprValueArrayList.add(dsl.literal(200));

    for (Expression expressionOne : exprValueArrayList) {
      for (Expression expressionTwo : exprValueArrayList) {
        FunctionExpression functionExpression = dsl.ifnull(expressionOne, expressionTwo);
        if (expressionOne.valueOf(valueEnv()) == LITERAL_NULL
                || expressionOne.valueOf(valueEnv()) == LITERAL_MISSING) {
          assertEquals(expressionTwo.valueOf(valueEnv()), functionExpression.valueOf(valueEnv()));
        } else {
          assertEquals(expressionOne.valueOf(valueEnv()), functionExpression.valueOf(valueEnv()));
        }
      }
    }

  }

  @Test
  public void test_nullif_predicate() {
    ArrayList<Expression> exprValueArrayList = new ArrayList<>();
    exprValueArrayList.add(DSL.literal(123));
    exprValueArrayList.add(DSL.literal(321));

    for (Expression v1 : exprValueArrayList) {
      for (Expression v2 : exprValueArrayList) {
        FunctionExpression result = dsl.nullif(v1, v2);
        if (v1.valueOf(valueEnv()) == v2.valueOf(valueEnv())) {
          assertEquals(LITERAL_NULL, result.valueOf(valueEnv()));
        } else {
          assertEquals(v1.valueOf(valueEnv()), result.valueOf(valueEnv()));
        }
      }
    }
  }

  @Test
  public void test_exprIfNull() {
    ArrayList<ExprValue> exprValues = new ArrayList<>();
    exprValues.add(LITERAL_NULL);
    exprValues.add(LITERAL_MISSING);
    exprValues.add(ExprValueUtils.integerValue(123));
    exprValues.add(ExprValueUtils.stringValue("test"));

    for (ExprValue exprValueOne : exprValues) {
      for (ExprValue exprValueTwo : exprValues) {
        ExprValue result = UnaryPredicateOperator.exprIfNull(exprValueOne, exprValueTwo);
        if (exprValueOne.isNull() || exprValueOne.isMissing()) {
          assertEquals(exprValueTwo.value(), result.value());
        } else {
          assertEquals(exprValueOne.value(), result.value());
        }
      }
    }
  }

  @Test
  public void test_exprNullIf() {
    ArrayList<ExprValue> exprValues = new ArrayList<>();
    exprValues.add(LITERAL_NULL);
    exprValues.add(LITERAL_MISSING);
    exprValues.add(ExprValueUtils.integerValue(123));
    exprValues.add(ExprValueUtils.integerValue(456));

    for (ExprValue exprValueOne : exprValues) {
      for (ExprValue exprValueTwo : exprValues) {
        ExprValue result = UnaryPredicateOperator.exprNullIf(exprValueOne, exprValueTwo);
        if (exprValueOne.equals(exprValueTwo)) {
          assertEquals(LITERAL_NULL.value(), result.value());
        } else {
          assertEquals(exprValueOne.value(), result.value());
        }
      }
    }
  }
}