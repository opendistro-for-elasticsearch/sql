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

import java.util.Iterator;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

/** Elasticsearch search response. */
@EqualsAndHashCode
@ToString
public class ElasticsearchResponse implements Iterable<SearchHit> {

  /** Search query result (non-aggregation). */
  private final SearchHits hits;

  public ElasticsearchResponse(SearchResponse esResponse) {
    this.hits = esResponse.getHits(); // TODO: aggregation result is separate and not in SearchHit[]
  }

  public ElasticsearchResponse(SearchHits hits) {
    this.hits = hits;
  }

  /**
   * Is response empty. As ES doc says, "Each call to the scroll API returns the next batch of
   * results until there are no more results left to return, ie the hits array is empty."
   *
   * @return true for empty
   */
  public boolean isEmpty() {
    return (hits.getHits() == null) || (hits.getHits().length == 0);
  }

  /**
   * Make response iterable without need to return internal data structure explicitly.
   *
   * @return search hit iterator
   */
  @Override
  public Iterator<SearchHit> iterator() {
    return hits.iterator();
  }
}
