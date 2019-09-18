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

package com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types;

import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType.NUMBER;

/**
 * Scalar SQL function
 */
public enum ScalarFunction implements TypeExpression {

    LOG(func(NUMBER).to(argTypes -> argTypes[0]));

    private final TypeExpressionSpec spec;

    ScalarFunction(TypeExpressionSpec spec) {
        this.spec = spec;
    }

    @Override
    public TypeExpressionSpec spec() {
        return spec;
    }

    private static TypeExpressionSpec func(Type... argTypes) {
        return new TypeExpressionSpec().map(argTypes);
    }

}
