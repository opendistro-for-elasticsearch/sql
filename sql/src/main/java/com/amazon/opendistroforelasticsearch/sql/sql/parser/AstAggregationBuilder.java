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
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Alias;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Literal;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Aggregation;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.UnresolvedPlan;
import com.amazon.opendistroforelasticsearch.sql.common.utils.StringUtils;
import com.amazon.opendistroforelasticsearch.sql.exception.SemanticCheckException;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.GroupByClauseContext;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParserBaseVisitor;
import com.amazon.opendistroforelasticsearch.sql.sql.parser.context.QuerySpecification;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * <pre>SelectExpressionAnalyzerTest
 * AST aggregation builder that builds AST aggregation node for the following scenarios:
 *
 *  1. Explicit GROUP BY
 *     1.1 Group by column name or scalar expression:
 *          SELECT ABS(age) FROM test GROUP BY ABS(age)
 *     1.2 Group by alias in SELECT AS clause:
 *          SELECT state AS s FROM test GROUP BY s
 *     1.3 Group by ordinal referring to select list:
 *          SELECT state FROM test GROUP BY 1
 *  2. Implicit GROUP BY
 *     2.1 No non-aggregated item (only aggregate functions):
 *          SELECT AVG(age), SUM(balance) FROM test
 *     2.2 Non-aggregated item exists:
 *          SELECT state, AVG(age) FROM test
 *
 *  For 1.1 and 2.1, Aggregation node is built with aggregators.
 *  For 1.2 and 1.3, alias and ordinal is replaced first and then
 *    Aggregation is built same as above.
 *  For 2.2, Exception thrown for now. We may support this by different SQL mode.
 *
 * Note the responsibility separation between this builder and analyzer in core engine:
 *
 *  1. This builder is only responsible for AST node building and handle special SQL
 *     syntactical cases aforementioned. The validation in this builder is essentially
 *     static based on syntactic information.
 *  2. Analyzer will perform semantic check and report semantic error as needed.
 * </pre>
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
      if (isAggregatorNotFoundAnywhere()) {
        // Simple select query without GROUP BY and aggregate function in SELECT
        return null;
      }
      return buildImplicitAggregation();
    }
    return super.visit(groupByClause);
  }

  @Override
  public UnresolvedPlan visitGroupByClause(GroupByClauseContext ctx) {
    List<UnresolvedExpression> groupByItems = replaceGroupByItemIfAliasOrOrdinal();
    return new Aggregation(
        new ArrayList<>(querySpec.getAggregators()),
        emptyList(),
        groupByItems);
  }

  private UnresolvedPlan buildImplicitAggregation() {
    Optional<UnresolvedExpression> invalidSelectItem = findNonAggregatedItemInSelect();

    if (invalidSelectItem.isPresent()) {
      // Report semantic error to avoid fall back to old engine again
      throw new SemanticCheckException(StringUtils.format(
          "Explicit GROUP BY clause is required because expression [%s] "
              + "contains non-aggregated column", invalidSelectItem.get()));
    }

    return new Aggregation(
        new ArrayList<>(querySpec.getAggregators()),
        emptyList(),
        querySpec.getGroupByItems());
  }

  private List<UnresolvedExpression> replaceGroupByItemIfAliasOrOrdinal() {
    return querySpec.getGroupByItems()
                    .stream()
                    .map(querySpec::replaceIfAliasOrOrdinal)
                    .map(expr -> new Alias(expr.toString(), expr))
                    .collect(Collectors.toList());
  }

  /**
   * Find non-aggregate item in SELECT clause. Note that literal is special which is not required
   * to be applied by aggregate function.
   */
  private Optional<UnresolvedExpression> findNonAggregatedItemInSelect() {
    return querySpec.getSelectItems().stream()
                                     .filter(this::isNonLiteral)
                                     .filter(this::isNonAggregatedExpression)
                                     .findFirst();
  }

  private boolean isAggregatorNotFoundAnywhere() {
    return querySpec.getAggregators().isEmpty();
  }

  private boolean isNonLiteral(UnresolvedExpression expr) {
    return !(expr instanceof Literal);
  }

  private boolean isNonAggregatedExpression(UnresolvedExpression expr) {
    if (expr instanceof AggregateFunction) {
      return false;
    }

    List<? extends Node> children = expr.getChild();
    return children.stream()
                   .allMatch(child -> isNonAggregatedExpression((UnresolvedExpression) child));
  }

}
