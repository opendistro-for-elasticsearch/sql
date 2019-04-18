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

package com.amazon.opendistroforelasticsearch.sql.esdomain;

import com.amazon.opendistroforelasticsearch.sql.plugin.SqlSettings;
import com.carrotsearch.hppc.cursors.ObjectObjectCursor;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.collect.Tuple;
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.index.IndexNotFoundException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.Collections.emptyMap;
import static org.elasticsearch.common.settings.Settings.EMPTY;

/**
 * Local cluster state information which may be stale but help avoid blocking operation in NIO thread.
 *
 *  1) Why extending TransportAction doesn't work here?
 *      TransportAction enforce implementation to be performed remotely but local cluster state read is expected here.
 *
 *  2) Why injection by AbstractModule doesn't work here?
 *      Because this state needs to be used across the plugin, ex. in rewriter, pretty formatter etc.
 */
public class LocalClusterState {

    private static final Logger LOG = LogManager.getLogger();

    /** Default types and field filter to match all */
    private static final String[] ALL_TYPES = new String[0];
    private static final Function<String, Predicate<String>> ALL_FIELDS = (anyIndex -> (anyField -> true));

    /** Singleton instance */
    private static LocalClusterState INSTANCE;

    /** Current cluster state on local node */
    private ClusterService clusterService;

    /** Sql specific settings in ES cluster settings */
    private SqlSettings sqlSettings;

    /** Index name expression resolver to get concrete index name */
    private IndexNameExpressionResolver resolver;

    /**
     * Thread-safe mapping cache to save the computation of sourceAsMap() which is not lightweight as thought
     * Array cannot be used as key because hashCode() always return reference address, so either use wrapper or List.
     */
    private final Cache<Tuple<List<String>, List<String>>, IndexMappings> cache;

    /** Latest setting value for each registered key. Thread-safe is required. */
    private final Map<String, Object> latestSettings = new ConcurrentHashMap<>();


    public static synchronized LocalClusterState state() {
        if (INSTANCE == null) {
            INSTANCE = new LocalClusterState();
        }
        return INSTANCE;
    }

    /** Give testing code a chance to inject mock object */
    public static synchronized void state(LocalClusterState instance) {
        INSTANCE = instance;
    }

