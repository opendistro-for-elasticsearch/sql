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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.opendistroforelasticsearch.sql.analysis.symbol.Namespace;
import com.amazon.opendistroforelasticsearch.sql.analysis.symbol.Symbol;
import com.amazon.opendistroforelasticsearch.sql.analysis.symbol.SymbolTable;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.UnresolvedPlan;
import com.amazon.opendistroforelasticsearch.sql.config.TestConfig;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.storage.StorageEngine;
import com.amazon.opendistroforelasticsearch.sql.storage.Table;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;


public class AnalyzerTestBase {

  protected Map<String, ExprType> typeMapping() {
    return TestConfig.typeMapping;
  }

  @Bean
  protected StorageEngine storageEngine() {
    return new StorageEngine() {
      @Override
      public Table getTable(String name) {
        return new Table() {
          @Override
          public Map<String, ExprType> getFieldTypes() {
            return typeMapping();
          }

          @Override
          public PhysicalPlan implement(LogicalPlan plan) {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }


  @Bean
  protected SymbolTable symbolTable() {
    SymbolTable symbolTable = new SymbolTable();
    typeMapping().entrySet()
        .forEach(
            entry -> symbolTable
                .store(new Symbol(Namespace.FIELD_NAME, entry.getKey()), entry.getValue()));
    return symbolTable;
  }

  @Bean
  protected Environment<Expression, ExprType> typeEnv() {
    return var -> {
      if (var instanceof ReferenceExpression) {
        ReferenceExpression refExpr = (ReferenceExpression) var;
        if (typeMapping().containsKey(refExpr.getAttr())) {
          return typeMapping().get(refExpr.getAttr());
        }
      }
      throw new ExpressionEvaluationException("type resolved failed");
    };
  }

  @Autowired
  protected DSL dsl;

  @Autowired
  protected AnalysisContext analysisContext;

  @Autowired
  protected ExpressionAnalyzer expressionAnalyzer;

  @Autowired
  protected Analyzer analyzer;

  @Autowired
  protected Environment<Expression, ExprType> typeEnv;

  @Bean
  protected Analyzer analyzer(ExpressionAnalyzer expressionAnalyzer, StorageEngine engine) {
    return new Analyzer(expressionAnalyzer, engine);
  }

  @Bean
  protected TypeEnvironment typeEnvironment(SymbolTable symbolTable) {
    return new TypeEnvironment(null, symbolTable);
  }

  @Bean
  protected AnalysisContext analysisContext(TypeEnvironment typeEnvironment) {
    return new AnalysisContext(typeEnvironment);
  }

  @Bean
  protected ExpressionAnalyzer expressionAnalyzer(DSL dsl, BuiltinFunctionRepository repo) {
    return new ExpressionAnalyzer(repo);
  }

  protected void assertAnalyzeEqual(LogicalPlan expected, UnresolvedPlan unresolvedPlan) {
    assertEquals(expected, analyze(unresolvedPlan));
  }

  protected LogicalPlan analyze(UnresolvedPlan unresolvedPlan) {
    return analyzer.analyze(unresolvedPlan, analysisContext);
  }
}
