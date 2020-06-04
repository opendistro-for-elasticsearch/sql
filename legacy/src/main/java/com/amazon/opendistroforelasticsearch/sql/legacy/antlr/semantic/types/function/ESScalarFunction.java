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

import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.BOOLEAN;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.DATE;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.GEO_POINT;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.NUMBER;
import static com.amazon.opendistroforelasticsearch.sql.legacy.antlr.semantic.types.base.ESDataType.STRING;

/**
 * Elasticsearch special scalar functions
 */
public enum ESScalarFunction implements TypeExpression {

    DATE_HISTOGRAM(), // this is aggregate function
    DAY_OF_MONTH(func(DATE).to(INTEGER)),
    DAY_OF_YEAR(func(DATE).to(INTEGER)),
    DAY_OF_WEEK(func(DATE).to(INTEGER)),
    EXCLUDE(),  // can only be used in SELECT?
    EXTENDED_STATS(), // need confirm
    FIELD(), // couldn't find test cases related
    FILTER(),
    GEO_BOUNDING_BOX(func(GEO_POINT, NUMBER, NUMBER, NUMBER, NUMBER).to(BOOLEAN)),
    GEO_CELL(), // optional arg or overloaded spec is required.
    GEO_DISTANCE(func(GEO_POINT, STRING, NUMBER, NUMBER).to(BOOLEAN)),
    GEO_DISTANCE_RANGE(func(GEO_POINT, STRING, NUMBER, NUMBER).to(BOOLEAN)),
    GEO_INTERSECTS(), //?
    GEO_POLYGON(), // varargs is required for 2nd arg
    HISTOGRAM(), // same as date_histogram
    HOUR_OF_DAY(func(DATE).to(INTEGER)),
    INCLUDE(), // same as exclude
    IN_TERMS(), // varargs
    MATCHPHRASE(
        func(STRING, STRING).to(BOOLEAN),
        func(STRING).to(STRING)
    ), //slop arg is optional
    MATCH_PHRASE(MATCHPHRASE.specifications()),
    MATCHQUERY(
        func(STRING, STRING).to(BOOLEAN),
        func(STRING).to(STRING)
    ),
    MATCH_QUERY(MATCHQUERY.specifications()),
    MINUTE_OF_DAY(func(DATE).to(INTEGER)), // or long?
    MINUTE_OF_HOUR(func(DATE).to(INTEGER)),
    MONTH_OF_YEAR(func(DATE).to(INTEGER)),
    MULTIMATCH(), // kw arguments
    MULTI_MATCH(MULTIMATCH.specifications()),
    NESTED(), // overloaded
    PERCENTILES(), //?
    REGEXP_QUERY(), //?
    REVERSE_NESTED(), // need overloaded
    QUERY(func(STRING).to(BOOLEAN)),
    RANGE(), // aggregate function
    SCORE(), // semantic problem?
    SECOND_OF_MINUTE(func(DATE).to(INTEGER)),
    STATS(),
    TERM(), // semantic problem
    TERMS(), // semantic problem
    TOPHITS(), // only available in SELECT
    WEEK_OF_YEAR(func(DATE).to(INTEGER)),
    WILDCARDQUERY(
        func(STRING, STRING).to(BOOLEAN),
        func(STRING).to(STRING)
    ),
    WILDCARD_QUERY(WILDCARDQUERY.specifications());


    private final TypeExpressionSpec[] specifications;

    ESScalarFunction(TypeExpressionSpec... specifications) {
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
