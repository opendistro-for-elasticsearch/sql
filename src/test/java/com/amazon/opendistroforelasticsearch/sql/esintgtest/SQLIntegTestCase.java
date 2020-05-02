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

import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.test.rest.ESRestTestCase;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils.createIndexByRestClient;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils.getAccountIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils.getBankIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils.getDateIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils.getDogIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils.getDogs2IndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils.getDogs3IndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils.getEmployeeNestedTypeIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils.getGameOfThronesIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils.getJoinTypeIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils.getLocationIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils.getNestedTypeIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils.getOdbcIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils.getOrderIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils.getPeople2IndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils.getPhraseIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils.getResponseBody;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils.getWeblogsIndexMapping;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils.isIndexExist;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils.loadDataByRestClient;
import static com.amazon.opendistroforelasticsearch.sql.plugin.RestSqlAction.EXPLAIN_API_ENDPOINT;
import static com.amazon.opendistroforelasticsearch.sql.plugin.RestSqlAction.QUERY_API_ENDPOINT;

/**
 * SQL plugin integration test base class.
 *
 * The execution of order is as follows:
 *
 *   ESRestTestCase:   1) initClient()                                       N+1) closeClient()
 *                            \                                                     /
 *   SQLIntegTestCase:     2) setUpIndices()  -> 4) setUpIndices() ... -> N) cleanUpIndices()
 *                                \                      \
 *   XXXTIT:                  3) init()             5) init()
 */
public abstract class SQLIntegTestCase extends ESRestTestCase {

    @Before
    public void setUpIndices() throws Exception {
        if (client() == null) {
            initClient();
        }

        increaseScriptMaxCompilationsRate();
        init();
    }

    @Override
    protected boolean preserveClusterUponCompletion() {
        return true; // Preserve test index, template and settings between test cases
    }

    /**
     * As JUnit JavaDoc says:
     *  "The @AfterClass methods declared in superclasses will be run after those of the current class."
     * So this method is supposed to run before closeClients() in parent class.
     */
    @AfterClass
    public static void cleanUpIndices() throws IOException {
        wipeAllIndices();
        wipeAllClusterSettings();
    }

    /**
     * Increase script.max_compilations_rate to large enough, which is only 75/5min by default.
     * This issue is due to our painless script not using params passed to compiled script.
     */
    private void increaseScriptMaxCompilationsRate() throws IOException {
        updateClusterSettings(new ClusterSetting("transient", "script.max_compilations_rate", "10000/1m"));
    }

    private static void wipeAllClusterSettings() throws IOException {
        updateClusterSettings(new ClusterSetting("persistent", "*", null));
        updateClusterSettings(new ClusterSetting("transient", "*", null));
    }

    /**
     * Provide for each test to load test index, data and other setup work
     */
    protected void init() throws Exception {}

    /**
     * Make it thread-safe in case tests are running in parallel but does not guarantee
     * if test like DeleteIT that mutates cluster running in parallel.
     */
    protected synchronized void loadIndex(Index index) throws IOException {
        String indexName = index.getName();
        String mapping = index.getMapping();
        String dataSet = index.getDataSet();

        if (!isIndexExist(client(), indexName)) {
            createIndexByRestClient(client(), indexName, mapping);
            loadDataByRestClient(client(), indexName, dataSet);
        }
    }

    protected Request getSqlRequest(String request, boolean explain) {
        String queryEndpoint = String.format("%s?format=%s", QUERY_API_ENDPOINT, "json");
        Request sqlRequest = new Request("POST", explain ? EXPLAIN_API_ENDPOINT : queryEndpoint);
        sqlRequest.setJsonEntity(request);
        RequestOptions.Builder restOptionsBuilder = RequestOptions.DEFAULT.toBuilder();
        restOptionsBuilder.addHeader("Content-Type", "application/json");
        sqlRequest.setOptions(restOptionsBuilder);

        return sqlRequest;
    }

