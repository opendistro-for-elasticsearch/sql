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
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.cluster.metadata.MetaData;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.xcontent.DeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElasticsearchNodeClientTest {

    private static final String TEST_MAPPING_FILE = "mappings/accounts.json";

    private ElasticsearchNodeClient client;

    @Mock
    private NodeClient esClient;

    @BeforeEach
    public void setUp() throws IOException {
        URL url = Resources.getResource(TEST_MAPPING_FILE);
        String mappings = Resources.toString(url, Charsets.UTF_8);
        ClusterService clusterService = mockClusterService(mappings);
        IndexNameExpressionResolver resolver = mockIndexNameExpressionResolver();
        client = new ElasticsearchNodeClient(clusterService, resolver, esClient);
    }

    @Test
    public void testGetIndexMappings() {
        Map<String, IndexMapping> indexMappings = client.getIndexMappings("test");

        assertEquals(1, indexMappings.size());
        IndexMapping indexMapping = indexMappings.values().iterator().next();

        assertEquals(20, indexMapping.size());
        assertEquals("text", indexMapping.getFieldType("address"));
        assertEquals("integer", indexMapping.getFieldType("age"));
        assertEquals("double", indexMapping.getFieldType("balance"));
        assertEquals("keyword", indexMapping.getFieldType("city"));
        assertEquals("date", indexMapping.getFieldType("birthday"));
        assertEquals("geo_point", indexMapping.getFieldType("location"));
        assertEquals("some_new_es_type_outside_type_system", indexMapping.getFieldType("new_field"));
        assertEquals("text", indexMapping.getFieldType("field with spaces"));
        assertEquals("text", indexMapping.getFieldType("employer"));
        assertEquals("keyword", indexMapping.getFieldType("employer.raw"));
        assertEquals("nested", indexMapping.getFieldType("projects"));
        assertEquals("boolean", indexMapping.getFieldType("projects.active"));
        assertEquals("date", indexMapping.getFieldType("projects.release"));
        assertEquals("nested", indexMapping.getFieldType("projects.members"));
        assertEquals("text", indexMapping.getFieldType("projects.members.name"));
        assertEquals("object", indexMapping.getFieldType("manager"));
        assertEquals("text", indexMapping.getFieldType("manager.name"));
        assertEquals("keyword", indexMapping.getFieldType("manager.name.keyword"));
        assertEquals("keyword", indexMapping.getFieldType("manager.address"));
        assertEquals("long", indexMapping.getFieldType("manager.salary"));
    }

    public ClusterService mockClusterService(String mappings) {
        ClusterService mockService = mock(ClusterService.class);
        ClusterState mockState = mock(ClusterState.class);
        MetaData mockMetaData = mock(MetaData.class);

        when(mockService.state()).thenReturn(mockState);
        when(mockState.metaData()).thenReturn(mockMetaData);
        try {
            ImmutableOpenMap.Builder<String, ImmutableOpenMap<String, MappingMetaData>> builder = ImmutableOpenMap.builder();
            builder.put("test", IndexMetaData.fromXContent(createParser(mappings)).getMappings());
            when(mockMetaData.findMappings(any(), any(), any())).thenReturn(builder.build());
        }
        catch (IOException e) {
            throw new IllegalStateException("Failed to mock cluster service", e);
        }
        return mockService;
    }

    private IndexNameExpressionResolver mockIndexNameExpressionResolver() {
        IndexNameExpressionResolver mockResolver = mock(IndexNameExpressionResolver.class);
        when(mockResolver.concreteIndexNames(any(), any(), any())).thenAnswer(
            (Answer<String[]>) invocation -> {
                // Return index expression directly without resolving
                Object indexExprs = invocation.getArguments()[2];
                if (indexExprs instanceof String) {
                    return new String[]{ (String) indexExprs };
                }
                return (String[]) indexExprs;
            }
        );
        return mockResolver;
    }

    private XContentParser createParser(String mappings) throws IOException {
        return XContentType.JSON.xContent().createParser(
            NamedXContentRegistry.EMPTY,
            DeprecationHandler.THROW_UNSUPPORTED_OPERATION,
            mappings
        );
    }

}