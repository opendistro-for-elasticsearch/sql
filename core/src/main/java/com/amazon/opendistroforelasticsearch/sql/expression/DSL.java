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

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public class DSL {
    private final BuiltinFunctionRepository repository;

    public static LiteralExpression literal(ExprValue value) {
        return new LiteralExpression(value);
    }

    public static ReferenceExpression ref(String ref) {
        return new ReferenceExpression(ref);
    }

    public FunctionExpression add(Expression... expressions) {
        return repository.compile(BuiltinFunctionName.ADD.getName(), Arrays.asList(expressions));
    }

    public FunctionExpression subtract(Expression... expressions) {
        return repository.compile(BuiltinFunctionName.SUBTRACT.getName(), Arrays.asList(expressions));
    }

    public FunctionExpression multiply(Expression... expressions) {
        return repository.compile(BuiltinFunctionName.MULTIPLY.getName(), Arrays.asList(expressions));
    }

    public FunctionExpression divide(Expression... expressions) {
        return repository.compile(BuiltinFunctionName.DIVIDE.getName(), Arrays.asList(expressions));
    }

    public FunctionExpression module(Expression... expressions) {
        return repository.compile(BuiltinFunctionName.MODULES.getName(), Arrays.asList(expressions));
    }

    public FunctionExpression and(Expression... expressions) {
        return repository.compile(BuiltinFunctionName.AND.getName(), Arrays.asList(expressions));
    }

    public FunctionExpression or(Expression... expressions) {
        return repository.compile(BuiltinFunctionName.OR.getName(), Arrays.asList(expressions));
    }

    public FunctionExpression xor(Expression... expressions) {
        return repository.compile(BuiltinFunctionName.XOR.getName(), Arrays.asList(expressions));
    }

    public FunctionExpression not(Expression... expressions) {
        return repository.compile(BuiltinFunctionName.NOT.getName(), Arrays.asList(expressions));
    }

    public FunctionExpression equal(Expression... expressions) {
        return repository.compile(BuiltinFunctionName.EQUAL.getName(), Arrays.asList(expressions));
    }
}
