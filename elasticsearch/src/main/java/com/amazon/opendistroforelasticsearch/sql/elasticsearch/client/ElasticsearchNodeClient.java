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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.client;

import com.amazon.opendistroforelasticsearch.sql.elasticsearch.mapping.IndexMapping;
import com.carrotsearch.hppc.cursors.ObjectObjectCursor;
import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.collect.ImmutableOpenMap;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Elasticsearch connection by node client.
 */
@RequiredArgsConstructor
public class ElasticsearchNodeClient implements ElasticsearchClient {

    /**
     * Default types and field filter to match all
     */
    public static final String[] ALL_TYPES = new String[0];
    public static final Function<String, Predicate<String>> ALL_FIELDS = (anyIndex -> (anyField -> true));

    /**
     * Current cluster state on local node
     */
    private final ClusterService clusterService;

    /**
     * Index name expression resolver to get concrete index name
     */
    private final IndexNameExpressionResolver resolver;

    /**
     * Node client provided by Elasticsearch container
     */
    private final NodeClient client;

    /**
     * Get field mappings of index by an index expression. Majority is copied from legacy LocalClusterState.
     *
     * For simplicity, removed type (deprecated) and field filter in argument list.
     * Also removed mapping cache, cluster state listener (mainly for performance and debugging).
     *
     * @param indexExpression     index name expression
     * @return index mapping(s)
     */
    @Override
    public Map<String, IndexMapping> getIndexMappings(String indexExpression) {
        try {
            ClusterState state = clusterService.state();
            String[] concreteIndices = resolveIndexExpression(state, new String[]{ indexExpression });

            return populateIndexMappings(state.metaData().findMappings(concreteIndices, ALL_TYPES, ALL_FIELDS));
        } catch (IOException e) {
            throw new IllegalStateException(
                "Failed to read mapping in cluster state for index pattern [" + indexExpression + "]", e);
        }
    }

    private String[] resolveIndexExpression(ClusterState state, String[] indices) {
        return resolver.concreteIndexNames(state, IndicesOptions.strictExpandOpen(), indices);
    }

    private Map<String, IndexMapping> populateIndexMappings(
        ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> indexMappings) {

        ImmutableMap.Builder<String, IndexMapping> result = ImmutableMap.builder();
        for (ObjectObjectCursor<String, ImmutableOpenMap<String, MappingMetaData>> cursor : indexMappings) {
            result.put(cursor.key, populateIndexMapping(cursor.value));
        }
        return result.build();
    }

    private IndexMapping populateIndexMapping(ImmutableOpenMap<String, MappingMetaData> indexMapping) {
        if (indexMapping.isEmpty()) {
            return new IndexMapping(Collections.emptyMap());
        }
        return new IndexMapping(indexMapping.iterator().next().value);
    }

}
