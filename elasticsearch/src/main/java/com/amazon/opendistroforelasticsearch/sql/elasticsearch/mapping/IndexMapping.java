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

import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Elasticsearch index mapping
 */
@RequiredArgsConstructor
public class IndexMapping {

    private final Map<String, Object> fieldMappings;

    /**
     * Return field type.
     * @param fieldName
     * @return
     */
    public String getFieldType(String fieldName) {
        return (String) fieldMappings.get(fieldName);
    }

    /**
     * Get all field types and transform raw type to expected type.
     * @param transform
     * @param <Type>
     * @return
     */
    public <Type> Map<String, Type> getAllFieldTypes(Function<Object, Type> transform) {
        return fieldMappings.entrySet().
                             stream().
                             collect(Collectors.toMap(
                                 Map.Entry::getKey,
                                 e -> transform.apply(e.getValue())
                             ));
    }

}
