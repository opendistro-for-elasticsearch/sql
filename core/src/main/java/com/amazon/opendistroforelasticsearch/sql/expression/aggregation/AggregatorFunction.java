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

package com.amazon.opendistroforelasticsearch.sql.expression.aggregation;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionBuilder;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionResolver;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionSignature;
import com.google.common.collect.ImmutableMap;
import lombok.experimental.UtilityClass;

import java.util.Collections;

/**
 * The definition of aggregator function
 * avg, Accepts two numbers and produces a number.
 * sum, Accepts two numbers and produces a number.
 * max, Accepts two numbers and produces a number.
 * min, Accepts two numbers and produces a number.
 * count, Accepts two numbers and produces a number.
 */
@UtilityClass
public class AggregatorFunction {

    public static void register(BuiltinFunctionRepository repository) {
        repository.register(avg());
        repository.register(sum());
    }

    private static FunctionResolver avg() {
        FunctionName functionName = BuiltinFunctionName.AVG.getName();
        return new FunctionResolver(
                functionName,
                new ImmutableMap.Builder<FunctionSignature, FunctionBuilder>()
                        .put(new FunctionSignature(functionName, Collections.singletonList(ExprType.DOUBLE)),
                                arguments -> new AvgAggregator(arguments, ExprType.DOUBLE))
                        .build()
        );
    }

    private static FunctionResolver sum() {
        FunctionName functionName = BuiltinFunctionName.SUM.getName();
        return new FunctionResolver(
                functionName,
                new ImmutableMap.Builder<FunctionSignature, FunctionBuilder>()
                        .put(new FunctionSignature(functionName, Collections.singletonList(ExprType.INTEGER)),
                                arguments -> new SumAggregator(arguments, ExprType.INTEGER))
                        .put(new FunctionSignature(functionName, Collections.singletonList(ExprType.LONG)),
                                arguments -> new SumAggregator(arguments, ExprType.LONG))
                        .put(new FunctionSignature(functionName, Collections.singletonList(ExprType.FLOAT)),
                                arguments -> new SumAggregator(arguments, ExprType.FLOAT))
                        .put(new FunctionSignature(functionName, Collections.singletonList(ExprType.DOUBLE)),
                                arguments -> new SumAggregator(arguments, ExprType.DOUBLE))
                        .build()
        );
    }
}
