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

import com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValueFactory;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValue.ExprValueKind.DOUBLE_VALUE;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValue.ExprValueKind.FLOAT_VALUE;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValue.ExprValueKind.INTEGER_VALUE;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValue.ExprValueKind.LONG_VALUE;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValueUtils.getDoubleValue;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValueUtils.getFloatValue;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValueUtils.getIntegerValue;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValueUtils.getLongValue;

/**
 * Binary Scalar Operator take two {@link ExprValue} as arguments ans return one {@link ExprValue} as result.
 */
@RequiredArgsConstructor
public class BinaryScalarOperator implements ScalarOperator {
    private static final Map<ExprValue.ExprValueKind, Integer> numberTypeOrder =
            new ImmutableMap.Builder<ExprValue.ExprValueKind, Integer>()
                    .put(INTEGER_VALUE, 0)
                    .put(LONG_VALUE, 1)
                    .put(DOUBLE_VALUE, 2)
                    .put(FLOAT_VALUE, 3)
                    .build();

    private final ScalarOperation op;
    private final BiFunction<Integer, Integer, Integer> integerFunc;
    private final BiFunction<Long, Long, Long> longFunc;
    private final BiFunction<Double, Double, Double> doubleFunc;
    private final BiFunction<Float, Float, Float> floatFunc;

    @Override
    public ExprValue apply(List<ExprValue> valueList) {
        ExprValue v1 = valueList.get(0);
        ExprValue v2 = valueList.get(1);
        if (!numberTypeOrder.containsKey(v1.kind()) || !numberTypeOrder.containsKey(v2.kind())) {
            throw new RuntimeException(
                    String.format("unexpected operation type: %s(%s, %s) ", op.name(), v1.kind(), v2.kind()));
        }
        ExprValue.ExprValueKind expectedType = numberTypeOrder.get(v1.kind()) > numberTypeOrder.get(v2.kind())
                ? v1.kind() : v2.kind();
        switch (expectedType) {
            case DOUBLE_VALUE:
                return ExprValueFactory.from(doubleFunc.apply(getDoubleValue(v1), getDoubleValue(v2)));
            case INTEGER_VALUE:
                return ExprValueFactory
                        .from(integerFunc.apply(getIntegerValue(v1), getIntegerValue(v2)));
            case LONG_VALUE:
                return ExprValueFactory
                        .from(longFunc.apply(getLongValue(v1), getLongValue(v2)));
            case FLOAT_VALUE:
                return ExprValueFactory
                        .from(floatFunc.apply(getFloatValue(v1), getFloatValue(v2)));
            default:
                throw new RuntimeException(String.format("unexpected operation type: %s(%s, %s)", op.name(), v1.kind(),
                                                         v2.kind()));
        }
    }

    @Override
    public String name() {
        return op.name();
    }
}
