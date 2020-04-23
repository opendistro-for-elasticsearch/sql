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

import com.amazon.opendistroforelasticsearch.sql.common.utils.StringUtils;
import com.amazon.opendistroforelasticsearch.sql.ppl.antlr.parser.OpenDistroPPLParserBaseVisitor;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.expression.Map;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.logical.Aggregation;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.logical.Filter;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.logical.UnresolvedPlan;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.logical.Project;
import com.amazon.opendistroforelasticsearch.sql.ppl.plans.logical.Relation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.tree.ParseTree;

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
        UnresolvedPlan reduce = ctx.commands().stream().map(this::visit).reduce(search, (r, e) -> e.attach(r));
        return reduce;
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
                        .collect(Collectors.toList())
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

    /** Stats command */
    @Override
    public UnresolvedPlan visitStatsCommand(StatsCommandContext ctx) {
        List<Expression> groupList = ctx.byClause() == null ? null :
                ctx.byClause()
                        .fieldList()
                        .fieldExpression()
                        .stream()
                        .map(this::visitExpression)
                        .collect(Collectors.toList());
        return new Aggregation(
                new ArrayList<>(Collections.singletonList(visitExpression(ctx.statsAggTerm()))),
                null,
                groupList
        );
    }

    /** Dedup command */
    @Override
    public UnresolvedPlan visitDedupCommand(DedupCommandContext ctx) {
        List<Expression> sortList = ctx.sortbyClause() == null ? null :
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
                null
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
                null
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
        return new Relation(StringUtils.unquoteIdentifier(ctx.tableSource().getText()));
    }

    /** Navigate to & build AST expression */
    private Expression visitExpression(ParseTree tree) {
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
