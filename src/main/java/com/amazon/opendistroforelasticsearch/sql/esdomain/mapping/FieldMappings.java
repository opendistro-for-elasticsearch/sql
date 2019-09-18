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

package com.amazon.opendistroforelasticsearch.sql.esdomain.mapping;

import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Field mappings in a specific type.
 * <p>
 * Sample:
 * fieldMappings: {
 * 'properties': {
 * 'balance': {
 * 'type': long
 * },
 * 'age': {
 * 'type': integer
 * },
 * 'state': {
 * 'type': text，
 * }
 * 'name': {
 * 'type': text，
 * 'fields': {
 * 'keyword': {
 * 'type': keyword,
 * 'ignore_above': 256
 * }
 * }
 * }
 * }
 * }
 */
@SuppressWarnings("unchecked")
public class FieldMappings implements Mappings<Map<String, Object>> {

    private static final String PROPERTIES = "properties";

    /**
     * Mapping from field name to its type
     */
    private final Map<String, Object> fieldMappings;

    public FieldMappings(MappingMetaData mappings) {
        fieldMappings = mappings.sourceAsMap();
    }

    public FieldMappings(Map<String, Map<String, Object>> mapping) {
        Map<String, Object> finalMapping = new HashMap<>();
        finalMapping.put(PROPERTIES, mapping);
        fieldMappings = finalMapping;
    }

    @Override
    public boolean has(String path) {
        return mapping(path) != null;
    }

    /**
     * Different from default implementation that search mapping for path is required
     */
    @Override
    public Map<String, Object> mapping(String path) {
        Map<String, Object> mapping = fieldMappings;
        for (String name : path.split("\\.")) {
            if (mapping == null || !mapping.containsKey(PROPERTIES)) {
                return null;
            }

            mapping = (Map<String, Object>)
                    ((Map<String, Object>) mapping.get(PROPERTIES)).get(name);
        }
        return mapping;
    }

    @Override
    public Map<String, Map<String, Object>> data() {
        // Is this assumption true? Is it possible mapping of field is NOT a Map<String,Object>?
        return (Map<String, Map<String, Object>>) fieldMappings.get(PROPERTIES);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FieldMappings that = (FieldMappings) o;
        return Objects.equals(fieldMappings, that.fieldMappings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldMappings);
    }

    @Override
    public String toString() {
        return "FieldMappings" + new JSONObject(fieldMappings).toString(2);
    }

}
