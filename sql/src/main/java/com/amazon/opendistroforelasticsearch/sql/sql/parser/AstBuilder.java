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

import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.FromClauseContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.SelectClauseContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.SelectElementContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.WhereClauseContext;
import static java.util.Collections.emptyList;

import com.amazon.opendistroforelasticsearch.sql.ast.expression.Alias;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.AllFields;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Filter;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Project;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Relation;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.UnresolvedPlan;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Values;
import com.amazon.opendistroforelasticsearch.sql.common.antlr.SyntaxCheckException;
import com.amazon.opendistroforelasticsearch.sql.common.utils.StringUtils;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.QuerySpecificationContext;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParserBaseVisitor;
import com.amazon.opendistroforelasticsearch.sql.sql.parser.context.ParsingContext;
import com.google.common.collect.ImmutableList;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Abstract syntax tree (AST) builder.
 */
@RequiredArgsConstructor
public class AstBuilder extends OpenDistroSQLParserBaseVisitor<UnresolvedPlan> {

  private final AstExpressionBuilder expressionBuilder = new AstExpressionBuilder();

  /**
   * Parsing context stack that contains context for current query parsing.
   */
  private final ParsingContext context = new ParsingContext();

  /**
   * SQL query to get original token text. This is necessary because token.getText() returns
   * text without whitespaces or other characters discarded by lexer.
   */
  private final String query;

  @Override
  public UnresolvedPlan visitQuerySpecification(QuerySpecificationContext query) {
    context.push();
    context.peek().collect(query);

    Project project = (Project) visit(query.selectClause());

    if (query.fromClause() == null) {
      Optional<UnresolvedExpression> allFields =
          project.getProjectList().stream().filter(node -> node instanceof AllFields)
              .findFirst();
      if (allFields.isPresent()) {
        throw new SyntaxCheckException("No FROM clause found for select all");
      }
      // Attach an Values operator with only a empty row inside so that
      // Project operator can have a chance to evaluate its expression
      // though the evaluation doesn't have any dependency on what's in Values.
      Values emptyValue = new Values(ImmutableList.of(emptyList()));
      return project.attach(emptyValue);
    }

    UnresolvedPlan result = project.attach(visit(query.fromClause()));
    context.pop();
    return result;
  }

  @Override
  public UnresolvedPlan visitSelectClause(SelectClauseContext ctx) {
    ImmutableList.Builder<UnresolvedExpression> builder =
        new ImmutableList.Builder<>();
    if (ctx.selectElements().star != null) { //TODO: project operator should be required?
      builder.add(AllFields.of());
    }
    ctx.selectElements().selectElement().forEach(field -> builder.add(visitSelectItem(field)));
    return new Project(builder.build());
  }

  @Override
  public UnresolvedPlan visitFromClause(FromClauseContext ctx) {
    UnresolvedExpression tableName = visitAstExpression(ctx.tableName());
    String tableAlias = (ctx.alias() == null) ? null
        : StringUtils.unquoteIdentifier(ctx.alias().getText());

    UnresolvedPlan result = new Relation(tableName, tableAlias);
    if (ctx.whereClause() != null) {
      result = visit(ctx.whereClause()).attach(result);
    }

    // Because aggregation maybe implicit, this has to be handled here instead of visitGroupByClause
    AstAggregationBuilder aggBuilder = new AstAggregationBuilder(context.peek());
    UnresolvedPlan aggregation = aggBuilder.visit(ctx.groupByClause());
    if (aggregation != null) {
      result = aggregation.attach(result);
    }

    return result;
  }

  @Override
  public UnresolvedPlan visitWhereClause(WhereClauseContext ctx) {
    return new Filter(visitAstExpression(ctx.expression()));
  }

  @Override
  protected UnresolvedPlan aggregateResult(UnresolvedPlan aggregate, UnresolvedPlan nextResult) {
    return nextResult != null ? nextResult : aggregate;
  }

  private UnresolvedExpression visitAstExpression(ParseTree tree) {
    return expressionBuilder.visit(tree);
  }

  private UnresolvedExpression visitSelectItem(SelectElementContext ctx) {
    String name = StringUtils.unquoteIdentifier(getTextInQuery(ctx.expression()));
    UnresolvedExpression expr = visitAstExpression(ctx.expression());

    if (ctx.alias() == null) {
      return new Alias(name, expr);
    } else {
      String alias = StringUtils.unquoteIdentifier(ctx.alias().getText());
      return new Alias(name, expr, alias);
    }
  }

  /**
   * Get original text in query.
   */
  private String getTextInQuery(ParserRuleContext ctx) {
    Token start = ctx.getStart();
    Token stop = ctx.getStop();
    return query.substring(start.getStartIndex(), stop.getStopIndex() + 1);
  }

}
