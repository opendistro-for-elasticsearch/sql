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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage;

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static org.elasticsearch.search.sort.FieldSortBuilder.DOC_FIELD_NAME;
import static org.elasticsearch.search.sort.SortOrder.ASC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.common.setting.Settings;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.client.ElasticsearchClient;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.value.ElasticsearchExprValueFactory;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.request.ElasticsearchQueryRequest;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.request.ElasticsearchRequest;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.response.ElasticsearchResponse;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

@ExtendWith(MockitoExtension.class)
class ElasticsearchIndexScanTest {

  @Mock
  private ElasticsearchClient client;

  @Mock
  private Settings settings;

  private ElasticsearchExprValueFactory exprValueFactory = new ElasticsearchExprValueFactory(
      ImmutableMap.of("name", STRING, "department", STRING));

  @BeforeEach
  void setup() {
    when(settings.getSettingValue(Settings.Key.QUERY_SIZE_LIMIT)).thenReturn(200);
  }

  @Test
  void queryEmptyResult() {
    mockResponse();
    try (ElasticsearchIndexScan indexScan =
             new ElasticsearchIndexScan(client, settings, "test", exprValueFactory)) {
      indexScan.open();
      assertFalse(indexScan.hasNext());
    }
    verify(client).cleanup(any());
  }

  @Test
  void queryAllResults() {
    mockResponse(
        new SearchHit[]{employee(1, "John", "IT"), employee(2, "Smith", "HR")},
        new SearchHit[]{employee(3, "Allen", "IT")});

    try (ElasticsearchIndexScan indexScan =
             new ElasticsearchIndexScan(client, settings, "employees", exprValueFactory)) {
      indexScan.open();

      assertTrue(indexScan.hasNext());
      assertEquals(tupleValue(employee(1, "John", "IT")), indexScan.next());

      assertTrue(indexScan.hasNext());
      assertEquals(tupleValue(employee(2, "Smith", "HR")), indexScan.next());

      assertTrue(indexScan.hasNext());
      assertEquals(tupleValue(employee(3, "Allen", "IT")), indexScan.next());

      assertFalse(indexScan.hasNext());
    }
    verify(client).cleanup(any());
  }

  @Test
  void pushDownFilters() {
    assertThat()
        .pushDown(QueryBuilders.termQuery("name", "John"))
        .shouldQuery(QueryBuilders.termQuery("name", "John"))
        .pushDown(QueryBuilders.termQuery("age", 30))
        .shouldQuery(
            QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery("name", "John"))
                .filter(QueryBuilders.termQuery("age", 30)))
        .pushDown(QueryBuilders.rangeQuery("balance").gte(10000))
        .shouldQuery(
            QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery("name", "John"))
                .filter(QueryBuilders.termQuery("age", 30))
                .filter(QueryBuilders.rangeQuery("balance").gte(10000)));
  }

  private PushDownAssertion assertThat() {
    return new PushDownAssertion(client, exprValueFactory, settings);
  }

  private static class PushDownAssertion {
    private final ElasticsearchClient client;
    private final ElasticsearchIndexScan indexScan;
    private final ElasticsearchResponse response;

    public PushDownAssertion(ElasticsearchClient client,
                             ElasticsearchExprValueFactory valueFactory,
                             Settings settings) {
      this.client = client;
      this.indexScan = new ElasticsearchIndexScan(client, settings, "test", valueFactory);
      this.response = mock(ElasticsearchResponse.class);
      when(response.isEmpty()).thenReturn(true);
    }

    PushDownAssertion pushDown(QueryBuilder query) {
      indexScan.pushDown(query);
      return this;
    }

    PushDownAssertion shouldQuery(QueryBuilder expected) {
      ElasticsearchRequest request = new ElasticsearchQueryRequest("test", 200);
      request.getSourceBuilder()
             .query(expected)
             .sort(DOC_FIELD_NAME, ASC);
      when(client.search(request)).thenReturn(response);
      indexScan.open();
      return this;
    }

  }

  private void mockResponse(SearchHit[]... searchHitBatches) {
    when(client.search(any()))
        .thenAnswer(
            new Answer<ElasticsearchResponse>() {
              private int batchNum;

              @Override
              public ElasticsearchResponse answer(InvocationOnMock invocation) {
                ElasticsearchResponse response = mock(ElasticsearchResponse.class);
                int totalBatch = searchHitBatches.length;
                if (batchNum < totalBatch) {
                  when(response.isEmpty()).thenReturn(false);
                  SearchHit[] searchHit = searchHitBatches[batchNum];
                  when(response.iterator()).thenReturn(Arrays.asList(searchHit).iterator());
                } else if (batchNum == totalBatch) {
                  when(response.isEmpty()).thenReturn(true);
                } else {
                  fail("Search request after empty response returned already");
                }

                batchNum++;
                return response;
              }
            });
  }

  protected SearchHit employee(int docId, String name, String department) {
    SearchHit hit = new SearchHit(docId);
    hit.sourceRef(
        new BytesArray("{\"name\":\"" + name + "\",\"department\":\"" + department + "\"}"));
    return hit;
  }

  private ExprValue tupleValue(SearchHit hit) {
    return ExprValueUtils.tupleValue(hit.getSourceAsMap());
  }
}
