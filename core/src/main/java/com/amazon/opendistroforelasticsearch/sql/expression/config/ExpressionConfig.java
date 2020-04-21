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

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.expression.scalar.arthmetic.ArithmeticFunction;
import com.amazon.opendistroforelasticsearch.sql.expression.scalar.predicate.BinaryPredicateFunction;
import com.amazon.opendistroforelasticsearch.sql.expression.scalar.predicate.UnaryPredicateFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

/**
 * Expression Config for Spring IoC.
 */
@Configuration
public class ExpressionConfig {
    @Bean
    public Environment<Expression, ExprType> emptyEnv() {
        return var -> {
            throw new ExpressionEvaluationException("empty env");
        };
    }

    @Bean
    public BuiltinFunctionRepository functionRepository(Environment<Expression, ExprType> typeEnv) {
        BuiltinFunctionRepository builtinFunctionRepository = new BuiltinFunctionRepository(new HashMap<>(), typeEnv);
        ArithmeticFunction.register(builtinFunctionRepository);
        BinaryPredicateFunction.register(builtinFunctionRepository);
        UnaryPredicateFunction.register(builtinFunctionRepository);
        return builtinFunctionRepository;
    }

    @Bean
    public DSL dsl(BuiltinFunctionRepository repository) {
        return new DSL(repository);
    }
}
