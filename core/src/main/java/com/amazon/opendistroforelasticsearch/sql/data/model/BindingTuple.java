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

package com.amazon.opendistroforelasticsearch.sql.data.model;

import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Singular;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * BindingTuple represents the a relationship between bindingName and ExprValue.
 * e.g. The operation output column name is bindingName, the value is the ExprValue.
 */
@Builder
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class BindingTuple implements Environment<Expression, ExprValue> {
    @Singular("binding")
    private final Map<String, ExprValue> bindingMap;

    /**
     * Resolve the Binding Name in BindingTuple context.
     *
     * @param bindingName binding name.
     * @return binding value.
     */
    public ExprValue resolve(String bindingName) {
        return bindingMap.getOrDefault(bindingName, ExprMissingValue.of());
    }

    @Override
    public ExprValue resolve(Expression var) {
        if(var instanceof ReferenceExpression) {
            return resolve(((ReferenceExpression) var).getAttr());
        } else {
            throw new ExpressionEvaluationException("can't resolve expression");
        }
    }


    @Override
    public String toString() {
        return bindingMap.entrySet()
                .stream()
                .map(entry -> String.format("%s:%s", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(",", "<", ">"));
    }

    public static BindingTuple from(Map<String, Object> map) {
        BindingTupleBuilder bindingTupleBuilder = BindingTuple.builder();
        map.forEach((key, value) -> bindingTupleBuilder.binding(key, ExprValueUtils.fromObjectValue(value)));
        return bindingTupleBuilder.build();
    }


}
