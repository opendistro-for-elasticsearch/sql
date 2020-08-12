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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.request;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.jupiter.api.Test;

class ElasticsearchScrollRequestTest {

  private final ElasticsearchScrollRequest request = new ElasticsearchScrollRequest("test");

  @Test
  void searchRequest() {
    request.getSourceBuilder().query(QueryBuilders.termQuery("name", "John"));

    assertEquals(
        new SearchRequest()
            .indices("test")
            .scroll(ElasticsearchScrollRequest.DEFAULT_SCROLL_TIMEOUT)
            .source(new SearchSourceBuilder().query(QueryBuilders.termQuery("name", "John"))),
        request.searchRequest());
  }

  @Test
  void isScrollStarted() {
    assertFalse(request.isScrollStarted());

    request.setScrollId("scroll123");
    assertTrue(request.isScrollStarted());
  }

  @Test
  void scrollRequest() {
    request.setScrollId("scroll123");
    assertEquals(
        new SearchScrollRequest()
            .scroll(ElasticsearchScrollRequest.DEFAULT_SCROLL_TIMEOUT)
            .scrollId("scroll123"),
        request.scrollRequest());
  }
}
