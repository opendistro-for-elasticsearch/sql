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

package com.amazon.opendistroforelasticsearch.sql.expression.scalar.arthmetic;


import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.expression.function.WideningTypeRule;
import com.amazon.opendistroforelasticsearch.sql.expression.scalar.FunctionTestBase;
import com.google.common.collect.Lists;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.literal;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ArithmeticFunctionTest extends FunctionTestBase {
    @Autowired
    private BuiltinFunctionRepository functionRepository;

    private static Stream<Arguments> arithmeticFunctionArguments() {
        List<ExprValue> numberOp1 = Stream.of(3, 3L, 3f, 3D)
                .map(ExprValueUtils::fromObjectValue).collect(Collectors.toList());
        List<ExprValue> numberOp2 = Stream.of(2, 2L, 2f, 2D)
                .map(ExprValueUtils::fromObjectValue).collect(Collectors.toList());
        return Lists.cartesianProduct(numberOp1, numberOp2).stream()
                .map(list -> Arguments.of(list.get(0), list.get(1)));
    }

    @ParameterizedTest(name = "add({1}, {2})")
    @MethodSource("arithmeticFunctionArguments")
    public void add(ExprValue op1, ExprValue op2) {
        FunctionExpression expression = dsl.add(literal(op1), literal(op2));
        ExprType expectedType = WideningTypeRule.max(op1.type(), op2.type());
        assertEquals(expectedType, expression.type(null));
        assertValueEqual(BuiltinFunctionName.ADD, expectedType, op1, op2, expression.valueOf(null));
    }

    @ParameterizedTest(name = "subtract({1}, {2})")
    @MethodSource("arithmeticFunctionArguments")
    public void subtract(ExprValue op1, ExprValue op2) {
        FunctionExpression expression = dsl.subtract(literal(op1), literal(op2));
        ExprType expectedType = WideningTypeRule.max(op1.type(), op2.type());
        assertEquals(expectedType, expression.type(null));
        assertValueEqual(BuiltinFunctionName.SUBTRACT, expectedType, op1, op2, expression.valueOf(null));
    }

    @ParameterizedTest(name = "multiply({1}, {2})")
    @MethodSource("arithmeticFunctionArguments")
    public void multiply(ExprValue op1, ExprValue op2) {
        FunctionExpression expression = dsl.multiply(literal(op1), literal(op2));
        ExprType expectedType = WideningTypeRule.max(op1.type(), op2.type());
        assertEquals(expectedType, expression.type(null));
        assertValueEqual(BuiltinFunctionName.MULTIPLY, expectedType, op1, op2, expression.valueOf(null));
    }

    @ParameterizedTest(name = "divide({1}, {2})")
    @MethodSource("arithmeticFunctionArguments")
    public void divide(ExprValue op1, ExprValue op2) {
        FunctionExpression expression = dsl.divide(literal(op1), literal(op2));
        ExprType expectedType = WideningTypeRule.max(op1.type(), op2.type());
        assertEquals(expectedType, expression.type(null));
        assertValueEqual(BuiltinFunctionName.DIVIDE, expectedType, op1, op2, expression.valueOf(null));
    }

    @ParameterizedTest(name = "module({1}, {2})")
    @MethodSource("arithmeticFunctionArguments")
    public void module(ExprValue op1, ExprValue op2) {
        FunctionExpression expression = dsl.module(literal(op1), literal(op2));
        ExprType expectedType = WideningTypeRule.max(op1.type(), op2.type());
        assertEquals(expectedType, expression.type(null));
        assertValueEqual(BuiltinFunctionName.MODULES, expectedType, op1, op2, expression.valueOf(null));
    }

    protected void assertValueEqual(BuiltinFunctionName builtinFunctionName, ExprType type, ExprValue op1,
                                    ExprValue op2,
                                    ExprValue actual) {
        switch (type) {
            case INTEGER:
                Integer vi1 = ExprValueUtils.getIntegerValue(op1);
                Integer vi2 = ExprValueUtils.getIntegerValue(op2);
                Integer viActual = ExprValueUtils.getIntegerValue(actual);
                switch (builtinFunctionName) {
                    case ADD:
                        assertEquals(vi1 + vi2, viActual);
                        return;
                    case SUBTRACT:
                        assertEquals(vi1 - vi2, viActual);
                        return;
                    case DIVIDE:
                        assertEquals(vi1 / vi2, viActual);
                        return;
                    case MULTIPLY:
                        assertEquals(vi1 * vi2, viActual);
                        return;
                    case MODULES:
                        assertEquals(vi1 % vi2, viActual);
                        return;
                    default:
                        throw new IllegalStateException("illegal function name " + builtinFunctionName);
                }
            case LONG:
                Long vl1 = ExprValueUtils.getLongValue(op1);
                Long vl2 = ExprValueUtils.getLongValue(op2);
                Long vlActual = ExprValueUtils.getLongValue(actual);
                switch (builtinFunctionName) {
                    case ADD:
                        assertEquals(vl1 + vl2, vlActual);
                        return;
                    case SUBTRACT:
                        assertEquals(vl1 - vl2, vlActual);
                        return;
                    case DIVIDE:
                        assertEquals(vl1 / vl2, vlActual);
                        return;
                    case MULTIPLY:
                        assertEquals(vl1 * vl2, vlActual);
                        return;
                    case MODULES:
                        assertEquals(vl1 % vl2, vlActual);
                        return;
                    default:
                        throw new IllegalStateException("illegal function name " + builtinFunctionName);
                }
            case FLOAT:
                Float vf1 = ExprValueUtils.getFloatValue(op1);
                Float vf2 = ExprValueUtils.getFloatValue(op2);
                Float vfActual = ExprValueUtils.getFloatValue(actual);
                switch (builtinFunctionName) {
                    case ADD:
                        assertEquals(vf1 + vf2, vfActual);
                        return;
                    case SUBTRACT:
                        assertEquals(vf1 - vf2, vfActual);
                        return;
                    case DIVIDE:
                        assertEquals(vf1 / vf2, vfActual);
                        return;
                    case MULTIPLY:
                        assertEquals(vf1 * vf2, vfActual);
                        return;
                    case MODULES:
                        assertEquals(vf1 % vf2, vfActual);
                        return;
                    default:
                        throw new IllegalStateException("illegal function name " + builtinFunctionName);
                }
            case DOUBLE:
                Double vd1 = ExprValueUtils.getDoubleValue(op1);
                Double vd2 = ExprValueUtils.getDoubleValue(op2);
                Double vdActual = ExprValueUtils.getDoubleValue(actual);
                switch (builtinFunctionName) {
                    case ADD:
                        assertEquals(vd1 + vd2, vdActual);
                        return;
                    case SUBTRACT:
                        assertEquals(vd1 - vd2, vdActual);
                        return;
                    case DIVIDE:
                        assertEquals(vd1 / vd2, vdActual);
                        return;
                    case MULTIPLY:
                        assertEquals(vd1 * vd2, vdActual);
                        return;
                    case MODULES:
                        assertEquals(vd1 % vd2, vdActual);
                        return;
                    default:
                        throw new IllegalStateException("illegal function name " + builtinFunctionName);
                }
        }
    }
}