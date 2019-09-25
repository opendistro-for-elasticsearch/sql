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

import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType.BOOLEAN;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType.DATE;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType.GEO_POINT;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType.NUMBER;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType.STRING;

/**
 * Elasticsearch special scalar functions
 */
public enum ESScalarFunction implements TypeExpression {

    //DATE_HISTOGRAM(esFunc().to()), // this is aggregate function
    DAY_OF_MONTH(func(DATE).to(INTEGER)),
    DAY_OF_YEAR(func(DATE).to(INTEGER)),
    DAY_OF_WEEK(func(DATE).to(INTEGER)),
    //EXCLUDE(esFunc(STRING).to(STRING)),  // can only be used in SELECT?
    //EXTENDED_STATS(esFunc(NUMBER).to(?)), // need confirm
    //FIELD(esFunc().to()), // couldn't find test cases related
    //FILTER(esFunc().to()),
    GEO_BOUNDING_BOX(func(GEO_POINT, NUMBER, NUMBER, NUMBER, NUMBER).to(BOOLEAN)),
    //GEO_CELL() // optional arg or overloaded spec is required.
    GEO_DISTANCE(func(GEO_POINT, STRING, NUMBER, NUMBER).to(BOOLEAN)),
    GEO_DISTANCE_RANGE(func(GEO_POINT, STRING, NUMBER, NUMBER).to(BOOLEAN)),
    //GEO_INTERSECTS(func(GEO_POINT, STRING).to(BOOLEAN)),
    //GEO_POLYGON(esFunc(GEO_POINT, )) // varargs is required for 2nd arg
    //HISTOGRAM(esFunc().to()), // same as date_histogram
    HOUR_OF_DAY(func(DATE).to(INTEGER)),
    //INCLUDE(esFunc().to()), // same as exclude
    //IN_TERMS(esFunc().to()),// varargs
    MATCHPHRASE(func(STRING, STRING).to(BOOLEAN)), //slop arg is optional
    MATCH_PHRASE(MATCHPHRASE.spec()),
    MATCHQUERY(func(STRING, STRING).to(BOOLEAN)),
    MATCH_QUERY(MATCHQUERY.spec()),
    MINUTE_OF_DAY(func(DATE).to(INTEGER)), // or long?
    MINUTE_OF_HOUR(func(DATE).to(INTEGER)),
    MONTH_OF_YEAR(func(DATE).to(INTEGER)),
    //MULTIMATCH(func().to()), // kw arguments
    //MULTI_MATCH(MULTIMATCH.spec()),
    //NESTED(func().to()), // overloaded
    //PERCENTILES(esFunc().to()), //?
    //REGEXP_QUERY(func().to()), //?
    //REVERSE_NESTED(func().to()), // need overloaded
    QUERY(func(STRING).to(BOOLEAN)),
    //RANGE(func().to()), // aggregate function
    //SCORE(esFunc().to()), // semantic problem?
    SECOND_OF_MINUTE(func(DATE).to(INTEGER)),
    //STATS(func().to()),
    //TERM(func(STRING).to()), // semantic problem
    //TERMS(func().to()), // semantic problem
    //TOPHITS(func().to()), // only available in SELECT
    WEEK_OF_YEAR(func(DATE).to(INTEGER)),
    WILDCARDQUERY(func(STRING, STRING).to(BOOLEAN)),
    WILDCARD_QUERY(WILDCARDQUERY.spec());


    private final TypeExpressionSpec spec;

    ESScalarFunction(TypeExpressionSpec spec) {
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
