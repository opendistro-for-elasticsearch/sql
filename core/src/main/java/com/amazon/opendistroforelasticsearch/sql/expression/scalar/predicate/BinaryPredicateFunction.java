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
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionBuilder;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionResolver;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionSignature;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_FALSE;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_MISSING;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_NULL;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_TRUE;

/**
 * The definition of binary predicate function
 * and, Accepts two Boolean values and produces a Boolean.
 * or,  Accepts two Boolean values and produces a Boolean.
 * xor, Accepts two Boolean values and produces a Boolean.
 * equal, Compare the left expression and right expression and produces a Boolean.
 */
@UtilityClass
public class BinaryPredicateFunction {
    public static void register(BuiltinFunctionRepository repository) {
        repository.register(and());
        repository.register(or());
        repository.register(xor());
        repository.register(equal());
    }

    /**
     * The and logic
     * A       B       A AND B
     * TRUE    TRUE    TRUE
     * TRUE    FALSE   FALSE
     * TRUE    NULL    NULL
     * TRUE    MISSING MISSING
     * FALSE   FALSE   FALSE
     * FALSE   NULL    FALSE
     * FALSE   MISSING FALSE
     * NULL    NULL    NULL
     * NULL    MISSING MISSING
     * MISSING MISSING MISSING
     */
    private static Table<ExprValue, ExprValue, ExprValue> andTable =
            new ImmutableTable.Builder<ExprValue, ExprValue, ExprValue>()
                    .put(LITERAL_TRUE, LITERAL_TRUE, LITERAL_TRUE)
                    .put(LITERAL_TRUE, LITERAL_FALSE, LITERAL_FALSE)
                    .put(LITERAL_TRUE, LITERAL_NULL, LITERAL_NULL)
                    .put(LITERAL_TRUE, LITERAL_MISSING, LITERAL_MISSING)
                    .put(LITERAL_FALSE, LITERAL_FALSE, LITERAL_FALSE)
                    .put(LITERAL_FALSE, LITERAL_NULL, LITERAL_FALSE)
                    .put(LITERAL_FALSE, LITERAL_MISSING, LITERAL_FALSE)
                    .put(LITERAL_NULL, LITERAL_NULL, LITERAL_NULL)
                    .put(LITERAL_NULL, LITERAL_MISSING, LITERAL_MISSING)
                    .put(LITERAL_MISSING, LITERAL_MISSING, LITERAL_MISSING)
                    .build();

    /**
     * The or logic
     * A       B       A AND B
     * TRUE    TRUE    TRUE
     * TRUE    FALSE   TRUE
     * TRUE    NULL    TRUE
     * TRUE    MISSING TRUE
     * FALSE   FALSE   FALSE
     * FALSE   NULL    NULL
     * FALSE   MISSING MISSING
     * NULL    NULL    NULL
     * NULL    MISSING NULL
     * MISSING MISSING MISSING
     */
    private static Table<ExprValue, ExprValue, ExprValue> orTable =
            new ImmutableTable.Builder<ExprValue, ExprValue, ExprValue>()
                    .put(LITERAL_TRUE, LITERAL_TRUE, LITERAL_TRUE)
                    .put(LITERAL_TRUE, LITERAL_FALSE, LITERAL_TRUE)
                    .put(LITERAL_TRUE, LITERAL_NULL, LITERAL_TRUE)
                    .put(LITERAL_TRUE, LITERAL_MISSING, LITERAL_TRUE)
                    .put(LITERAL_FALSE, LITERAL_FALSE, LITERAL_FALSE)
                    .put(LITERAL_FALSE, LITERAL_NULL, LITERAL_NULL)
                    .put(LITERAL_FALSE, LITERAL_MISSING, LITERAL_MISSING)
                    .put(LITERAL_NULL, LITERAL_NULL, LITERAL_NULL)
                    .put(LITERAL_NULL, LITERAL_MISSING, LITERAL_NULL)
                    .put(LITERAL_MISSING, LITERAL_MISSING, LITERAL_MISSING)
                    .build();

