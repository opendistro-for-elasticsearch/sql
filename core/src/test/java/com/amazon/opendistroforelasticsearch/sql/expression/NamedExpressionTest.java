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

package com.amazon.opendistroforelasticsearch.sql.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class NamedExpressionTest extends ExpressionTestBase {

  @Test
  void name_an_expression() {
    LiteralExpression delegated = DSL.literal(10);
    NamedExpression namedExpression = DSL.named("10", delegated);

    assertEquals("10", namedExpression.getName());
    assertEquals(delegated.type(), namedExpression.type());
    assertEquals(delegated.valueOf(valueEnv()), namedExpression.valueOf(valueEnv()));
  }

  @Test
  void name_an_expression_with_alias() {
    LiteralExpression delegated = DSL.literal(10);
    NamedExpression namedExpression = DSL.named("10", delegated, "ten");
    assertEquals("ten", namedExpression.getName());
  }

}