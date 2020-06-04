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

package com.amazon.opendistroforelasticsearch.sql.legacy.expression.model;

import com.google.common.annotations.VisibleForTesting;

import java.util.List;
import java.util.Map;

import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValue.ExprValueKind.BOOLEAN_VALUE;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValue.ExprValueKind.COLLECTION_VALUE;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValue.ExprValueKind.STRING_VALUE;
import static com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValue.ExprValueKind.TUPLE_VALUE;

/**
 * The definition of ExprValue Utils.
 */
public class ExprValueUtils {
    public static Integer getIntegerValue(ExprValue exprValue) {
        return getNumberValue(exprValue).intValue();
    }

    public static Double getDoubleValue(ExprValue exprValue) {
        return getNumberValue(exprValue).doubleValue();
    }

    public static Long getLongValue(ExprValue exprValue) {
        return getNumberValue(exprValue).longValue();
    }

    public static Float getFloatValue(ExprValue exprValue) {
        return getNumberValue(exprValue).floatValue();
    }

    public static String getStringValue(ExprValue exprValue) {
        return convert(exprValue, STRING_VALUE);
    }

    public static List<ExprValue> getCollectionValue(ExprValue exprValue) {
        return convert(exprValue, COLLECTION_VALUE);
    }

    public static Map<String, ExprValue> getTupleValue(ExprValue exprValue) {
        return convert(exprValue, TUPLE_VALUE);
    }

    public static Boolean getBooleanValue(ExprValue exprValue) {
        return convert(exprValue, BOOLEAN_VALUE);
    }

    @VisibleForTesting
    public static Number getNumberValue(ExprValue exprValue) {
        switch (exprValue.kind()) {
            case INTEGER_VALUE:
            case DOUBLE_VALUE:
            case LONG_VALUE:
            case FLOAT_VALUE:
                return (Number) exprValue.value();
            default:
                break;
        }
        throw new IllegalStateException(
                String.format("invalid to get NUMBER_VALUE from expr type of %s", exprValue.kind()));
    }

    @SuppressWarnings("unchecked")
    private static <T> T convert(ExprValue exprValue, ExprValue.ExprValueKind toType) {
        if (exprValue.kind() == toType) {
            return (T) exprValue.value();
        } else {
            throw new IllegalStateException(
                    String.format("invalid to get %s from expr type of %s", toType, exprValue.kind()));
        }
    }
}
