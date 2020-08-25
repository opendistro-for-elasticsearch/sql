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

package com.amazon.opendistroforelasticsearch.sql.sql.parser;

import static java.util.Collections.emptyList;

import com.amazon.opendistroforelasticsearch.sql.ast.Node;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.AggregateFunction;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Aggregation;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.UnresolvedPlan;
import com.amazon.opendistroforelasticsearch.sql.common.antlr.SyntaxCheckException;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.GroupByClauseContext;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParserBaseVisitor;
import com.amazon.opendistroforelasticsearch.sql.sql.parser.context.QuerySpecification;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * AST aggregation builder that builds AST aggregation node.
 */
@RequiredArgsConstructor
public class AstAggregationBuilder extends OpenDistroSQLParserBaseVisitor<UnresolvedPlan> {

  /**
   * Query specification that contains info collected beforehand.
   */
  private final QuerySpecification querySpec;

  @Override
  public UnresolvedPlan visit(ParseTree tree) {
    if (tree == null) {
      return buildImplicitAggregation();
    }
    return super.visit(tree);
  }

  @Override
  public UnresolvedPlan visitGroupByClause(GroupByClauseContext ctx) {
    Optional<UnresolvedExpression> invalidSelectItem = findSelectItemMissingInGroupBy();
    if (invalidSelectItem.isPresent()) {
      throw new SyntaxCheckException("");
    }

    return new Aggregation(
        new ArrayList<>(querySpec.getAggregators()),
        emptyList(),
        querySpec.getGroupByItems());
  }

  private UnresolvedPlan buildImplicitAggregation() {
    return new Aggregation(
        new ArrayList<>(querySpec.getAggregators()),
        emptyList(),
        querySpec.getGroupByItems());
  }

  private Optional<UnresolvedExpression> findSelectItemMissingInGroupBy() {
    Set<UnresolvedExpression> groupByItems = new HashSet<>(querySpec.getGroupByItems());
    return querySpec.getSelectItems().stream()
                                     .filter(this::isScalarExpression)
                                     .filter(expr -> !groupByItems.contains(expr))
                                     .findFirst();
  }

  private boolean isScalarExpression(UnresolvedExpression expr) {
    List<? extends Node> children = expr.getChild();
    if (children == null) {
      return true;
    }
    return !(expr instanceof AggregateFunction)
        && children.stream()
                   .allMatch(child -> isScalarExpression((UnresolvedExpression) child));
  }

}
