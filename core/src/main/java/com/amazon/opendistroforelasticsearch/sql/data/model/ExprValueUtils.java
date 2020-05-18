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

package com.amazon.opendistroforelasticsearch.sql.data.model;

import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.google.common.annotations.VisibleForTesting;
import java.util.LinkedHashMap;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprType.ARRAY;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprType.BOOLEAN;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprType.STRUCT;

/**
 * The definition of {@link ExprValue} factory.
 */
@UtilityClass
public class ExprValueUtils {
    public static final ExprValue LITERAL_TRUE = ExprBooleanValue.ofTrue();
    public static final ExprValue LITERAL_FALSE = ExprBooleanValue.ofFalse();
    public static final ExprValue LITERAL_NULL = ExprNullValue.of();
    public static final ExprValue LITERAL_MISSING = ExprMissingValue.of();

    public static ExprValue booleanValue(Boolean value) {
        return value ? LITERAL_TRUE : LITERAL_FALSE;
    }

    public static ExprValue integerValue(Integer value) {
        return new ExprIntegerValue(value);
    }

    public static ExprValue doubleValue(Double value) {
        return new ExprDoubleValue(value);
    }

    public static ExprValue floatValue(Float value) {
        return new ExprFloatValue(value);
    }

    public static ExprValue longValue(Long value) {
        return new ExprLongValue(value);
    }

    public static ExprValue stringValue(String value) {
        return new ExprStringValue(value);
    }

    public static ExprValue tupleValue(Map<String, Object> map) {
        LinkedHashMap<String, ExprValue> valueMap = new LinkedHashMap<>();
        map.forEach((k, v) -> valueMap.put(k, fromObjectValue(v)));
        return new ExprTupleValue(valueMap);
    }

    public static ExprValue collectionValue(List<Object> list) {
        List<ExprValue> valueList = new ArrayList<>();
        list.forEach(o -> valueList.add(fromObjectValue(o)));
        return new ExprCollectionValue(valueList);
    }

    public static ExprValue missingValue() {
        return ExprMissingValue.of();
    }

    public static ExprValue nullValue() {
        return ExprNullValue.of();
    }

    public static ExprValue fromObjectValue(Object o) {
        if ( null == o) {
            return LITERAL_NULL;
        }
        if (o instanceof Map) {
            return tupleValue((Map) o);
        } else if (o instanceof List) {
            return collectionValue(((List) o));
        } else if (o instanceof Integer) {
            return integerValue((Integer) o);
        } else if (o instanceof Long) {
            return longValue(((Long) o));
        } else if (o instanceof Boolean) {
            return booleanValue((Boolean) o);
        } else if (o instanceof Double) {
            return doubleValue((Double) o);
        } else if (o instanceof String) {
            return stringValue((String) o);
        } else if (o instanceof Float) {
            return floatValue((Float) o);
        } else {
            throw new ExpressionEvaluationException("unsupported object " + o.getClass());
        }
    }

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
        return convert(exprValue, STRING);
    }

    public static List<ExprValue> getCollectionValue(ExprValue exprValue) {
        return convert(exprValue, ARRAY);
    }

    public static Map<String, ExprValue> getTupleValue(ExprValue exprValue) {
        return convert(exprValue, STRUCT);
    }

    public static Boolean getBooleanValue(ExprValue exprValue) {
        return convert(exprValue, BOOLEAN);
    }

    @VisibleForTesting
    public static Number getNumberValue(ExprValue exprValue) {
        switch (exprValue.type()) {
            case INTEGER:
            case DOUBLE:
            case LONG:
            case FLOAT:
                return (Number) exprValue.value();
            default:
                break;
        }
        throw new ExpressionEvaluationException(
                String.format("invalid to getNumberValue with expression has type of %s", exprValue.type()));
    }

    @SuppressWarnings("unchecked")
    private static <T> T convert(ExprValue exprValue, ExprType toType) {
        if (exprValue.type() == toType) {
            return (T) exprValue.value();
        } else {
            throw new ExpressionEvaluationException(
                    String.format("invalid to convert expression with type:%s to type:%s", exprValue.type(), toType));
        }
    }
}
