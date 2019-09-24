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

import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType.DATE;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.antlr.semantic.types.BaseType.STRING;

public enum ESScalarFunction implements TypeExpression {

    //DATE_HISTOGRAM(esFunc().to()),
    DAY_OF_MONTH(esFunc(DATE).to(INTEGER)),
    DAY_OF_YEAR(esFunc(DATE).to(INTEGER)),
    DAY_OF_WEEK(esFunc(DATE).to(INTEGER)),
    //EXCLUDE(esFunc(STRING).to(STRING)),  // can only be used in SELECT?
    //EXTENDED_STATS(esFunc(NUMBER).to(?)), // need confirm
    //FIELD(esFunc().to()), // couldn't find test cases related
    /*
    FILTER(esFunc().to()),
    GEO_BOUNDING_BOX(esFunc().to()),
    GEO_DISTANCE(esFunc().to()),
    GEO_INTERSETS(esFunc().to()),
    HISTOGRAM(esFunc().to()),
    HOUR_OF_DAY(esFunc().to()),
    INCLUDE(esFunc().to()),
    IN_TERMS(esFunc().to()),
    MATCHPHASE(esFunc().to()),
    MATCH_PHASE(esFunc().to()),
    MATCHQUERY(esFunc().to()),
    MATCH_QUERY(esFunc().to()),
    MINUTE_OF_DAY(esFunc().to()),
    MINUTE_OF_HOUR(esFunc().to()),
    MONTH_OF_YEAR(esFunc().to()),
    MULTIMATCH(esFunc().to()),
    MULTI_MATCH(esFunc().to()),
    NESTED(esFunc().to()),
    PERCENTILES(esFunc().to()),
    REGEXP_QUERY(esFunc().to()),
    REVERSE_NESTED(esFunc().to()),
    QUERY(esFunc().to()),
    RANGE(esFunc().to()),
    SCORE(esFunc().to()),
    SECOND_OF_MINUE(esFunc().to()),
    STATS(esFunc().to()),
    TERM(esFunc().to()),
    TERMS(esFunc().to()),
    TOPHITS(esFunc().to()),
    WEEK_OF_YEAR(esFunc().to()),
    WILDCARDQUERY(esFunc().to()),
    WILDCARD_QUERY(esFunc().to());
     */

    DATE_HISTOGRAM(null);

    private final TypeExpressionSpec spec;

    ESScalarFunction(TypeExpressionSpec spec) {
        this.spec = spec;
    }

    @Override
    public TypeExpressionSpec spec() {
        return spec;
    }

    private static TypeExpressionSpec esFunc(Type... argTypes) {
        return new TypeExpressionSpec().map(argTypes);
    }
}
