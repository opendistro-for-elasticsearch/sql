/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.ppl.parser;

import com.amazon.opendistroforelasticsearch.ppl.plans.expression.Count;
import com.amazon.opendistroforelasticsearch.ppl.plans.expression.Literal;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Aggregation;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Expression;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Filter;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Project;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Relation;
import com.amazon.opendistroforelasticsearch.ppl.plans.logical.Top;
import com.amazon.opendistroforelasticsearch.sql.antlr.parser.PPLParser;
import com.amazon.opendistroforelasticsearch.sql.antlr.parser.PPLParserBaseVisitor;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class AstBuilder extends PPLParserBaseVisitor<LogicalPlan> {
    private final AstExpressionBuilder expressionBuilder;

    @Override
    public LogicalPlan visitPplStatement(PPLParser.PplStatementContext ctx) {
        LogicalPlan search = visit(ctx.searchCommands());
        LogicalPlan reduce = ctx.commands().stream().map(this::visit).reduce(search, (r, e) -> e.withInput(r));
        return reduce;
    }

    @Override
    public LogicalPlan visitFieldsCommand(PPLParser.FieldsCommandContext ctx) {
        return new Project(
                ctx.fieldList()
                        .fieldExpression()
                        .stream()
                        .map(this::visitExpression)
                        .collect(Collectors.toList()));
    }

    @Override
    public LogicalPlan visitFieldsCommand(PPLParser.FieldsCommandContext ctx) {
        return visitFieldList(ctx.fieldList());
    }

    @Override
    public LogicalPlan visitSearchWithoutFilter(PPLParser.SearchWithoutFilterContext ctx) {
        return visitFromClause(ctx.fromClause());
    }

    @Override
    public LogicalPlan visitSearchFromClauseLogicExpr(PPLParser.SearchFromClauseLogicExprContext ctx) {
        return new Filter(visitExpression(ctx.logicalExpression())).withInput(visit(ctx.fromClause()));
    }

    @Override
    public LogicalPlan visitSearchLogicExprFromClause(PPLParser.SearchLogicExprFromClauseContext ctx) {
        return new Filter(visitExpression(ctx.logicalExpression())).withInput(visit(ctx.fromClause()));
    }

    @Override
    public LogicalPlan visitFromClause(PPLParser.FromClauseContext ctx) {
        return new Relation(ctx.tableSource().getText());
    }

    @Override
    public LogicalPlan visitTopCommand(PPLParser.TopCommandContext ctx) {
        return new Top(visitFieldList(ctx.fieldList()))
                .byClause(visitExpression(ctx.byClause().fieldList()))
                .count((Literal) visitExpression(ctx.count));
    }

    @Override
    public LogicalPlan visitStatsCommand(PPLParser.StatsCommandContext ctx) {
        List<Expression> aggList = ctx.statsAggTerm().stream().map(this::visitExpression).collect(Collectors.toList());
        List<Expression> groupList = ctx.byClause().stream().map(this::visitExpression).collect(Collectors.toList());
        return new Aggregation(aggList, groupList);
    }

    /* ---------------------------------- */

    private Expression visitExpression(ParseTree tree) {
        return expressionBuilder.visit(tree);
    }

    /**
     * Simply return non-default value for now
     */
    @Override
    protected LogicalPlan aggregateResult(LogicalPlan aggregate, LogicalPlan nextResult) {
        if (nextResult != defaultResult()) {
            return nextResult;
        }
        return aggregate;
    }
}
