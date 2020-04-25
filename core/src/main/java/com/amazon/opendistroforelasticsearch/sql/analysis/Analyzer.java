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
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Filter;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Relation;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.UnresolvedPlan;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalFilter;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalRelation;
import com.amazon.opendistroforelasticsearch.sql.schema.Namespace;
import com.amazon.opendistroforelasticsearch.sql.schema.Schema;
import com.amazon.opendistroforelasticsearch.sql.schema.SymbolTable;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Analyzer extends AbstractNodeVisitor<LogicalPlan, AnalysisContext> {
    private final ExpressionAnalyzer expressionAnalyzer;
    private final Schema schema;

    public LogicalPlan analyze(UnresolvedPlan unresolved, AnalysisContext context) {
        return unresolved.accept(this, context);
    }

    @Override
    public LogicalPlan visitRelation(Relation node, AnalysisContext context) {
        context.push();
        TypeEnvironment curEnv = context.peek();
        SymbolTable symbolTable = schema.resolveSymbolTable(node.getTableName());
        symbolTable.lookupAll(Namespace.FIELD_NAME).forEach((k, v) -> curEnv.define(DSL.ref(k), v));
        return new LogicalRelation(node.getTableName());
    }

    @Override
    public LogicalPlan visitFilter(Filter node, AnalysisContext context) {
        LogicalPlan child = node.getChild().get(0).accept(this, context);
        Expression condition = expressionAnalyzer.analyze(node.getCondition(), context);
        return new LogicalFilter(condition, child);
    }
}
