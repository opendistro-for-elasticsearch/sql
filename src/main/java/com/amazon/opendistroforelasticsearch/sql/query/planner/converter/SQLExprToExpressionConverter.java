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

package com.amazon.opendistroforelasticsearch.sql.query.planner.converter;

import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOpExpr;
import com.alibaba.druid.sql.ast.expr.SQLBinaryOperator;
import com.alibaba.druid.sql.ast.expr.SQLCastExpr;
import com.alibaba.druid.sql.ast.expr.SQLMethodInvokeExpr;
import com.amazon.opendistroforelasticsearch.sql.expression.core.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.core.ExpressionFactory;
import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.amazon.opendistroforelasticsearch.sql.expression.core.ExpressionFactory.cast;
import static com.amazon.opendistroforelasticsearch.sql.expression.core.ExpressionFactory.log;

/**
 * The definition of {@link SQLExpr} to {@link Expression} converter.
 */
@RequiredArgsConstructor
public class SQLExprToExpressionConverter {
    private final SQLAggregationParser.Context context;

    /**
     * Convert the {@link SQLExpr} to {@link Expression}
     * @param expr {@link SQLExpr}
     * @return expression {@link Expression}
     */
    public Expression convert(SQLExpr expr) {
        Optional<Expression> resolvedExpression = context.resolve(expr);
        if (resolvedExpression.isPresent()) {
            return resolvedExpression.get();
        } else {
            if (expr instanceof SQLBinaryOpExpr) {
                return binaryOperatorToExpression((SQLBinaryOpExpr) expr, this::convert);
            } else if (expr instanceof SQLMethodInvokeExpr) {
                return methodToExpression((SQLMethodInvokeExpr) expr, this::convert);
            } else if (expr instanceof SQLCastExpr) {
                return cast(convert(((SQLCastExpr) expr).getExpr()));
            } else {
                throw new RuntimeException("unsupported expr: " + expr);
            }
        }
    }

    private Expression binaryOperatorToExpression(SQLBinaryOpExpr expr,
                                                        Function<SQLExpr, Expression> converter) {
        return BinaryExpression.of(expr.getOperator())
                               .map(binaryExpression -> binaryExpression.getExpressionBuilder()
                                                                        .apply(converter.apply((expr.getLeft())),
                                                                               converter.apply(expr.getRight())))
                               .orElseThrow(() -> new RuntimeException(
                                       "unsupported operator in select: " + (expr.getOperator())));
    }

    private Expression methodToExpression(SQLMethodInvokeExpr expr, Function<SQLExpr, Expression> converter) {
        if (("log").equalsIgnoreCase(expr.getMethodName())) {
            return log(converter.apply(expr.getParameters().get(0)));
        } else {
            throw new RuntimeException("unsupported operator in select: " + expr.getMethodName());
        }
    }

    @Getter
    @RequiredArgsConstructor
    private enum BinaryExpression {
        ADD(SQLBinaryOperator.Add, ExpressionFactory::add),
        SUB(SQLBinaryOperator.Subtract, ExpressionFactory::sub);

        private final SQLBinaryOperator sqlBinaryOperator;
        private final BiFunction<Expression, Expression, Expression> expressionBuilder;

        private static final Map<SQLBinaryOperator, BinaryExpression> BINARY_OPERATOR_MAP;

        static {
            ImmutableMap.Builder<SQLBinaryOperator, BinaryExpression> builder = new ImmutableMap.Builder<>();
            for (BinaryExpression operator : BinaryExpression.values()) {
                builder.put(operator.getSqlBinaryOperator(), operator);
            }
            BINARY_OPERATOR_MAP = builder.build();
        }

        public static Optional<BinaryExpression> of(SQLBinaryOperator operator) {
            return Optional.ofNullable(BINARY_OPERATOR_MAP.get(operator));
        }
    }
}