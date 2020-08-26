/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.opendistroforelasticsearch.sql.ast;

import com.amazon.opendistroforelasticsearch.sql.ast.expression.*;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.*;

/**
 * AST nodes visitor Defines the traverse path.
 */
public abstract class AbstractNodeVisitor<T, C> {

  public T visit(Node node, C context) {
    return null;
  }

  /**
   * Visit child node.
   * @param node {@link Node}
   * @param context Context
   * @return Return Type.
   */
  public T visitChildren(Node node, C context) {
    T result = defaultResult();

    for (Node child : node.getChild()) {
      T childResult = child.accept(this, context);
      result = aggregateResult(result, childResult);
    }
    return result;
  }

  private T defaultResult() {
    return null;
  }

  private T aggregateResult(T aggregate, T nextResult) {
    return nextResult;
  }

  public T visitRelation(Relation node, C context) {
    return visitChildren(node, context);
  }

  public T visitFilter(Filter node, C context) {
    return visitChildren(node, context);
  }

  public T visitProject(Project node, C context) {
    return visitChildren(node, context);
  }

  public T visitAggregation(Aggregation node, C context) {
    return visitChildren(node, context);
  }

  public T visitEqualTo(EqualTo node, C context) {
    return visitChildren(node, context);
  }

  public T visitLiteral(Literal node, C context) {
    return visitChildren(node, context);
  }

  public T visitUnresolvedAttribute(UnresolvedAttribute node, C context) {
    return visitChildren(node, context);
  }

  public T visitAttributeList(AttributeList node, C context) {
    return visitChildren(node, context);
  }

  public T visitMap(Map node, C context) {
    return visitChildren(node, context);
  }

  public T visitNot(Not node, C context) {
    return visitChildren(node, context);
  }

  public T visitOr(Or node, C context) {
    return visitChildren(node, context);
  }

  public T visitAnd(And node, C context) {
    return visitChildren(node, context);
  }

  public T visitXor(Xor node, C context) {
    return visitChildren(node, context);
  }

  public T visitAggregateFunction(AggregateFunction node, C context) {
    return visitChildren(node, context);
  }

  public T visitFunction(Function node, C context) {
    return visitChildren(node, context);
  }

  public T visitIn(In node, C context) {
    return visitChildren(node, context);
  }

  public T visitCompare(Compare node, C context) {
    return visitChildren(node, context);
  }

  public T visitArgument(Argument node, C context) {
    return visitChildren(node, context);
  }

  public T visitField(Field node, C context) {
    return visitChildren(node, context);
  }

  public T visitQualifiedName(QualifiedName node, C context) {
    return visitChildren(node, context);
  }

  public T visitRename(Rename node, C context) {
    return visitChildren(node, context);
  }

  public T visitEval(Eval node, C context) {
    return visitChildren(node, context);
  }

  public T visitLet(Let node, C context) {
    return visitChildren(node, context);
  }

  public T visitSort(Sort node, C context) {
    return visitChildren(node, context);
  }

  public T visitDedupe(Dedupe node, C context) {
    return visitChildren(node, context);
  }

  public T visitHead(Head node, C context) {
    return visitChildren(node, context);
  }

  public T visitValues(Values node, C context) {
    return visitChildren(node, context);
  }

  public T visitAlias(Alias node, C context) {
    return visitChildren(node, context);
  }

  public T visitAllFields(AllFields node, C context) {
    return visitChildren(node, context);
  }

  public T visitUnresolvedArgument(UnresolvedArgument node, C context) {
    return visitChildren(node, context);
  }
}
