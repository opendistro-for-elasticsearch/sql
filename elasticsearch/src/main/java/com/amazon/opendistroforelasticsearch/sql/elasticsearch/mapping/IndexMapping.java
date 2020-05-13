/*
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.mapping;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import com.google.common.collect.ImmutableMap;
import org.elasticsearch.cluster.metadata.MappingMetaData;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Elasticsearch index mapping
 */
public class IndexMapping {

    private final Map<String, String> fieldMappings;

    public IndexMapping(Map<String, String> fieldMappings) {
        this.fieldMappings = fieldMappings;
    }

    @SuppressWarnings("unchecked")
    public IndexMapping(MappingMetaData metaData) {
        ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<>();
        Map<String, Object> indexMapping = metaData.getSourceAsMap();
        if (indexMapping.containsKey("properties")) {
            flatMappings(((Map<String, Object>) indexMapping.get("properties")), "", builder::put);
        }
        this.fieldMappings = builder.build();
    }

    /**
     * How many fields in the index (after flatten)
     * @return  field size
     */
    public int size() {
        return fieldMappings.size();
    }

    /**
     * Return field type.
     * @param fieldName
     * @return
     */
    public String getFieldType(String fieldName) {
        return fieldMappings.get(fieldName);
    }

    /**
     * Get all field types and transform raw type to expected type.
     * @param transform
     * @param <Type>
     * @return
     */
    public <Type> Map<String, Type> getAllFieldTypes(Function<String, Type> transform) {
        return fieldMappings.entrySet().
                             stream().
                             collect(Collectors.toMap(
                                 Map.Entry::getKey,
                                 e -> transform.apply(e.getValue())
                             ));
    }

    @SuppressWarnings("unchecked")
    private void flatMappings(Map<String, Object> mappings,
                              String path,
                              BiConsumer<String, String> func) {
        mappings.forEach(
            (fieldName, mappingObject) -> {
                Map<String, Object> mapping = (Map<String, Object>) mappingObject;
                String fullFieldName = path.isEmpty() ? fieldName : path + "." + fieldName;
                String type = (String) mapping.getOrDefault("type", "object");
                func.accept(fullFieldName, type);

                if (mapping.containsKey("fields")) {
                    ((Map<String, Map<String, Object>>) mapping.get("fields")).forEach(
                        (innerFieldName, innerMapping) ->
                            func.accept(fullFieldName + "." + innerFieldName,
                                (String) innerMapping.getOrDefault("type", "object"))
                    );
                }

                if (mapping.containsKey("properties")) {
                    flatMappings(
                        (Map<String, Object>) mapping.get("properties"),
                        fullFieldName,
                        func
                    );
                }
            }
        );
    }

}
