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

import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.Aggregator;

/**
 * Abstract visitor for expression tree nodes.
 * @param <T>   type of return value to accumulate when visiting.
 * @param <C>   type of context.
 */
public abstract class ExpressionNodeVisitor<T, C> {

  public T visitNode(Expression node, C context) {
    return null;
  }

  public T visitLiteral(LiteralExpression node, C context) {
    return visitNode(node, context);
  }

  public T visitNamed(NamedExpression node, C context) {
    return visitNode(node, context);
  }

  public T visitReference(ReferenceExpression node, C context) {
    return visitNode(node, context);
  }

  public T visitFunction(FunctionExpression node, C context) {
    return visitNode(node, context);
  }

  public T visitAggregator(Aggregator<?> node, C context) {
    return visitNode(node, context);
  }

}
