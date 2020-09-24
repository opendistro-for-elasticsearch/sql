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

public class TextFunctionIT extends SQLIntegTestCase {

  @Override
  public void init() throws Exception {
    super.init();
    TestUtils.enableNewQueryEngine(client());
  }

  void verifyQuery(String query, String type, String output) throws IOException {
    JSONObject result = executeQuery("select " + query);
    verifySchema(result, schema(query, null, type));
    verifyDataRows(result, rows(output));
  }

  void verifyQuery(String query, String type, Integer output) throws IOException {
    JSONObject result = executeQuery("select " + query);
    verifySchema(result, schema(query, null, type));
    verifyDataRows(result, rows(output));
  }

  @Test
  public void testRegexp() throws IOException {
    verifyQuery("'a' regexp 'b'", "integer", 0);
    verifyQuery("'a' regexp '.*'", "integer", 1);
  }

  @Test
  public void testSubstr() throws IOException {
    verifyQuery("substr('hello', 2)", "string", "ello");
    verifyQuery("substr('hello', 2, 2)", "string", "el");
  }

  @Test
  public void testSubstring() throws IOException {
    verifyQuery("substring('hello', 2)", "string", "ello");
    verifyQuery("substring('hello', 2, 2)", "string", "el");
  }

  @Test
  public void testUpper() throws IOException {
    verifyQuery("upper('hello')", "string", "HELLO");
    verifyQuery("upper('HELLO')", "string", "HELLO");
  }

  @Test
  public void testLower() throws IOException {
    verifyQuery("lower('hello')", "string", "hello");
    verifyQuery("lower('HELLO')", "string", "hello");
  }

  @Test
  public void testTrim() throws IOException {
    verifyQuery("trim(' hello')", "string", "hello");
    verifyQuery("trim('hello ')", "string", "hello");
    verifyQuery("trim('  hello  ')", "string", "hello");
  }

  @Test
  public void testRtrim() throws IOException {
    verifyQuery("rtrim(' hello')", "string", " hello");
    verifyQuery("rtrim('hello ')", "string", "hello");
    verifyQuery("rtrim('  hello  ')", "string", "  hello");
  }

  @Test
  public void testLtrim() throws IOException {
    verifyQuery("ltrim(' hello')", "string", "hello");
    verifyQuery("ltrim('hello ')", "string", "hello ");
    verifyQuery("ltrim('  hello  ')", "string", "hello  ");
  }

  @Test
  public void testConcat() throws IOException {
    verifyQuery("concat('hello', 'world')", "string", "helloworld");
    verifyQuery("concat('', 'hello')", "string", "hello");
  }

  @Test
  public void testConcat_ws() throws IOException {
    verifyQuery("concat_ws(',', 'hello', 'world')", "string", "hello,world");
    verifyQuery("concat_ws(',', '', 'hello')", "string", ",hello");
  }

  @Test
  public void testLength() throws IOException {
    verifyQuery("length('hello')", "integer", 5);
  }

  @Test
  public void testStrcmp() throws IOException {
    verifyQuery("strcmp('hello', 'world')", "integer", -1);
    verifyQuery("strcmp('hello', 'hello')", "integer", 0);
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
