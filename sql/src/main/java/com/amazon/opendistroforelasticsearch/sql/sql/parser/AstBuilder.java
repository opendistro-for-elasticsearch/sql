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

import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Filter;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Project;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Relation;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.UnresolvedPlan;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParserBaseVisitor;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.stream.Collectors;

import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.FromClauseContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.SelectElementsContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.SelectStatementContext;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.WhereClauseContext;

/**
 * Abstract syntax tree builder
 */
public class AstBuilder extends OpenDistroSQLParserBaseVisitor<UnresolvedPlan> {

    private final AstExpressionBuilder expressionBuilder = new AstExpressionBuilder();

    @Override
    public UnresolvedPlan visitSelectStatement(SelectStatementContext ctx) {
        return visit(ctx.selectElements()).attach(
                    visit(ctx.whereClause()).attach(
                        visit(ctx.fromClause())));
    }

    @Override
    public UnresolvedPlan visitSelectElements(SelectElementsContext ctx) {
        return new Project(ctx.children.stream().
                                        map(this::visitAstExpression).
                                        collect(Collectors.toList()));
    }

    @Override
    public UnresolvedPlan visitFromClause(FromClauseContext ctx) {
        return new Relation(visitAstExpression(ctx.tableName().uid()));
    }

    @Override
    public UnresolvedPlan visitWhereClause(WhereClauseContext ctx) {
        return new Filter(visitAstExpression(ctx.whereExpr));
    }

    @Override
    protected UnresolvedPlan aggregateResult(UnresolvedPlan aggregate, UnresolvedPlan nextResult) {
        return nextResult != null ? nextResult : aggregate;
    }

    private UnresolvedExpression visitAstExpression(ParseTree tree) {
        return expressionBuilder.visit(tree);
    }

}
