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

package com.amazon.opendistroforelasticsearch.sql.expression.scalar.arthmetic;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionExpressionBuilder;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionResolver;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionSignature;
import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.amazon.opendistroforelasticsearch.sql.expression.scalar.OperatorUtils.binaryOperator;

public class ArithmeticFunction {

    public static void register(BuiltinFunctionRepository repository) {
        repository.register(add());
        repository.register(subtract());
        repository.register(multiply());
        repository.register(divide());
        repository.register(modules());
    }

    private static FunctionResolver add() {
        return new FunctionResolver(
                BuiltinFunctionName.ADD.getName(),
                scalarFunction(BuiltinFunctionName.ADD.getName(),
                        Math::addExact,
                        Math::addExact,
                        Float::sum,
                        Double::sum)
        );
    }

    private static FunctionResolver subtract() {
        return new FunctionResolver(
                BuiltinFunctionName.SUBTRACT.getName(),
                scalarFunction(BuiltinFunctionName.SUBTRACT.getName(),
                        Math::subtractExact,
                        Math::subtractExact,
                        (v1, v2) -> v1 - v2,
                        (v1, v2) -> v1 - v2)
        );
    }

    private static FunctionResolver multiply() {
        return new FunctionResolver(
                BuiltinFunctionName.MULTIPLY.getName(),
                scalarFunction(BuiltinFunctionName.MULTIPLY.getName(),
                        Math::multiplyExact,
                        Math::multiplyExact,
                        (v1, v2) -> v1 * v2,
                        (v1, v2) -> v1 * v2)
        );
    }

    private static FunctionResolver divide() {
        return new FunctionResolver(
                BuiltinFunctionName.DIVIDE.getName(),
                scalarFunction(BuiltinFunctionName.DIVIDE.getName(),
                        (v1, v2) -> v1 / v2,
                        (v1, v2) -> v1 / v2,
                        (v1, v2) -> v1 / v2,
                        (v1, v2) -> v1 / v2)
        );
    }


    private static FunctionResolver modules() {
        return new FunctionResolver(
                BuiltinFunctionName.MODULES.getName(),
                scalarFunction(BuiltinFunctionName.MODULES.getName(),
                        (v1, v2) -> v1 % v2,
                        (v1, v2) -> v1 % v2,
                        (v1, v2) -> v1 % v2,
                        (v1, v2) -> v1 % v2)
        );
    }

    private static Map<FunctionSignature, FunctionExpressionBuilder> scalarFunction(
            FunctionName functionName,
            BiFunction<Integer, Integer, Integer> integerFunc,
            BiFunction<Long, Long, Long> longFunc,
            BiFunction<Float, Float, Float> floatFunc,
            BiFunction<Double, Double, Double> doubleFunc) {
        ImmutableMap.Builder<FunctionSignature, FunctionExpressionBuilder> builder = new ImmutableMap.Builder<>();
        builder.put(new FunctionSignature(functionName, Arrays.asList(ExprType.INTEGER, ExprType.INTEGER)),
                binaryOperator(functionName, integerFunc, ExprValueUtils::getIntegerValue, ExprType.INTEGER));
        builder.put(new FunctionSignature(functionName, Arrays.asList(ExprType.LONG, ExprType.LONG)),
                binaryOperator(functionName, longFunc, ExprValueUtils::getLongValue, ExprType.LONG));
        builder.put(new FunctionSignature(functionName, Arrays.asList(ExprType.FLOAT, ExprType.FLOAT)),
                binaryOperator(functionName, floatFunc, ExprValueUtils::getFloatValue, ExprType.FLOAT));
        builder.put(new FunctionSignature(functionName, Arrays.asList(ExprType.DOUBLE, ExprType.DOUBLE)),
                binaryOperator(functionName, doubleFunc, ExprValueUtils::getDoubleValue, ExprType.DOUBLE));
        return builder.build();
    }
}
