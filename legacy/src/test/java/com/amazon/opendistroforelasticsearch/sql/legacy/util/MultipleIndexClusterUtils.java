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

package com.amazon.opendistroforelasticsearch.sql.legacy.util;

import com.amazon.opendistroforelasticsearch.sql.legacy.esdomain.LocalClusterState;
import com.google.common.collect.ImmutableMap;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.collect.ImmutableOpenMap;

import java.io.IOException;
import java.util.Map;

import static com.amazon.opendistroforelasticsearch.sql.legacy.util.CheckScriptContents.createParser;
import static com.amazon.opendistroforelasticsearch.sql.legacy.util.CheckScriptContents.mockIndexNameExpressionResolver;
import static com.amazon.opendistroforelasticsearch.sql.legacy.util.CheckScriptContents.mockSqlSettings;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test Utility which provide the cluster have 2 indices.
 */
public class MultipleIndexClusterUtils {
    public final static String INDEX_ACCOUNT_1 = "account1";
    public final static String INDEX_ACCOUNT_2 = "account2";
    public final static String INDEX_ACCOUNT_ALL = "account*";

    public static String INDEX_ACCOUNT_1_MAPPING = "{\n" +
            "  \"field_mappings\": {\n" +
            "    \"mappings\": {\n" +
            "      \"account1\": {\n" +
            "        \"properties\": {\n" +
            "          \"id\": {\n" +
            "            \"type\": \"long\"\n" +
            "          },\n" +
            "          \"address\": {\n" +
            "            \"type\": \"text\",\n" +
            "            \"fields\": {\n" +
            "              \"keyword\": {\n" +
            "                \"type\": \"keyword\"\n" +
            "              }\n" +
            "            },\n" +
            "            \"fielddata\": true\n" +
            "          },\n" +
            "          \"age\": {\n" +
            "            \"type\": \"integer\"\n" +
            "          },\n" +
            "          \"projects\": {\n" +
            "            \"type\": \"nested\",\n" +
            "            \"properties\": {\n" +
            "              \"name\": {\n" +
            "                \"type\": \"text\",\n" +
            "                \"fields\": {\n" +
            "                  \"keyword\": {\n" +
            "                    \"type\": \"keyword\"\n" +
            "                  }\n" +
            "                },\n" +
            "                \"fielddata\": true\n" +
            "              },\n" +
            "              \"started_year\": {\n" +
            "                \"type\": \"int\"\n" +
            "              }\n" +
            "            }\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    \"settings\": {\n" +
            "      \"index\": {\n" +
            "        \"number_of_shards\": 1,\n" +
            "        \"number_of_replicas\": 0,\n" +
            "        \"version\": {\n" +
            "          \"created\": \"6050399\"\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    \"mapping_version\": \"1\",\n" +
            "    \"settings_version\": \"1\"\n" +
            "  }\n" +
            "}";

    /**
     * The difference with account1.
     * 1. missing address.
     * 2. age has different type.
     * 3. projects.started_year has different type.
     */
    public static String INDEX_ACCOUNT_2_MAPPING = "{\n" +
            "  \"field_mappings\": {\n" +
            "    \"mappings\": {\n" +
            "      \"account2\": {\n" +
            "        \"properties\": {\n" +
            "          \"id\": {\n" +
            "            \"type\": \"long\"\n" +
            "          },\n" +
            "          \"age\": {\n" +
            "            \"type\": \"long\"\n" +
            "          },\n" +
            "          \"projects\": {\n" +
            "            \"type\": \"nested\",\n" +
            "            \"properties\": {\n" +
            "              \"name\": {\n" +
            "                \"type\": \"text\",\n" +
            "                \"fields\": {\n" +
            "                  \"keyword\": {\n" +
            "                    \"type\": \"keyword\"\n" +
            "                  }\n" +
            "                },\n" +
            "                \"fielddata\": true\n" +
            "              },\n" +
            "              \"started_year\": {\n" +
            "                \"type\": \"long\"\n" +
            "              }\n" +
            "            }\n" +
            "          }\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    \"settings\": {\n" +
            "      \"index\": {\n" +
            "        \"number_of_shards\": 1,\n" +
            "        \"number_of_replicas\": 0,\n" +
            "        \"version\": {\n" +
            "          \"created\": \"6050399\"\n" +
            "        }\n" +
            "      }\n" +
            "    },\n" +
            "    \"mapping_version\": \"1\",\n" +
            "    \"settings_version\": \"1\"\n" +
            "  }\n" +
            "}";

    public static void mockMultipleIndexEnv() {
        mockLocalClusterState(new ImmutableMap.Builder<String, ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>>>()
                .put(INDEX_ACCOUNT_1, buildIndexMapping(INDEX_ACCOUNT_1, INDEX_ACCOUNT_1_MAPPING))
                .put(INDEX_ACCOUNT_2, buildIndexMapping(INDEX_ACCOUNT_2, INDEX_ACCOUNT_2_MAPPING))
                .put(INDEX_ACCOUNT_ALL, buildIndexMapping(new ImmutableMap.Builder<String, String>()
                        .put(INDEX_ACCOUNT_1, INDEX_ACCOUNT_1_MAPPING)
                        .put(INDEX_ACCOUNT_2, INDEX_ACCOUNT_2_MAPPING)
                        .build()))
                .build());
    }

    public static void mockLocalClusterState(Map<String, ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>>> indexMapping) {
        LocalClusterState.state().setClusterService(mockClusterService(indexMapping));
        LocalClusterState.state().setResolver(mockIndexNameExpressionResolver());
        LocalClusterState.state().setSqlSettings(mockSqlSettings());
    }


    public static ClusterService mockClusterService(Map<String, ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>>> indexMapping) {
        ClusterService mockService = mock(ClusterService.class);
        ClusterState mockState = mock(ClusterState.class);
        MetaData mockMetaData = mock(MetaData.class);

        when(mockService.state()).thenReturn(mockState);
        when(mockState.metaData()).thenReturn(mockMetaData);
        try {
            for (Map.Entry<String, ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>>> entry : indexMapping.entrySet()) {
                when(mockMetaData.findMappings(eq(new String[]{entry.getKey()}), any(), any())).thenReturn(entry.getValue());
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return mockService;
    }

    private static ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> buildIndexMapping(Map<String, String> indexMapping) {
        try {
            ImmutableOpenMap.Builder<String, ImmutableOpenMap<String, MappingMetaData>> builder = ImmutableOpenMap.builder();
            for (Map.Entry<String, String> entry : indexMapping.entrySet()) {
                builder.put(entry.getKey(), IndexMetaData.fromXContent(createParser(entry.getValue())).getMappings());
            }
            return builder.build();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static ImmutableOpenMap<String, ImmutableOpenMap<String, MappingMetaData>> buildIndexMapping(String index,
                                                                                                         String mapping) {
        try {
            ImmutableOpenMap.Builder<String, ImmutableOpenMap<String, MappingMetaData>> builder = ImmutableOpenMap.builder();
            builder.put(index, IndexMetaData.fromXContent(createParser(mapping)).getMappings());
            return builder.build();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