    public void setClusterService(ClusterService clusterService) {
        this.clusterService = clusterService;

        clusterService.addListener(event -> {
            if (event.metaDataChanged()) {
                // State in cluster service is already changed to event.state() before listener fired
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Metadata in cluster state changed: {}", new IndexMappings(clusterService.state().metaData()));
                }
                cache.invalidateAll();
            }
        });
    }

    public void setSqlSettings(SqlSettings sqlSettings) {
        this.sqlSettings = sqlSettings;
        for (Setting<?> setting : sqlSettings.getSettings()) {
            clusterService.getClusterSettings().addSettingsUpdateConsumer(
                setting,
                newVal -> {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("The value of setting [{}] changed to [{}]", setting.getKey(), newVal);
                    }
                    latestSettings.put(setting.getKey(), newVal);
                });
        }
    }

    public void setResolver(IndexNameExpressionResolver resolver) {
        this.resolver = resolver;
    }

    private LocalClusterState() {
        cache = CacheBuilder.newBuilder().maximumSize(100).build();
    }

    /**
     * Get setting value by key. Return default value if not configured explicitly.
     *
     * @param key   setting key registered during plugin launch.
     * @return      setting value or default
     */
    @SuppressWarnings("unchecked")
    public <T> T getSettingValue(String key) {
        Objects.requireNonNull(sqlSettings, "SQL setting is null");
        return (T) latestSettings.getOrDefault(key, sqlSettings.getSetting(key).getDefault(EMPTY));
    }

    /** Get field mappings by index expressions. All types and fields are included in response. */
    public IndexMappings getFieldMappings(String[] indices) {
        return getFieldMappings(indices, ALL_TYPES, ALL_FIELDS);
    }

    /** Get field mappings by index expressions, type. All fields are included in response. */
    public IndexMappings getFieldMappings(String[] indices, String[] types) {
        return getFieldMappings(indices, types, ALL_FIELDS);
    }

    /**
     * Get field mappings by index expressions, type and field filter. Because IndexMetaData/MappingMetaData is hard to convert to FieldMappingMetaData,
     * custom mapping domain objects are being used here. In future, it should be moved to domain model layer for all ES specific knowledge.
     *
     * Note that cluster state may be change inside ES so it's possible to read different state in 2 accesses to ClusterService.state() here.
     *
     * @param indices       index name expression
     * @param types         type name
     * @param fieldFilter   field filter predicate
     * @return              index mapping(s)
     */
    public IndexMappings getFieldMappings(String[] indices, String[] types, Function<String, Predicate<String>> fieldFilter) {
        Objects.requireNonNull(clusterService, "Cluster service is null");
        Objects.requireNonNull(resolver, "Index name expression resolver is null");

        try {
            ClusterState state = clusterService.state();
            String[] concreteIndices = resolveIndexExpression(state, indices);

            IndexMappings mappings;
            if (fieldFilter == ALL_FIELDS) {
                mappings = findMappingsInCache(state, concreteIndices, types);
            } else {
                mappings = findMappings(state, concreteIndices, types, fieldFilter);
            }

            LOG.debug("Found mappings: {}", mappings);
            return mappings;
        } catch (IndexNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException(
                "Failed to read mapping in cluster state for indices="
                    + Arrays.toString(indices) + ", types=" + Arrays.toString(types), e);
        }
    }

    private String[] resolveIndexExpression(ClusterState state, String[] indices) {
        String[] concreteIndices = resolver.concreteIndexNames(state, IndicesOptions.strictExpandOpen(), indices);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Resolved index expression {} to concrete index names {}",
                Arrays.toString(indices), Arrays.toString(concreteIndices));
        }
        return concreteIndices;
    }

    private IndexMappings findMappings(ClusterState state, String[] indices, String[] types,
                                       Function<String, Predicate<String>> fieldFilter) throws IOException {
        LOG.debug("Cache didn't help. Load and parse mapping in cluster state");
        return new IndexMappings(
            state.metaData().findMappings(indices, types, fieldFilter)
        );
    }

    private IndexMappings findMappingsInCache(ClusterState state, String[] indices, String[] types) throws ExecutionException {
        LOG.debug("Looking for mapping in cache: {}", cache.asMap());
        return cache.get(
            new Tuple<>(sortToList(indices), sortToList(types)),
            () -> findMappings(state, indices, types, ALL_FIELDS)
        );
    }

    private <T> List<T> sortToList(T[] array) {
        // Mostly array has single element
        Arrays.sort(array);
        return Arrays.asList(array);
    }

    /**
     * Mappings interface to provide default implementation (minimal set of Map methods) for subclass in hierarchy.
     * @param <T>   Type of nested mapping
     */
    public interface Mappings<T> {

        default boolean has(String name) {
            return data().containsKey(name);
        }

        default Collection<String> allNames() {
            return data().keySet();
        }

        default T mapping(String name) {
            return data().get(name);
        }

        default T firstMapping() {
            return allMappings().iterator().next();
        }

        default Collection<T> allMappings() {
            return data().values();
        }

        default boolean isEmpty() {
            return data().isEmpty();
        }

        Map<String, T> data();
    }

    /**
     * Index mappings in the cluster.
     *
     * Sample:
     *  indexMappings: {
     *      'accounts': typeMappings1,
     *      'logs':     typeMappings2
     *  }
     *
     * Difference between response of getMapping/clusterState and getFieldMapping:
     *
     * 1) MappingMetadata:
     * ((Map) ((Map) (mapping.get("bank").get("account").sourceAsMap().get("properties"))).get("balance")).get("type")
     *
     * 2) FieldMetadata:
     * ((Map) client.admin().indices().getFieldMappings(request).actionGet().mappings().get("bank").get("account").get("balance").sourceAsMap().get("balance")).get("type")
     */
    public static class IndexMappings implements Mappings<TypeMappings> {

        public static final IndexMappings EMPTY = new IndexMappings();

        /** Mapping from Index name to mappings of all Types in it */
        private final Map<String, TypeMappings> indexMappings;

        public IndexMappings() {
            this.indexMappings = emptyMap();
        }

        public IndexMappings(MetaData metaData) {
            this.indexMappings = buildMappings(metaData.indices(), indexMetaData -> new TypeMappings(indexMetaData.getMappings()));
        }

        public IndexMappings(ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> mappings) {
            this.indexMappings = buildMappings(mappings, TypeMappings::new);
        }

        @Override
        public Map<String, TypeMappings> data() {
            return indexMappings;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
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

    /**
     * Type mappings in a specific index.
     *
     * Sample:
     *  typeMappings: {
     *      '_doc': fieldMappings
     *  }
     */
    public static class TypeMappings implements Mappings<FieldMappings> {

        /** Mapping from Type name to mappings of all Fields in it */
        private final Map<String, FieldMappings> typeMappings;

        public TypeMappings(ImmutableOpenMap<String, MappingMetaData> mappings) {
            typeMappings = buildMappings(mappings, FieldMappings::new);
        }

        @Override
        public Map<String, FieldMappings> data() {
            return typeMappings;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
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

    /**
     * Field mappings in a specific type.
     *
     * Sample:
     *  fieldMappings: {
     *      'properties': {
     *          'balance': {
     *              'type': long
     *          },
     *          'age': {
     *              'type': integer
     *          },
     *          'state': {
     *              'type': text，
     *          }
     *          'name': {
     *              'type': text，
     *              'fields': {
     *                  'keyword': {
     *                      'type': keyword,
     *                      'ignore_above': 256
     *                  }
     *              }
     *          }
     *      }
     *  }
     */
    @SuppressWarnings("unchecked")
    public static class FieldMappings implements Mappings<Map<String, Object>> {

        private static final String PROPERTIES = "properties";

        /** Mapping from field name to its type */
        private final Map<String, Object> fieldMappings;

        public FieldMappings(MappingMetaData mappings) {
            fieldMappings = mappings.sourceAsMap();
        }

        @Override
        public boolean has(String path) {
            return mapping(path) != null;
        }

        /** Different from default implementation that search mapping for path is required */
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
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
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

    /** Convert ES ImmutableOpenMap<String, T> to JDK Map<String, U> by applying function: U func(T) */
    private static <T, U> Map<String, U> buildMappings(ImmutableOpenMap<String, T> mappings, Function<T, U> func) {
        ImmutableMap.Builder<String, U> builder = ImmutableMap.builder();
        for (ObjectObjectCursor<String, T> mapping : mappings) {
            builder.put(mapping.key, func.apply(mapping.value));
        }
        return builder.build();
    }

}
