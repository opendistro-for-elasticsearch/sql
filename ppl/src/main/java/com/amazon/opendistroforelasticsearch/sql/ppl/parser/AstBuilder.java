/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.ppl.parser;

import com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser;
import com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParserBaseVisitor;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Map;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.logical.Aggregation;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.logical.Filter;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.logical.Project;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.logical.Relation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.tree.ParseTree;

@RequiredArgsConstructor
public class AstBuilder extends OpenDistroPPLParserBaseVisitor<LogicalPlan> {
    private final AstExpressionBuilder expressionBuilder;

    @Override
    public LogicalPlan visitPplStatement(OpenDistroPPLParser.PplStatementContext ctx) {
        LogicalPlan search = visit(ctx.searchCommand());
        LogicalPlan reduce = ctx.commands().stream().map(this::visit).reduce(search, (r, e) -> e.withInput(r));
        return reduce;
    }

    /** Search command */
    @Override
    public LogicalPlan visitSearchFrom(OpenDistroPPLParser.SearchFromContext ctx) {
        return visitFromClause(ctx.fromClause());
    }

    @Override
    public LogicalPlan visitSearchFromFilter(OpenDistroPPLParser.SearchFromFilterContext ctx) {
        return new Filter(visitExpression(ctx.logicalExpression())).withInput(visit(ctx.fromClause()));
    }

    @Override
    public LogicalPlan visitSearchFilterFrom(OpenDistroPPLParser.SearchFilterFromContext ctx) {
        return new Filter(visitExpression(ctx.logicalExpression())).withInput(visit(ctx.fromClause()));
    }

    /** Where command */
    @Override
    public LogicalPlan visitWhereCommand(OpenDistroPPLParser.WhereCommandContext ctx) {
        return new Filter(visitExpression(ctx.logicalExpression()));
    }

    /** Fields command */
    @Override
    public LogicalPlan visitFieldsCommand(OpenDistroPPLParser.FieldsCommandContext ctx) {
        return new Project(
                ctx.wcFieldList()
                        .wcFieldExpression()
                        .stream()
                        .map(this::visitExpression)
                        .collect(Collectors.toList())
        );
    }

    /** Rename command */
    @Override
    public LogicalPlan visitRenameCommand(OpenDistroPPLParser.RenameCommandContext ctx) {
        return new Project(
                new ArrayList<>(
                        Collections.singletonList(
                                new Map(
                                        visitExpression(ctx.orignalField),
                                        visitExpression(ctx.renamedField)
                                )
                        )
                )
        );
    }

    /** Stats command */
    @Override
    public LogicalPlan visitStatsCommand(OpenDistroPPLParser.StatsCommandContext ctx) {
        return new Aggregation(
                new ArrayList<>(
                        Collections.singletonList(
                                visitExpression(ctx.statsAggTerm())
                        )
                ),
                null,
                ctx.byClause()
                        .fieldList()
                        .fieldExpression()
                        .stream()
                        .map(this::visitExpression)
                        .collect(Collectors.toList())
        );
    }

    /** Dedup command */
    @Override
    public LogicalPlan visitDedupCommand(OpenDistroPPLParser.DedupCommandContext ctx) {
        return new Aggregation(
                ctx.fieldList()
                        .fieldExpression()
                        .stream()
                        .map(this::visitExpression)
                        .collect(Collectors.toList()),
                ctx.sortbyClause()
                        .sortField()
                        .stream()
                        .map(this::visitExpression)
                        .collect(Collectors.toList()),
                null
        );
    }

    /** Sort command */
    @Override
    public LogicalPlan visitSortCommand(OpenDistroPPLParser.SortCommandContext ctx) {
        return new Aggregation(
                null,
                ctx.sortbyClause()
                        .sortField()
                        .stream()
                        .map(this::visitExpression)
                        .collect(Collectors.toList()),
                null
        );
    }

    /** Eval command */
    @Override
    public LogicalPlan visitEvalCommand(OpenDistroPPLParser.EvalCommandContext ctx) {
        return new Project(
                ctx.evalExpression()
                        .stream()
                        .map(this::visitExpression)
                        .collect(Collectors.toList())
        );
    }

    /** From clause */
    @Override
    public LogicalPlan visitFromClause(OpenDistroPPLParser.FromClauseContext ctx) {
        return new Relation(ctx.tableSource().getText());
    }

    /** Navigate to & build AST expression */
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
