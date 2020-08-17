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

import com.amazon.opendistroforelasticsearch.sql.common.setting.Settings;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.client.ElasticsearchClient;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.value.ElasticsearchExprValueFactory;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.request.ElasticsearchQueryRequest;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.request.ElasticsearchRequest;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.response.ElasticsearchResponse;
import com.amazon.opendistroforelasticsearch.sql.storage.TableScanOperator;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

/**
 * Elasticsearch index scan operator.
 */
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
public class ElasticsearchIndexScan extends TableScanOperator {

  /** Elasticsearch client. */
  private final ElasticsearchClient client;

  private final ElasticsearchExprValueFactory exprValueFactory;

  /** Search request. */
  @EqualsAndHashCode.Include
  @ToString.Include
  private final ElasticsearchRequest request;

  /** Search response for current batch. */
  private Iterator<SearchHit> hits;

  /**
   * Todo.
   */
  public ElasticsearchIndexScan(ElasticsearchClient client,
                                Settings settings, String indexName,
                                ElasticsearchExprValueFactory exprValueFactory) {
    this.client = client;
    this.request = new ElasticsearchQueryRequest(indexName,
        settings.getSettingValue(Settings.Key.QUERY_SIZE_LIMIT));
    this.exprValueFactory = exprValueFactory;
  }

  @Override
  public void open() {
    super.open();

    // For now pull all results immediately once open
    List<ElasticsearchResponse> responses = new ArrayList<>();
    ElasticsearchResponse response = client.search(request);
    while (!response.isEmpty()) {
      responses.add(response);
      response = client.search(request);
    }
    hits = Iterables.concat(responses.toArray(new ElasticsearchResponse[0])).iterator();
  }

  @Override
  public boolean hasNext() {
    return hits.hasNext();
  }

  @Override
  public ExprValue next() {
    return exprValueFactory.construct(hits.next().getSourceAsString());
  }

  /**
   * Push down query to DSL request.
   * @param query  query request
   */
  public void pushDown(QueryBuilder query) {
    SearchSourceBuilder source = request.getSourceBuilder();
    QueryBuilder current = source.query();
    if (current == null) {
      source.query(query);
    } else {
      if (isBoolMustQuery(current)) {
        ((BoolQueryBuilder) current).must(query);
      } else {
        source.query(QueryBuilders.boolQuery()
                                  .must(current)
                                  .must(query));
      }
    }
  }

  @Override
  public void close() {
    super.close();

    client.cleanup(request);
  }

  private boolean isBoolMustQuery(QueryBuilder current) {
    return (current instanceof BoolQueryBuilder)
        && !((BoolQueryBuilder) current).must().isEmpty();
  }

}
