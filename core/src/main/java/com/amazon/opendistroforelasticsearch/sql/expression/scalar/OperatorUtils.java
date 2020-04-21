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
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionExpressionBuilder;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionName;
import lombok.experimental.UtilityClass;

import java.util.function.BiFunction;
import java.util.function.Function;

@UtilityClass
public class OperatorUtils {
    public static <T, R> FunctionExpressionBuilder binaryOperator(FunctionName functionName,
                                                                  BiFunction<T, T, R> function,
                                                                  Function<ExprValue, T> observer,
                                                                  ExprType returnType) {
        return binaryOperator(functionName, function, observer, observer, returnType);
    }

    public static <T, U, R> FunctionExpressionBuilder binaryOperator(FunctionName functionName,
                                                                     BiFunction<T, U, R> function,
                                                                     Function<ExprValue, T> observer1,
                                                                     Function<ExprValue, U> observer2,
                                                                     ExprType returnType) {
        return arguments -> new FunctionExpression(functionName, arguments) {
            @Override
            public ExprValue valueOf() {
                ExprValue arg1 = arguments.get(0).valueOf();
                ExprValue arg2 = arguments.get(1).valueOf();
                return ExprValueUtils.fromObjectValue(
                        function.apply(observer1.apply(arg1), observer2.apply(arg2)));
            }

            @Override
            public ExprType type() {
                return returnType;
            }
        };
    }
}
