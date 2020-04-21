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

package com.amazon.opendistroforelasticsearch.sql.expression.scalar.conversion;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionExpressionBuilder;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionResolver;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionSignature;
import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.Map;

/**
 * tostring(X,Y)
 */
public class ToStringFunction {
    public static final FunctionName TOSTRING = FunctionName.of("tostring");

    public static void register(BuiltinFunctionRepository repository) {
        repository.register(new FunctionResolver(TOSTRING, tostring(TOSTRING)));
    }

    private static Map<FunctionSignature, FunctionExpressionBuilder> tostring(FunctionName functionName) {
        ImmutableMap.Builder<FunctionSignature, FunctionExpressionBuilder> builder = new ImmutableMap.Builder<>();
        builder.put(new FunctionSignature(functionName, Arrays.asList(ExprType.INTEGER, ExprType.STRING)),
                tostring(functionName, ExprType.STRING));
        builder.put(new FunctionSignature(functionName, Arrays.asList(ExprType.LONG, ExprType.STRING)),
                tostring(functionName, ExprType.STRING));
        builder.put(new FunctionSignature(functionName, Arrays.asList(ExprType.FLOAT, ExprType.STRING)),
                tostring(functionName, ExprType.STRING));
        builder.put(new FunctionSignature(functionName, Arrays.asList(ExprType.DOUBLE, ExprType.STRING)),
                tostring(functionName, ExprType.STRING));
        builder.put(new FunctionSignature(functionName, Arrays.asList(ExprType.BOOLEAN, ExprType.STRING)),
                tostring(functionName, ExprType.STRING));
        return builder.build();
    }

    private static FunctionExpressionBuilder tostring(FunctionName functionName,
                                                      ExprType returnType) {
        return arguments -> new FunctionExpression(functionName, arguments) {
            @Override
            public ExprValue valueOf() {
                Expression x = arguments.get(0);
                Expression y = arguments.get(1);
                String opt = (String) y.valueOf().value();
                if (opt.equalsIgnoreCase("hex")) {
                    return ExprValueUtils.stringValue(
                            String.format("0x%X", x.valueOf().value()));
                } else {
                    return ExprValueUtils.stringValue(x.valueOf().toString());
                }
            }

            @Override
            public ExprType type() {
                return returnType;
            }
        };
    }
}
