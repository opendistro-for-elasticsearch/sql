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

package com.amazon.opendistroforelasticsearch.sql.sql;

import com.amazon.opendistroforelasticsearch.sql.analysis.AnalysisContext;
import com.amazon.opendistroforelasticsearch.sql.analysis.Analyzer;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.UnresolvedPlan;
import com.amazon.opendistroforelasticsearch.sql.common.response.ResponseListener;
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionResolver;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionSignature;
import com.amazon.opendistroforelasticsearch.sql.planner.Planner;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlanNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.SQLSyntaxParser;
import com.amazon.opendistroforelasticsearch.sql.sql.functions.Substring;
import com.amazon.opendistroforelasticsearch.sql.sql.parser.AstBuilder;
import com.amazon.opendistroforelasticsearch.sql.storage.StorageEngine;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine.QueryResponse;

/**
 * SQL service
 */
public class SQLService {

    private final SQLSyntaxParser parser;

    private final Analyzer analyzer;

    private final StorageEngine storageEngine;

    private final ExecutionEngine executionEngine;

    private final BuiltinFunctionRepository functionRepository;


    public SQLService(SQLSyntaxParser parser, Analyzer analyzer,
                      StorageEngine storageEngine, ExecutionEngine executionEngine,
                      BuiltinFunctionRepository functionRepository) {
        this.parser = parser;
        this.analyzer = analyzer;
        this.storageEngine = storageEngine;
        this.executionEngine = executionEngine;
        this.functionRepository = functionRepository;

        registerSQLFunctions();
    }

    /**
     * Parse, analyze, plan and execute the query.
     * @param query         SQL query
     * @param listener      callback listener
     */
    public void execute(String query, ResponseListener<QueryResponse> listener) {
        try {
            PhysicalPlan physicalPlan = plan(analyze(parse(query)));

            executionEngine.execute(physicalPlan, listener);
        } catch (Exception e) {
            listener.onFailure(e);
        }
    }

    public String explain(String query) {
        LogicalPlan logicalPlan = analyze(parse(query));
        String logicalPlanExplain = logicalPlan.accept(new LogicalPlanNodeVisitor<String, Integer>() {
            @Override
            protected String visitNode(LogicalPlan node, Integer depth) {
                return Strings.repeat("\t", depth) + node + "\n"
                    + node.getChild().stream().
                                      map(c -> c.accept(this, depth + 1)).
                                      collect(Collectors.joining());
            }
        }, 0);

        PhysicalPlan physicalPlan = plan(logicalPlan);
        String physicalPlanExplain = physicalPlan.accept(new PhysicalPlanNodeVisitor<String, Integer>() {
            @Override
            protected String visitNode(PhysicalPlan node, Integer depth) {
                return Strings.repeat("\t", depth) + node + "\n"
                    + node.getChild().stream().
                                      map(c -> c.accept(this, depth + 1)).
                                      collect(Collectors.joining());
            }
        }, 0);

        return String.format("=== Logical plan === \n %s \n" +
                             "=== Physical Plan === \n %s \n",
                             logicalPlanExplain, physicalPlanExplain);
    }

    /** Parse query and convert parse tree (CST) to abstract syntax tree (AST) */
    private UnresolvedPlan parse(String query) {
        ParseTree cst = parser.parse(query);
        return cst.accept(new AstBuilder());
    }

    /** Analyze abstract syntax to generate logical plan */
    private LogicalPlan analyze(UnresolvedPlan ast) {
        return analyzer.analyze(ast, new AnalysisContext());
    }

    /** Generate optimal physical plan from logical plan */
    private PhysicalPlan plan(LogicalPlan logicalPlan) {
        return new Planner(storageEngine).plan(logicalPlan);
    }

    private void registerSQLFunctions() {
        functionRepository.register(substring());
    }

    private FunctionResolver substring() {
        return new FunctionResolver(
            Substring.FUNCTION_NAME,
            ImmutableMap.of(
                new FunctionSignature(Substring.FUNCTION_NAME, Arrays.asList(STRING, INTEGER, INTEGER)),
                Substring::new
            )
        );
    }

}
