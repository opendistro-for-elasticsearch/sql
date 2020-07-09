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

package com.amazon.opendistroforelasticsearch.sql.legacy.esdomain.mapping;

import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.collect.ImmutableOpenMap;

import java.util.Map;
import java.util.Objects;

/**
 * Type mappings in a specific index.
 * <p>
 * Sample:
 * typeMappings: {
 * '_doc': fieldMappings
 * }
 */
public class TypeMappings implements Mappings<FieldMappings> {

    /**
     * Mapping from Type name to mappings of all Fields in it
     */
    private final Map<String, FieldMappings> typeMappings;

    public TypeMappings(ImmutableOpenMap<String, MappingMetadata> mappings) {
        typeMappings = buildMappings(mappings, FieldMappings::new);
    }

    @Override
    public Map<String, FieldMappings> data() {
        return typeMappings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TypeMappings that = (TypeMappings) o;
        return Objects.equals(typeMappings, that.typeMappings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeMappings);
    }

    @Override
    public String toString() {
        return "TypeMappings{" + typeMappings + '}';
    }
}
