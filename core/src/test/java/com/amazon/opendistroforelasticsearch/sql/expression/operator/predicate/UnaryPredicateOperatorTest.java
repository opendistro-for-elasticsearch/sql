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
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionTestBase;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
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
  public void is_null_predicate() {
    FunctionExpression expression = dsl.isnull(DSL.literal(1));
    assertEquals(BOOLEAN, expression.type());
    assertEquals(LITERAL_FALSE, expression.valueOf(valueEnv()));

    expression = dsl.isnull(DSL.literal(ExprNullValue.of()));
    assertEquals(BOOLEAN, expression.type());
    assertEquals(LITERAL_TRUE, expression.valueOf(valueEnv()));
  }

  @Test
  public void is_not_null_predicate() {
    FunctionExpression expression = dsl.isnotnull(DSL.literal(1));
    assertEquals(BOOLEAN, expression.type());
    assertEquals(LITERAL_TRUE, expression.valueOf(valueEnv()));

    expression = dsl.isnotnull(DSL.literal(ExprNullValue.of()));
    assertEquals(BOOLEAN, expression.type());
    assertEquals(LITERAL_FALSE, expression.valueOf(valueEnv()));
  }
}