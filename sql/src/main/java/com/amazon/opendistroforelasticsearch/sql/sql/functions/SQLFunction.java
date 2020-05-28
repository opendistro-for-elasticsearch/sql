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

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionName;

import java.util.List;

/**
 * SQL function abstract class
 */
public abstract class SQLFunction extends FunctionExpression {

    public SQLFunction(FunctionName functionName, List<Expression> arguments) {
        super(functionName, arguments);
    }

    @Override
    public ExprValue valueOf(Environment<Expression, ExprValue> valueEnv) {
        List<Expression> argExprs = getArguments();
        ExprValue[] argValues = new ExprValue[argExprs.size()];

        for (int i = 0; i < argExprs.size(); i++) {
            ExprValue argValue = argExprs.get(i).valueOf(valueEnv);
            if (argValue.isMissing()) {
                return ExprValueUtils.missingValue();
            }
            if (argValue.isNull()) {
                return ExprValueUtils.nullValue();
            }
            argValues[i] = argValue;
        }
        return valueOf(argValues);
    }

    protected abstract ExprValue valueOf(ExprValue[] argValues);

}
