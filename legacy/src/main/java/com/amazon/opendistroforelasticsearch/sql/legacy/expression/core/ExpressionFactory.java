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

package com.amazon.opendistroforelasticsearch.sql.legacy.expression.core;


import com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.builder.ExpressionBuilder;
import com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.builder.ArithmeticFunctionFactory;
import com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator.ScalarOperation;
import com.amazon.opendistroforelasticsearch.sql.legacy.expression.domain.BindingTuple;
import com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValue;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;

import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator.ScalarOperation.ABS;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator.ScalarOperation.ACOS;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator.ScalarOperation.ADD;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator.ScalarOperation.ASIN;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator.ScalarOperation.ATAN;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator.ScalarOperation.ATAN2;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator.ScalarOperation.CBRT;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator.ScalarOperation.CEIL;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator.ScalarOperation.COS;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator.ScalarOperation.COSH;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator.ScalarOperation.DIVIDE;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator.ScalarOperation.EXP;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator.ScalarOperation.FLOOR;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator.ScalarOperation.LN;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator.ScalarOperation.LOG;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator.ScalarOperation.LOG10;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator.ScalarOperation.LOG2;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator.ScalarOperation.MODULES;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator.ScalarOperation.MULTIPLY;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator.ScalarOperation.SUBTRACT;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.core.operator.ScalarOperation.TAN;


/**
 * The definition of Expression factory.
 */
public class ExpressionFactory {

    private static final Map<ScalarOperation, ExpressionBuilder> operationExpressionBuilderMap =
            new ImmutableMap.Builder<ScalarOperation, ExpressionBuilder>()
                    .put(ADD, ArithmeticFunctionFactory.add())
                    .put(SUBTRACT, ArithmeticFunctionFactory.subtract())
                    .put(MULTIPLY, ArithmeticFunctionFactory.multiply())
                    .put(DIVIDE, ArithmeticFunctionFactory.divide())
                    .put(MODULES, ArithmeticFunctionFactory.modules())
                    .put(ABS, ArithmeticFunctionFactory.abs())
                    .put(ACOS, ArithmeticFunctionFactory.acos())
                    .put(ASIN, ArithmeticFunctionFactory.asin())
                    .put(ATAN, ArithmeticFunctionFactory.atan())
                    .put(ATAN2, ArithmeticFunctionFactory.atan2())
                    .put(TAN, ArithmeticFunctionFactory.tan())
                    .put(CBRT, ArithmeticFunctionFactory.cbrt())
                    .put(CEIL, ArithmeticFunctionFactory.ceil())
                    .put(COS, ArithmeticFunctionFactory.cos())
                    .put(COSH, ArithmeticFunctionFactory.cosh())
                    .put(EXP, ArithmeticFunctionFactory.exp())
                    .put(FLOOR, ArithmeticFunctionFactory.floor())
                    .put(LN, ArithmeticFunctionFactory.ln())
                    .put(LOG, ArithmeticFunctionFactory.log())
                    .put(LOG2, ArithmeticFunctionFactory.log2())
                    .put(LOG10, ArithmeticFunctionFactory.log10())
                    .build();

    public static Expression of(ScalarOperation op, List<Expression> expressions) {
        return operationExpressionBuilderMap.get(op).build(expressions);
    }

    /**
     * Ref Expression. Define the binding name which could be resolved in {@link BindingTuple}
     */
    public static Expression ref(String bindingName) {
        return new Expression() {
            @Override
            public ExprValue valueOf(BindingTuple tuple) {
                return tuple.resolve(bindingName);
            }

            @Override
            public String toString() {
                return String.format("%s", bindingName);
            }
        };
    }

    /**
     * Literal Expression.
     */
    public static Expression literal(ExprValue value) {
        return new Expression() {
            @Override
            public ExprValue valueOf(BindingTuple tuple) {
                return value;
            }

            @Override
            public String toString() {
                return String.format("%s", value);
            }
        };
    }

    /**
     * Cast Expression.
     */
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
}
