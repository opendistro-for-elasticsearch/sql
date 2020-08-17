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

package com.amazon.opendistroforelasticsearch.sql.legacy.plugin;

import static com.amazon.opendistroforelasticsearch.sql.legacy.plugin.RestSQLQueryAction.NOT_SUPPORTED_YET;
import static com.amazon.opendistroforelasticsearch.sql.legacy.plugin.RestSqlAction.EXPLAIN_API_ENDPOINT;
import static com.amazon.opendistroforelasticsearch.sql.legacy.plugin.RestSqlAction.QUERY_API_ENDPOINT;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import com.amazon.opendistroforelasticsearch.sql.common.setting.Settings;
import com.amazon.opendistroforelasticsearch.sql.sql.domain.SQLQueryRequest;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.cluster.service.ClusterService;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class RestSQLQueryActionTest {

  @Mock
  private ClusterService clusterService;

  @Mock
  private NodeClient nodeClient;

  @Mock
  private Settings settings;

  @Test
  public void handleQueryThatCanSupport() {
    SQLQueryRequest request = new SQLQueryRequest(
        new JSONObject("{\"query\": \"SELECT -123\"}"),
        "SELECT -123",
        QUERY_API_ENDPOINT,
        "");

    RestSQLQueryAction queryAction = new RestSQLQueryAction(clusterService, settings);
    assertNotSame(NOT_SUPPORTED_YET, queryAction.prepareRequest(request, nodeClient));
  }

  @Test
  public void skipExplainThatNotSupport() {
    SQLQueryRequest request = new SQLQueryRequest(
        new JSONObject("{\"query\": \"SELECT * FROM test\"}"),
        "SELECT * FROM test",
        EXPLAIN_API_ENDPOINT,
        "");

    RestSQLQueryAction queryAction = new RestSQLQueryAction(clusterService, settings);
    assertSame(NOT_SUPPORTED_YET, queryAction.prepareRequest(request, nodeClient));
  }

  @Test
  public void skipQueryThatNotSupport() {
    SQLQueryRequest request = new SQLQueryRequest(
        new JSONObject("{\"query\": \"SELECT * FROM test WHERE age = 10\"}"),
        "SELECT * FROM test WHERE age = 10",
        QUERY_API_ENDPOINT,
        "");

    RestSQLQueryAction queryAction = new RestSQLQueryAction(clusterService, settings);
    assertSame(NOT_SUPPORTED_YET, queryAction.prepareRequest(request, nodeClient));
  }

}