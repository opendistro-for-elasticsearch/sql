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

package com.amazon.opendistroforelasticsearch.sql.expression.scalar.predicate;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionExpressionBuilder;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionResolver;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionSignature;
import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static com.amazon.opendistroforelasticsearch.sql.expression.scalar.OperatorUtils.binaryOperator;

public class BinaryPredicateFunction {
    public static void register(BuiltinFunctionRepository repository) {
        repository.register(and());
        repository.register(or());
        repository.register(xor());
        repository.register(equal());
        repository.register(notEqual());
    }

    private static FunctionResolver and() {
        FunctionName functionName = BuiltinFunctionName.AND.getName();
        return FunctionResolver.builder()
                .functionName(functionName)
                .functionBundle(new FunctionSignature(functionName,
                        Arrays.asList(ExprType.BOOLEAN, ExprType.BOOLEAN)), binaryOperator(functionName,
                        (v1, v2) -> v1 && v2, ExprValueUtils::getBooleanValue, ExprType.BOOLEAN))
                .build();
    }

    private static FunctionResolver or() {
        FunctionName functionName = BuiltinFunctionName.OR.getName();
        return FunctionResolver.builder()
                .functionName(functionName)
                .functionBundle(new FunctionSignature(functionName,
                        Arrays.asList(ExprType.BOOLEAN, ExprType.BOOLEAN)), binaryOperator(functionName,
                        (v1, v2) -> v1 || v2, ExprValueUtils::getBooleanValue, ExprType.BOOLEAN))
                .build();
    }

    private static FunctionResolver xor() {
        FunctionName functionName = BuiltinFunctionName.XOR.getName();
        return FunctionResolver.builder()
                .functionName(functionName)
                .functionBundle(new FunctionSignature(functionName,
                        Arrays.asList(ExprType.BOOLEAN, ExprType.BOOLEAN)), binaryOperator(functionName,
                        (v1, v2) -> v1 ^ v2, ExprValueUtils::getBooleanValue, ExprType.BOOLEAN))
                .build();
    }

    private static FunctionResolver equal() {
        return new FunctionResolver(
                BuiltinFunctionName.EQUAL.getName(),
                predicateFunction(
                        BuiltinFunctionName.EQUAL.getName(),
                        Integer::equals,
                        Long::equals,
                        Float::equals,
                        Double::equals,
                        String::equals,
                        Boolean::equals,
                        List::equals,
                        Map::equals
                )
        );
    }

    private static FunctionResolver notEqual() {
        return new FunctionResolver(
                BuiltinFunctionName.NOTEQUAL.getName(),
                predicateFunction(
                        BuiltinFunctionName.NOTEQUAL.getName(),
                        (v1, v2) -> !v1.equals(v2),
                        (v1, v2) -> !v1.equals(v2),
                        (v1, v2) -> !v1.equals(v2),
                        (v1, v2) -> !v1.equals(v2),
                        (v1, v2) -> !v1.equals(v2),
                        (v1, v2) -> !v1.equals(v2),
                        (v1, v2) -> !v1.equals(v2),
                        (v1, v2) -> !v1.equals(v2)
                )
        );
    }

    private static Map<FunctionSignature, FunctionExpressionBuilder> predicateFunction(
            FunctionName functionName,
            BiFunction<Integer, Integer, Boolean> integerFunc,
            BiFunction<Long, Long, Boolean> longFunc,
            BiFunction<Float, Float, Boolean> floatFunc,
            BiFunction<Double, Double, Boolean> doubleFunc,
            BiFunction<String, String, Boolean> stringFunc,
            BiFunction<Boolean, Boolean, Boolean> booleanFunc,
            BiFunction<List, List, Boolean> listFunc,
            BiFunction<Map, Map, Boolean> mapFunc) {
        ImmutableMap.Builder<FunctionSignature, FunctionExpressionBuilder> builder = new ImmutableMap.Builder<>();
        builder.put(new FunctionSignature(functionName, Arrays.asList(ExprType.INTEGER, ExprType.INTEGER)),
                binaryOperator(functionName, integerFunc, ExprValueUtils::getIntegerValue,
                        ExprType.BOOLEAN));
        builder.put(new FunctionSignature(functionName, Arrays.asList(ExprType.LONG, ExprType.LONG)),
                binaryOperator(functionName, longFunc, ExprValueUtils::getLongValue,
                        ExprType.BOOLEAN));
        builder.put(new FunctionSignature(functionName, Arrays.asList(ExprType.FLOAT, ExprType.FLOAT)),
                binaryOperator(functionName, floatFunc, ExprValueUtils::getFloatValue,
                        ExprType.BOOLEAN));
        builder.put(new FunctionSignature(functionName, Arrays.asList(ExprType.DOUBLE, ExprType.DOUBLE)),
                binaryOperator(functionName, doubleFunc, ExprValueUtils::getDoubleValue,
                        ExprType.BOOLEAN));
        builder.put(new FunctionSignature(functionName, Arrays.asList(ExprType.STRING, ExprType.STRING)),
                binaryOperator(functionName, stringFunc, ExprValueUtils::getStringValue,
                        ExprType.BOOLEAN));
        builder.put(new FunctionSignature(functionName, Arrays.asList(ExprType.BOOLEAN, ExprType.BOOLEAN)),
                binaryOperator(functionName, booleanFunc, ExprValueUtils::getBooleanValue,
                        ExprType.BOOLEAN));
        builder.put(new FunctionSignature(functionName, Arrays.asList(ExprType.ARRAY, ExprType.ARRAY)),
                binaryOperator(functionName, listFunc, ExprValueUtils::getCollectionValue,
                        ExprType.BOOLEAN));
        builder.put(new FunctionSignature(functionName, Arrays.asList(ExprType.STRUCT, ExprType.STRUCT)),
                binaryOperator(functionName, mapFunc, ExprValueUtils::getTupleValue,
                        ExprType.BOOLEAN));
        return builder.build();
    }
}
