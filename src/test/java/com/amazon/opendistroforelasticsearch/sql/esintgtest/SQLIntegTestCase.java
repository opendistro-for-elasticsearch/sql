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
import org.junit.Assert;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils.getResponseBody;
import static com.amazon.opendistroforelasticsearch.sql.plugin.RestSqlAction.EXPLAIN_API_ENDPOINT;
import static com.amazon.opendistroforelasticsearch.sql.plugin.RestSqlAction.QUERY_API_ENDPOINT;

public abstract class SQLIntegTestCase extends RestIntegTestCase {

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

}
