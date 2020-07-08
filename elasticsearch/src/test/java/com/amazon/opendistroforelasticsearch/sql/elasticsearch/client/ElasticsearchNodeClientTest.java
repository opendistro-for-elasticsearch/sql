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
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.elasticsearch.mapping.IndexMapping;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.request.ElasticsearchRequest;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.response.ElasticsearchResponse;
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.io.Resources;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.ClearScrollRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.metadata.IndexAbstraction;
import org.elasticsearch.cluster.metadata.IndexMetadata;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.cluster.metadata.Metadata;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.collect.ImmutableOpenMap;
import org.elasticsearch.common.xcontent.DeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.threadpool.ThreadPool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ElasticsearchNodeClientTest {

  private static final String TEST_MAPPING_FILE = "mappings/accounts.json";

  @Mock(answer = RETURNS_DEEP_STUBS)
  private NodeClient nodeClient;

  @Test
  public void getIndexMappings() throws IOException {
    URL url = Resources.getResource(TEST_MAPPING_FILE);
    String mappings = Resources.toString(url, Charsets.UTF_8);
    String indexName = "test";
    ElasticsearchNodeClient client = mockClient(indexName, mappings);

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
  public void getIndexMappingsWithEmptyMapping() {
    String indexName = "test";
    ElasticsearchNodeClient client = mockClient(indexName, "");
    Map<String, IndexMapping> indexMappings = client.getIndexMappings(indexName);
    assertEquals(1, indexMappings.size());

    IndexMapping indexMapping = indexMappings.values().iterator().next();
    assertEquals(0, indexMapping.size());
  }

  @Test
  public void getIndexMappingsWithIOException() {
    String indexName = "test";
    ClusterService clusterService = mockClusterService(indexName, new IOException());
    ElasticsearchNodeClient client = new ElasticsearchNodeClient(clusterService, nodeClient);

    assertThrows(IllegalStateException.class, () -> client.getIndexMappings(indexName));
  }

  @Test
  public void getIndexMappingsWithNonExistIndex() {
    ElasticsearchNodeClient client =
        new ElasticsearchNodeClient(mockClusterService("test"), nodeClient);

    assertThrows(IndexNotFoundException.class, () -> client.getIndexMappings("non_exist_index"));
  }

  /** Jacoco enforce this constant lambda be tested. */
  @Test
  public void testAllFieldsPredicate() {
    assertTrue(ElasticsearchNodeClient.ALL_FIELDS.apply("any_index").test("any_field"));
  }

  @Test
  public void search() {
    ElasticsearchNodeClient client =
        new ElasticsearchNodeClient(mock(ClusterService.class), nodeClient);

    // Mock first scroll request
    SearchResponse searchResponse = mock(SearchResponse.class);
    when(nodeClient.search(any()).actionGet()).thenReturn(searchResponse);
    when(searchResponse.getScrollId()).thenReturn("scroll123");
    when(searchResponse.getHits())
        .thenReturn(
            new SearchHits(
                new SearchHit[] {new SearchHit(1)},
                new TotalHits(1L, TotalHits.Relation.EQUAL_TO),
                1.0F));

    // Mock second scroll request followed
    SearchResponse scrollResponse = mock(SearchResponse.class);
    when(nodeClient.searchScroll(any()).actionGet()).thenReturn(scrollResponse);
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
  void schedule() {
    ThreadPool threadPool = mock(ThreadPool.class);
    when(nodeClient.threadPool()).thenReturn(threadPool);

    doAnswer(
        invocation -> {
          Runnable task = invocation.getArgument(0);
          task.run();
          return null;
        })
        .when(threadPool)
        .schedule(any(), any(), any());

    ElasticsearchNodeClient client =
        new ElasticsearchNodeClient(mock(ClusterService.class), nodeClient);
    AtomicBoolean isRun = new AtomicBoolean(false);
    client.schedule(() -> isRun.set(true));
    assertTrue(isRun.get());
  }

  @Test
  void cleanup() {
    ClearScrollRequestBuilder requestBuilder = mock(ClearScrollRequestBuilder.class);
    when(nodeClient.prepareClearScroll()).thenReturn(requestBuilder);
    when(requestBuilder.addScrollId(any())).thenReturn(requestBuilder);
    when(requestBuilder.get()).thenReturn(null);

    ElasticsearchNodeClient client =
        new ElasticsearchNodeClient(mock(ClusterService.class), nodeClient);
    ElasticsearchRequest request = new ElasticsearchRequest("test");
    request.setScrollId("scroll123");
    client.cleanup(request);
    assertFalse(request.isScrollStarted());

    InOrder inOrder = Mockito.inOrder(nodeClient, requestBuilder);
    inOrder.verify(nodeClient).prepareClearScroll();
    inOrder.verify(requestBuilder).addScrollId("scroll123");
    inOrder.verify(requestBuilder).get();
  }

  @Test
  void cleanupWithoutScrollId() {
    ElasticsearchNodeClient client =
        new ElasticsearchNodeClient(mock(ClusterService.class), nodeClient);

    ElasticsearchRequest request = new ElasticsearchRequest("test");
    client.cleanup(request);
    verify(nodeClient, never()).prepareClearScroll();
  }

  private ElasticsearchNodeClient mockClient(String indexName, String mappings) {
    ClusterService clusterService = mockClusterService(indexName, mappings);
    return new ElasticsearchNodeClient(clusterService, nodeClient);
  }

  /** Mock getAliasAndIndexLookup() only for index name resolve test. */
  public ClusterService mockClusterService(String indexName) {
    ClusterService mockService = mock(ClusterService.class);
    ClusterState mockState = mock(ClusterState.class);
    Metadata mockMetaData = mock(Metadata.class);

    when(mockService.state()).thenReturn(mockState);
    when(mockState.metadata()).thenReturn(mockMetaData);
    when(mockMetaData.getIndicesLookup())
            .thenReturn(ImmutableSortedMap.of(indexName, mock(IndexAbstraction.class)));
    return mockService;
  }

  public ClusterService mockClusterService(String indexName, String mappings) {
    ClusterService mockService = mock(ClusterService.class);
    ClusterState mockState = mock(ClusterState.class);
    Metadata mockMetaData = mock(Metadata.class);

    when(mockService.state()).thenReturn(mockState);
    when(mockState.metadata()).thenReturn(mockMetaData);
    try {
      ImmutableOpenMap.Builder<String, ImmutableOpenMap<String, MappingMetadata>> builder =
          ImmutableOpenMap.builder();
      ImmutableOpenMap<String, MappingMetadata> metadata;
      if (mappings.isEmpty()) {
        metadata = ImmutableOpenMap.of();
      } else {
        metadata = IndexMetadata.fromXContent(createParser(mappings)).getMappings();
      }
      builder.put(indexName, metadata);
      when(mockMetaData.findMappings(any(), any(), any())).thenReturn(builder.build());

      // IndexNameExpressionResolver use this method to check if index exists. If not,
      // IndexNotFoundException is thrown.
      when(mockMetaData.getIndicesLookup())
              .thenReturn(ImmutableSortedMap.of(indexName, mock(IndexAbstraction.class)));
    } catch (IOException e) {
      throw new IllegalStateException("Failed to mock cluster service", e);
    }
    return mockService;
  }

  public ClusterService mockClusterService(String indexName, Throwable t) {
    ClusterService mockService = mock(ClusterService.class);
    ClusterState mockState = mock(ClusterState.class);
    Metadata mockMetaData = mock(Metadata.class);

    when(mockService.state()).thenReturn(mockState);
    when(mockState.metadata()).thenReturn(mockMetaData);
    try {
      when(mockMetaData.findMappings(any(), any(), any())).thenThrow(t);
      when(mockMetaData.getIndicesLookup())
              .thenReturn(ImmutableSortedMap.of(indexName, mock(IndexAbstraction.class)));
    } catch (IOException e) {
      throw new IllegalStateException("Failed to mock cluster service", e);
    }
    return mockService;
  }

  private XContentParser createParser(String mappings) throws IOException {
    return XContentType.JSON
        .xContent()
        .createParser(
            NamedXContentRegistry.EMPTY, DeprecationHandler.THROW_UNSUPPORTED_OPERATION, mappings);
  }
}
