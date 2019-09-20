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

import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType.NUMBER;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.Generic.T;

/**
 * Scalar SQL function
 */
public enum ScalarFunction implements TypeExpression {

    //ABS(func(NUMBER).to(argTypes -> argTypes[0])),

    ABS(func(T(NUMBER)).to(T)),

    ACOS(func(NUMBER).to(argTypes -> argTypes[0])),
    ASIN(func(NUMBER).to(argTypes -> argTypes[0])),
    ATAN(func(NUMBER).to(argTypes -> argTypes[0])),
    ATAN2(func(NUMBER).to(argTypes -> argTypes[0])),
    CBRT(func(NUMBER).to(argTypes -> argTypes[0])),
    CEIL(func(NUMBER).to(argTypes -> argTypes[0])),
    CONCAT(func(NUMBER).to(argTypes -> argTypes[0])),
    CONCAT_WS(func(NUMBER).to(argTypes -> argTypes[0])),
    COS(func(NUMBER).to(argTypes -> argTypes[0])),
    COSH(func(NUMBER).to(argTypes -> argTypes[0])),
    DATE_FORMAT(func(NUMBER).to(argTypes -> argTypes[0])),
    DEGREES(func(NUMBER).to(argTypes -> argTypes[0])),
    E(func().to(argTyps -> DOUBLE)),
    EXP(func(NUMBER).to(argTypes -> argTypes[0])),
    EXPM1(func(NUMBER).to(argTypes -> argTypes[0])),
    FLOOR(func(NUMBER).to(argTypes -> argTypes[0])),

    LOG(func(T(NUMBER)).to(T)),

    LOG2(func(NUMBER).to(argTypes -> argTypes[0])),
    LOG10(func(NUMBER).to(argTypes -> argTypes[0])),
    LOWER(func(STRING).to(argTypes -> argTypes[0])),
    PI(func().to(argTypes -> DOUBLE)),
    POW(func(NUMBER).to(argTypes -> argTypes[0])),
    RADIANS(func(NUMBER).to(argTypes -> argTypes[0])),
    RANDOM(func(NUMBER).to(argTypes -> argTypes[0])),
    RINT(func(NUMBER).to(argTypes -> argTypes[0])),
    ROUND(func(NUMBER).to(argTypes -> argTypes[0])),
    SIN(func(NUMBER).to(argTypes -> argTypes[0])),
    SINH(func(NUMBER).to(argTypes -> argTypes[0])),
    SQRT(func(NUMBER).to(argTypes -> argTypes[0])),
    TAN(func(NUMBER).to(argTypes -> argTypes[0])),
    UPPER(func(STRING).to(argTypes -> argTypes[0]));


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
