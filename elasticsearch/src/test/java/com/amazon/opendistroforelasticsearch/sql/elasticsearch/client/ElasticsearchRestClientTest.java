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
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetFieldMappingsRequest;
import org.elasticsearch.client.indices.GetFieldMappingsResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.elasticsearch.client.indices.GetFieldMappingsResponse.*;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ElasticsearchRestClientTest {

    @Mock(answer = RETURNS_DEEP_STUBS)
    private RestHighLevelClient restClient;

    private ElasticsearchRestClient client;

    @BeforeEach
    void setUp() {
        client = new ElasticsearchRestClient(restClient);
    }

    @Test
    void getIndexMappings() throws IOException {
        String indexName = "test";
        Map<String, Map<String, FieldMappingMetaData>> indexMappings = new HashMap<>();
        Map<String, FieldMappingMetaData> fieldMappings = new HashMap<>();


        indexMappings.put(indexName, fieldMappings);

        GetFieldMappingsResponse response = mock(GetFieldMappingsResponse.class);
        when(response.mappings()).thenReturn(indexMappings);
        when(restClient.indices().getFieldMapping(
            any(GetFieldMappingsRequest.class), any())).thenReturn(response);

        Map<String, IndexMapping> actual = client.getIndexMappings(indexName);
    }

    @Test
    void search() {
    }

    @Test
    void schedule() {
    }
}