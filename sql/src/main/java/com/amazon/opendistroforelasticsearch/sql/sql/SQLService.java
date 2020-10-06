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
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine.ExplainResponse;
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine.QueryResponse;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.planner.Planner;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.optimizer.LogicalPlanOptimizer;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.SQLSyntaxParser;
import com.amazon.opendistroforelasticsearch.sql.sql.domain.SQLQueryRequest;
import com.amazon.opendistroforelasticsearch.sql.sql.parser.AstBuilder;
import com.amazon.opendistroforelasticsearch.sql.storage.StorageEngine;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * SQL service.
 */
@RequiredArgsConstructor
public class SQLService {

  private final SQLSyntaxParser parser;

  private final Analyzer analyzer;

  private final StorageEngine storageEngine;

  private final ExecutionEngine executionEngine;

  private final BuiltinFunctionRepository repository;

  /**
   * Parse, analyze, plan and execute the query.
   * @param request       SQL query request
   * @param listener      callback listener
   */
  public void execute(SQLQueryRequest request, ResponseListener<QueryResponse> listener) {
    try {
      executionEngine.execute(
                        plan(
                            analyze(
                                parse(request.getQuery()))), listener);
    } catch (Exception e) {
      listener.onFailure(e);
    }
  }

  /**
   * Given physical plan, execute it and listen on response.
   * @param plan        physical plan
   * @param listener    callback listener
   */
  public void execute(PhysicalPlan plan, ResponseListener<QueryResponse> listener) {
    try {
      executionEngine.execute(plan, listener);
    } catch (Exception e) {
      listener.onFailure(e);
    }
  }

  /**
   * Given physical plan, explain it.
   * @param plan        physical plan
   * @param listener    callback listener
   */
  public void explain(PhysicalPlan plan, ResponseListener<ExplainResponse> listener) {
    try {
      executionEngine.explain(plan, listener);
    } catch (Exception e) {
      listener.onFailure(e);
    }
  }

  /**
   * Parse query and convert parse tree (CST) to abstract syntax tree (AST).
   */
  public UnresolvedPlan parse(String query) {
    ParseTree cst = parser.parse(query);
    return cst.accept(new AstBuilder(query));
  }

  /**
   * Analyze abstract syntax to generate logical plan.
   */
  public LogicalPlan analyze(UnresolvedPlan ast) {
    return analyzer.analyze(ast, new AnalysisContext());
  }

  /**
   * Generate optimal physical plan from logical plan.
   */
  public PhysicalPlan plan(LogicalPlan logicalPlan) {
    return new Planner(storageEngine, LogicalPlanOptimizer.create(new DSL(repository)))
        .plan(logicalPlan);
  }

}
