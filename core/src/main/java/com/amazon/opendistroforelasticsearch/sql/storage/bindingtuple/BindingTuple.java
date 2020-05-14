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

package com.amazon.opendistroforelasticsearch.sql.storage.bindingtuple;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;

import java.util.Map;

/**
 * BindingTuple represents the a relationship between bindingName and ExprValue.
 * e.g. The operation output column name is bindingName, the value is the ExprValue.
 */
public abstract class BindingTuple implements Environment<Expression, ExprValue> {

    /**
     * Resolve {@link Expression} in the BindingTuple environment.
     */
    @Override
    public ExprValue resolve(Expression var) {
        if (var instanceof ReferenceExpression) {
            return resolve(((ReferenceExpression) var));
        } else {
            throw new ExpressionEvaluationException(String.format("can resolve expression: %s", var));
        }
    }

    /**
     * Resolve the {@link ReferenceExpression} in BindingTuple context.
     */
    public abstract ExprValue resolve(ReferenceExpression ref);

    /**
     * Create {@link BindingTuple} from Map Object.
     */
    public static BindingTuple from(Map<String, Object> map) {
        MapBasedBindingTuple.MapBasedBindingTupleBuilder builder = new MapBasedBindingTuple.MapBasedBindingTupleBuilder();
        map.forEach((key, value) -> builder.binding(key, ExprValueUtils.fromObjectValue(value)));
        return builder.build();
    }
}
