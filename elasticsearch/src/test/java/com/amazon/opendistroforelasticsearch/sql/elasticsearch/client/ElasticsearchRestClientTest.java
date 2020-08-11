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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.elasticsearch.mapping.IndexMapping;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.request.ElasticsearchRequest;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.response.ElasticsearchResponse;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.elasticsearch.client.indices.GetMappingsResponse;
import org.elasticsearch.cluster.metadata.IndexMetadata;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.xcontent.DeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    when(restClient.indices().getMapping(any(GetMappingsRequest.class), any()))
        .thenReturn(response);

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
    assertEquals("text_keyword", indexMapping.getFieldType("employer"));
    assertEquals("keyword", indexMapping.getFieldType("employer.raw"));
    assertEquals("nested", indexMapping.getFieldType("projects"));
    assertEquals("boolean", indexMapping.getFieldType("projects.active"));
    assertEquals("date", indexMapping.getFieldType("projects.release"));
    assertEquals("nested", indexMapping.getFieldType("projects.members"));
    assertEquals("text", indexMapping.getFieldType("projects.members.name"));
    assertEquals("object", indexMapping.getFieldType("manager"));
    assertEquals("text_keyword", indexMapping.getFieldType("manager.name"));
    assertEquals("keyword", indexMapping.getFieldType("manager.name.keyword"));
    assertEquals("keyword", indexMapping.getFieldType("manager.address"));
    assertEquals("long", indexMapping.getFieldType("manager.salary"));
  }

  @Test
  void getIndexMappingsWithIOException() throws IOException {
    when(restClient.indices().getMapping(any(GetMappingsRequest.class), any()))
        .thenThrow(new IOException());
    assertThrows(IllegalStateException.class, () -> client.getIndexMappings("test"));
  }

  @Test
  void search() throws IOException {
    // Mock first scroll request
    SearchResponse searchResponse = mock(SearchResponse.class);
    when(restClient.search(any(), any())).thenReturn(searchResponse);
    when(searchResponse.getScrollId()).thenReturn("scroll123");
    when(searchResponse.getHits())
        .thenReturn(
            new SearchHits(
                new SearchHit[] {new SearchHit(1)},
                new TotalHits(1L, TotalHits.Relation.EQUAL_TO),
                1.0F));

    // Mock second scroll request followed
    SearchResponse scrollResponse = mock(SearchResponse.class);
    when(restClient.scroll(any(), any())).thenReturn(scrollResponse);
    when(scrollResponse.getScrollId()).thenReturn("scroll456");
    when(scrollResponse.getHits()).thenReturn(SearchHits.empty());

    // Verify response for first scroll request
    ElasticsearchRequest request = new ElasticsearchRequest("test");
    ElasticsearchResponse response1 = client.search(request);
    assertFalse(response1.isEmpty());

    Iterator<SearchHit> hits = response1.iterator();
    assertTrue(hits.hasNext());
    assertEquals(new SearchHit(1), hits.next());
    assertFalse(hits.hasNext());

    // Verify response for second scroll request
    ElasticsearchResponse response2 = client.search(request);
    assertTrue(response2.isEmpty());
  }

  @Test
  void searchWithIOException() throws IOException {
    when(restClient.search(any(), any())).thenThrow(new IOException());
    assertThrows(
        IllegalStateException.class, () -> client.search(new ElasticsearchRequest("test")));
  }

  @Test
  void schedule() {
    AtomicBoolean isRun = new AtomicBoolean(false);
    client.schedule(
        () -> {
          isRun.set(true);
        });
    assertTrue(isRun.get());
  }

  @Test
  void cleanup() throws IOException {
    ElasticsearchRequest request = new ElasticsearchRequest("test");
    request.setScrollId("scroll123");
    client.cleanup(request);
    verify(restClient).clearScroll(any(), any());
    assertFalse(request.isScrollStarted());
  }

  @Test
  void cleanupWithoutScrollId() throws IOException {
    ElasticsearchRequest request = new ElasticsearchRequest("test");
    client.cleanup(request);
    verify(restClient, never()).clearScroll(any(), any());
  }

  @Test
  void cleanupWithIOException() throws IOException {
    when(restClient.clearScroll(any(), any())).thenThrow(new IOException());

    ElasticsearchRequest request = new ElasticsearchRequest("test");
    request.setScrollId("scroll123");
    assertThrows(IllegalStateException.class, () -> client.cleanup(request));
  }

  private Map<String, MappingMetadata> mockFieldMappings(String indexName, String mappings)
      throws IOException {
    return ImmutableMap.of(indexName, IndexMetadata.fromXContent(createParser(mappings)).mapping());
  }

  private XContentParser createParser(String mappings) throws IOException {
    return XContentType.JSON
        .xContent()
        .createParser(
            NamedXContentRegistry.EMPTY, DeprecationHandler.THROW_UNSUPPORTED_OPERATION, mappings);
  }
}
