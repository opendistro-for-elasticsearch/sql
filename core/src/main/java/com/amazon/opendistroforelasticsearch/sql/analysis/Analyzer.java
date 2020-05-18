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

package com.amazon.opendistroforelasticsearch.sql.analysis;

import com.amazon.opendistroforelasticsearch.sql.ast.AbstractNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Field;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Aggregation;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Filter;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Relation;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Rename;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.UnresolvedPlan;
import com.amazon.opendistroforelasticsearch.sql.exception.SemanticCheckException;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.Aggregator;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalAggregation;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalFilter;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalRelation;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalRename;
import com.amazon.opendistroforelasticsearch.sql.storage.StorageEngine;
import com.amazon.opendistroforelasticsearch.sql.storage.Table;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;

/**
 * Analyze the {@link UnresolvedPlan} in the {@link AnalysisContext} to construct the {@link LogicalPlan}
 */
@RequiredArgsConstructor
public class Analyzer extends AbstractNodeVisitor<LogicalPlan, AnalysisContext> {
    private final ExpressionAnalyzer expressionAnalyzer;
    private final StorageEngine storageEngine;

    public LogicalPlan analyze(UnresolvedPlan unresolved, AnalysisContext context) {
        return unresolved.accept(this, context);
    }

    @Override
    public LogicalPlan visitRelation(Relation node, AnalysisContext context) {
        context.push();
        TypeEnvironment curEnv = context.peek();
        Table table = storageEngine.getTable(node.getTableName());
        table.getFieldTypes().forEach((k, v) -> curEnv.define(DSL.ref(k), v));
        return new LogicalRelation(node.getTableName());
    }

    @Override
    public LogicalPlan visitFilter(Filter node, AnalysisContext context) {
        LogicalPlan child = node.getChild().get(0).accept(this, context);
        Expression condition = expressionAnalyzer.analyze(node.getCondition(), context);
        return new LogicalFilter(child, condition);
    }

    /**
     * Build {@link LogicalRename}
     */
    @Override
    public LogicalPlan visitRename(Rename node, AnalysisContext context) {
        LogicalPlan child = node.getChild().get(0).accept(this, context);
        ImmutableMap.Builder<ReferenceExpression, ReferenceExpression> renameMapBuilder = new ImmutableMap.Builder<>();
        for (com.amazon.opendistroforelasticsearch.sql.ast.expression.Map renameMap : node.getRenameList()) {
            Expression origin = expressionAnalyzer.analyze(renameMap.getOrigin(), context);
            // We should define the new target field in the context instead of analyze it.
            if (renameMap.getTarget() instanceof Field) {
                ReferenceExpression target =
                        new ReferenceExpression(((Field) renameMap.getTarget()).getField().toString());
                context.peek().define(target, origin.type(context.peek()));
                renameMapBuilder.put(DSL.ref(origin.toString()), target);
            } else {
                throw new SemanticCheckException(String.format("the target expected to be field, but is %s",
                        renameMap.getTarget()));
            }
        }

        return new LogicalRename(child, renameMapBuilder.build());
    }

    /**
     * Build {@link LogicalAggregation}
     */
    @Override
    public LogicalPlan visitAggregation(Aggregation node, AnalysisContext context) {
        LogicalPlan child = node.getChild().get(0).accept(this, context);
        ImmutableList.Builder<Aggregator> aggregatorBuilder = new ImmutableList.Builder<>();
        for (UnresolvedExpression uExpr : node.getAggExprList()) {
            aggregatorBuilder.add((Aggregator) expressionAnalyzer.analyze(uExpr, context));
        }

        ImmutableList.Builder<Expression> groupbyBuilder = new ImmutableList.Builder<>();
        for (UnresolvedExpression uExpr : node.getGroupExprList()) {
            groupbyBuilder.add(expressionAnalyzer.analyze(uExpr, context));
        }
        return new LogicalAggregation(child, aggregatorBuilder.build(), groupbyBuilder.build());
    }
}
