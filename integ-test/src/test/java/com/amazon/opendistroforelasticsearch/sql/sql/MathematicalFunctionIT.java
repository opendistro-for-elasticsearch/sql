/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.opendistroforelasticsearch.sql.sql;

import static com.amazon.opendistroforelasticsearch.sql.legacy.plugin.RestSqlAction.QUERY_API_ENDPOINT;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.rows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.schema;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifyDataRows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifySchema;
import static com.amazon.opendistroforelasticsearch.sql.util.TestUtils.getResponseBody;

import com.amazon.opendistroforelasticsearch.sql.legacy.SQLIntegTestCase;
import com.amazon.opendistroforelasticsearch.sql.util.TestUtils;
import java.io.IOException;
import java.util.Locale;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class MathematicalFunctionIT extends SQLIntegTestCase {

  @Override
  public void init() throws Exception {
    super.init();
    TestUtils.enableNewQueryEngine(client());
  }

  @Test
  public void testConv() throws IOException {
    JSONObject result = executeQuery("select conv(11, 10, 16)");
    verifySchema(result, schema("conv(11, 10, 16)", null, "string"));
    verifyDataRows(result, rows("b"));

    result = executeQuery("select conv(11, 16, 10)");
    verifySchema(result, schema("conv(11, 16, 10)", null, "string"));
    verifyDataRows(result, rows("17"));
  }

  @Test
  public void testCrc32() throws IOException {
    JSONObject result = executeQuery("select crc32('MySQL')");
    verifySchema(result, schema("crc32(\"MySQL\")", null, "long"));
    verifyDataRows(result, rows(3259397556L));
  }

  @Test
  public void testE() throws IOException {
    JSONObject result = executeQuery("select e()");
    verifySchema(result, schema("e()", null, "double"));
    verifyDataRows(result, rows(Math.E));
  }

  @Test
  public void testMod() throws IOException {
    JSONObject result = executeQuery("select mod(3, 2)");
    verifySchema(result, schema("mod(3, 2)", null, "integer"));
    verifyDataRows(result, rows(1));

    result = executeQuery("select mod(3.1, 2)");
    verifySchema(result, schema("mod(3.1, 2)", null, "double"));
    verifyDataRows(result, rows(1.1));
  }

  @Test
  public void testRound() throws IOException {
    JSONObject result = executeQuery("select round(56.78)");
    verifySchema(result, schema("round(56.78)", null, "double"));
    verifyDataRows(result, rows(57));

    result = executeQuery("select round(56.78, 1)");
    verifySchema(result, schema("round(56.78, 1)", null, "double"));
    verifyDataRows(result, rows(56.8));

    result = executeQuery("select round(56.78, -1)");
    verifySchema(result, schema("round(56.78, -1)", null, "double"));
    verifyDataRows(result, rows(60));

    result = executeQuery("select round(-56)");
    verifySchema(result, schema("round(-56)", null, "long"));
    verifyDataRows(result, rows(-56));

    result = executeQuery("select round(-56, 1)");
    verifySchema(result, schema("round(-56, 1)", null, "long"));
    verifyDataRows(result, rows(-56));

    result = executeQuery("select round(-56, -1)");
    verifySchema(result, schema("round(-56, -1)", null, "long"));
    verifyDataRows(result, rows(-60));
  }

  /**
   * Test sign function with double value.
   */
  @Test
  public void testSign() throws IOException {
    JSONObject result = executeQuery("select sign(1.1)");
    verifySchema(result, schema("sign(1.1)", null, "integer"));
    verifyDataRows(result, rows(1));

    result = executeQuery("select sign(-1.1)");
    verifySchema(result, schema("sign(-1.1)", null, "integer"));
    verifyDataRows(result, rows(-1));
  }

  @Test
  public void testTruncate() throws IOException {
    JSONObject result = executeQuery("select truncate(56.78, 1)");
    verifySchema(result, schema("truncate(56.78, 1)", null, "double"));
    verifyDataRows(result, rows(56.7));

    result = executeQuery("select truncate(56.78, -1)");
    verifySchema(result, schema("truncate(56.78, -1)", null, "double"));
    verifyDataRows(result, rows(50));

    result = executeQuery("select truncate(-56, 1)");
    verifySchema(result, schema("truncate(-56, 1)", null, "long"));
    verifyDataRows(result, rows(-56));

    result = executeQuery("select truncate(-56, -1)");
    verifySchema(result, schema("truncate(-56, -1)", null, "long"));
    verifyDataRows(result, rows(-50));
  }

  protected JSONObject executeQuery(String query) throws IOException {
    Request request = new Request("POST", QUERY_API_ENDPOINT);
    request.setJsonEntity(String.format(Locale.ROOT, "{\n" + "  \"query\": \"%s\"\n" + "}", query));

    RequestOptions.Builder restOptionsBuilder = RequestOptions.DEFAULT.toBuilder();
    restOptionsBuilder.addHeader("Content-Type", "application/json");
    request.setOptions(restOptionsBuilder);

    Response response = client().performRequest(request);
    return new JSONObject(getResponseBody(response));
  }
}
