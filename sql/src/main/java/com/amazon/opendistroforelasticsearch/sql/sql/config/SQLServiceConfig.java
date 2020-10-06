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

package com.amazon.opendistroforelasticsearch.sql.sql.config;

import com.amazon.opendistroforelasticsearch.sql.analysis.Analyzer;
import com.amazon.opendistroforelasticsearch.sql.analysis.ExpressionAnalyzer;
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine;
import com.amazon.opendistroforelasticsearch.sql.expression.config.ExpressionConfig;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.sql.SQLService;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.SQLSyntaxParser;
import com.amazon.opendistroforelasticsearch.sql.storage.StorageEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * SQL service configuration for Spring container initialization.
 */
@Configuration
@Import({ExpressionConfig.class})
public class SQLServiceConfig {

  @Autowired
  private StorageEngine storageEngine;

  @Autowired
  private ExecutionEngine executionEngine;

  @Autowired
  private BuiltinFunctionRepository functionRepository;

  @Bean
  public Analyzer analyzer() {
    return new Analyzer(new ExpressionAnalyzer(functionRepository), storageEngine);
  }

  @Bean
  public SQLService sqlService() {
    return new SQLService(new SQLSyntaxParser(), analyzer(), storageEngine, executionEngine,
        functionRepository);
  }

}

