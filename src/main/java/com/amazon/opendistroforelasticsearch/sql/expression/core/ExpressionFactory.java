/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.expression.core;


import com.amazon.opendistroforelasticsearch.sql.expression.domain.BindingTuple;
import com.amazon.opendistroforelasticsearch.sql.expression.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.expression.model.ExprValueFactory;
import lombok.RequiredArgsConstructor;

import java.util.function.BiFunction;

import static com.amazon.opendistroforelasticsearch.sql.expression.model.ExprValueFactory.doubleValue;

/**
 * The definition of Expression factory.
 */
public class ExpressionFactory {
    /**
     * Reference
     */
    public static Expression ref(String bindingName) {
        return new Expression() {
            @Override
            public String toString() {
                return String.format("%s", bindingName);
            }

            @Override
            public ExprValue valueOf(BindingTuple tuple) {
                return tuple.resolve(bindingName);
            }
        };
    }

    @RequiredArgsConstructor
    enum ArithmeticOperation {
        ADD(Integer::sum, Double::sum),
        SUB((arg1, arg2) -> arg1 - arg2,
            (arg1, arg2) -> arg1 - arg2);

        private final BiFunction<Integer, Integer, Integer> integerFunc;
        private final BiFunction<Double, Double, Double> doubleFunc;
    }

    public static Expression add(Expression left, Expression right) {
        return new Expression() {
            @Override
            public ExprValue valueOf(BindingTuple tuple) {
                return arithmetic(ArithmeticOperation.ADD, left.valueOf(tuple), right.valueOf(tuple));
            }

            @Override
            public String toString() {
                return String.format("add(%s,%s)", left, right);
            }
        };
    }

    public static Expression sub(Expression left, Expression right) {
        return new Expression() {
            @Override
            public ExprValue valueOf(BindingTuple tuple) {
                return arithmetic(ArithmeticOperation.ADD, left.valueOf(tuple), right.valueOf(tuple));
            }

            @Override
            public String toString() {
                return String.format("sub(%s,%s)", left, right);
            }
        };
    }

    public static Expression log(Expression expr) {
        return new Expression() {
            @Override
            public ExprValue valueOf(BindingTuple tuple) {
                final ExprValue exprValue = expr.valueOf(tuple);
                switch (exprValue.kind()) {
                    case INTEGER_VALUE:
                        return doubleValue(Math.log(exprValue.numberValue().intValue()));
                    case DOUBLE_VALUE:
                        return doubleValue(Math.log(exprValue.numberValue().doubleValue()));
                    default:
                        throw new RuntimeException("unsupported log operand: " + exprValue.kind());
                }
            }

            @Override
            public String toString() {
                return String.format("log(%s)", expr);
            }
        };
    }

    public static Expression cast(Expression expr) {
        return new Expression() {
            @Override
            public ExprValue valueOf(BindingTuple tuple) {
                return expr.valueOf(tuple);
            }

            @Override
            public String toString() {
                return String.format("cast(%s)", expr);
            }
        };
    }

    private static ExprValue arithmetic(ArithmeticOperation op, ExprValue v1, ExprValue v2) {
        if (v1.kind() != v2.kind()) {
            throw new RuntimeException(
                    String.format("operation with different type is unsupported: %s(%s, %s)", op.name(), v1.kind(),
                                  v2.kind()));
        } else {
            switch (v1.kind()) {
                case DOUBLE_VALUE:
                    return ExprValueFactory.doubleValue(
                            op.doubleFunc.apply(v1.numberValue().doubleValue(), v2.numberValue().doubleValue()));
                case INTEGER_VALUE:
                    return ExprValueFactory
                            .integerValue(
                                    op.integerFunc.apply(v1.numberValue().intValue(), v2.numberValue().intValue()));
                default:
                    throw new RuntimeException(String.format("unsupported operation: %s(%s, %s)", op.name(), v1.kind(),
                                                             v2.kind()));
            }
        }
    }
}
