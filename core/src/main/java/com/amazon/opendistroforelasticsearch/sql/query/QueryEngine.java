/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.query;

import com.amazon.opendistroforelasticsearch.sql.analysis.AnalysisContext;
import com.amazon.opendistroforelasticsearch.sql.analysis.Analyzer;
import com.amazon.opendistroforelasticsearch.sql.analysis.ExpressionAnalyzer;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.UnresolvedPlan;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.planner.Planner;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.storage.StorageEngine;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;

@RequiredArgsConstructor
public class QueryEngine {

    private final Parser parser;
    private final StorageEngine storageEngine;

    public PhysicalPlan plan(String query) {
        // Parsing: Query -> AST
        UnresolvedPlan ast = parser.parse(query);

        // Analyzing: AST -> Logical plan
        Analyzer analyzer = new Analyzer(new ExpressionAnalyzer(
                                            new DSL(
                                                new BuiltinFunctionRepository(new HashMap<>()))),
                                         storageEngine);
        LogicalPlan logicalPlan = analyzer.analyze(ast, new AnalysisContext());

        // Planning: Logical -> Physical plan
        Planner planner = new Planner(storageEngine);
        return planner.plan(logicalPlan);
    }

}
