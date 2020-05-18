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

package com.amazon.opendistroforelasticsearch.sql.expression.scalar;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionBuilder;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionName;
import lombok.experimental.UtilityClass;

import java.util.function.BiFunction;
import java.util.function.Function;

@UtilityClass
public class OperatorUtils {
    /**
     * Construct {@link FunctionBuilder} which call function with arguments produced by observer.
     *
     * @param functionName function name
     * @param function     {@link BiFunction}
     * @param observer     extract the value of type T from the first argument
     * @param returnType   return type
     * @param <T>          the type of the first and second argument to the function
     * @param <R>          the type of the result of the function
     * @return {@link FunctionBuilder}
     */
    public static <T, R> FunctionBuilder binaryOperator(FunctionName functionName,
                                                        BiFunction<T, T, R> function,
                                                        Function<ExprValue, T> observer,
                                                        ExprType returnType) {
        return binaryOperator(functionName, function, observer, observer, returnType);
    }

    /**
     * Construct {@link FunctionBuilder} which
     * call function with arguments produced by observer1 and observer2
     * In general, if any operand evaluates to a MISSING value, the enclosing operator will return MISSING;
     * if none of operands evaluates to a MISSING value but there is an operand evaluates to a NULL value,
     * the enclosing operator will return NULL
     *
     * @param functionName function name
     * @param function     {@link BiFunction}
     * @param observer1    extract the value of type T from the first argument
     * @param observer2    extract the value of type U from the second argument
     * @param returnType   return type
     * @param <T>          the type of the first argument to the function
     * @param <U>          the type of the second argument to the function
     * @param <R>          the type of the result of the function
     * @return {@link FunctionBuilder}
     */
    public static <T, U, R> FunctionBuilder binaryOperator(FunctionName functionName,
                                                           BiFunction<T, U, R> function,
                                                           Function<ExprValue, T> observer1,
                                                           Function<ExprValue, U> observer2,
                                                           ExprType returnType) {
        return arguments -> new FunctionExpression(functionName, arguments) {
            @Override
            public ExprValue valueOf(Environment<Expression, ExprValue> env) {
                ExprValue arg1 = arguments.get(0).valueOf(env);
                ExprValue arg2 = arguments.get(1).valueOf(env);
                if (arg1.isMissing() || arg2.isMissing()) {
                    return ExprValueUtils.missingValue();
                } else if (arg1.isNull() || arg2.isNull()) {
                    return ExprValueUtils.nullValue();
                } else {
                    return ExprValueUtils.fromObjectValue(
                            function.apply(observer1.apply(arg1), observer2.apply(arg2)));
                }
            }

            @Override
            public ExprType type(Environment<Expression, ExprType> env) {
                return returnType;
            }
        };
    }
}
