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

package com.amazon.opendistroforelasticsearch.sql.esintgtest;

import com.alibaba.druid.sql.parser.ParserException;
import com.amazon.opendistroforelasticsearch.sql.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.query.ESActionFactory;
import com.amazon.opendistroforelasticsearch.sql.query.QueryAction;
import com.amazon.opendistroforelasticsearch.sql.query.SqlElasticRequestBuilder;
import com.amazon.opendistroforelasticsearch.sql.request.SqlRequest;
import com.amazon.opendistroforelasticsearch.sql.util.CheckScriptContents;
import com.amazon.opendistroforelasticsearch.sql.utils.JsonPrettyFormatter;
import com.google.common.io.Files;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.test.ESIntegTestCase;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLFeatureNotSupportedException;

import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_ACCOUNT;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_BANK;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_DOG;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_LOCATION;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_NESTED_TYPE;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_PEOPLE;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_PHRASE;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class ExplainIT extends SQLIntegTestCase {

    @Override
    protected void init() throws Exception {
        loadIndex(Index.ACCOUNT);
        loadIndex(Index.DOG);
        loadIndex(Index.PEOPLE);
        loadIndex(Index.PHRASE);
        loadIndex(Index.LOCATION);
        loadIndex(Index.NESTED);
    }

    @Test
    public void searchSanity() throws IOException {

        String expectedOutputFilePath = TestUtils.getResourceFilePath(
                "src/test/resources/expectedOutput/search_explain.json");
        String expectedOutput = Files.toString(new File(expectedOutputFilePath), StandardCharsets.UTF_8)
                .replaceAll("\r","");

        String result = explainQuery(String.format("SELECT * FROM %s WHERE firstname LIKE 'A%%' AND age > 20 " +
                "GROUP BY gender order by _score", TEST_INDEX_ACCOUNT));
        Assert.assertThat(result.replaceAll("\\s+",""), equalTo(expectedOutput.replaceAll("\\s+","")));
    }

    @Test
    public void aggregationQuery() throws IOException {

        String expectedOutputFilePath = TestUtils.getResourceFilePath(
                "src/test/resources/expectedOutput/aggregation_query_explain.json");
        String expectedOutput = Files.toString(new File(expectedOutputFilePath), StandardCharsets.UTF_8)
                .replaceAll("\r","");

        String result = explainQuery(String.format("SELECT a, CASE WHEN gender='0' then 'aaa' else 'bbb'end a2345," +
                        "count(c) FROM %s GROUP BY terms('field'='a','execution_hint'='global_ordinals'),a2345",
                TEST_INDEX_ACCOUNT));
        Assert.assertThat(result.replaceAll("\\s+",""), equalTo(expectedOutput.replaceAll("\\s+","")));
    }

    @Test
    public void explainScriptValue() throws IOException {

        String expectedOutputFilePath = TestUtils.getResourceFilePath(
                "src/test/resources/expectedOutput/script_value.json");
        String expectedOutput = Files.toString(new File(expectedOutputFilePath), StandardCharsets.UTF_8)
                .replaceAll("\r","");

        String result = explainQuery(String.format("SELECT case when gender is null then 'aaa' " +
                "else gender  end  test , cust_code FROM %s", TEST_INDEX_ACCOUNT));
        Assert.assertThat(result.replaceAll("\\s+",""), equalTo(expectedOutput.replaceAll("\\s+","")));
    }

    @Test
    public void betweenScriptValue() throws IOException {

        String expectedOutputFilePath = TestUtils.getResourceFilePath(
                "src/test/resources/expectedOutput/between_query.json");
        String expectedOutput = Files.toString(new File(expectedOutputFilePath), StandardCharsets.UTF_8)
                .replaceAll("\r","");

        String result = explainQuery(String.format("SELECT  case when value between 100 and 200 then 'aaa' " +
                "else value  end  test, cust_code FROM %s", TEST_INDEX_ACCOUNT));
        Assert.assertThat(result.replaceAll("\\s+",""), equalTo(expectedOutput.replaceAll("\\s+","")));
    }

    @Test
    public void searchSanityFilter() throws IOException {

        String expectedOutputFilePath = TestUtils.getResourceFilePath(
                "src/test/resources/expectedOutput/search_explain_filter.json");
        String expectedOutput = Files.toString(new File(expectedOutputFilePath), StandardCharsets.UTF_8)
                .replaceAll("\r","");

        String result = explainQuery(String.format("SELECT * FROM %s WHERE firstname LIKE 'A%%' " +
                "AND age > 20 GROUP BY gender", TEST_INDEX_ACCOUNT));
        Assert.assertThat(result.replaceAll("\\s+",""), equalTo(expectedOutput.replaceAll("\\s+","")));
    }

    @Test
    public void deleteSanity() throws IOException {

        String expectedOutputFilePath = TestUtils.getResourceFilePath(
                "src/test/resources/expectedOutput/delete_explain.json");
        String expectedOutput = Files.toString(new File(expectedOutputFilePath), StandardCharsets.UTF_8)
                .replaceAll("\r","");;

        String result = explainQuery(String.format("DELETE FROM %s WHERE firstname LIKE 'A%%' AND age > 20",
                TEST_INDEX_ACCOUNT));
        Assert.assertThat(result.replaceAll("\\s+",""), equalTo(expectedOutput.replaceAll("\\s+","")));
    }

    @Test
    public void spatialFilterExplainTest() throws IOException {

        String expectedOutputFilePath = TestUtils.getResourceFilePath(
                "src/test/resources/expectedOutput/search_spatial_explain.json");
        String expectedOutput = Files.toString(new File(expectedOutputFilePath), StandardCharsets.UTF_8)
                .replaceAll("\r","");;

        String result = explainQuery(String.format("SELECT * FROM %s WHERE GEO_INTERSECTS" +
                "(place,'POLYGON ((102 2, 103 2, 103 3, 102 3, 102 2))')", TEST_INDEX_LOCATION));
        Assert.assertThat(result.replaceAll("\\s+",""), equalTo(expectedOutput.replaceAll("\\s+","")));
    }

    @Test
    public void orderByOnNestedFieldTest() throws Exception {

        String result = explainQuery(String.format("SELECT * FROM %s ORDER BY NESTED('message.info','message')",
                TEST_INDEX_NESTED_TYPE));
        Assert.assertThat(result.replaceAll("\\s+", ""),
                equalTo("{\"from\":0,\"size\":200,\"sort\":[{\"message.info\":" +
                        "{\"order\":\"asc\",\"nested\":{\"path\":\"message\"}}}]}"));
    }

    @Test
    public void multiMatchQuery() throws IOException {

        String expectedOutputFilePath = TestUtils.getResourceFilePath(
                "src/test/resources/expectedOutput/multi_match_query.json");
        String expectedOutput = Files.toString(new File(expectedOutputFilePath), StandardCharsets.UTF_8)
                .replaceAll("\r", "");

        String result = explainQuery(String.format("SELECT * FROM %s WHERE q=multimatch(query='this is a test'," +
                "fields='subject^3,message',analyzer='standard',type='best_fields',boost=1.0," +
                "slop=0,tie_breaker=0.3,operator='and')", TEST_INDEX_ACCOUNT));
        Assert.assertThat(result.replaceAll("\\s+", ""), equalTo(expectedOutput.replaceAll("\\s+", "")));
    }

    @Test
    public void termsIncludeExcludeExplainTest() throws IOException {

        final String queryPrefix = "SELECT * FROM " + TEST_INDEX_PHRASE + " GROUP BY ";
        final String expected1 = "\"include\":\".*sport.*\",\"exclude\":\"water_.*\"";
        final String expected2 = "\"include\":[\"honda\",\"mazda\"],\"exclude\":[\"jensen\",\"rover\"]";
        final String expected3 = "\"include\":{\"partition\":0,\"num_partitions\":20}";

        String result = explainQuery(queryPrefix + " terms(field='correspond_brand_name',size='10'," +
                "alias='correspond_brand_name',include='\\\".*sport.*\\\"',exclude='\\\"water_.*\\\"')");
        Assert.assertThat(result, containsString(expected1));

        result = explainQuery(queryPrefix + "terms(field='correspond_brand_name',size='10'," +
                "alias='correspond_brand_name',include='[\\\"mazda\\\", \\\"honda\\\"]'," +
                "exclude='[\\\"rover\\\", \\\"jensen\\\"]')");
        Assert.assertThat(result, containsString(expected2));

        result = explainQuery(queryPrefix + "terms(field='correspond_brand_name',size='10'," +
                "alias='correspond_brand_name',include='{\\\"partition\\\":0,\\\"num_partitions\\\":20}')");
        Assert.assertThat(result, containsString(expected3));
    }

    @Test
    public void explainNLJoin() throws IOException {

        String expectedOutputFilePath = TestUtils.getResourceFilePath(
                "src/test/resources/expectedOutput/nested_loop_join_explain.json");
        String expectedOutput = Files.toString(new File(expectedOutputFilePath), StandardCharsets.UTF_8)
                .replaceAll("\r", "");

        String query = "SELECT /*! USE_NL*/ a.firstname ,a.lastname , a.gender ,d.dog_name  FROM " +
                TEST_INDEX_PEOPLE + "/people a JOIN " + TEST_INDEX_DOG + "/dog d on d.holdersName = a.firstname" +
                " WHERE (a.age > 10 OR a.balance > 2000) AND d.age > 1";
        String result = explainQuery(query);

        Assert.assertThat(result.replaceAll("\\s+", ""), equalTo(expectedOutput.replaceAll("\\s+", "")));
    }

    public void testContentTypeOfExplainRequestShouldBeJson() throws IOException {
        String query = makeRequest("SELECT firstname FROM elasticsearch-sql_test_index_account");
        Request request = getSqlRequest(query, true);

        RestClient restClient = ESIntegTestCase.getRestClient();
        Response response = restClient.performRequest(request);

        assertEquals("application/json; charset=UTF-8", response.getHeader("content-type"));
    }

    @Test
    public void assertExplainRequestPrettyFormatted() throws IOException {
        String expectedOutputFilePath = TestUtils.getResourceFilePath(
                "src/test/resources/expectedOutput/explain_format_pretty.json");
        String expectedOutput = Files.toString(new File(expectedOutputFilePath), StandardCharsets.UTF_8);

        String explainResult = explain(String.format("{\"query\":\"" +
                "SELECT firstname FROM %s\"}", TestsConstants.TEST_INDEX_ACCOUNT));
        String result = new JsonPrettyFormatter().format(explainResult);

        Assert.assertThat(result, equalTo(expectedOutput));
    }

    @Test
    public void assertExplainJoinRequestPrettyFormatted() throws IOException {
        String expectedOutPutFilePath = TestUtils.getResourceFilePath(
                "src/test/resources/expectedOutput/explain_join_format_pretty.json");
        String expectedOutput = Files.toString(new File(expectedOutPutFilePath), StandardCharsets.UTF_8);

        String explainResult = explain(String.format("{\"query\":\"" +
                "SELECT b1.firstname, b1.lastname, b2.age  " +
                "FROM %s b1 " +
                "LEFT JOIN %s b2 " +
                "ON b1.age = b2.age AND b1.state = b2.state\"}", TEST_INDEX_BANK, TEST_INDEX_BANK));
        String result = new JsonPrettyFormatter().format(explainResult);

        Assert.assertThat(result, equalTo(expectedOutput));
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
        QueryAction queryAction = ESActionFactory.create(mockClient, sql);

        SqlRequest sqlRequest = new SqlRequest(sql, jsonRequest);
        queryAction.setSqlRequest(sqlRequest);

        SqlElasticRequestBuilder requestBuilder = queryAction.explain();
        return requestBuilder.explain();
    }
}
