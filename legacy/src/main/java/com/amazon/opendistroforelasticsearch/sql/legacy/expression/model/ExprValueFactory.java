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

package com.amazon.opendistroforelasticsearch.sql.legacy.expression.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The definition of {@link ExprValue} factory.
 */
public class ExprValueFactory {

    public static ExprValue booleanValue(Boolean value) {
        return new ExprBooleanValue(value);
    }

    public static ExprValue integerValue(Integer value) {
        return new ExprIntegerValue(value);
    }

    public static ExprValue doubleValue(Double value) {
        return new ExprDoubleValue(value);
    }

    public static ExprValue stringValue(String value) {
        return new ExprStringValue(value);
    }

    public static ExprValue longValue(Long value) {
        return new ExprLongValue(value);
    }

    public static ExprValue tupleValue(Map<String, Object> map) {
        Map<String, ExprValue> valueMap = new HashMap<>();
        map.forEach((k, v) -> valueMap.put(k, from(v)));
        return new ExprTupleValue(valueMap);
    }

    public static ExprValue collectionValue(List<Object> list) {
        List<ExprValue> valueList = new ArrayList<>();
        list.forEach(o -> valueList.add(from(o)));
        return new ExprCollectionValue(valueList);
    }

    public static ExprValue from(Object o) {
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
        } else {
            throw new IllegalStateException("unsupported type " + o.getClass());
        }
    }
}
