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
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.elasticsearch.client.indices.GetMappingsResponse;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.xcontent.DeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElasticsearchRestClientTest {

    private static final String TEST_MAPPING_FILE = "mappings/accounts.json";

    @Mock(answer = RETURNS_DEEP_STUBS)
    private RestHighLevelClient restClient;

    private ElasticsearchRestClient client;

    @BeforeEach
    void setUp() {
        client = new ElasticsearchRestClient(restClient);
    }

    @Test
    void getIndexMappings() throws IOException {
        URL url = Resources.getResource(TEST_MAPPING_FILE);
        String mappings = Resources.toString(url, Charsets.UTF_8);
        String indexName = "test";

        GetMappingsResponse response = mock(GetMappingsResponse.class);
        when(response.mappings()).thenReturn(mockFieldMappings(indexName, mappings));
        when(restClient.indices().getMapping(any(GetMappingsRequest.class), any())).thenReturn(response);

        Map<String, IndexMapping> indexMappings = client.getIndexMappings(indexName);
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

    @Test
    void search() {
    }

    @Test
    void schedule() {
    }

    private Map<String, MappingMetaData> mockFieldMappings(String indexName, String mappings) throws IOException {
        return ImmutableMap.of(indexName, IndexMetaData.fromXContent(createParser(mappings)).mapping());
    }

    private XContentParser createParser(String mappings) throws IOException {
        return XContentType.JSON.xContent().createParser(
            NamedXContentRegistry.EMPTY,
            DeprecationHandler.THROW_UNSUPPORTED_OPERATION,
            mappings
        );
    }
}