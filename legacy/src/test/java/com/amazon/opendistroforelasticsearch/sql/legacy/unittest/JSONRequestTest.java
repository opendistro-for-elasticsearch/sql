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

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest;

import com.alibaba.druid.sql.parser.ParserException;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.ColumnTypeProvider;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.QueryActionRequest;
import com.amazon.opendistroforelasticsearch.sql.legacy.util.TestsConstants;
import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.Format;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.format.Schema;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.ESActionFactory;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.QueryAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.SqlElasticRequestBuilder;
import com.amazon.opendistroforelasticsearch.sql.legacy.request.SqlRequest;
import com.amazon.opendistroforelasticsearch.sql.legacy.util.CheckScriptContents;
import com.google.common.io.Files;
import org.elasticsearch.client.Client;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLFeatureNotSupportedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class JSONRequestTest {

    @Mock
    private ColumnTypeProvider columnTypeProvider;

    @Before
    public void setup() {
        when(columnTypeProvider.get(anyInt())).thenReturn(Schema.Type.DOUBLE);
    }

    @Test
    public void aggWithoutWhere() {
        String explainSQL = explainSQL("SELECT name, COUNT(nested(projects, 'projects')) AS c " +
                                       "FROM employee " +
                                       "GROUP BY name " +
                                       "HAVING c > 1");
        assertThat(explainSQL, containsString(
                "\"projects@NESTED\":{\"nested\":{\"path\":\"projects\"},\"aggregations\":{\"c\":{\"value_count\":{\"field\":\"_index\"}}}"));
        assertThat(explainSQL, containsString(
                "\"buckets_path\":{\"c\":\"projects@NESTED.c\"}"));
    }

    @Test
    public void aggWithWhereOnParent() {
        String explainSQL = explainSQL("SELECT name, COUNT(nested(projects, 'projects')) AS c " +
                                       "FROM employee " +
                                       "WHERE name LIKE '%smith%' " +
                                       "GROUP BY name " +
                                       "HAVING c > 1");

        assertThat(explainSQL, containsString(
                "\"projects@NESTED\":{\"nested\":{\"path\":\"projects\"},\"aggregations\":{\"c\":{\"value_count\":{\"field\":\"_index\"}}}}"));
        assertThat(explainSQL, containsString(
                "\"buckets_path\":{\"c\":\"projects@NESTED.c\"}"));
    }

    @Test
    public void aggWithWhereOnNested() {
        String explainSQL = explainSQL("SELECT name, COUNT(nested(projects, 'projects')) AS c " +
                                       "FROM employee " +
                                       "WHERE nested(projects.name, 'projects') LIKE '%security%' " +
                                       "GROUP BY name " +
                                       "HAVING c > 1");

        assertThat(explainSQL, containsString("\"aggregations\":{\"projects@NESTED\":{\"nested\":{\"path\":\"projects\"},\"aggregations\":{\"projects@FILTER\":{\"filter\":{\"bool\":{\"must\":[{\"wildcard\":{\"projects.name\":{\"wildcard\":\"*security*\",\"boost\":1.0}}}],\"adjust_pure_negative\":true,\"boost\":1.0}},\"aggregations\":{\"c\":{\"value_count\":{\"field\":\"_index\"}}}}}}"));
        assertThat(explainSQL, containsString("\"buckets_path\":{\"c\":\"projects@NESTED>projects@FILTER.c\"}"));
    }

    @Test
    public void aggWithWhereOnParentOrNested() {
        String explainSQL = explainSQL("SELECT name, COUNT(nested(projects, 'projects')) AS c " +
                                       "FROM employee " +
                                       "WHERE name LIKE '%smith%' OR nested(projects.name, 'projects') LIKE '%security%' " +
                                       "GROUP BY name " +
                                       "HAVING c > 1");
        assertThat(explainSQL, containsString(
                "\"projects@NESTED\":{\"nested\":{\"path\":\"projects\"},\"aggregations\":{\"c\":{\"value_count\":{\"field\":\"_index\"}}}}"));
        assertThat(explainSQL, containsString(
                "\"buckets_path\":{\"c\":\"projects@NESTED.c\"}"));
    }

    @Test
    public void aggWithWhereOnParentAndNested() {
        String explainSQL = explainSQL("SELECT name, COUNT(nested(projects, 'projects')) AS c " +
                                       "FROM employee " +
                                       "WHERE name LIKE '%smith%' AND nested(projects.name, 'projects') LIKE '%security%' " +
                                       "GROUP BY name " +
                                       "HAVING c > 1");
        assertThat(explainSQL, containsString(
                "\"aggregations\":{\"projects@NESTED\":{\"nested\":{\"path\":\"projects\"},\"aggregations\":{\"projects@FILTER\":{\"filter\":{\"bool\":{\"must\":[{\"wildcard\":{\"projects.name\":{\"wildcard\":\"*security*\",\"boost\":1.0}}}],\"adjust_pure_negative\":true,\"boost\":1.0}},\"aggregations\":{\"c\":{\"value_count\":{\"field\":\"_index\"}}}}}"));
        assertThat(explainSQL, containsString("\"buckets_path\":{\"c\":\"projects@NESTED>projects@FILTER.c\"}"));
    }

    @Test
    public void aggWithWhereOnNestedAndNested() {
        String explainSQL = explainSQL("SELECT name, COUNT(nested(projects, 'projects')) AS c " +
                                       "FROM employee " +
                                       "WHERE nested('projects', projects.started_year > 2000 AND projects.name LIKE '%security%') " +
                                       "GROUP BY name " +
                                       "HAVING c > 1");
        assertThat(explainSQL, containsString("\"aggregations\":{\"projects@NESTED\":{\"nested\":{\"path\":\"projects\"},\"aggregations\":{\"projects@FILTER\":{\"filter\":{\"bool\":{\"must\":[{\"bool\":{\"must\":[{\"range\":{\"projects.started_year\":{\"from\":2000,\"to\":null,\"include_lower\":false,\"include_upper\":true,\"boost\":1.0}}},{\"wildcard\":{\"projects.name\":{\"wildcard\":\"*security*\",\"boost\":1.0}}}"));
        assertThat(explainSQL, containsString("\"buckets_path\":{\"c\":\"projects@NESTED>projects@FILTER.c\"}"));
    }

    @Test
    public void aggWithWhereOnNestedOrNested() {
        String explainSQL = explainSQL("SELECT name, COUNT(nested(projects, 'projects')) AS c " +
                                       "FROM employee " +
                                       "WHERE nested('projects', projects.started_year > 2000 OR projects.name LIKE '%security%') " +
                                       "GROUP BY name " +
                                       "HAVING c > 1");
        assertThat(explainSQL, containsString("\"aggregations\":{\"projects@NESTED\":{\"nested\":{\"path\":\"projects\"},\"aggregations\":{\"projects@FILTER\":{\"filter\":{\"bool\":{\"must\":[{\"bool\":{\"should\":[{\"range\":{\"projects.started_year\":{\"from\":2000,\"to\":null,\"include_lower\":false,\"include_upper\":true,\"boost\":1.0}}},{\"wildcard\":{\"projects.name\":{\"wildcard\":\"*security*\",\"boost\":1.0}}}"));
        assertThat(explainSQL, containsString("\"buckets_path\":{\"c\":\"projects@NESTED>projects@FILTER.c\"}"));
    }

    @Test
    public void aggInHavingWithoutWhere() {
        JSONObject explainSQL = explainSQLToJson("SELECT name " +
                                                 "FROM employee " +
                                                 "GROUP BY name " +
                                                 "HAVING COUNT(nested(projects, 'projects')) > 1");
        assertThat(
                query(explainSQL, "/aggregations/name/aggregations/projects@NESTED/aggregations/count_0/value_count"),
                equalTo("{\"field\":\"_index\"}"));
        assertThat(
                query(explainSQL, "/aggregations/name/aggregations/bucket_filter/bucket_selector/buckets_path"),
                equalTo("{\"count_0\":\"projects@NESTED.count_0\"}"));
    }

    @Test
    public void aggInHavingWithWhereOnParent() {
        JSONObject explainSQL = explainSQLToJson("SELECT name " +
                                                 "FROM employee " +
                                                 "WHERE name LIKE '%smith%' " +
                                                 "GROUP BY name " +
                                                 "HAVING COUNT(nested(projects, 'projects')) > 1");
        assertThat(
                query(explainSQL, "/aggregations/name/aggregations/projects@NESTED/aggregations/count_0/value_count"),
                equalTo("{\"field\":\"_index\"}"));
        assertThat(
                query(explainSQL, "/aggregations/name/aggregations/bucket_filter/bucket_selector/buckets_path"),
                equalTo("{\"count_0\":\"projects@NESTED.count_0\"}"));
    }

    @Test
    public void aggInHavingWithWhereOnNested() {
        JSONObject explainSQL = explainSQLToJson("SELECT name " +
                                                 "FROM employee " +
                                                 "WHERE nested(projects.name, 'projects') LIKE '%security%' " +
                                                 "GROUP BY name " +
                                                 "HAVING COUNT(nested(projects, 'projects')) > 1");
        assertThat(
                query(explainSQL, "/aggregations/name/aggregations/projects@NESTED/aggregations/projects@FILTER/aggregations/count_0/value_count"),
                equalTo("{\"field\":\"_index\"}"));
        assertThat(
                query(explainSQL, "/aggregations/name/aggregations/bucket_filter/bucket_selector/buckets_path"),
                equalTo("{\"count_0\":\"projects@NESTED>projects@FILTER.count_0\"}"));
        assertThat(
                query(explainSQL, "/aggregations/name/aggregations/projects@NESTED/aggregations/projects@FILTER/filter/bool/must"),
                equalTo("[{\"wildcard\":{\"projects.name\":{\"boost\":1,\"wildcard\":\"*security*\"}}}]"));
    }

    @Test
    public void aggInHavingWithWhereOnParentOrNested() {
        JSONObject explainSQL = explainSQLToJson("SELECT name " +
                                                 "FROM employee " +
                                                 "WHERE name LIKE '%smith%' OR nested(projects.name, 'projects') LIKE '%security%' " +
                                                 "GROUP BY name " +
                                                 "HAVING COUNT(nested(projects, 'projects')) > 1");
        assertThat(
                query(explainSQL, "/aggregations/name/aggregations/projects@NESTED/aggregations/count_0/value_count"),
                equalTo("{\"field\":\"_index\"}"));
        assertThat(
                query(explainSQL, "/aggregations/name/aggregations/bucket_filter/bucket_selector/buckets_path"),
                equalTo("{\"count_0\":\"projects@NESTED.count_0\"}"));
    }

    @Test
    public void aggInHavingWithWhereOnParentAndNested() {
        JSONObject explainSQL = explainSQLToJson("SELECT name " +
                                                 "FROM employee " +
                                                 "WHERE name LIKE '%smith%' AND nested(projects.name, 'projects') LIKE '%security%' " +
                                                 "GROUP BY name " +
                                                 "HAVING COUNT(nested(projects, 'projects')) > 1");
        assertThat(
                query(explainSQL, "/aggregations/name/aggregations/projects@NESTED/aggregations/projects@FILTER/aggregations/count_0/value_count"),
                equalTo("{\"field\":\"_index\"}"));
        assertThat(
                query(explainSQL, "/aggregations/name/aggregations/bucket_filter/bucket_selector/buckets_path"),
                equalTo("{\"count_0\":\"projects@NESTED>projects@FILTER.count_0\"}"));
        assertThat(
                query(explainSQL, "/aggregations/name/aggregations/projects@NESTED/aggregations/projects@FILTER/filter/bool/must"),
                equalTo("[{\"wildcard\":{\"projects.name\":{\"boost\":1,\"wildcard\":\"*security*\"}}}]"));
    }

    @Test
    public void aggInHavingWithWhereOnNestedAndNested() {
        JSONObject explainSQL = explainSQLToJson("SELECT name " +
                                                 "FROM employee " +
                                                 "WHERE nested('projects', projects.started_year > 2000 AND projects.name LIKE '%security%') " +
                                                 "GROUP BY name " +
                                                 "HAVING COUNT(nested(projects, 'projects')) > 1");

        assertThat(
                query(explainSQL, "/aggregations/name/aggregations/projects@NESTED/aggregations/projects@FILTER/aggregations/count_0/value_count"),
                equalTo("{\"field\":\"_index\"}"));
        assertThat(
                query(explainSQL, "/aggregations/name/aggregations/bucket_filter/bucket_selector/buckets_path"),
                equalTo("{\"count_0\":\"projects@NESTED>projects@FILTER.count_0\"}"));
        assertThat(
                query(explainSQL, "/aggregations/name/aggregations/projects@NESTED/aggregations/projects@FILTER/filter/bool/must"),
                equalTo("[{\"bool\":{\"adjust_pure_negative\":true,\"must\":[{\"range\":{\"projects.started_year\":{\"include_lower\":false,\"include_upper\":true,\"from\":2000,\"boost\":1,\"to\":null}}},{\"wildcard\":{\"projects.name\":{\"boost\":1,\"wildcard\":\"*security*\"}}}],\"boost\":1}}]"));
    }

    @Test
    public void aggInHavingWithWhereOnNestedOrNested() {
        JSONObject explainSQL = explainSQLToJson("SELECT name " +
                                                 "FROM employee " +
                                                 "WHERE nested('projects', projects.started_year > 2000 OR projects.name LIKE '%security%') " +
                                                 "GROUP BY name " +
                                                 "HAVING COUNT(nested(projects, 'projects')) > 1");
        assertThat(
                query(explainSQL,
                        "/aggregations/name/aggregations/projects@NESTED/aggregations/projects@FILTER/aggregations/count_0/value_count"),
                equalTo("{\"field\":\"_index\"}"));
        assertThat(
                query(explainSQL, "/aggregations/name/aggregations/bucket_filter/bucket_selector/buckets_path"),
                equalTo("{\"count_0\":\"projects@NESTED>projects@FILTER.count_0\"}"));
        assertThat(
                query(explainSQL,
                        "/aggregations/name/aggregations/projects@NESTED/aggregations/projects@FILTER/filter/bool/must"),
                equalTo("[{\"bool\":{\"adjust_pure_negative\":true,\"should\":[{\"range\":{\"projects.started_year\":{\"include_lower\":false,\"include_upper\":true,\"from\":2000,\"boost\":1,\"to\":null}}},{\"wildcard\":{\"projects.name\":{\"boost\":1,\"wildcard\":\"*security*\"}}}],\"boost\":1}}]"));
    }

    @Test
    public void searchSanity() throws IOException {
        String result = explain(String.format("{\"query\":\"" +
                "SELECT * " +
                "FROM %s " +
                "WHERE firstname LIKE 'A%%' AND age > 20 " +
                "GROUP BY gender " +
                "ORDER BY _score\"}", TestsConstants.TEST_INDEX_ACCOUNT));
        String expectedOutput = Files.toString(
                new File(getResourcePath() + "src/test/resources/expectedOutput/search_explain.json"), StandardCharsets.UTF_8)
                .replaceAll("\r", "");

        assertThat(removeSpaces(result), equalTo(removeSpaces(expectedOutput)));
    }

    // This test was ignored because group by case function is not supported
    @Ignore
    @Test
    public void aggregationQuery() throws IOException {
        String result = explain(String.format("{\"query\":\"" +
                "SELECT address, CASE WHEN gender='0' THEN 'aaa' ELSE 'bbb' END AS a2345, count(age) " +
                "FROM %s " +
                "GROUP BY terms('field'='address','execution_hint'='global_ordinals'), a2345\"}", TestsConstants.TEST_INDEX_ACCOUNT));
        String expectedOutput = Files.toString(
                new File(getResourcePath() + "src/test/resources/expectedOutput/aggregation_query_explain.json"), StandardCharsets.UTF_8)
                .replaceAll("\r", "");

        assertThat(removeSpaces(result), equalTo(removeSpaces(expectedOutput)));
    }

    @Test
    public void deleteSanity() throws IOException {
        String result = explain(String.format("{\"query\":\"" +
                "DELETE " +
                "FROM %s " +
                "WHERE firstname LIKE 'A%%' AND age > 20\"}", TestsConstants.TEST_INDEX_ACCOUNT));
        String expectedOutput = Files.toString(
                new File(getResourcePath() + "src/test/resources/expectedOutput/delete_explain.json"), StandardCharsets.UTF_8)
                .replaceAll("\r", "");

        assertThat(removeSpaces(result), equalTo(removeSpaces(expectedOutput)));
    }

    @Test
    public void queryFilter() throws IOException {
        /*
         * Human readable format of the request defined below:
         * {
         *   "query": "SELECT * FROM accounts WHERE age > 25",
         *   "filter": {
         *     "range": {
         *       "balance": {
         *         "lte": 30000
         *       }
         *     }
         *   }
         * }
         */
        String result = explain(String.format("{\"query\":\"" +
                "SELECT * " +
                "FROM %s " +
                "WHERE age > 25\"," +
                "\"filter\":{\"range\":{\"balance\":{\"lte\":30000}}}}", TestsConstants.TEST_INDEX_ACCOUNT));
        String expectedOutput = Files.toString(
                new File(getResourcePath() + "src/test/resources/expectedOutput/json_filter_explain.json"), StandardCharsets.UTF_8)
                .replaceAll("\r", "");

        assertThat(removeSpaces(result), equalTo(removeSpaces(expectedOutput)));
    }

    private String removeSpaces(String s) {
        return s.replaceAll("\\s+", "");
    }

    private String explainSQL(String sql) {
        return explain(String.format("{\"query\":\"%s\"}", sql));
    }

    private JSONObject explainSQLToJson(String sql) {
        return new JSONObject(explain(String.format("{\"query\":\"%s\"}", sql)));
    }

    private String query(JSONObject jsonObject, String jsonPath) {
        return jsonObject.query(jsonPath).toString();
    }

    private String explain(String request) {
        try {
            JSONObject jsonRequest = new JSONObject(request);
            String sql = jsonRequest.getString("query");

            return translate(sql, jsonRequest);
        } catch (SqlParseException | SQLFeatureNotSupportedException e) {
            throw new ParserException("Illegal sql expr in request: " + request);
        }
    }

    private String translate(String sql, JSONObject jsonRequest) throws SQLFeatureNotSupportedException, SqlParseException {
        Client mockClient = Mockito.mock(Client.class);
        CheckScriptContents.stubMockClient(mockClient);
        QueryAction queryAction =
                ESActionFactory.create(mockClient, new QueryActionRequest(sql, columnTypeProvider, Format.JDBC));

        SqlRequest sqlRequest = new SqlRequest(sql, jsonRequest);
        queryAction.setSqlRequest(sqlRequest);

        SqlElasticRequestBuilder requestBuilder = queryAction.explain();
        return requestBuilder.explain();
    }

    private String getResourcePath() {
        String projectRoot = System.getProperty("project.root");
        if ( projectRoot!= null && projectRoot.trim().length() > 0) {
            return projectRoot.trim() + "/";
        } else {
            return "";
        }
    }
}
