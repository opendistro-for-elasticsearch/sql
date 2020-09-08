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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.response;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprIntegerValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTupleValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.value.ElasticsearchExprValueFactory;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregations;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ElasticsearchResponseTest {

  @Mock
  private SearchResponse esResponse;

  @Mock
  private ElasticsearchExprValueFactory factory;

  @Mock
  private SearchHit searchHit1;

  @Mock
  private SearchHit searchHit2;

  @Mock
  private Aggregations aggregations;

  private ExprTupleValue exprTupleValue1 = ExprTupleValue.fromExprValueMap(ImmutableMap.of("id1",
      new ExprIntegerValue(1)));

  private ExprTupleValue exprTupleValue2 = ExprTupleValue.fromExprValueMap(ImmutableMap.of("id2",
      new ExprIntegerValue(2)));

  @Test
  void isEmpty() {
    when(esResponse.getHits())
        .thenReturn(
            new SearchHits(
                new SearchHit[] {searchHit1, searchHit2},
                new TotalHits(2L, TotalHits.Relation.EQUAL_TO),
                1.0F));

    ElasticsearchResponse response1 = new ElasticsearchResponse(esResponse, factory);
    assertFalse(response1.isEmpty());

    when(esResponse.getHits()).thenReturn(SearchHits.empty());
    ElasticsearchResponse response2 = new ElasticsearchResponse(esResponse, factory);
    assertTrue(response2.isEmpty());

    when(esResponse.getHits())
        .thenReturn(new SearchHits(null, new TotalHits(0, TotalHits.Relation.EQUAL_TO), 0));
    ElasticsearchResponse response3 = new ElasticsearchResponse(esResponse, factory);
    assertTrue(response3.isEmpty());
  }

  @Test
  void iterator() {
    when(esResponse.getHits())
        .thenReturn(
            new SearchHits(
                new SearchHit[] {searchHit1, searchHit2},
                new TotalHits(2L, TotalHits.Relation.EQUAL_TO),
                1.0F));

    when(searchHit1.getSourceAsString()).thenReturn("{\"id1\", 1}");
    when(searchHit2.getSourceAsString()).thenReturn("{\"id1\", 2}");
    when(factory.construct(any())).thenReturn(exprTupleValue1).thenReturn(exprTupleValue2);

    int i = 0;
    for (ExprValue hit : new ElasticsearchResponse(esResponse, factory)) {
      if (i == 0) {
        assertEquals(exprTupleValue1, hit);
      } else if (i == 1) {
        assertEquals(exprTupleValue2, hit);
      } else {
        fail("More search hits returned than expected");
      }
      i++;
    }
  }

  @Test
  void response_is_aggregation_when_aggregation_not_empty() {
    when(esResponse.getAggregations()).thenReturn(aggregations);

    ElasticsearchResponse response = new ElasticsearchResponse(esResponse, factory);
    assertTrue(response.isAggregationResponse());
  }

  @Test
  void response_isnot_aggregation_when_aggregation_is_empty() {
    when(esResponse.getAggregations()).thenReturn(null);

    ElasticsearchResponse response = new ElasticsearchResponse(esResponse, factory);
    assertFalse(response.isAggregationResponse());
  }

  @Test
  void aggregation_iterator() {
    try (
        MockedStatic<ElasticsearchAggregationResponseParser> mockedStatic = Mockito
            .mockStatic(ElasticsearchAggregationResponseParser.class)) {
      when(ElasticsearchAggregationResponseParser.parse(any()))
          .thenReturn(Arrays.asList(ImmutableMap.of("id1", 1), ImmutableMap.of("id2", 2)));
      when(esResponse.getAggregations()).thenReturn(aggregations);
      when(factory.construct(anyString(), any())).thenReturn(new ExprIntegerValue(1))
          .thenReturn(new ExprIntegerValue(2));

      int i = 0;
      for (ExprValue hit : new ElasticsearchResponse(esResponse, factory)) {
        if (i == 0) {
          assertEquals(exprTupleValue1, hit);
        } else if (i == 1) {
          assertEquals(exprTupleValue2, hit);
        } else {
          fail("More search hits returned than expected");
        }
        i++;
      }
    }
  }
}
