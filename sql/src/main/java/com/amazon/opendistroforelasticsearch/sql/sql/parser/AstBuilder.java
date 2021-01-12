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

import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.qualifiedName;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.FromClauseContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.HavingClauseContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.SelectClauseContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.SelectElementContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.SubqueryAsRelationContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.TableAsRelationContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.WhereClauseContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.parser.ParserUtils.getTextInQuery;
import static com.amazon.opendistroforelasticsearch.sql.utils.SystemIndexUtils.TABLE_INFO;
import static com.amazon.opendistroforelasticsearch.sql.utils.SystemIndexUtils.mappingTable;
import static java.util.Collections.emptyList;

import com.amazon.opendistroforelasticsearch.sql.ast.expression.Alias;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.AllFields;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Function;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Filter;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Limit;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Project;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Relation;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.RelationSubquery;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.UnresolvedPlan;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Values;
import com.amazon.opendistroforelasticsearch.sql.common.antlr.SyntaxCheckException;
import com.amazon.opendistroforelasticsearch.sql.common.utils.StringUtils;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.QuerySpecificationContext;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParserBaseVisitor;
import com.amazon.opendistroforelasticsearch.sql.sql.parser.context.ParsingContext;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
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
  public UnresolvedPlan visitShowStatement(OpenDistroSQLParser.ShowStatementContext ctx) {
    final UnresolvedExpression tableFilter = visitAstExpression(ctx.tableFilter());
    return new Project(Collections.singletonList(AllFields.of()))
        .attach(new Filter(tableFilter).attach(new Relation(qualifiedName(TABLE_INFO))));
  }

  @Override
  public UnresolvedPlan visitDescribeStatement(OpenDistroSQLParser.DescribeStatementContext ctx) {
    final Function tableFilter = (Function) visitAstExpression(ctx.tableFilter());
    final String tableName = tableFilter.getFuncArgs().get(1).toString();
    final Relation table = new Relation(qualifiedName(mappingTable(tableName.toString())));
    if (ctx.columnFilter() == null) {
      return new Project(Collections.singletonList(AllFields.of())).attach(table);
    } else {
      return new Project(Collections.singletonList(AllFields.of()))
          .attach(new Filter(visitAstExpression(ctx.columnFilter())).attach(table));
    }
  }

  @Override
  public UnresolvedPlan visitQuerySpecification(QuerySpecificationContext queryContext) {
    context.push();
    context.peek().collect(queryContext, query);

    Project project = (Project) visit(queryContext.selectClause());

    if (queryContext.fromClause() == null) {
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

    // If limit (and offset) keyword exists:
    // Add Limit node, plan structure becomes:
    // Project -> Limit -> visit(fromClause)
    // Else:
    // Project -> visit(fromClause)
    UnresolvedPlan from = visit(queryContext.fromClause());
    if (queryContext.limitClause() != null) {
      from = visit(queryContext.limitClause()).attach(from);
    }
    UnresolvedPlan result = project.attach(from);
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
  public UnresolvedPlan visitLimitClause(OpenDistroSQLParser.LimitClauseContext ctx) {
    return new Limit(
        Integer.parseInt(ctx.limit.getText()),
        ctx.offset == null ? 0 : Integer.parseInt(ctx.offset.getText())
    );
  }

  @Override
  public UnresolvedPlan visitFromClause(FromClauseContext ctx) {
    UnresolvedPlan result = visit(ctx.relation());

    if (ctx.whereClause() != null) {
      result = visit(ctx.whereClause()).attach(result);
    }

    // Because aggregation maybe implicit, this has to be handled here instead of visitGroupByClause
    AstAggregationBuilder aggBuilder = new AstAggregationBuilder(context.peek());
    UnresolvedPlan aggregation = aggBuilder.visit(ctx.groupByClause());
    if (aggregation != null) {
      result = aggregation.attach(result);
    }

    if (ctx.havingClause() != null) {
      result = visit(ctx.havingClause()).attach(result);
    }

    if (ctx.orderByClause() != null) {
      AstSortBuilder sortBuilder = new AstSortBuilder(context.peek());
      result = sortBuilder.visit(ctx.orderByClause()).attach(result);
    }
    return result;
  }

  @Override
  public UnresolvedPlan visitTableAsRelation(TableAsRelationContext ctx) {
    String tableAlias = (ctx.alias() == null) ? null
        : StringUtils.unquoteIdentifier(ctx.alias().getText());
    return new Relation(visitAstExpression(ctx.tableName()), tableAlias);
  }

  @Override
  public UnresolvedPlan visitSubqueryAsRelation(SubqueryAsRelationContext ctx) {
    return new RelationSubquery(visit(ctx.subquery), ctx.alias().getText());
  }

  @Override
  public UnresolvedPlan visitWhereClause(WhereClauseContext ctx) {
    return new Filter(visitAstExpression(ctx.expression()));
  }

  @Override
  public UnresolvedPlan visitHavingClause(HavingClauseContext ctx) {
    AstHavingFilterBuilder builder = new AstHavingFilterBuilder(context.peek());
    return new Filter(builder.visit(ctx.expression()));
  }

  @Override
  protected UnresolvedPlan aggregateResult(UnresolvedPlan aggregate, UnresolvedPlan nextResult) {
    return nextResult != null ? nextResult : aggregate;
  }

  private UnresolvedExpression visitAstExpression(ParseTree tree) {
    return expressionBuilder.visit(tree);
  }

  private UnresolvedExpression visitSelectItem(SelectElementContext ctx) {
    String name = StringUtils.unquoteIdentifier(getTextInQuery(ctx.expression(), query));
    UnresolvedExpression expr = visitAstExpression(ctx.expression());

    if (ctx.alias() == null) {
      return new Alias(name, expr);
    } else {
      String alias = StringUtils.unquoteIdentifier(ctx.alias().getText());
      return new Alias(name, expr, alias);
    }
  }

}
