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

package com.amazon.opendistroforelasticsearch.sql.sql.parser.context;

import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.GroupByElementContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.SelectElementContext;

import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.AggregateFunctionCallContext;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.QuerySpecificationContext;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParserBaseVisitor;
import com.amazon.opendistroforelasticsearch.sql.sql.parser.AstExpressionBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Query specification domain that collects and represents essential info for a simple query.
 * During collect, it avoids collecting sub-query info wrongly.
 * (I) What is the responsibility?
 *  This abstraction turns AST building process into two passes:
 *  1) Query specification collects info
 *  2) AST builder uses the info to build AST node
 * (II) Why is this required?
 *  There are two reasons as follows that make one pass building hard or impossible:
 *  1) Some info spread across the query: aggregation AST node build needs to know all
 *     aggregate function calls in SELECT and HAVING clause
 *  2) Some AST node needs info from other node built later: GROUP BY or HAVING clause
 *     may contain aliases defined in SELECT clause.
 */
@Getter
@RequiredArgsConstructor
public class QuerySpecification extends OpenDistroSQLParserBaseVisitor<Void> {

  private final QuerySpecification parent;

  private final AstExpressionBuilder expressionBuilder = new AstExpressionBuilder();

  /**
   * Items in SELECT clause.
   */
  private final List<UnresolvedExpression> selectItems = new ArrayList<>();

  /**
   * Aggregate function calls that spreads in SELECT, HAVING clause. Since this is going to be
   * pushed to aggregation operator, de-duplicate is necessary to avoid duplicate computation.
   */
  private final Set<UnresolvedExpression> aggregators = new HashSet<>();

  /**
   * Items in GROUP BY clause that may be simple field name or nested in scalar function call.
   */
  private final List<UnresolvedExpression> groupByItems = new ArrayList<>();

  public QuerySpecification() {
    this(null);
  }

  public void collect(QuerySpecificationContext query) {
    query.accept(this);
  }

  @Override
  public Void visitSelectElement(SelectElementContext ctx) {
    selectItems.add(visitAstExpression(ctx));
    return super.visitSelectElement(ctx);
  }

  @Override
  public Void visitGroupByElement(GroupByElementContext ctx) {
    groupByItems.add(visitAstExpression(ctx));
    return super.visitGroupByElement(ctx);
  }

  @Override
  public Void visitAggregateFunctionCall(AggregateFunctionCallContext ctx) {
    aggregators.add(visitAstExpression(ctx));
    return super.visitAggregateFunctionCall(ctx);
  }

  /*
  @VisibleForTesting
  public void addSelectItem(UnresolvedExpression expr) {
    selectItems.add(expr);
  }

  @VisibleForTesting
  public void addAggregator(UnresolvedExpression expr) {
    aggregators.add(expr);
  }

  @VisibleForTesting
  public void addGroupByItems(UnresolvedExpression expr) {
    groupByItems.add(expr);
  }
  */

  /*
  @Override
  public Void visitQuerySpecification(QuerySpecificationContext ctx) {
    // Avoid collecting items on deeper level once sub-queries enabled
    return null;
  }
  */

  private UnresolvedExpression visitAstExpression(ParseTree tree) {
    return expressionBuilder.visit(tree);
  }

}
