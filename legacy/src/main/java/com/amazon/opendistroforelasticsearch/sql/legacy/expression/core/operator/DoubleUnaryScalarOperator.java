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

package com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator;

import com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprDoubleValue;
import com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValueFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Function;

import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValueUtils.getDoubleValue;

/**
 * Unary Binary Scalar Operator take one {@link ExprValue} which have double value as arguments ans return one
 * {@link ExprDoubleValue} as result.
 */
@RequiredArgsConstructor
public class DoubleUnaryScalarOperator implements ScalarOperator {
    private final ScalarOperation op;
    private final Function<Double, Double> doubleFunc;

    @Override
    public ExprValue apply(List<ExprValue> exprValues) {
        ExprValue exprValue = exprValues.get(0);
        switch (exprValue.kind()) {
            case DOUBLE_VALUE:
            case INTEGER_VALUE:
            case LONG_VALUE:
            case FLOAT_VALUE:
                return ExprValueFactory.from(doubleFunc.apply(getDoubleValue(exprValue)));
            default:
                throw new RuntimeException(String.format("unexpected operation type: %s(%s)",
                                                         op.name(), exprValue.kind()));
        }
    }

    @Override
    public String name() {
        return op.name();
    }
}
