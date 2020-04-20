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
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionExpressionBuilder;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionResolver;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionSignature;

import java.util.Arrays;
import java.util.function.Predicate;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.getBooleanValue;

public class UnaryPredicateFunction {
    public static void register(BuiltinFunctionRepository repository) {
        repository.register(not());
    }

    private static FunctionResolver not() {
        FunctionName functionName = BuiltinFunctionName.NOT.getName();
        return FunctionResolver.builder()
                .functionName(functionName)
                .functionBundle(new FunctionSignature(functionName,
                        Arrays.asList(ExprType.BOOLEAN)), predicateFunction(functionName,
                        v1 -> !v1, ExprType.BOOLEAN))
                .build();
    }

    private static FunctionExpressionBuilder predicateFunction(
            FunctionName functionName,
            Predicate<Boolean> predicate,
            ExprType returnType) {
        return arguments -> new FunctionExpression(functionName, arguments) {
            @Override
            public ExprValue valueOf() {
                ExprValue arg1 = arguments.get(0).valueOf();
                return ExprValueUtils.fromObjectValue(predicate.test(getBooleanValue(arg1)));
            }

            @Override
            public ExprType type() {
                return returnType;
            }
        };
    }
}
