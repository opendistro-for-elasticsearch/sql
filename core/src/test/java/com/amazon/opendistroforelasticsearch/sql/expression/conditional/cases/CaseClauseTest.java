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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprIntegerValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprNullValue;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionTestBase;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class CaseClauseTest extends ExpressionTestBase {

  @Mock
  private WhenClause whenClause;

  @Test
  void should_return_when_clause_result_if_matched() {
    when(whenClause.isTrue(any())).thenReturn(true);
    when(whenClause.valueOf(any())).thenReturn(new ExprIntegerValue(30));

    CaseClause caseClause = new CaseClause(ImmutableList.of(whenClause), null);
    assertEquals(new ExprIntegerValue(30), caseClause.valueOf(valueEnv()));
  }

  @Test
  void should_return_default_result_if_none_matched() {
    when(whenClause.isTrue(any())).thenReturn(false);

    CaseClause caseClause = new CaseClause(ImmutableList.of(whenClause), DSL.literal(50));
    assertEquals(new ExprIntegerValue(50), caseClause.valueOf(valueEnv()));
  }

  @Test
  void should_return_default_result_if_none_matched_and_no_default() {
    when(whenClause.isTrue(any())).thenReturn(false);

    CaseClause caseClause = new CaseClause(ImmutableList.of(whenClause), null);
    assertEquals(ExprNullValue.of(), caseClause.valueOf(valueEnv()));
  }

  @Test
  void should_use_type_of_when_clause() {
    when(whenClause.type()).thenReturn(ExprCoreType.INTEGER);

    CaseClause caseClause = new CaseClause(ImmutableList.of(whenClause), null);
    assertEquals(ExprCoreType.INTEGER, caseClause.type());
  }

  @Test
  void should_return_all_result_types_including_default() {
    when(whenClause.type()).thenReturn(ExprCoreType.INTEGER);
    Expression defaultResult = mock(Expression.class);
    when(defaultResult.type()).thenReturn(ExprCoreType.STRING);

    CaseClause caseClause = new CaseClause(ImmutableList.of(whenClause), defaultResult);
    assertEquals(
        ImmutableList.of(ExprCoreType.INTEGER, ExprCoreType.STRING),
        caseClause.allResultTypes());
  }

}