    /**
     * The xor logic
     * A       B       A AND B
     * TRUE    TRUE    FALSE
     * TRUE    FALSE   TRUE
     * TRUE    NULL    TRUE
     * TRUE    MISSING TRUE
     * FALSE   FALSE   FALSE
     * FALSE   NULL    NULL
     * FALSE   MISSING MISSING
     * NULL    NULL    NULL
     * NULL    MISSING NULL
     * MISSING MISSING MISSING
     */
    private static Table<ExprValue, ExprValue, ExprValue> xorTable =
            new ImmutableTable.Builder<ExprValue, ExprValue, ExprValue>()
                    .put(LITERAL_TRUE, LITERAL_TRUE, LITERAL_FALSE)
                    .put(LITERAL_TRUE, LITERAL_FALSE, LITERAL_TRUE)
                    .put(LITERAL_TRUE, LITERAL_NULL, LITERAL_TRUE)
                    .put(LITERAL_TRUE, LITERAL_MISSING, LITERAL_TRUE)
                    .put(LITERAL_FALSE, LITERAL_FALSE, LITERAL_FALSE)
                    .put(LITERAL_FALSE, LITERAL_NULL, LITERAL_NULL)
                    .put(LITERAL_FALSE, LITERAL_MISSING, LITERAL_MISSING)
                    .put(LITERAL_NULL, LITERAL_NULL, LITERAL_NULL)
                    .put(LITERAL_NULL, LITERAL_MISSING, LITERAL_NULL)
                    .put(LITERAL_MISSING, LITERAL_MISSING, LITERAL_MISSING)
                    .build();

    /**
     * The equal logic
     * A       B       A == B
     * NULL    NULL    TRUE
     * NULL    MISSING FALSE
     * MISSING NULL    FALSE
     * MISSING MISSING TRUE
     */
    private static Table<ExprValue, ExprValue, ExprValue> equalTable =
            new ImmutableTable.Builder<ExprValue, ExprValue, ExprValue>()
                    .put(LITERAL_NULL, LITERAL_NULL, LITERAL_TRUE)
                    .put(LITERAL_NULL, LITERAL_MISSING, LITERAL_FALSE)
                    .put(LITERAL_MISSING, LITERAL_NULL, LITERAL_FALSE)
                    .put(LITERAL_MISSING, LITERAL_MISSING, LITERAL_TRUE)
                    .build();

    private static FunctionResolver and() {
        FunctionName functionName = BuiltinFunctionName.AND.getName();
        return FunctionResolver.builder()
                .functionName(functionName)
                .functionBundle(new FunctionSignature(functionName,
                        Arrays.asList(ExprType.BOOLEAN, ExprType.BOOLEAN)), binaryPredicate(functionName,
                        andTable, ExprType.BOOLEAN))
                .build();
    }

    private static FunctionResolver or() {
        FunctionName functionName = BuiltinFunctionName.OR.getName();
        return FunctionResolver.builder()
                .functionName(functionName)
                .functionBundle(new FunctionSignature(functionName,
                        Arrays.asList(ExprType.BOOLEAN, ExprType.BOOLEAN)), binaryPredicate(functionName,
                        orTable, ExprType.BOOLEAN))
                .build();
    }

