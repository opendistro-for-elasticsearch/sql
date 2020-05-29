/*
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.sql.functions;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.exception.SemanticCheckException;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionName;

import java.util.Arrays;
import java.util.List;

/**
 * SQL Substring function
 */
public class Substring extends SQLFunction {

    public static final FunctionName FUNCTION_NAME = FunctionName.of("SUBSTRING");

    public Substring(List<Expression> arguments) {
        super(FUNCTION_NAME, arguments);
    }

    @Override
    protected ExprValue valueOf(ExprValue[] argValues) {
        String str = (String) argValues[0].value();
        int start = (int) argValues[1].value();
        int length = (int) argValues[2].value();
        int end = start + length;

        if (!(0 <= start && end <= str.length())) {
            throw new SemanticCheckException(String.format(
                "Semantic error on arguments: %s." +
                    "Expect: start is non-negative and start + length is no greater than string length.",
                        Arrays.toString(argValues)));
        }

        String result = str.substring(start, end);
        return ExprValueUtils.stringValue(result);
    }

    @Override
    public ExprType type(Environment<Expression, ExprType> typeEnv) {
        return ExprType.STRING;
    }

}
