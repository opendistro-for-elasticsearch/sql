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
import com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType;

import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.BOOLEAN;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.ES_TYPE;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.NUMBER;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.special.Generic.T;

/**
 * Scalar SQL function
 */
public enum ScalarFunction implements TypeExpression {

    ABS(func(T(NUMBER)).to(T)), // translate to Java: <T extends Number> T ABS(T)
    ACOS(func(T(NUMBER)).to(DOUBLE)),
    ADD(func(T(NUMBER), NUMBER).to(T)),
    ASCII(func(T(STRING)).to(INTEGER)),
    ASIN(func(T(NUMBER)).to(DOUBLE)),
    ATAN(func(T(NUMBER)).to(DOUBLE)),
    ATAN2(func(T(NUMBER), NUMBER).to(DOUBLE)),
    CAST(),
    CBRT(func(T(NUMBER)).to(T)),
    CEIL(func(T(NUMBER)).to(T)),
    CONCAT(), // TODO: varargs support required
    CONCAT_WS(),
    COS(func(T(NUMBER)).to(DOUBLE)),
    COSH(func(T(NUMBER)).to(DOUBLE)),
    COT(func(T(NUMBER)).to(DOUBLE)),
    CURDATE(func().to(ESDataType.DATE)),
    DATE(func(ESDataType.DATE).to(ESDataType.DATE)),
    DATE_FORMAT(
        func(ESDataType.DATE, STRING).to(STRING),
        func(ESDataType.DATE, STRING, STRING).to(STRING)
    ),
    DAYOFMONTH(func(ESDataType.DATE).to(INTEGER)),
    DEGREES(func(T(NUMBER)).to(DOUBLE)),
    DIVIDE(func(T(NUMBER), NUMBER).to(T)),
    E(func().to(DOUBLE)),
    EXP(func(T(NUMBER)).to(T)),
    EXPM1(func(T(NUMBER)).to(T)),
    FLOOR(func(T(NUMBER)).to(T)),
    IF(func(BOOLEAN, ES_TYPE, ES_TYPE).to(ES_TYPE)),
    IFNULL(func(ES_TYPE, ES_TYPE).to(ES_TYPE)),
    ISNULL(func(ES_TYPE).to(INTEGER)),
    LEFT(func(T(STRING), INTEGER).to(T)),
    LENGTH(func(STRING).to(INTEGER)),
    LN(func(T(NUMBER)).to(DOUBLE)),
    LOCATE(
            func(STRING, STRING, INTEGER).to(INTEGER),
            func(STRING, STRING).to(INTEGER)
    ),
    LOG(
        func(T(NUMBER)).to(DOUBLE),
        func(T(NUMBER), NUMBER).to(DOUBLE)
    ),
    LOG2(func(T(NUMBER)).to(DOUBLE)),
    LOG10(func(T(NUMBER)).to(DOUBLE)),
    LOWER(
        func(T(STRING)).to(T),
        func(T(STRING), STRING).to(T)
    ),
    LTRIM(func(T(STRING)).to(T)),
    MAKETIME(func(INTEGER, INTEGER, INTEGER).to(ESDataType.DATE)),
    MODULUS(func(T(NUMBER), NUMBER).to(T)),
    MONTH(func(ESDataType.DATE).to(INTEGER)),
    MONTHNAME(func(ESDataType.DATE).to(STRING)),
    MULTIPLY(func(T(NUMBER), NUMBER).to(NUMBER)),
    NOW(func().to(ESDataType.DATE)),
    PI(func().to(DOUBLE)),
    POW(
            func(T(NUMBER)).to(T),
            func(T(NUMBER), NUMBER).to(T)
    ),
    POWER(
        func(T(NUMBER)).to(T),
        func(T(NUMBER), NUMBER).to(T)
    ),
    RADIANS(func(T(NUMBER)).to(DOUBLE)),
    RAND(
            func().to(NUMBER),
            func(T(NUMBER)).to(T)
    ),
    REPLACE(func(T(STRING), STRING, STRING).to(T)),
    RIGHT(func(T(STRING), INTEGER).to(T)),
    RINT(func(T(NUMBER)).to(T)),
    ROUND(func(T(NUMBER)).to(T)),
    RTRIM(func(T(STRING)).to(T)),
    SIGN(func(T(NUMBER)).to(T)),
    SIGNUM(func(T(NUMBER)).to(T)),
    SIN(func(T(NUMBER)).to(DOUBLE)),
    SINH(func(T(NUMBER)).to(DOUBLE)),
    SQRT(func(T(NUMBER)).to(T)),
    SUBSTRING(func(T(STRING), INTEGER, INTEGER).to(T)),
    SUBTRACT(func(T(NUMBER), NUMBER).to(T)),
    TAN(func(T(NUMBER)).to(DOUBLE)),
    TIMESTAMP(func(ESDataType.DATE).to(ESDataType.DATE)),
    TRIM(func(T(STRING)).to(T)),
    UPPER(
        func(T(STRING)).to(T),
        func(T(STRING), STRING).to(T)
    ),
    YEAR(func(ESDataType.DATE).to(INTEGER));

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
