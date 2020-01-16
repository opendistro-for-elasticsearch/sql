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

package com.amazon.opendistroforelasticsearch.sql.expression.core.operator;

import com.amazon.opendistroforelasticsearch.sql.expression.core.ScalarOperation;
import com.amazon.opendistroforelasticsearch.sql.expression.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.expression.model.ExprValueFactory;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Function;

@RequiredArgsConstructor
public class UnaryScalarOperator implements ScalarOperator {
    private final ScalarOperation op;
    private final Function<Integer, Integer> integerFunc;
    private final Function<Long, Long> longFunc;
    private final Function<Double, Double> doubleFunc;
    private final Function<Float, Float> floatFunc;

    @Override
    public ExprValue apply(List<ExprValue> exprValues) {
        ExprValue exprValue = exprValues.get(0);
        switch (exprValue.kind()) {
            case DOUBLE_VALUE:
                return ExprValueFactory.from(doubleFunc.apply(exprValue.numberValue().doubleValue()));
            case INTEGER_VALUE:
                return ExprValueFactory.from(integerFunc.apply(exprValue.numberValue().intValue()));
            case LONG_VALUE:
                return ExprValueFactory.from(longFunc.apply(exprValue.numberValue().longValue()));
            case FLOAT_VALUE:
                return ExprValueFactory.from(floatFunc.apply(exprValue.numberValue().floatValue()));
            default:
                throw new RuntimeException(String.format("unexpected operation type: %s(%s)", op.name(),
                                                         exprValue.kind()));
        }
    }

    @Override
    public String name() {
        return op.name();
    }
}
