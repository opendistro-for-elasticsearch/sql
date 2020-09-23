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

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTupleValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.value.ElasticsearchExprValueFactory;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregations;

/**
 * Elasticsearch search response.
 */
@EqualsAndHashCode
@ToString
public class ElasticsearchResponse implements Iterable<ExprValue> {

  /**
   * Search query result (non-aggregation).
   */
  private final SearchHits hits;

  /**
   * Search aggregation result.
   */
  private final Aggregations aggregations;

  /**
   * ElasticsearchExprValueFactory used to build ExprValue from search result.
   */
  @EqualsAndHashCode.Exclude
  private final ElasticsearchExprValueFactory exprValueFactory;

  /**
   * Constructor of ElasticsearchResponse.
   */
  public ElasticsearchResponse(SearchResponse esResponse,
                               ElasticsearchExprValueFactory exprValueFactory) {
    this.hits = esResponse.getHits();
    this.aggregations = esResponse.getAggregations();
    this.exprValueFactory = exprValueFactory;
  }

  /**
   * Constructor of ElasticsearchResponse with SearchHits.
   */
  public ElasticsearchResponse(SearchHits hits, ElasticsearchExprValueFactory exprValueFactory) {
    this.hits = hits;
    this.aggregations = null;
    this.exprValueFactory = exprValueFactory;
  }

  /**
   * Is response empty. As ES doc says, "Each call to the scroll API returns the next batch of
   * results until there are no more results left to return, ie the hits array is empty."
   *
   * @return true for empty
   */
  public boolean isEmpty() {
    return (hits.getHits() == null) || (hits.getHits().length == 0) && aggregations == null;
  }

  public boolean isAggregationResponse() {
    return aggregations != null;
  }

  /**
   * Make response iterable without need to return internal data structure explicitly.
   *
   * @return search hit iterator
   */
  public Iterator<ExprValue> iterator() {
    if (isAggregationResponse()) {
      return ElasticsearchAggregationResponseParser.parse(aggregations).stream().map(entry -> {
        ImmutableMap.Builder<String, ExprValue> builder = new ImmutableMap.Builder<>();
        for (Map.Entry<String, Object> value : entry.entrySet()) {
          builder.put(value.getKey(), exprValueFactory.construct(value.getKey(), value.getValue()));
        }
        return (ExprValue) ExprTupleValue.fromExprValueMap(builder.build());
      }).iterator();
    } else {
      return Arrays.stream(hits.getHits())
          .map(hit -> (ExprValue) exprValueFactory.construct(hit.getSourceAsString())).iterator();
    }
  }
}
