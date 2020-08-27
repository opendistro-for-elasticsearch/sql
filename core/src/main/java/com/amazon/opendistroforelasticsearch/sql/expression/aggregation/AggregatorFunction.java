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

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.ARRAY;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.BOOLEAN;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DATE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DATETIME;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.FLOAT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.LONG;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRUCT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.TIME;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.TIMESTAMP;

import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionBuilder;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionResolver;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionSignature;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import lombok.experimental.UtilityClass;

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
  /**
   * Register Aggregation Function.
   * @param repository {@link BuiltinFunctionRepository}.
   */
  public static void register(BuiltinFunctionRepository repository) {
    repository.register(avg());
    repository.register(sum());
    repository.register(count());
    repository.register(min());
    repository.register(max());
  }

  private static FunctionResolver avg() {
    FunctionName functionName = BuiltinFunctionName.AVG.getName();
    return new FunctionResolver(
        functionName,
        new ImmutableMap.Builder<FunctionSignature, FunctionBuilder>()
            .put(new FunctionSignature(functionName, Collections.singletonList(DOUBLE)),
                arguments -> new AvgAggregator(arguments, DOUBLE))
            .build()
    );
  }

  private static FunctionResolver count() {
    FunctionName functionName = BuiltinFunctionName.COUNT.getName();
    return new FunctionResolver(
        functionName,
        new ImmutableMap.Builder<FunctionSignature, FunctionBuilder>()
            .put(new FunctionSignature(functionName, Collections.singletonList(INTEGER)),
                arguments -> new CountAggregator(arguments, INTEGER))
            .put(new FunctionSignature(functionName, Collections.singletonList(LONG)),
                arguments -> new CountAggregator(arguments, INTEGER))
            .put(new FunctionSignature(functionName, Collections.singletonList(FLOAT)),
                arguments -> new CountAggregator(arguments, INTEGER))
            .put(new FunctionSignature(functionName, Collections.singletonList(DOUBLE)),
                arguments -> new CountAggregator(arguments, INTEGER))
            .put(new FunctionSignature(functionName, Collections.singletonList(STRING)),
                arguments -> new CountAggregator(arguments, INTEGER))
            .put(new FunctionSignature(functionName, Collections.singletonList(STRUCT)),
                arguments -> new CountAggregator(arguments, INTEGER))
            .put(new FunctionSignature(functionName, Collections.singletonList(ARRAY)),
                arguments -> new CountAggregator(arguments, INTEGER))
            .put(new FunctionSignature(functionName, Collections.singletonList(BOOLEAN)),
                arguments -> new CountAggregator(arguments, INTEGER))
            .build()
    );
  }

  private static FunctionResolver sum() {
    FunctionName functionName = BuiltinFunctionName.SUM.getName();
    return new FunctionResolver(
        functionName,
        new ImmutableMap.Builder<FunctionSignature, FunctionBuilder>()
            .put(new FunctionSignature(functionName, Collections.singletonList(INTEGER)),
                arguments -> new SumAggregator(arguments, INTEGER))
            .put(new FunctionSignature(functionName, Collections.singletonList(LONG)),
                arguments -> new SumAggregator(arguments, LONG))
            .put(new FunctionSignature(functionName, Collections.singletonList(FLOAT)),
                arguments -> new SumAggregator(arguments, FLOAT))
            .put(new FunctionSignature(functionName, Collections.singletonList(DOUBLE)),
                arguments -> new SumAggregator(arguments, DOUBLE))
            .build()
    );
  }

  private static FunctionResolver min() {
    FunctionName functionName = BuiltinFunctionName.MIN.getName();
    return new FunctionResolver(
        functionName,
        new ImmutableMap.Builder<FunctionSignature, FunctionBuilder>()
            .put(new FunctionSignature(functionName, Collections.singletonList(INTEGER)),
                arguments -> new MinAggregator(arguments, INTEGER))
            .put(new FunctionSignature(functionName, Collections.singletonList(LONG)),
                arguments -> new MinAggregator(arguments, LONG))
            .put(new FunctionSignature(functionName, Collections.singletonList(FLOAT)),
                arguments -> new MinAggregator(arguments, FLOAT))
            .put(new FunctionSignature(functionName, Collections.singletonList(DOUBLE)),
                arguments -> new MinAggregator(arguments, DOUBLE))
            .put(new FunctionSignature(functionName, Collections.singletonList(STRING)),
                arguments -> new MinAggregator(arguments, STRING))
            .put(new FunctionSignature(functionName, Collections.singletonList(DATE)),
                arguments -> new MinAggregator(arguments, DATE))
            .put(new FunctionSignature(functionName, Collections.singletonList(DATETIME)),
                arguments -> new MinAggregator(arguments, DATETIME))
            .put(new FunctionSignature(functionName, Collections.singletonList(TIME)),
                arguments -> new MinAggregator(arguments, TIME))
            .put(new FunctionSignature(functionName, Collections.singletonList(TIMESTAMP)),
                arguments -> new MinAggregator(arguments, TIMESTAMP))
            .build());
  }

  private static FunctionResolver max() {
    FunctionName functionName = BuiltinFunctionName.MAX.getName();
    return new FunctionResolver(
        functionName,
        new ImmutableMap.Builder<FunctionSignature, FunctionBuilder>()
            .put(new FunctionSignature(functionName, Collections.singletonList(INTEGER)),
                arguments -> new MaxAggregator(arguments, INTEGER))
            .put(new FunctionSignature(functionName, Collections.singletonList(LONG)),
                arguments -> new MaxAggregator(arguments, LONG))
            .put(new FunctionSignature(functionName, Collections.singletonList(FLOAT)),
                arguments -> new MaxAggregator(arguments, FLOAT))
            .put(new FunctionSignature(functionName, Collections.singletonList(DOUBLE)),
                arguments -> new MaxAggregator(arguments, DOUBLE))
            .put(new FunctionSignature(functionName, Collections.singletonList(STRING)),
                arguments -> new MaxAggregator(arguments, STRING))
            .put(new FunctionSignature(functionName, Collections.singletonList(DATE)),
                arguments -> new MaxAggregator(arguments, DATE))
            .put(new FunctionSignature(functionName, Collections.singletonList(DATETIME)),
                arguments -> new MaxAggregator(arguments, DATETIME))
            .put(new FunctionSignature(functionName, Collections.singletonList(TIME)),
                arguments -> new MaxAggregator(arguments, TIME))
            .put(new FunctionSignature(functionName, Collections.singletonList(TIMESTAMP)),
                arguments -> new MaxAggregator(arguments, TIMESTAMP))
            .build()
    );
  }
}
