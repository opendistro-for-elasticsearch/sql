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
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import static com.amazon.opendistroforelasticsearch.sql.expression.model.ExprValue.ExprValueKind.DOUBLE_VALUE;
import static com.amazon.opendistroforelasticsearch.sql.expression.model.ExprValue.ExprValueKind.FLOAT_VALUE;
import static com.amazon.opendistroforelasticsearch.sql.expression.model.ExprValue.ExprValueKind.INTEGER_VALUE;
import static com.amazon.opendistroforelasticsearch.sql.expression.model.ExprValue.ExprValueKind.LONG_VALUE;

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
                return ExprValueFactory.from(
                        doubleFunc.apply(v1.numberValue().doubleValue(), v2.numberValue().doubleValue()));
            case INTEGER_VALUE:
                return ExprValueFactory
                        .from(integerFunc.apply(v1.numberValue().intValue(), v2.numberValue().intValue()));
            case LONG_VALUE:
                return ExprValueFactory
                        .from(longFunc.apply(v1.numberValue().longValue(), v2.numberValue().longValue()));
            case FLOAT_VALUE:
                return ExprValueFactory
                        .from(floatFunc.apply(v1.numberValue().floatValue(), v2.numberValue().floatValue()));
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
