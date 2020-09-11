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
import static com.amazon.opendistroforelasticsearch.sql.sql.parser.ParserUtils.getTextInQuery;

import com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.common.utils.StringUtils;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.AggregateFunctionCallContext;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.QuerySpecificationContext;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParserBaseVisitor;
import com.amazon.opendistroforelasticsearch.sql.sql.parser.AstExpressionBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Query specification domain that collects basic info for a simple query.
 * <pre>
 * (I) What is the impact of this new abstraction?
 *  This abstraction and collecting process turns AST building process into two phases:
 *  1) Query specification collects info
 *  2) AST builder uses the info to build AST node
 *
 * (II) Why is this required?
 *  There are two reasons as follows that make single pass building hard or impossible:
 *  1) Building aggregation AST node needs to know all aggregate function calls
 *     in SELECT and HAVING clause
 *  2) GROUP BY or HAVING clause may contain aliases defined in SELECT clause
 *
 * </pre>
 */
@EqualsAndHashCode
@Getter
@ToString
public class QuerySpecification {

  /**
   * Items in SELECT clause and mapping from alias to select item.
   */
  private final List<UnresolvedExpression> selectItems = new ArrayList<>();
  private final Map<String, UnresolvedExpression> selectItemsByAlias = new HashMap<>();

  /**
   * Aggregate function calls that spreads in SELECT, HAVING clause. Since this is going to be
   * pushed to aggregation operator, de-duplicate is necessary to avoid duplication.
   */
  private final Set<UnresolvedExpression> aggregators = new HashSet<>();

  /**
   * Items in GROUP BY clause that may be simple field name or nested in scalar function call.
   */
  private final List<UnresolvedExpression> groupByItems = new ArrayList<>();

  /**
   * Collect all query information in the parse tree excluding info in sub-query).
   * @param query   query spec node in parse tree
   */
  public void collect(QuerySpecificationContext query, String queryString) {
    query.accept(new QuerySpecificationCollector(queryString));
  }

  /*
   * Query specification collector that visits a parse tree to collect query info.
   * Most visit methods only collect info and returns nothing. However, one exception is
   * visitQuerySpec() which needs to change visit ordering to avoid visiting sub-query.
   */
  private class QuerySpecificationCollector extends OpenDistroSQLParserBaseVisitor<Void> {
    private final AstExpressionBuilder expressionBuilder = new AstExpressionBuilder();

    private final String queryString;

    public QuerySpecificationCollector(String queryString) {
      this.queryString = queryString;
    }

    @Override
    public Void visitQuerySpecification(QuerySpecificationContext ctx) {
      // TODO: avoid collect sub-query
      return super.visitQuerySpecification(ctx);
    }

    @Override
    public Void visitSelectElement(SelectElementContext ctx) {
      UnresolvedExpression expr = visitAstExpression(ctx.expression());
      selectItems.add(expr);

      if (ctx.alias() != null) {
        String alias = StringUtils.unquoteIdentifier(ctx.alias().getText());
        selectItemsByAlias.put(alias, expr);
      }
      return super.visitSelectElement(ctx);
    }

    @Override
    public Void visitGroupByElement(GroupByElementContext ctx) {
      groupByItems.add(visitAstExpression(ctx));
      return super.visitGroupByElement(ctx);
    }

    @Override
    public Void visitAggregateFunctionCall(AggregateFunctionCallContext ctx) {
      aggregators.add(AstDSL.alias(getTextInQuery(ctx, queryString), visitAstExpression(ctx)));
      return super.visitAggregateFunctionCall(ctx);
    }

    private UnresolvedExpression visitAstExpression(ParseTree tree) {
      return expressionBuilder.visit(tree);
    }
  }

}
