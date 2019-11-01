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

package com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.function;

import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.Type;
import com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.TypeExpression;

import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.ESDataType.DATE;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.ESDataType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.ESDataType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.ESDataType.NUMBER;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.base.ESDataType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.special.Generic.T;

/**
 * Scalar SQL function
 */
public enum ScalarFunction implements TypeExpression {

    ABS(func(T(NUMBER)).to(T)), // translate to Java: <T extends Number> T ABS(T)
    ASCII(func(T(STRING)).to(T)),
    ASIN(func(T(NUMBER)).to(T)),
    ATAN(func(T(NUMBER)).to(T)),
    ATAN2(func(T(NUMBER), NUMBER).to(T)),
    CBRT(func(T(NUMBER)).to(T)),
    CEIL(func(T(NUMBER)).to(T)),
    CONCAT(), // TODO: varargs support required
    CONCAT_WS(),
    COS(func(T(NUMBER)).to(T)),
    COSH(func(T(NUMBER)).to(T)),
    COT(func(T(NUMBER)).to(T)),
    DATE_FORMAT(
        func(DATE, STRING).to(STRING),
        func(DATE, STRING, STRING).to(STRING)
    ),
    DEGREES(func(T(NUMBER)).to(T)),
    E(func().to(DOUBLE)),
    EXP(func(T(NUMBER)).to(T)),
    EXPM1(func(T(NUMBER)).to(T)),
    FLOOR(func(T(NUMBER)).to(T)),
    LENGTH(func(STRING).to(INTEGER)
),
    LOCATE(
            func(STRING, STRING, INTEGER).to(INTEGER),
            func(STRING, STRING).to(INTEGER)
    ),
    LOG(
        func(T(NUMBER)).to(T),
        func(T(NUMBER), NUMBER).to(T)
    ),
    LOG2(func(T(NUMBER)).to(T)),
    LOG10(func(T(NUMBER)).to(T)),
    LN(func(T(NUMBER)).to(T)),
    LOWER(
        func(T(STRING)).to(T),
        func(T(STRING), STRING).to(T)
    ),
    LTRIM(func(T(STRING)).to(T)),
    PI(func().to(DOUBLE)),
    POW, POWER(
        func(T(NUMBER)).to(T),
        func(T(NUMBER), NUMBER).to(T)
    ),
    RADIANS(func(T(NUMBER)).to(T)),
    RANDOM(func(T(NUMBER)).to(T)),
    REPLACE(func(T(STRING), STRING, STRING).to(T)),
    RINT(func(T(NUMBER)).to(T)),
    ROUND(func(T(NUMBER)).to(T)),
    RTRIM(func(T(STRING)).to(T)),
    SIGN(func(T(NUMBER)).to(T)),
    SIGNUM(func(T(NUMBER)).to(T)),
    SIN(func(T(NUMBER)).to(T)),
    SINH(func(T(NUMBER)).to(T)),
    SQRT(func(T(NUMBER)).to(T)),
    SUBSTRING(func(T(STRING), INTEGER, INTEGER).to(T)),
    TAN(func(T(NUMBER)).to(T)),
    UPPER(
        func(T(STRING)).to(T),
        func(T(STRING), STRING).to(T)
    ),
    YEAR(func(DATE).to(INTEGER));

    private final TypeExpressionSpec[] specifications;

    ScalarFunction(TypeExpressionSpec... specifications) {
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
