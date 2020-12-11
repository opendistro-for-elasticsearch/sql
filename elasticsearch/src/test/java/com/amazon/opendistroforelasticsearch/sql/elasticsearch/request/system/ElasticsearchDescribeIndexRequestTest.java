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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.request.system;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.stringValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.client.ElasticsearchClient;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.mapping.IndexMapping;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ElasticsearchDescribeIndexRequestTest {

  @Mock
  private ElasticsearchClient client;

  @Test
  void testSearch() {
    when(client.getIndexMappings("index"))
        .thenReturn(
            ImmutableMap.of(
                "test",
                new IndexMapping(
                    ImmutableMap.<String, String>builder()
                        .put("name", "keyword")
                        .build())));

    final List<ExprValue> results = new ElasticsearchDescribeIndexRequest(client, "index").search();
    assertEquals(1, results.size());
    assertThat(results.get(0).tupleValue(), anyOf(
        hasEntry("TABLE_NAME", stringValue("index")),
        hasEntry("COLUMN_NAME", stringValue("name")),
        hasEntry("TYPE_NAME", stringValue("STRING"))
    ));
  }

  @Test
  void testToString() {
    assertEquals("ElasticsearchDescribeIndexRequest{indexName='index'}",
        new ElasticsearchDescribeIndexRequest(client, "index").toString());
  }
}