    protected String executeQuery(String query, String requestType) {
        try {
            String endpoint = "/_opendistro/_sql?format=" + requestType;
            String requestBody = makeRequest(query);

            Request sqlRequest = new Request("POST", endpoint);
            sqlRequest.setJsonEntity(requestBody);

            Response response = client().performRequest(sqlRequest);
            Assert.assertEquals(200, response.getStatusLine().getStatusCode());
            String responseString = getResponseBody(response, true);

            return responseString;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected Request buildGetEndpointRequest(final String sqlQuery) {

        final String utf8CharsetName = StandardCharsets.UTF_8.name();
        String urlEncodedQuery = "";

        try {
            urlEncodedQuery = URLEncoder.encode(sqlQuery, utf8CharsetName);
        } catch (UnsupportedEncodingException e) {
            // Will never reach here since UTF-8 is always supported
            Assert.fail(utf8CharsetName + " not available");
        }

        final String requestUrl = String.format(Locale.ROOT, "%s?sql=%s&format=%s", QUERY_API_ENDPOINT,
                                                urlEncodedQuery, "json");
        return new Request("GET", requestUrl);
    }

    protected JSONObject executeQuery(final String sqlQuery) throws IOException {

        final String requestBody = makeRequest(sqlQuery);
        return executeRequest(requestBody);
    }

    protected String explainQuery(final String sqlQuery) throws IOException {

        final String requestBody = makeRequest(sqlQuery);
        return executeExplainRequest(requestBody);
    }

    protected String executeQueryWithStringOutput(final String sqlQuery) throws IOException {

        final String requestString = makeRequest(sqlQuery);
        return executeRequest(requestString, false);
    }

    protected JSONObject executeRequest(final String requestBody) throws IOException {

        return new JSONObject(executeRequest(requestBody, false));
    }

    protected String executeExplainRequest(final String requestBody) throws IOException {

        return executeRequest(requestBody, true);
    }

    private String executeRequest(final String requestBody, final boolean isExplainQuery) throws IOException {

        Request sqlRequest = getSqlRequest(requestBody, isExplainQuery);
        return executeRequest(sqlRequest);
    }

    protected static String executeRequest(final Request request) throws IOException {

        Response response = client().performRequest(request);
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        return getResponseBody(response);
    }

    protected JSONObject executeQueryWithGetRequest(final String sqlQuery) throws IOException {

        final Request request = buildGetEndpointRequest(sqlQuery);
        final String result = executeRequest(request);
        return new JSONObject(result);
    }

    protected static JSONObject updateClusterSettings(ClusterSetting setting) throws IOException {
        Request request = new Request("PUT", "/_cluster/settings");
        String persistentSetting = String.format(Locale.ROOT,
            "{\"%s\": {\"%s\": %s}}", setting.type, setting.name, setting.value);
        request.setJsonEntity(persistentSetting);
        RequestOptions.Builder restOptionsBuilder = RequestOptions.DEFAULT.toBuilder();
        restOptionsBuilder.addHeader("Content-Type", "application/json");
        request.setOptions(restOptionsBuilder);
        return new JSONObject(executeRequest(request));
    }

    protected static class ClusterSetting {
        private final String type;
        private final String name;
        private final String value;

        ClusterSetting(String type, String name, String value) {
            this.type = type;
            this.name = name;
            this.value = (value == null) ? "null" : ("\"" + value + "\"");
        }

        ClusterSetting nullify() {
            return new ClusterSetting(type, name, null);
        }

        @Override
        public String toString() {
            return "ClusterSetting{" +
                "type='" + type + '\'' +
                ", path='" + name + '\'' +
                ", value='" + value + '\'' +
                '}';
        }
    }

    protected String makeRequest(String query) {
        return String.format("{\n" +
                "  \"query\": \"%s\"\n" +
                "}", query);
    }

    protected JSONArray getHits(JSONObject response) {
        Assert.assertTrue(response.getJSONObject("hits").has("hits"));

        return response.getJSONObject("hits").getJSONArray("hits");
    }

    protected int getTotalHits(JSONObject response) {
        Assert.assertTrue(response.getJSONObject("hits").has("total"));
        Assert.assertTrue(response.getJSONObject("hits").getJSONObject("total").has("value"));

        return response.getJSONObject("hits").getJSONObject("total").getInt("value");
    }

    protected JSONObject getSource(JSONObject hit) {
        return hit.getJSONObject("_source");
    }

    /**
     * Enum for associating test index with relevant mapping and data.
     */
    public enum Index {
        ONLINE(TestsConstants.TEST_INDEX_ONLINE,
                "online",
                null,
                "src/test/resources/online.json"),
        ACCOUNT(TestsConstants.TEST_INDEX_ACCOUNT,
                "account",
                getAccountIndexMapping(),
                "src/test/resources/accounts.json"),
        PHRASE(TestsConstants.TEST_INDEX_PHRASE,
                "phrase",
                getPhraseIndexMapping(),
                "src/test/resources/phrases.json"),
        DOG(TestsConstants.TEST_INDEX_DOG,
                "dog",
                getDogIndexMapping(),
                "src/test/resources/dogs.json"),
        DOGS2(TestsConstants.TEST_INDEX_DOG2,
                "dog",
                getDogs2IndexMapping(),
                "src/test/resources/dogs2.json"),
        DOGS3(TestsConstants.TEST_INDEX_DOG3,
                "dog",
                getDogs3IndexMapping(),
                "src/test/resources/dogs3.json"),
        DOGSSUBQUERY(TestsConstants.TEST_INDEX_DOGSUBQUERY,
                "dog",
                getDogIndexMapping(),
                "src/test/resources/dogsubquery.json"),
        PEOPLE(TestsConstants.TEST_INDEX_PEOPLE,
                "people",
                null,
                "src/test/resources/peoples.json"),
        PEOPLE2(TestsConstants.TEST_INDEX_PEOPLE2,
                "people",
                getPeople2IndexMapping(),
                "src/test/resources/people2.json"),
        GAME_OF_THRONES(TestsConstants.TEST_INDEX_GAME_OF_THRONES,
                "gotCharacters",
                getGameOfThronesIndexMapping(),
                "src/test/resources/game_of_thrones_complex.json"),
        SYSTEM(TestsConstants.TEST_INDEX_SYSTEM,
                "systems",
                null,
                "src/test/resources/systems.json"),
        ODBC(TestsConstants.TEST_INDEX_ODBC,
                "odbc",
                getOdbcIndexMapping(),
                "src/test/resources/odbc-date-formats.json"),
        LOCATION(TestsConstants.TEST_INDEX_LOCATION,
                "location",
                getLocationIndexMapping(),
                "src/test/resources/locations.json"),
        LOCATION_TWO(TestsConstants.TEST_INDEX_LOCATION2,
                "location2",
                getLocationIndexMapping(),
                "src/test/resources/locations2.json"),
        NESTED(TestsConstants.TEST_INDEX_NESTED_TYPE,
                "nestedType",
                getNestedTypeIndexMapping(),
                "src/test/resources/nested_objects.json"),
        NESTED_WITH_QUOTES(TestsConstants.TEST_INDEX_NESTED_WITH_QUOTES,
                "nestedType",
                getNestedTypeIndexMapping(),
                "src/test/resources/nested_objects_quotes_in_values.json"),
        EMPLOYEE_NESTED(TestsConstants.TEST_INDEX_EMPLOYEE_NESTED,
                "_doc",
                getEmployeeNestedTypeIndexMapping(),
                "src/test/resources/employee_nested.json"),
        JOIN(TestsConstants.TEST_INDEX_JOIN_TYPE,
                "joinType",
                getJoinTypeIndexMapping(),
                "src/test/resources/join_objects.json"),
        BANK(TestsConstants.TEST_INDEX_BANK,
                "account",
                getBankIndexMapping(),
                "src/test/resources/bank.json"),
        BANK_TWO(TestsConstants.TEST_INDEX_BANK_TWO,
                "account_two",
                getBankIndexMapping(),
                "src/test/resources/bank_two.json"),
        ORDER(TestsConstants.TEST_INDEX_ORDER,
                "_doc",
                 getOrderIndexMapping(),
                "src/test/resources/order.json"),
        WEBLOG(TestsConstants.TEST_INDEX_WEBLOG,
                "weblog",
                getWeblogsIndexMapping(),
                "src/test/resources/weblogs.json"),
        DATE(TestsConstants.TEST_INDEX_DATE,
                "dates",
                getDateIndexMapping(),
                "src/test/resources/dates.json");

        private final String name;
        private final String type;
        private final String mapping;
        private final String dataSet;

        Index(String name, String type, String mapping, String dataSet) {
            this.name = name;
            this.type = type;
            this.mapping = mapping;
            this.dataSet = dataSet;
        }

        public String getName() { return this.name; }

        public String getType() { return this.type; }

        public String getMapping() { return this.mapping; }

        public String getDataSet() { return this.dataSet; }
    }
}
