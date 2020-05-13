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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.index.IndexNotFoundException;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Elasticsearch connection by node client.
 */
@RequiredArgsConstructor
public class ElasticsearchNodeClient implements ElasticsearchClient {

    private static final Logger LOG = LogManager.getLogger();

    /**
     * Default types and field filter to match all
     */
    private static final String[] ALL_TYPES = new String[0];
    private static final Function<String, Predicate<String>> ALL_FIELDS = (anyIndex -> (anyField -> true));

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
     * Get field mappings by index expressions, type and field filter. Because IndexMetaData/MappingMetaData
     * is hard to convert to FieldMappingMetaData, custom mapping domain objects are being used here. In future,
     * it should be moved to domain model layer for all ES specific knowledge.
     * <p>
     * Note that cluster state may be change inside ES so it's possible to read different state in 2 accesses
     * to ClusterService.state() here.
     *
     * For simplicity, removed type (deprecated) and field filter in argument list.
     * Also removed mapping cache, cluster state listener (mainly for debugging).
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

        } catch (IndexNotFoundException e) {
            throw e;
        } catch (Exception e) {
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
            throw new IllegalStateException("Index mapping is empty.");
        }
        return new IndexMapping(indexMapping.iterator().next().value.getSourceAsMap());
    }

}