    private static FunctionResolver xor() {
        FunctionName functionName = BuiltinFunctionName.XOR.getName();
        return FunctionResolver.builder()
                .functionName(functionName)
                .functionBundle(new FunctionSignature(functionName,
                        Arrays.asList(ExprType.BOOLEAN, ExprType.BOOLEAN)), binaryPredicate(functionName,
                        xorTable, ExprType.BOOLEAN))
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

    private static Map<FunctionSignature, FunctionBuilder> predicateFunction(
            FunctionName functionName,
            BiFunction<Integer, Integer, Boolean> integerFunc,
            BiFunction<Long, Long, Boolean> longFunc,
            BiFunction<Float, Float, Boolean> floatFunc,
            BiFunction<Double, Double, Boolean> doubleFunc,
            BiFunction<String, String, Boolean> stringFunc,
            BiFunction<Boolean, Boolean, Boolean> booleanFunc,
            BiFunction<List, List, Boolean> listFunc,
            BiFunction<Map, Map, Boolean> mapFunc) {
        ImmutableMap.Builder<FunctionSignature, FunctionBuilder> builder = new ImmutableMap.Builder<>();
        builder.put(new FunctionSignature(functionName, Arrays.asList(ExprType.INTEGER, ExprType.INTEGER)),
                equal(functionName, integerFunc, ExprValueUtils::getIntegerValue,
                        ExprType.BOOLEAN));
        builder.put(new FunctionSignature(functionName, Arrays.asList(ExprType.LONG, ExprType.LONG)),
                equal(functionName, longFunc, ExprValueUtils::getLongValue,
                        ExprType.BOOLEAN));
        builder.put(new FunctionSignature(functionName, Arrays.asList(ExprType.FLOAT, ExprType.FLOAT)),
                equal(functionName, floatFunc, ExprValueUtils::getFloatValue,
                        ExprType.BOOLEAN));
        builder.put(new FunctionSignature(functionName, Arrays.asList(ExprType.DOUBLE, ExprType.DOUBLE)),
                equal(functionName, doubleFunc, ExprValueUtils::getDoubleValue,
                        ExprType.BOOLEAN));
        builder.put(new FunctionSignature(functionName, Arrays.asList(ExprType.STRING, ExprType.STRING)),
                equal(functionName, stringFunc, ExprValueUtils::getStringValue,
                        ExprType.BOOLEAN));
        builder.put(new FunctionSignature(functionName, Arrays.asList(ExprType.BOOLEAN, ExprType.BOOLEAN)),
                equal(functionName, booleanFunc, ExprValueUtils::getBooleanValue,
                        ExprType.BOOLEAN));
        builder.put(new FunctionSignature(functionName, Arrays.asList(ExprType.ARRAY, ExprType.ARRAY)),
                equal(functionName, listFunc, ExprValueUtils::getCollectionValue,
                        ExprType.BOOLEAN));
        builder.put(new FunctionSignature(functionName, Arrays.asList(ExprType.STRUCT, ExprType.STRUCT)),
                equal(functionName, mapFunc, ExprValueUtils::getTupleValue,
                        ExprType.BOOLEAN));
        return builder.build();
    }

    private static FunctionBuilder binaryPredicate(FunctionName functionName,
                                                   Table<ExprValue, ExprValue, ExprValue> logicalTable,
                                                   ExprType returnType) {
        return arguments -> new FunctionExpression(functionName, arguments) {
            @Override
            public ExprValue valueOf(Environment<Expression, ExprValue> env) {
                ExprValue arg1 = arguments.get(0).valueOf(env);
                ExprValue arg2 = arguments.get(1).valueOf(env);
                if (logicalTable.contains(arg1, arg2)) {
                    return logicalTable.get(arg1, arg2);
                } else {
                    return logicalTable.get(arg2, arg1);
                }
            }

            @Override
            public ExprType type(Environment<Expression, ExprType> env) {
                return returnType;
            }
        };
    }

    private static <T, R> FunctionBuilder equal(FunctionName functionName,
                                                BiFunction<T, T, R> function,
                                                Function<ExprValue, T> observer,
                                                ExprType returnType) {
        return arguments -> new FunctionExpression(functionName, arguments) {
            @Override
            public ExprValue valueOf(Environment<Expression, ExprValue> env) {
                ExprValue arg1 = arguments.get(0).valueOf(env);
                ExprValue arg2 = arguments.get(1).valueOf(env);
                if (equalTable.contains(arg1, arg2)) {
                    return equalTable.get(arg1, arg2);
                } else if (arg1.isMissing() || arg1.isNull() || arg2.isMissing() || arg2.isNull()) {
                    return LITERAL_FALSE;
                } else {
                    return ExprValueUtils.fromObjectValue(
                            function.apply(observer.apply(arg1), observer.apply(arg2)));
                }
            }

            @Override
            public ExprType type(Environment<Expression, ExprType> env) {
                return returnType;
            }
        };
    }
}
