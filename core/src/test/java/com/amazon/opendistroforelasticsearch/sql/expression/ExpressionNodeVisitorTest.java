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

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.literal;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.named;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.ref;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.Aggregator;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.AvgAggregator;
import com.amazon.opendistroforelasticsearch.sql.expression.config.ExpressionConfig;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ExpressionNodeVisitorTest {

  private final DSL dsl = new ExpressionConfig().dsl(new ExpressionConfig().functionRepository());

  @Test
  void should_return_null_by_default() {
    ExpressionNodeVisitor<Object, Object> visitor = new ExpressionNodeVisitor<Object, Object>(){};
    assertNull(literal(10).accept(visitor, null));
    assertNull(ref("name", STRING).accept(visitor, null));
    assertNull(named("bool", literal(true)).accept(visitor, null));
    assertNull(dsl.abs(literal(-10)).accept(visitor, null));
    assertNull(dsl.sum(literal(10)).accept(visitor, null));
    assertNull(named("avg", new AvgAggregator(Collections.singletonList(ref("age", INTEGER)),
        INTEGER)).accept(visitor, null));
  }

  @Test
  void can_visit_all_types_of_expression_node() {
    Expression expr =
        dsl.sum(
            dsl.add(
                ref("balance", INTEGER),
                literal(10)));

    Expression actual = expr.accept(new ExpressionNodeVisitor<Expression, Object>() {
      @Override
      public Expression visitLiteral(LiteralExpression node, Object context) {
        return node;
      }

      @Override
      public Expression visitReference(ReferenceExpression node, Object context) {
        return node;
      }

      @Override
      public Expression visitFunction(FunctionExpression node, Object context) {
        return dsl.add(visitArguments(node.getArguments(), context));
      }

      @Override
      public Expression visitAggregator(Aggregator<?> node, Object context) {
        return dsl.sum(visitArguments(node.getArguments(), context));
      }

      private Expression[] visitArguments(List<Expression> arguments, Object context) {
        return arguments.stream()
                        .map(arg -> arg.accept(this, context))
                        .toArray(Expression[]::new);
      }
    }, null);

    assertEquals(expr, actual);
  }

}