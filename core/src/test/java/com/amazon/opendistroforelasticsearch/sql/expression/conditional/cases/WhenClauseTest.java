/*
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.expression.conditional.cases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprIntegerValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionTestBase;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class WhenClauseTest extends ExpressionTestBase {

  @Test
  void should_not_match_if_condition_evaluated_to_null() {
    Expression condition = mock(Expression.class);
    when(condition.valueOf(any())).thenReturn(ExprValueUtils.nullValue());

    WhenClause whenClause = new WhenClause(condition, DSL.literal(30));
    assertFalse(whenClause.isTrue(valueEnv()));
  }

  @Test
  void should_not_match_if_condition_evaluated_to_missing() {
    Expression condition = mock(Expression.class);
    when(condition.valueOf(any())).thenReturn(ExprValueUtils.missingValue());

    WhenClause whenClause = new WhenClause(condition, DSL.literal(30));
    assertFalse(whenClause.isTrue(valueEnv()));
  }

  @Test
  void should_match_and_return_result_if_condition_is_true() {
    WhenClause whenClause = new WhenClause(DSL.literal(true), DSL.literal(30));
    assertTrue(whenClause.isTrue(valueEnv()));
    assertEquals(new ExprIntegerValue(30), whenClause.valueOf(valueEnv()));
  }

  @Test
  void should_use_result_expression_type() {
    WhenClause whenClause = new WhenClause(DSL.literal(true), DSL.literal(30));
    assertEquals(ExprCoreType.INTEGER, whenClause.type());
  }

}