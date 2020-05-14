/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import com.amazon.opendistroforelasticsearch.sql.ast.expression.Map;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Aggregation;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Filter;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Project;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Relation;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Rename;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.UnresolvedPlan;
import com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser;
import com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParserBaseVisitor;
import com.amazon.opendistroforelasticsearch.sql.ppl.utils.ArgumentFactory;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.DedupCommandContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.EvalCommandContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.FieldsCommandContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.FromClauseContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.PplStatementContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.RenameCommandContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.SearchFilterFromContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.SearchFromContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.SearchFromFilterContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.SortCommandContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.StatsCommandContext;
import static com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParser.WhereCommandContext;

/**
 * Class of building the AST
 * Refines the visit path and build the AST nodes
 */
@RequiredArgsConstructor
public class AstBuilder extends OpenDistroPPLParserBaseVisitor<UnresolvedPlan> {
    private final AstExpressionBuilder expressionBuilder;

    @Override
    public UnresolvedPlan visitPplStatement(PplStatementContext ctx) {
        UnresolvedPlan search = visit(ctx.searchCommand());
        return ctx.commands()
                .stream()
                .map(this::visit)
                .reduce(search, (r, e) -> e.attach(r));
    }

    /** Search command */
    @Override
    public UnresolvedPlan visitSearchFrom(SearchFromContext ctx) {
        return visitFromClause(ctx.fromClause());
    }

    @Override
    public UnresolvedPlan visitSearchFromFilter(SearchFromFilterContext ctx) {
        return new Filter(visitExpression(ctx.logicalExpression())).attach(visit(ctx.fromClause()));
    }

    @Override
    public UnresolvedPlan visitSearchFilterFrom(SearchFilterFromContext ctx) {
        return new Filter(visitExpression(ctx.logicalExpression())).attach(visit(ctx.fromClause()));
    }

    /** Where command */
    @Override
    public UnresolvedPlan visitWhereCommand(WhereCommandContext ctx) {
        return new Filter(visitExpression(ctx.logicalExpression()));
    }

    /** Fields command */
    @Override
    public UnresolvedPlan visitFieldsCommand(FieldsCommandContext ctx) {
        return new Project(
                ctx.wcFieldList()
                        .wcFieldExpression()
                        .stream()
                        .map(this::visitExpression)
                        .collect(Collectors.toList()),
                ArgumentFactory.getArgumentList(ctx)
        );
    }

    /** Rename command */
    @Override
    public UnresolvedPlan visitRenameCommand(RenameCommandContext ctx) {
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

    /**
     * Stats command
     */
    @Override
    public UnresolvedPlan visitStatsCommand(StatsCommandContext ctx) {
        ImmutableList.Builder<UnresolvedExpression> aggListBuilder = new ImmutableList.Builder<>();
        ImmutableList.Builder<Map> renameListBuilder = new ImmutableList.Builder<>();
        for (OpenDistroPPLParser.StatsAggTermContext aggCtx : ctx.statsAggTerm()) {
            UnresolvedExpression aggExpression = visitExpression(aggCtx.statsFunction());
            aggListBuilder.add(aggExpression);
            if (aggCtx.alias != null) {
                renameListBuilder
                        .add(new Map(aggExpression, visitExpression(aggCtx.alias)));
            }
        }
        List<UnresolvedExpression> groupList = ctx.byClause() == null ? null :
                ctx.byClause()
                        .fieldList()
                        .fieldExpression()
                        .stream()
                        .map(this::visitExpression)
                        .collect(Collectors.toList());
        Aggregation aggregation = new Aggregation(
                aggListBuilder.build(),
                null,
                groupList,
                ArgumentFactory.getArgumentList(ctx)
        );
        List<Map> renameList = renameListBuilder.build();
        return renameList.isEmpty() ? aggregation : new Rename(renameList, aggregation);
    }

    /** Dedup command */
    @Override
    public UnresolvedPlan visitDedupCommand(DedupCommandContext ctx) {
        List<UnresolvedExpression> sortList = ctx.sortbyClause() == null ? null :
                ctx.sortbyClause()
                        .sortField()
                        .stream()
                        .map(this::visitExpression)
                        .collect(Collectors.toList());
        return new Aggregation(
                ctx.fieldList()
                        .fieldExpression()
                        .stream()
                        .map(this::visitExpression)
                        .collect(Collectors.toList()),
                sortList,
                null,
                ArgumentFactory.getArgumentList(ctx)
        );
    }

    /** Sort command */
    @Override
    public UnresolvedPlan visitSortCommand(SortCommandContext ctx) {
        return new Aggregation(
                null,
                ctx.sortbyClause()
                        .sortField()
                        .stream()
                        .map(this::visitExpression)
                        .collect(Collectors.toList()),
                null,
                ArgumentFactory.getArgumentList(ctx)
        );
    }

    /** Eval command */
    @Override
    public UnresolvedPlan visitEvalCommand(EvalCommandContext ctx) {
        return new Project(
                ctx.evalExpression()
                        .stream()
                        .map(this::visitExpression)
                        .collect(Collectors.toList())
        );
    }

    /** From clause */
    @Override
    public UnresolvedPlan visitFromClause(FromClauseContext ctx) {
        return new Relation(visitExpression(ctx.tableSource().qualifiedName()));
    }

    /** Navigate to & build AST expression */
    private UnresolvedExpression visitExpression(ParseTree tree) {
        return expressionBuilder.visit(tree);
    }

    /**
     * Simply return non-default value for now
     */
    @Override
    protected UnresolvedPlan aggregateResult(UnresolvedPlan aggregate, UnresolvedPlan nextResult) {
        if (nextResult != defaultResult()) {
            return nextResult;
        }
        return aggregate;
    }

}
