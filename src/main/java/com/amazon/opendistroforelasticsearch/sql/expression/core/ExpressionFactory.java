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


import com.amazon.opendistroforelasticsearch.sql.expression.core.expression.ExpressionBuilder;
import com.amazon.opendistroforelasticsearch.sql.expression.core.scalar.ArithmeticFunctionFactory;
import com.amazon.opendistroforelasticsearch.sql.expression.domain.BindingTuple;
import com.amazon.opendistroforelasticsearch.sql.expression.model.ExprValue;
import com.google.common.collect.ImmutableMap;

import java.util.List;
import java.util.Map;

import static com.amazon.opendistroforelasticsearch.sql.expression.core.ScalarOperation.ABS;
import static com.amazon.opendistroforelasticsearch.sql.expression.core.ScalarOperation.ACOS;
import static com.amazon.opendistroforelasticsearch.sql.expression.core.ScalarOperation.ADD;
import static com.amazon.opendistroforelasticsearch.sql.expression.core.ScalarOperation.ASIN;
import static com.amazon.opendistroforelasticsearch.sql.expression.core.ScalarOperation.ATAN;
import static com.amazon.opendistroforelasticsearch.sql.expression.core.ScalarOperation.ATAN2;
import static com.amazon.opendistroforelasticsearch.sql.expression.core.ScalarOperation.CBRT;
import static com.amazon.opendistroforelasticsearch.sql.expression.core.ScalarOperation.CEIL;
import static com.amazon.opendistroforelasticsearch.sql.expression.core.ScalarOperation.COS;
import static com.amazon.opendistroforelasticsearch.sql.expression.core.ScalarOperation.COSH;
import static com.amazon.opendistroforelasticsearch.sql.expression.core.ScalarOperation.DIVIDE;
import static com.amazon.opendistroforelasticsearch.sql.expression.core.ScalarOperation.EXP;
import static com.amazon.opendistroforelasticsearch.sql.expression.core.ScalarOperation.FLOOR;
import static com.amazon.opendistroforelasticsearch.sql.expression.core.ScalarOperation.LN;
import static com.amazon.opendistroforelasticsearch.sql.expression.core.ScalarOperation.LOG;
import static com.amazon.opendistroforelasticsearch.sql.expression.core.ScalarOperation.LOG10;
import static com.amazon.opendistroforelasticsearch.sql.expression.core.ScalarOperation.LOG2;
import static com.amazon.opendistroforelasticsearch.sql.expression.core.ScalarOperation.MODULES;
import static com.amazon.opendistroforelasticsearch.sql.expression.core.ScalarOperation.MULTIPLY;
import static com.amazon.opendistroforelasticsearch.sql.expression.core.ScalarOperation.SUBTRACT;


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
     * Ref Expression
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

    public static Expression constant(ExprValue value) {
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
     * Cast Expression
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
