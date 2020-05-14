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

package com.amazon.opendistroforelasticsearch.sql.expression;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.Aggregator;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public class DSL {
    private final BuiltinFunctionRepository repository;

    public static LiteralExpression literal(Integer value) {
        return new LiteralExpression(ExprValueUtils.integerValue(value));
    }

    public static LiteralExpression literal(ExprValue value) {
        return new LiteralExpression(value);
    }

    public static ReferenceExpression ref(String ref) {
        return new ReferenceExpression(ref);
    }

    public FunctionExpression add(Environment<Expression, ExprType> env, Expression... expressions) {
        return (FunctionExpression) repository.compile(BuiltinFunctionName.ADD.getName(), Arrays.asList(expressions), env);
    }

    public FunctionExpression subtract(Environment<Expression, ExprType> env, Expression... expressions) {
        return (FunctionExpression) repository.compile(BuiltinFunctionName.SUBTRACT.getName(), Arrays.asList(expressions), env);
    }

    public FunctionExpression multiply(Environment<Expression, ExprType> env, Expression... expressions) {
        return (FunctionExpression) repository.compile(BuiltinFunctionName.MULTIPLY.getName(), Arrays.asList(expressions), env);
    }

    public FunctionExpression divide(Environment<Expression, ExprType> env, Expression... expressions) {
        return (FunctionExpression) repository.compile(BuiltinFunctionName.DIVIDE.getName(), Arrays.asList(expressions), env);
    }

    public FunctionExpression module(Environment<Expression, ExprType> env, Expression... expressions) {
        return (FunctionExpression) repository.compile(BuiltinFunctionName.MODULES.getName(), Arrays.asList(expressions), env);
    }

    public FunctionExpression and(Environment<Expression, ExprType> env, Expression... expressions) {
        return (FunctionExpression) repository.compile(BuiltinFunctionName.AND.getName(), Arrays.asList(expressions), env);
    }

    public FunctionExpression or(Environment<Expression, ExprType> env, Expression... expressions) {
        return (FunctionExpression) repository.compile(BuiltinFunctionName.OR.getName(), Arrays.asList(expressions), env);
    }

    public FunctionExpression xor(Environment<Expression, ExprType> env, Expression... expressions) {
        return (FunctionExpression) repository.compile(BuiltinFunctionName.XOR.getName(), Arrays.asList(expressions), env);
    }

    public FunctionExpression not(Environment<Expression, ExprType> env, Expression... expressions) {
        return (FunctionExpression) repository.compile(BuiltinFunctionName.NOT.getName(), Arrays.asList(expressions), env);
    }

    public FunctionExpression equal(Environment<Expression, ExprType> env, Expression... expressions) {
        return (FunctionExpression) repository.compile(BuiltinFunctionName.EQUAL.getName(), Arrays.asList(expressions), env);
    }

    public Aggregator avg(Environment<Expression, ExprType> env, Expression... expressions) {
        return (Aggregator) repository.compile(BuiltinFunctionName.AVG.getName(), Arrays.asList(expressions), env);
    }

    public Aggregator sum(Environment<Expression, ExprType> env, Expression... expressions) {
        return (Aggregator) repository.compile(BuiltinFunctionName.SUM.getName(), Arrays.asList(expressions), env);
    }
}
