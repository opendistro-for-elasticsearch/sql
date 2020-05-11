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

import com.amazon.opendistroforelasticsearch.sql.config.TestConfig;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.config.ExpressionConfig;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import com.amazon.opendistroforelasticsearch.sql.analysis.schema.Schema;
import com.amazon.opendistroforelasticsearch.sql.analysis.schema.SymbolTable;
import com.amazon.opendistroforelasticsearch.sql.storage.StorageEngine;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;


@Configuration
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ExpressionConfig.class, AnalyzerTestBase.class, TestConfig.class})
public class AnalyzerTestBase {

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
    protected ExpressionAnalyzer expressionAnalyzer(DSL dsl) {
        return new ExpressionAnalyzer(dsl);
    }
}
