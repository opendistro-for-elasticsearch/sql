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

package com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.function;

import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.Type;
import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.TypeExpression;

import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.ES_TYPE;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.NUMBER;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.special.Generic.T;

/**
 * Aggregate function
 */
public enum AggregateFunction implements TypeExpression {
    COUNT(
        func().to(INTEGER), // COUNT(*)
        func(ES_TYPE).to(INTEGER)
    ),
    MAX(func(T(NUMBER)).to(T)),
    MIN(func(T(NUMBER)).to(T)),
    AVG(func(T(NUMBER)).to(DOUBLE)),
    SUM(func(T(NUMBER)).to(T));

    private TypeExpressionSpec[] specifications;

    AggregateFunction(TypeExpressionSpec... specifications) {
        this.specifications = specifications;
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public TypeExpressionSpec[] specifications() {
        return specifications;
    }

    private static TypeExpressionSpec func(Type... argTypes) {
        return new TypeExpressionSpec().map(argTypes);
    }

    @Override
    public String toString() {
        return "Function [" + name() + "]";
    }
}
