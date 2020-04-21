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

package com.amazon.opendistroforelasticsearch.sql.expression.function;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.google.common.collect.ImmutableMap;
import lombok.experimental.UtilityClass;

import java.util.Map;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprType.ARRAY;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprType.BOOLEAN;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprType.FLOAT;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprType.LONG;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprType.MISSING;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprType.STRUCT;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprType.UNKNOWN;

/**
 * The definition of widening type rule for expression value.
 * ExprType     Widens to data types
 * INTEGER      LONG, FLOAT, DOUBLE
 * LONG         FLOAT, DOUBLE
 * FLOAT        DOUBLE
 * DOUBLE       DOUBLE
 * STRING       STRING
 * BOOLEAN      BOOLEAN
 * ARRAY        ARRAY
 * STRUCT       STRUCT
 */
@UtilityClass
public class WideningTypeRule {
    public static final int IMPOSSIBLE_WIDENING = Integer.MAX_VALUE;
    public static final int TYPE_EQUAL = 0;

    private static final Map<ExprType, ExprType> typeToWidenParent;

    static {
        ImmutableMap.Builder<ExprType, ExprType> builder = new ImmutableMap.Builder<>();
        builder.put(INTEGER, LONG);
        builder.put(LONG, FLOAT);
        builder.put(FLOAT, DOUBLE);
        builder.put(DOUBLE, UNKNOWN);
        builder.put(STRING, UNKNOWN);
        builder.put(BOOLEAN, UNKNOWN);
        builder.put(ARRAY, UNKNOWN);
        builder.put(STRUCT, UNKNOWN);
        builder.put(MISSING, UNKNOWN);
        typeToWidenParent = builder.build();
    }

    /**
     * The widening distance is calculated from the leaf to root.
     * e.g. distance(INTEGER, FLOAT) = 2, but distance(FLOAT, INTEGER) =
     *
     * @param type1 widen from type
     * @param type2 widen to type
     * @return The widening distance when widen one type to another type.
     */
    public static int distance(ExprType type1, ExprType type2) {
        return distance(type1, type2, TYPE_EQUAL);
    }

    private static int distance(ExprType type1, ExprType type2, int distance) {
        if (type1 == UNKNOWN) {
            return IMPOSSIBLE_WIDENING;
        } else if (type1 == type2) {
            return distance;
        } else {
            return distance(typeToWidenParent.get(type1), type2, distance + 1);
        }
    }

    /**
     * The max type among two types. The max is defined as follow
     * if type1 could widen to type2, then max is type2, vice versa
     * if type1 could't widen to type2 and type2 could't widen to type1, then throw {@link ExpressionEvaluationException}
     *
     * @param type1 type1
     * @param type2 type2
     * @return the max type among two types.
     */
    public static ExprType max(ExprType type1, ExprType type2) {
        int type1To2 = distance(type1, type2);
        int type2To1 = distance(type2, type1);

        if (type1To2 == Integer.MAX_VALUE && type2To1 == Integer.MAX_VALUE) {
            throw new ExpressionEvaluationException(String.format("no max type of %s and %s ", type1, type2));
        } else {
            return type1To2 == Integer.MAX_VALUE ? type1 : type2;
        }
    }
}
