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
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.expression.scalar.arthmetic.ArithmeticFunction;
import com.amazon.opendistroforelasticsearch.sql.expression.scalar.conversion.ToStringFunction;
import com.amazon.opendistroforelasticsearch.sql.expression.scalar.predicate.BinaryPredicateFunction;
import com.amazon.opendistroforelasticsearch.sql.expression.scalar.predicate.UnaryPredicateFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;

@Configuration
public class FunctionConfig {

    @Bean
    public BuiltinFunctionRepository functionRepository() {
        BuiltinFunctionRepository builtinFunctionRepository = new BuiltinFunctionRepository(new HashMap<>());
        ArithmeticFunction.register(builtinFunctionRepository);
        ToStringFunction.register(builtinFunctionRepository);
        BinaryPredicateFunction.register(builtinFunctionRepository);
        UnaryPredicateFunction.register(builtinFunctionRepository);
        return builtinFunctionRepository;
    }

    @Bean
    public DSL dsl(BuiltinFunctionRepository repository) {
        return new DSL(repository);
    }
}
