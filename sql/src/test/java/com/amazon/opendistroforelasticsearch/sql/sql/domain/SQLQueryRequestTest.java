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

package com.amazon.opendistroforelasticsearch.sql.sql.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazon.opendistroforelasticsearch.sql.protocol.response.format.Format;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class SQLQueryRequestTest {

  @Test
  public void shouldSupportQuery() {
    SQLQueryRequest request = SQLQueryRequestBuilder.request("SELECT 1").build();
    assertTrue(request.isSupported());
  }

  @Test
  public void shouldSupportQueryWithJDBCFormat() {
    SQLQueryRequest request = SQLQueryRequestBuilder.request("SELECT 1")
                                                    .format("jdbc")
                                                    .build();
    assertTrue(request.isSupported());
    assertEquals(request.format(), Format.JDBC);
  }

  @Test
  public void shouldSupportQueryWithQueryFieldOnly() {
    SQLQueryRequest request =
        SQLQueryRequestBuilder.request("SELECT 1")
                              .jsonContent("{\"query\": \"SELECT 1\"}")
                              .build();
    assertTrue(request.isSupported());
  }

  @Test
  public void shouldSupportQueryWithZeroFetchSize() {
    SQLQueryRequest request =
        SQLQueryRequestBuilder.request("SELECT 1")
                              .jsonContent("{\"query\": \"SELECT 1\", \"fetch_size\": 0}")
                              .build();
    assertTrue(request.isSupported());
  }

  @Test
  public void shouldSupportExplain() {
    SQLQueryRequest explainRequest =
        SQLQueryRequestBuilder.request("SELECT 1")
                              .path("_opendistro/_sql/_explain")
                              .build();
    assertTrue(explainRequest.isExplainRequest());
    assertTrue(explainRequest.isSupported());
  }

  @Test
  public void shouldNotSupportCursorRequest() {
    SQLQueryRequest fetchSizeRequest =
        SQLQueryRequestBuilder.request("SELECT 1")
                              .jsonContent("{\"query\": \"SELECT 1\", \"fetch_size\": 5}")
                              .build();
    assertFalse(fetchSizeRequest.isSupported());

    SQLQueryRequest cursorRequest =
        SQLQueryRequestBuilder.request("SELECT 1")
                              .jsonContent("{\"cursor\": \"abcdefgh...\"}")
                              .build();
    assertFalse(cursorRequest.isSupported());
  }

  @Test
  public void shouldUseJDBCFormatByDefault() {
    SQLQueryRequest request =
        SQLQueryRequestBuilder.request("SELECT 1").params(ImmutableMap.of()).build();
    assertEquals(request.format(), Format.JDBC);
  }

  @Test
  public void shouldSupportCSVFormatAndSanitize() {
    SQLQueryRequest csvRequest =
        SQLQueryRequestBuilder.request("SELECT 1")
                              .format("csv")
                              .build();
    assertTrue(csvRequest.isSupported());
    assertEquals(csvRequest.format(), Format.CSV);
    assertTrue(csvRequest.sanitize());
  }

  @Test
  public void shouldSkipSanitizeIfSetFalse() {
    ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
    Map<String, String> params = builder.put("format", "csv").put("sanitize", "false").build();
    SQLQueryRequest csvRequest = SQLQueryRequestBuilder.request("SELECT 1").params(params).build();
    assertEquals(csvRequest.format(), Format.CSV);
    assertFalse(csvRequest.sanitize());
  }

  @Test
  public void shouldNotSupportRawFormat() {
    SQLQueryRequest csvRequest =
        SQLQueryRequestBuilder.request("SELECT 1")
            .format("raw")
            .build();
    assertFalse(csvRequest.isSupported());
    assertThrows(IllegalArgumentException.class, csvRequest::format,
        "response in raw format is not supported.");
  }

  /**
   * SQL query request build helper to improve test data setup readability.
   */
  private static class SQLQueryRequestBuilder {
    private String jsonContent;
    private String query;
    private String path = "_/opendistro/_sql";
    private String format;
    private Map<String, String> params;

    static SQLQueryRequestBuilder request(String query) {
      SQLQueryRequestBuilder builder = new SQLQueryRequestBuilder();
      builder.query = query;
      return builder;
    }

    SQLQueryRequestBuilder jsonContent(String jsonContent) {
      this.jsonContent = jsonContent;
      return this;
    }

    SQLQueryRequestBuilder path(String path) {
      this.path = path;
      return this;
    }

    SQLQueryRequestBuilder format(String format) {
      this.format = format;
      return this;
    }

    SQLQueryRequestBuilder params(Map<String, String> params) {
      this.params = params;
      return this;
    }

    SQLQueryRequest build() {
      if (jsonContent == null) {
        jsonContent = "{\"query\": \"" + query + "\"}";
      }
      if (params != null) {
        return new SQLQueryRequest(new JSONObject(jsonContent), query, path, params);
      }
      return new SQLQueryRequest(new JSONObject(jsonContent), query, path, format);
    }
  }

}