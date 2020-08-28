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
import com.amazon.opendistroforelasticsearch.sql.exception.SemanticCheckException;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.GroupByClauseContext;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParserBaseVisitor;
import com.amazon.opendistroforelasticsearch.sql.sql.parser.context.QuerySpecification;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
  public UnresolvedPlan visit(ParseTree groupByClause) {
    if (groupByClause == null) {
      if (isAllSelectItemNonAggregated()) {
        // No GROUP BY and aggregate function in SELECT
        return null;
      }
      return buildImplicitAggregation();
    }
    return super.visit(groupByClause);
  }

  @Override
  public UnresolvedPlan visitGroupByClause(GroupByClauseContext ctx) {
    List<UnresolvedExpression> groupByItems = replaceGroupByItemIfAliasOrOrdinal();

    Optional<UnresolvedExpression> invalidSelectItem =
        findNonAggregatedSelectItemMissingInGroupBy(groupByItems);

    if (invalidSelectItem.isPresent()) {
      throw new SemanticCheckException(String.format(
          "Expression [%s] that contains non-aggregated column is not present in group by clause",
              invalidSelectItem.get()));
    }

    return new Aggregation(
        new ArrayList<>(querySpec.getAggregators()),
        emptyList(),
        groupByItems);
  }

  private UnresolvedPlan buildImplicitAggregation() {
    Optional<UnresolvedExpression> invalidSelectItem =
        findNonAggregatedSelectItemMissingInGroupBy(emptyList());

    if (invalidSelectItem.isPresent()) {
      // Report semantic error to avoid fall back to old engine again
      throw new SemanticCheckException(String.format(
          "Explicit GROUP BY clause is required because expression [%s] "
              + "contains non-aggregated column", invalidSelectItem.get()));
    }

    return new Aggregation(
        new ArrayList<>(querySpec.getAggregators()),
        emptyList(),
        querySpec.getGroupByItems());
  }

  private List<UnresolvedExpression> replaceGroupByItemIfAliasOrOrdinal() {
    Map<String, UnresolvedExpression> selectItemsByAlias = querySpec.getSelectItemsByAlias();
    List<UnresolvedExpression> groupByItems = new ArrayList<>();
    for (UnresolvedExpression expr : querySpec.getGroupByItems()) {
      groupByItems.add(
          selectItemsByAlias.getOrDefault(expr.toString(), expr));
    }
    return groupByItems;
  }

  private Optional<UnresolvedExpression> findNonAggregatedSelectItemMissingInGroupBy(
      List<UnresolvedExpression> groupByItemList) {
    Set<UnresolvedExpression> groupByItems = new HashSet<>(groupByItemList);
    return querySpec.getSelectItems().stream()
                                     .filter(this::isNonAggregatedExpression)
                                     .filter(expr -> !groupByItems.contains(expr))
                                     .findFirst();
  }

  private boolean isAllSelectItemNonAggregated() {
    return querySpec.getSelectItems().stream()
                                     .allMatch(this::isNonAggregatedExpression);
  }

  private boolean isNonAggregatedExpression(UnresolvedExpression expr) {
    List<? extends Node> children = expr.getChild();
    if (children.isEmpty()) {
      return true;
    }
    return !(expr instanceof AggregateFunction)
        && children.stream()
                   .allMatch(child -> isNonAggregatedExpression((UnresolvedExpression) child));
  }

}
