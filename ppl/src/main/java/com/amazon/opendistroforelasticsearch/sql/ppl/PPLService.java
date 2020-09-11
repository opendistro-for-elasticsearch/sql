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

package com.amazon.opendistroforelasticsearch.sql.ppl;

import static com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine.QueryResponse;

import com.amazon.opendistroforelasticsearch.sql.analysis.AnalysisContext;
import com.amazon.opendistroforelasticsearch.sql.analysis.Analyzer;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.UnresolvedPlan;
import com.amazon.opendistroforelasticsearch.sql.common.response.ResponseListener;
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine;
import com.amazon.opendistroforelasticsearch.sql.planner.Planner;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.ppl.antlr.PPLSyntaxParser;
import com.amazon.opendistroforelasticsearch.sql.ppl.domain.PPLQueryRequest;
import com.amazon.opendistroforelasticsearch.sql.ppl.parser.AstBuilder;
import com.amazon.opendistroforelasticsearch.sql.ppl.parser.AstExpressionBuilder;
import com.amazon.opendistroforelasticsearch.sql.ppl.utils.UnresolvedPlanHelper;
import com.amazon.opendistroforelasticsearch.sql.storage.StorageEngine;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.tree.ParseTree;

@RequiredArgsConstructor
public class PPLService {
  private final PPLSyntaxParser parser;

  private final Analyzer analyzer;

  private final StorageEngine storageEngine;

  private final ExecutionEngine executionEngine;

  /**
   * Execute the {@link PPLQueryRequest}, using {@link ResponseListener} to get response.
   * @param request {@link PPLQueryRequest}
   * @param listener {@link ResponseListener}
   */
  public void execute(PPLQueryRequest request, ResponseListener<QueryResponse> listener) {
    try {
      // 1.Parse query and convert parse tree (CST) to abstract syntax tree (AST)
      ParseTree cst = parser.analyzeSyntax(request.getRequest());
      UnresolvedPlan ast =
          cst.accept(new AstBuilder(new AstExpressionBuilder(), request.getRequest()));

      // 2.Analyze abstract syntax to generate logical plan
      LogicalPlan logicalPlan = analyzer.analyze(UnresolvedPlanHelper.addSelectAll(ast),
          new AnalysisContext());

      // 3.Generate optimal physical plan from logical plan
      PhysicalPlan physicalPlan = new Planner(storageEngine).plan(logicalPlan);

      // 4.Execute physical plan and send response
      executionEngine.execute(physicalPlan, listener);
    } catch (Exception e) {
      listener.onFailure(e);
    }
  }
}
