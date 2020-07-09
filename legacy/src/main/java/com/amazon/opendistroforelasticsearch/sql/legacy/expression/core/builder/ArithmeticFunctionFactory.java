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

import com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator.ScalarOperation;
import com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator.BinaryScalarOperator;
import com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator.DoubleBinaryScalarOperator;
import com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator.DoubleUnaryScalarOperator;
import com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator.UnaryScalarOperator;

/**
 * The definition of arithmetic function builder factory.
 */
public class ArithmeticFunctionFactory {
    public static ExpressionBuilder add() {
        return new BinaryExpressionBuilder(
                new BinaryScalarOperator(
                        ScalarOperation.ADD,
                        Math::addExact,
                        Math::addExact,
                        Double::sum,
                        Float::sum));
    }

    public static ExpressionBuilder subtract() {
        return new BinaryExpressionBuilder(
                new BinaryScalarOperator(
                        ScalarOperation.ADD,
                        Math::subtractExact,
                        Math::subtractExact,
                        (v1, v2) -> v1 - v2,
                        (v1, v2) -> v1 - v2));
    }

    public static ExpressionBuilder multiply() {
        return new BinaryExpressionBuilder(
                new BinaryScalarOperator(
                        ScalarOperation.MULTIPLY,
                        Math::multiplyExact,
                        Math::multiplyExact,
                        (v1, v2) -> v1 * v2,
                        (v1, v2) -> v1 * v2
                ));
    }

    public static ExpressionBuilder divide() {
        return new BinaryExpressionBuilder(
                new BinaryScalarOperator(
                        ScalarOperation.DIVIDE,
                        (v1, v2) -> v1 / v2,
                        (v1, v2) -> v1 / v2,
                        (v1, v2) -> v1 / v2,
                        (v1, v2) -> v1 / v2
                ));
    }

    public static ExpressionBuilder modules() {
        return new BinaryExpressionBuilder(
                new BinaryScalarOperator(
                        ScalarOperation.MODULES,
                        (v1, v2) -> v1 % v2,
                        (v1, v2) -> v1 % v2,
                        (v1, v2) -> v1 % v2,
                        (v1, v2) -> v1 % v2
                ));
    }

    public static ExpressionBuilder abs() {
        return new UnaryExpressionBuilder(
                new UnaryScalarOperator(
                        ScalarOperation.ABS,
                        Math::abs,
                        Math::abs,
                        Math::abs,
                        Math::abs
                ));
    }

    public static ExpressionBuilder acos() {
        return new UnaryExpressionBuilder(
                new DoubleUnaryScalarOperator(
                        ScalarOperation.ACOS,
                        Math::acos
                ));
    }

    public static ExpressionBuilder asin() {
        return new UnaryExpressionBuilder(
                new DoubleUnaryScalarOperator(
                        ScalarOperation.ASIN,
                        Math::asin
                )
        );
    }

    public static ExpressionBuilder atan() {
        return new UnaryExpressionBuilder(
                new DoubleUnaryScalarOperator(
                        ScalarOperation.ATAN,
                        Math::atan
                )
        );
    }

    public static ExpressionBuilder atan2() {
        return new BinaryExpressionBuilder(
                new DoubleBinaryScalarOperator(
                        ScalarOperation.ATAN2,
                        Math::atan2
                )
        );
    }

    public static ExpressionBuilder tan() {
        return new UnaryExpressionBuilder(
                new DoubleUnaryScalarOperator(
                        ScalarOperation.TAN,
                        Math::tan
                )
        );
    }

    public static ExpressionBuilder cbrt() {
        return new UnaryExpressionBuilder(
                new DoubleUnaryScalarOperator(
                        ScalarOperation.CBRT,
                        Math::cbrt
                )
        );
    }

    public static ExpressionBuilder ceil() {
        return new UnaryExpressionBuilder(
                new DoubleUnaryScalarOperator(
                        ScalarOperation.CEIL,
                        Math::ceil
                )
        );
    }

    public static ExpressionBuilder cos() {
        return new UnaryExpressionBuilder(
                new DoubleUnaryScalarOperator(
                        ScalarOperation.COS,
                        Math::cos
                )
        );
    }

    public static ExpressionBuilder cosh() {
        return new UnaryExpressionBuilder(
                new DoubleUnaryScalarOperator(
                        ScalarOperation.COSH,
                        Math::cosh
                )
        );
    }

    public static ExpressionBuilder exp() {
        return new UnaryExpressionBuilder(
                new DoubleUnaryScalarOperator(
                        ScalarOperation.EXP,
                        Math::exp
                )
        );
    }

    public static ExpressionBuilder floor() {
        return new UnaryExpressionBuilder(
                new DoubleUnaryScalarOperator(
                        ScalarOperation.FLOOR,
                        Math::floor
                )
        );
    }

    public static ExpressionBuilder ln() {
        return new UnaryExpressionBuilder(
                new DoubleUnaryScalarOperator(
                        ScalarOperation.LN,
                        Math::log
                )
        );
    }

    public static ExpressionBuilder log() {
        return new UnaryExpressionBuilder(
                new DoubleUnaryScalarOperator(
                        ScalarOperation.LOG,
                        Math::log
                )
        );
    }

    public static ExpressionBuilder log2() {
        return new UnaryExpressionBuilder(
                new DoubleUnaryScalarOperator(
                        ScalarOperation.LOG2,
                        (x) -> Math.log(x) / Math.log(2d)
                )
        );
    }

    public static ExpressionBuilder log10() {
        return new UnaryExpressionBuilder(
                new DoubleUnaryScalarOperator(
                        ScalarOperation.LOG10,
                        Math::log10
                )
        );
    }
}
