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

package com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.builder;

import com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.Expression;
import com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator.ScalarOperator;
import com.amazon.opendistroforelasticsearch.sql.legacy.expression.domain.BindingTuple;
import com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * The definition of the Expression Builder which has one argument.
 */
@RequiredArgsConstructor
public class UnaryExpressionBuilder implements ExpressionBuilder {
    private final ScalarOperator op;

    /**
     * Build the expression with two {@link Expression} as arguments.
     * @param expressionList expression list.
     * @return expression.
     */
    @Override
    public Expression build(List<Expression> expressionList) {
        Expression expression = expressionList.get(0);

        return new Expression() {
            @Override
            public ExprValue valueOf(BindingTuple tuple) {
                return op.apply(Arrays.asList(expression.valueOf(tuple)));
            }

            @Override
            public String toString() {
                return String.format("%s(%s)", op.name(), expression);
            }
        };
    }
}
