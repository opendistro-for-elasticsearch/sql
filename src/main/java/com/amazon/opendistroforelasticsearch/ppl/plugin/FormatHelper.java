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

package com.amazon.opendistroforelasticsearch.ppl.plugin;

import com.amazon.opendistroforelasticsearch.sql.expression.domain.BindingTuple;
import com.amazon.opendistroforelasticsearch.sql.expression.model.ExprBooleanValue;
import com.amazon.opendistroforelasticsearch.sql.expression.model.ExprDoubleValue;
import com.amazon.opendistroforelasticsearch.sql.expression.model.ExprIntegerValue;
import com.amazon.opendistroforelasticsearch.sql.expression.model.ExprLongValue;
import com.amazon.opendistroforelasticsearch.sql.expression.model.ExprStringValue;
import com.amazon.opendistroforelasticsearch.sql.expression.model.ExprTupleValue;
import com.amazon.opendistroforelasticsearch.sql.expression.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.utils.JsonPrettyFormatter;
import lombok.SneakyThrows;
import org.json.JSONArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FormatHelper {

    @SneakyThrows
    public static String jsonFormat(List<BindingTuple> bindingTuples) {
        List<Map<String, Object>> rowList = bindingTuples.stream().map(tuple -> {
            Map<String, ExprValue> bindingMap = tuple.getBindingMap();
            Map<String, Object> rowMap = new HashMap<>();
            for (String s : bindingMap.keySet()) {
                rowMap.put(s, to(bindingMap.get(s)));
            }
            return rowMap;
        }).collect(Collectors.toList());
        return JsonPrettyFormatter.format(new JSONArray(rowList).toString());
    }

    public static Map<String, Object> tupleValue(Map<String, ExprValue> map) {
        Map<String, Object> valueMap = new HashMap<>();
        map.forEach((k, v) -> valueMap.put(k, to(v)));
        return valueMap;
    }

    public static Object to(ExprValue o) {
        if (o instanceof ExprTupleValue) {
            return tupleValue((Map) o.value());
        }  else if (o instanceof ExprIntegerValue) {
            return o.value();
        } else if (o instanceof ExprLongValue) {
            return o.value();
        } else if (o instanceof ExprBooleanValue) {
            return o.value();
        } else if (o instanceof ExprDoubleValue) {
            return o.value();
        } else if (o instanceof ExprStringValue) {
            return o.value();
        } else {
            throw new IllegalStateException("unsupported type " + o.getClass());
        }
    }
}
