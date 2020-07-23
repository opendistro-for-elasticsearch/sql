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

import static com.amazon.opendistroforelasticsearch.sql.common.utils.StringUtils.unquoteIdentifier;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.FromClauseContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.SelectClauseContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.SelectElementContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.SimpleSelectContext;

import com.amazon.opendistroforelasticsearch.sql.ast.expression.Alias;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Project;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Relation;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.UnresolvedPlan;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Values;
import com.amazon.opendistroforelasticsearch.sql.common.antlr.SyntaxCheckException;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.QuerySpecificationContext;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParserBaseVisitor;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * Abstract syntax tree (AST) builder.
 */
public class AstBuilder extends OpenDistroSQLParserBaseVisitor<UnresolvedPlan> {

  private static final Project SELECT_ALL = null;

  /**
   * SQL query to get original token name because token.getText() returns text
   * without whitespaces or other characters discarded by lexer.
   */
  private String query;

  public AstBuilder(String query) {
    this.query = query;
  }

  public AstBuilder() {
  }

  private final AstExpressionBuilder expressionBuilder = new AstExpressionBuilder();

  @Override
  public UnresolvedPlan visitSimpleSelect(SimpleSelectContext ctx) {
    QuerySpecificationContext query = ctx.querySpecification();
    UnresolvedPlan project = visit(query.selectClause());

    if (query.fromClause() == null) {
      if (project == SELECT_ALL) {
        throw new SyntaxCheckException("No FROM clause found for select all");
      }

      // Attach an Values operator with only a empty row inside so that
      // Project operator can have a chance to evaluate its expression
      // though the evaluation doesn't have any dependency on what's in Values.
      Values emptyValue = new Values(ImmutableList.of(Collections.emptyList()));
      return project.attach(emptyValue);
    }

    UnresolvedPlan relation = visit(query.fromClause());
    return (project == SELECT_ALL) ? relation : project.attach(relation);
  }

  @Override
  public UnresolvedPlan visitSelectClause(SelectClauseContext ctx) {
    if (ctx.selectElements().star != null) { //TODO: project operator should be required?
      return SELECT_ALL;
    }

    List<SelectElementContext> selectElements = ctx.selectElements().selectElement();
    return new Project(selectElements.stream()
                                     .map(this::visitSelectItem)
                                     .collect(Collectors.toList()));
  }

  @Override
  public UnresolvedPlan visitFromClause(FromClauseContext ctx) {
    return new Relation(visitAstExpression(ctx.tableName().qualifiedName()));
  }

  @Override
  protected UnresolvedPlan aggregateResult(UnresolvedPlan aggregate, UnresolvedPlan nextResult) {
    return nextResult != null ? nextResult : aggregate;
  }

  private UnresolvedExpression visitAstExpression(ParseTree tree) {
    return expressionBuilder.visit(tree);
  }

  private UnresolvedExpression visitSelectItem(SelectElementContext ctx) {
    UnresolvedExpression delegate = visitAstExpression(ctx.expression());
    return new Alias(unquoteIdentifier(getAlias(ctx)), delegate);
  }

  private String getAlias(SelectElementContext ctx) {
    if (ctx.alias() == null) {
      Token start = ctx.expression().getStart();
      Token stop = ctx.expression().getStop();
      return query.substring(start.getStartIndex(), stop.getStopIndex() + 1);
    }
    return ctx.alias().getText();
  }

}
