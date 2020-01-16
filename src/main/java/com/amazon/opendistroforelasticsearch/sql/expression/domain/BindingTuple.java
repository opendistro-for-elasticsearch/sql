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

package com.amazon.opendistroforelasticsearch.sql.expression.domain;


import com.amazon.opendistroforelasticsearch.sql.expression.model.ExprMissingValue;
import com.amazon.opendistroforelasticsearch.sql.expression.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.expression.model.ExprValueFactory;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Singular;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * BindingTuple represents the a relationship between bindingName and ExprValue.
 * e.g. The operation output column name is bindingName, the value is the ExprValue.
 */
@Builder
@Getter
@EqualsAndHashCode
public class BindingTuple {
    @Singular("binding")
    private Map<String, ExprValue> bindingMap;

    /**
     * Resolve the Binding Name in BindingTuple context.
     * @param bindingName binding name.
     * @return binding value.
     */
    public ExprValue resolve(String bindingName) {
        return bindingMap.getOrDefault(bindingName, new ExprMissingValue());
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("<");
        final List<String> list = bindingMap.entrySet()
                .stream()
                .map(entry -> String.format("%s:%s", entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        sb.append(String.join(",", list));
        sb.append('>');
        return sb.toString();
    }

    public static BindingTuple from(Map<String, Object> map) {
        Map<String, ExprValue> ssValueMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            ssValueMap.put(entry.getKey(), ExprValueFactory.from(entry.getValue()));
        }
        return BindingTuple.builder()
                .bindingMap(ssValueMap)
                .build();
    }

    public static BindingTuple from(JSONObject json) {
        Map<String, ExprValue> valueMap = new HashMap<>();
        for (String s : json.keySet()) {
            valueMap.put(s, ExprValueFactory.from(json.get(s)));
        }
        return new BindingTuple(valueMap);
    }
}
