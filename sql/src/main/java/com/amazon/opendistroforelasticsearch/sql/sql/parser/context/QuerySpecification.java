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
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.OrderByElementContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.SelectElementContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.SubqueryAsRelationContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.parser.ParserUtils.getTextInQuery;

import com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.DataType;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Literal;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.QualifiedName;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.common.utils.StringUtils;
import com.amazon.opendistroforelasticsearch.sql.exception.SemanticCheckException;
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
   * Items in GROUP BY clause that may be:
   *  1) Simple field name
   *  2) Field nested in scalar function call
   *  3) Ordinal that points to expression in SELECT
   *  4) Alias that points to expression in SELECT.
   */
  private final List<UnresolvedExpression> groupByItems = new ArrayList<>();

  /**
   * Items in ORDER BY clause that may be different forms as above and its options.
   */
  private final List<UnresolvedExpression> orderByItems = new ArrayList<>();
  private final List<String> orderByOptions = new ArrayList<>();

  /**
   * Collect all query information in the parse tree excluding info in sub-query).
   * @param query   query spec node in parse tree
   */
  public void collect(QuerySpecificationContext query, String queryString) {
    query.accept(new QuerySpecificationCollector(queryString));
  }

  /**
   * Replace unresolved expression if it's an alias or ordinal that represents
   * an actual expression in SELECT list.
   * @param expr item to be replaced
   * @return select item that the given expr represents
   */
  public UnresolvedExpression replaceIfAliasOrOrdinal(UnresolvedExpression expr) {
    if (isIntegerLiteral(expr)) {
      return getSelectItemByOrdinal(expr);
    } else if (isSelectAlias(expr)) {
      return getSelectItemByAlias(expr);
    } else {
      return expr;
    }
  }

  private boolean isIntegerLiteral(UnresolvedExpression expr) {
    if (!(expr instanceof Literal)) {
      return false;
    }

    if (((Literal) expr).getType() != DataType.INTEGER) {
      throw new SemanticCheckException(StringUtils.format(
          "Non-integer constant [%s] found in ordinal", expr));
    }
    return true;
  }

  private UnresolvedExpression getSelectItemByOrdinal(UnresolvedExpression expr) {
    int ordinal = (Integer) ((Literal) expr).getValue();
    if (ordinal <= 0 || ordinal > selectItems.size()) {
      throw new SemanticCheckException(StringUtils.format(
          "Ordinal [%d] is out of bound of select item list", ordinal));
    }
    return selectItems.get(ordinal - 1);
  }

  /**
   * Check if an expression is a select alias.
   * @param expr  expression
   * @return true if it's an alias
   */
  public boolean isSelectAlias(UnresolvedExpression expr) {
    return (expr instanceof QualifiedName)
        && (selectItemsByAlias.containsKey(expr.toString()));
  }

  /**
   * Get original expression aliased in SELECT clause.
   * @param expr  alias
   * @return expression in SELECT
   */
  public UnresolvedExpression getSelectItemByAlias(UnresolvedExpression expr) {
    return selectItemsByAlias.get(expr.toString());
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
    public Void visitSubqueryAsRelation(SubqueryAsRelationContext ctx) {
      // skip collecting subquery for current layer
      return null;
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
    public Void visitOrderByElement(OrderByElementContext ctx) {
      orderByItems.add(visitAstExpression(ctx.expression()));
      orderByOptions.add((ctx.order == null) ? "ASC" : ctx.order.getText());
      return super.visitOrderByElement(ctx);
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
