/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.correctness.runner.connection;

import com.amazon.opendistroforelasticsearch.sql.correctness.runner.resultset.DBResult;
import java.io.IOException;
import java.util.List;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.json.JSONObject;

/**
 * Elasticsearch database connection for insertion. This class wraps JDBCConnection to delegate query method.
 */
public class ESConnection implements DBConnection {

  /**
   * Connection via our Elasticsearch JDBC driver
   */
  private final DBConnection connection;

  /**
   * Native Elasticsearch REST client for operation unsupported by driver such as CREATE/INSERT
   */
  private final RestClient client;

  public ESConnection(String connectionUrl, RestClient client) {
    this.connection = new JDBCConnection("Elasticsearch", connectionUrl);
    this.client = client;
  }

  @Override
  public String getDatabaseName() {
    return "Elasticsearch";
  }

  @Override
  public void connect() {
    connection.connect();
  }

  @Override
  public void create(String tableName, String schema) {
    Request request = new Request("PUT", "/" + tableName);
    request.setJsonEntity(schema);
    performRequest(request);
  }

  @Override
  public void drop(String tableName) {
    performRequest(new Request("DELETE", "/" + tableName));
  }

  @Override
  public void insert(String tableName, String[] columnNames, List<Object[]> batch) {
    Request request = new Request("POST", "/" + tableName + "/_bulk?refresh=true");
    request.setJsonEntity(buildBulkBody(columnNames, batch));
    performRequest(request);
  }

  @Override
  public DBResult select(String query) {
    return connection.select(query);
  }

  @Override
  public void close() {
    // Only close database connection and leave ES REST connection alone
    // because it's initialized and manged by ES test base class.
    connection.close();
  }

  private void performRequest(Request request) {
    try {
      Response response = client.performRequest(request);
      int status = response.getStatusLine().getStatusCode();
      if (status != 200) {
        throw new IllegalStateException("Failed to perform request. Error code: " + status);
      }
    } catch (IOException e) {
      throw new IllegalStateException("Failed to perform request", e);
    }
  }

  private String buildBulkBody(String[] columnNames, List<Object[]> batch) {
    StringBuilder body = new StringBuilder();
    for (Object[] fieldValues : batch) {
      JSONObject json = new JSONObject();
      for (int i = 0; i < columnNames.length; i++) {
        json.put(columnNames[i], fieldValues[i]);
      }

      body.append("{\"index\":{}}\n").
          append(json).append("\n");
    }
    return body.toString();
  }

}
