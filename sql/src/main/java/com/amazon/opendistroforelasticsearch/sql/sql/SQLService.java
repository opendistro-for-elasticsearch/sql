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
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine.QueryResponse;
import com.amazon.opendistroforelasticsearch.sql.planner.Planner;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.SQLSyntaxParser;
import com.amazon.opendistroforelasticsearch.sql.sql.domain.SQLQueryRequest;
import com.amazon.opendistroforelasticsearch.sql.sql.parser.AstBuilder;
import com.amazon.opendistroforelasticsearch.sql.storage.StorageEngine;
import org.antlr.v4.runtime.tree.ParseTree;

/**
 * SQL service.
 */
public class SQLService {

  private final SQLSyntaxParser parser;

  private final Analyzer analyzer;

  private final StorageEngine storageEngine;

  private final ExecutionEngine executionEngine;

  /**
   * Initialize SQL service.
   * @param parser              SQL syntax parser
   * @param analyzer            AST analyzer
   * @param storageEngine       storage engine
   * @param executionEngine     execution engine
   */
  public SQLService(SQLSyntaxParser parser, Analyzer analyzer,
                    StorageEngine storageEngine, ExecutionEngine executionEngine) {
    this.parser = parser;
    this.analyzer = analyzer;
    this.storageEngine = storageEngine;
    this.executionEngine = executionEngine;
  }

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
   * Given AST, run the remaining steps to execute it.
   * @param ast         AST
   * @param listener    callback listener
   */
  public void execute(UnresolvedPlan ast, ResponseListener<QueryResponse> listener) {
    try {
      executionEngine.execute(
          plan(
              analyze(ast)), listener);
    } catch (Exception e) {
      listener.onFailure(e);
    }
  }

  /**
   * Parse query and convert parse tree (CST) to abstract syntax tree (AST).
   */
  public UnresolvedPlan parse(String query) {
    ParseTree cst = parser.parse(query);
    return cst.accept(new AstBuilder());
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
    return new Planner(storageEngine).plan(logicalPlan);
  }

}
