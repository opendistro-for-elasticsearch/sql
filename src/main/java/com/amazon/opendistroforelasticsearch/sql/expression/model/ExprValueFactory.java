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

package com.amazon.opendistroforelasticsearch.sql.expression.model;

import org.json.JSONArray;
import org.json.JSONObject;

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

    public static ExprValue longValue(Long value) {
        return new ExprLongValue(value);
    }

    public static ExprValue floatValue(Float value) {
        return new ExprFloatValue(value);
    }

    public static ExprValue doubleValue(Double value) {
        return new ExprDoubleValue(value);
    }

    public static ExprValue stringValue(String value) {
        return new ExprStringValue(value);
    }


    public static ExprValue tupleValue(JSONObject jsonObject) {
        Map<String, ExprValue> valueMap = new HashMap<>();
        for (String s : jsonObject.keySet()) {
            valueMap.put(s, from(jsonObject.get(s)));
        }
        return new ExprTupleValue(valueMap);
    }

    public static ExprValue collectionValue(JSONArray array) {
        List<ExprValue> valueList = new ArrayList<>();
        for (Object o : array) {
            valueList.add(from(o));
        }
        return new ExprCollectionValue(valueList);
    }

    public static ExprValue from(Object o) {
        if (o instanceof JSONObject) {
            return tupleValue((JSONObject) o);
        } else if (o instanceof JSONArray) {
            return collectionValue(((JSONArray) o));
        } else if (o instanceof Integer) {
            return integerValue((Integer) o);
        } else if (o instanceof Long) {
            return longValue((Long) o);
        } else if (o instanceof Float) {
            return floatValue((Float) o);
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
