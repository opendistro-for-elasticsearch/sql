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

package com.amazon.opendistroforelasticsearch.sql.sql;

import static com.amazon.opendistroforelasticsearch.sql.legacy.TestUtils.getResponseBody;
import static com.amazon.opendistroforelasticsearch.sql.legacy.plugin.RestSqlAction.QUERY_API_ENDPOINT;

import com.amazon.opendistroforelasticsearch.sql.legacy.RestIntegTestCase;
import java.io.IOException;
import java.util.Locale;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Integration test for different type of literal values such as integer, decimal, boolean etc.
 */
@Ignore
public class LiteralValueIT extends RestIntegTestCase {

  @Test
  public void testSelectLiterals() {
    // TODO: Temporary manual assertion and will be replaced by comparison test soon.
    String expected =
        "{\n"
            + "  \"schema\": [\n"
            + "    {\n"
            + "      \"name\": \"123\",\n"
            + "      \"type\": \"integer\"\n"
            + "    },\n"
            + "    {\n"
            + "      \"name\": \"\\\"hello\\\"\",\n"
            + "      \"type\": \"string\"\n"
            + "    },\n"
            + "    {\n"
            + "      \"name\": \"false\",\n"
            + "      \"type\": \"boolean\"\n"
            + "    },\n"
            + "    {\n"
            + "      \"name\": \"-4.567\",\n"
            + "      \"type\": \"double\"\n"
            + "    }\n"
            + "  ],\n"
            + "  \"total\": 1,\n"
            + "  \"datarows\": [[\n"
            + "    123,\n"
            + "    \"hello\",\n"
            + "    false,\n"
            + "    -4.567\n"
            + "  ]],\n"
            + "  \"size\": 1\n"
            + "}\n";

    assertEquals(expected, executeQuery("SELECT 123, 'hello', false, -4.567"));
  }

  /**
   * This is temporary and would be replaced by comparison test very soon.
   */
  private String executeQuery(String query) {
    Request request = new Request("POST", QUERY_API_ENDPOINT);
    request.setJsonEntity(String.format(Locale.ROOT, "{\n" + "  \"query\": \"%s\"\n" + "}", query));

    RequestOptions.Builder restOptionsBuilder = RequestOptions.DEFAULT.toBuilder();
    restOptionsBuilder.addHeader("Content-Type", "application/json");
    request.setOptions(restOptionsBuilder);

    try {
      Response response = client().performRequest(request);
      Assert.assertEquals(200, response.getStatusLine().getStatusCode());
      return getResponseBody(response, true);
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

}
