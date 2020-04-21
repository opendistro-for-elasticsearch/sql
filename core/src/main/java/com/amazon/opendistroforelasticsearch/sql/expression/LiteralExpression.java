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
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import lombok.RequiredArgsConstructor;

/**
 * Literal Expression
 */
@RequiredArgsConstructor
public class LiteralExpression implements Expression {
    private final ExprValue exprValue;

    @Override
    public ExprValue valueOf(Environment<Expression, ExprValue> env) {
        return exprValue;
    }

    @Override
    public ExprType type(Environment<Expression, ExprType> env) {
        return exprValue.type();
    }
}
