/*
 *
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.request;

import com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.value.ElasticsearchExprValueFactory;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.response.ElasticsearchResponse;
import com.google.common.annotations.VisibleForTesting;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

/**
 * Elasticsearch search request. This has to be stateful because it needs to:
 *
 * <p>1) Accumulate search source builder when visiting logical plan to push down operation. 2)
 * Indicate the search already done.
 */
@EqualsAndHashCode
@Getter
@ToString
public class ElasticsearchQueryRequest implements ElasticsearchRequest {

  /**
   * Default query timeout in minutes.
   */
  public static final TimeValue DEFAULT_QUERY_TIMEOUT = TimeValue.timeValueMinutes(1L);

  /**
   * Index name.
   */
  private final String indexName;

  /**
   * Search request source builder.
   */
  private final SearchSourceBuilder sourceBuilder;


  /**
   * ElasticsearchExprValueFactory.
   */
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private final ElasticsearchExprValueFactory exprValueFactory;

  /**
   * Indicate the search already done.
   */
  private boolean searchDone = false;

  /**
   * Constructor of ElasticsearchQueryRequest.
   */
  public ElasticsearchQueryRequest(String indexName, int size,
                                   ElasticsearchExprValueFactory factory) {
    this.indexName = indexName;
    this.sourceBuilder = new SearchSourceBuilder();
    sourceBuilder.from(0);
    sourceBuilder.size(size);
    sourceBuilder.timeout(DEFAULT_QUERY_TIMEOUT);
    this.exprValueFactory = factory;
  }

  @Override
  public ElasticsearchResponse search(Function<SearchRequest, SearchResponse> searchAction,
                                      Function<SearchScrollRequest, SearchResponse> scrollAction) {
    if (searchDone) {
      return new ElasticsearchResponse(SearchHits.empty(), exprValueFactory);
    } else {
      searchDone = true;
      return new ElasticsearchResponse(searchAction.apply(searchRequest()), exprValueFactory);
    }
  }

  @Override
  public void clean(Consumer<String> cleanAction) {
    //do nothing.
  }

  /**
   * Generate Elasticsearch search request.
   *
   * @return search request
   */
  @VisibleForTesting
  protected SearchRequest searchRequest() {
    return new SearchRequest()
        .indices(indexName)
        .source(sourceBuilder);
  }
}
