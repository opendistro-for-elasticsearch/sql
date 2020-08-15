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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.response.error;

import static org.elasticsearch.rest.RestStatus.SERVICE_UNAVAILABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.SearchPhaseExecutionException;
import org.elasticsearch.action.search.ShardSearchFailure;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ElasticsearchErrorMessageTest {

  @Mock
  private ElasticsearchException elasticsearchException;

  @Mock
  private SearchPhaseExecutionException searchPhaseExecutionException;

  @Mock
  private ShardSearchFailure shardSearchFailure;

  @Test
  public void fetchReason() {
    when(elasticsearchException.getMessage()).thenReturn("error");

    ElasticsearchErrorMessage errorMessage =
        new ElasticsearchErrorMessage(elasticsearchException, SERVICE_UNAVAILABLE.getStatus());
    assertEquals("Error occurred in Elasticsearch engine: error", errorMessage.fetchReason());
  }

  @Test
  public void fetchDetailsWithElasticsearchException() {
    when(elasticsearchException.getDetailedMessage()).thenReturn("detail error");

    ElasticsearchErrorMessage errorMessage =
        new ElasticsearchErrorMessage(elasticsearchException, SERVICE_UNAVAILABLE.getStatus());
    assertEquals("detail error\n"
            + "For more details, please send request for "
            + "Json format to see the raw response from elasticsearch engine.",
        errorMessage.fetchDetails());
  }

  @Test
  public void fetchDetailsWithSearchPhaseExecutionException() {
    when(searchPhaseExecutionException.shardFailures())
        .thenReturn(new ShardSearchFailure[] {shardSearchFailure});
    when(shardSearchFailure.shardId()).thenReturn(1);
    when(shardSearchFailure.getCause()).thenReturn(new IllegalStateException("illegal state"));

    ElasticsearchErrorMessage errorMessage =
        new ElasticsearchErrorMessage(searchPhaseExecutionException,
            SERVICE_UNAVAILABLE.getStatus());
    assertEquals("Shard[1]: java.lang.IllegalStateException: illegal state\n"
            + "\n"
            + "For more details, please send request for Json format to see the "
            + "raw response from elasticsearch engine.",
        errorMessage.fetchDetails());
  }
}