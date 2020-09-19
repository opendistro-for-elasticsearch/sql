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

    @Test
    public void testRegexp() throws IOException {
        verifyQuery("'a' regexp 'b'", "INT", "0");
        verifyQuery("'a' regexp '.*'", "INT", "1");
    }

    @Test
    public void testSubstr() throws IOException {
        verifyQuery("substr('hello', 2)", "TEXT", "ello");
        verifyQuery("substr('hello', 2, 2)", "TEXT", "el");
    }

    @Test
    public void testSubstring() throws IOException {
        verifyQuery("substring('hello', 2)", "TEXT", "ello");
        verifyQuery("substring('hello', 2, 2)", "TEXT", "el");
    }

    @Test
    public void testUpper() throws IOException {
        verifyQuery("upper('hello')", "TEXT", "HELLO");
        verifyQuery("upper('HELLO')", "TEXT", "HELLO");
    }

    @Test
    public void testLower() throws IOException {
        verifyQuery("lower('hello')", "TEXT", "hello");
        verifyQuery("lower('HELLO')", "TEXT", "hello");
    }

    @Test
    public void testTrim() throws IOException {
        verifyQuery("trim(' hello')", "TEXT", "hello");
        verifyQuery("trim('hello ')", "TEXT", "hello");
        verifyQuery("trim('  hello  ')", "TEXT", "hello");
    }

    @Test
    public void testRtrim() throws IOException {
        verifyQuery("rtrim(' hello')", "TEXT", " hello");
        verifyQuery("rtrim('hello ')", "TEXT", "hello");
        verifyQuery("rtrim('  hello  ')", "TEXT", "  hello");
    }

    @Test
    public void testLtrim() throws IOException {
        verifyQuery("ltrim(' hello')", "TEXT", "hello ");
        verifyQuery("ltrim('hello ')", "TEXT", "hello");
        verifyQuery("ltrim('  hello  ')", "TEXT", "hello  ");
    }

    @Test
    public void testConcat() throws IOException {
        verifyQuery("concat('hello', 'world')", "TEXT", "helloworld");
        verifyQuery("concat('', 'hello')", "TEXT", "hello");
    }

    @Test
    public void testConcat_ws() throws IOException {
        verifyQuery("concat_ws(',', 'hello', 'world')", "TEXT", "hello,world");
        verifyQuery("concat_ws(',', '', 'hello')", "TEXT", ",hello");
    }

    @Test
    public void testLength() throws IOException {
        verifyQuery("concat_ws(',', 'hello', 'world')", "INT", "hello,world");
        verifyQuery("concat_ws(',', '', 'hello')", "INT", ",hello");
    }

    @Test
    public void testStrcmp() throws IOException {
        verifyQuery("strcmp('hello', 'world')", "INT", "-1");
        verifyQuery("strcmp('hello', 'hello')", "INT", "0");
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