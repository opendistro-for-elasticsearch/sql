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
import org.elasticsearch.cluster.metadata.Metadata;
import org.elasticsearch.common.collect.ImmutableOpenMap;

import java.util.Map;
import java.util.Objects;

import static java.util.Collections.emptyMap;

/**
 * Index mappings in the cluster.
 * <p>
 * Sample:
 * indexMappings: {
 * 'accounts': typeMappings1,
 * 'logs':     typeMappings2
 * }
 * <p>
 * Difference between response of getMapping/clusterState and getFieldMapping:
 * <p>
 * 1) MappingMetadata:
 * ((Map) ((Map) (mapping.get("bank").get("account").sourceAsMap().get("properties"))).get("balance")).get("type")
 * <p>
 * 2) FieldMetadata:
 * ((Map) client.admin().indices().getFieldMappings(request).actionGet().mappings().get("bank")
 * .get("account").get("balance").sourceAsMap().get("balance")).get("type")
 */
public class IndexMappings implements Mappings<TypeMappings> {

    public static final IndexMappings EMPTY = new IndexMappings();

    /**
     * Mapping from Index name to mappings of all Types in it
     */
    private final Map<String, TypeMappings> indexMappings;

    public IndexMappings() {
        this.indexMappings = emptyMap();
    }

    public IndexMappings(Metadata metaData) {
        this.indexMappings = buildMappings(metaData.indices(),
                indexMetaData -> new TypeMappings(indexMetaData.getMappings()));
    }

    public IndexMappings(ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetadata>> mappings) {
        this.indexMappings = buildMappings(mappings, TypeMappings::new);
    }

    @Override
    public Map<String, TypeMappings> data() {
        return indexMappings;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        IndexMappings that = (IndexMappings) o;
        return Objects.equals(indexMappings, that.indexMappings);
    }

    @Override
    public int hashCode() {
        return Objects.hash(indexMappings);
    }

    @Override
    public String toString() {
        return "IndexMappings{" + indexMappings + '}';
    }
}
