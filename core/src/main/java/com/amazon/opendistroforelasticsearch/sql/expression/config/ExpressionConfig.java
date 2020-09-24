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

package com.amazon.opendistroforelasticsearch.sql.expression.config;

import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.AggregatorFunction;
import com.amazon.opendistroforelasticsearch.sql.expression.datetime.DateTimeFunction;
import com.amazon.opendistroforelasticsearch.sql.expression.datetime.IntervalClause;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.expression.operator.arthmetic.ArithmeticFunction;
import com.amazon.opendistroforelasticsearch.sql.expression.operator.arthmetic.MathematicalFunction;
import com.amazon.opendistroforelasticsearch.sql.expression.operator.predicate.BinaryPredicateOperator;
import com.amazon.opendistroforelasticsearch.sql.expression.operator.predicate.UnaryPredicateOperator;
import com.amazon.opendistroforelasticsearch.sql.expression.text.TextFunction;
import java.util.HashMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Expression Config for Spring IoC.
 */
@Configuration
public class ExpressionConfig {
  /**
   * BuiltinFunctionRepository constructor.
   */
  @Bean
  public BuiltinFunctionRepository functionRepository() {
    BuiltinFunctionRepository builtinFunctionRepository =
        new BuiltinFunctionRepository(new HashMap<>());
    ArithmeticFunction.register(builtinFunctionRepository);
    BinaryPredicateOperator.register(builtinFunctionRepository);
    MathematicalFunction.register(builtinFunctionRepository);
    UnaryPredicateOperator.register(builtinFunctionRepository);
    AggregatorFunction.register(builtinFunctionRepository);
    DateTimeFunction.register(builtinFunctionRepository);
    IntervalClause.register(builtinFunctionRepository);
    TextFunction.register(builtinFunctionRepository);
    return builtinFunctionRepository;
  }

  @Bean
  public DSL dsl(BuiltinFunctionRepository repository) {
    return new DSL(repository);
  }
}
