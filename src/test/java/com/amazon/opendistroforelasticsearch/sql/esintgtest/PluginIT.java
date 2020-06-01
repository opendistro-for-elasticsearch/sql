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
import org.elasticsearch.client.ResponseException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.util.Locale;

import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils.getResponseBody;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_ACCOUNT;
import static org.hamcrest.Matchers.equalTo;

public class PluginIT extends SQLIntegTestCase {

    @Override
    protected void init() throws Exception {
        wipeAllClusterSettings();
    }

    @Test
    public void sqlEnableSettingsTest() throws IOException {
        loadIndex(Index.ACCOUNT);
        updateClusterSettings(new ClusterSetting(PERSISTENT, "opendistro.sql.enabled", "true"));
        String query = String.format(Locale.ROOT, "SELECT firstname FROM %s WHERE account_number=1", TEST_INDEX_ACCOUNT);
        JSONObject queryResult = executeQuery(query);
        assertThat(getHits(queryResult).length(), equalTo(1));

        updateClusterSettings(new ClusterSetting(PERSISTENT, "opendistro.sql.enabled", "false"));
        Response response = null;
        try {
            queryResult = executeQuery(query);
        } catch (ResponseException ex) {
            response = ex.getResponse();
        }

        queryResult = new JSONObject(TestUtils.getResponseBody(response));
        assertThat(queryResult.getInt("status"), equalTo(400));
        JSONObject error = queryResult.getJSONObject("error");
        assertThat(error.getString("reason"), equalTo("Invalid SQL query"));
        assertThat(error.getString("details"), equalTo("Either opendistro.sql.enabled or rest.action.multi.allow_explicit_index setting is false"));
        assertThat(error.getString("type"), equalTo("SQLFeatureDisabledException"));
        wipeAllClusterSettings();
    }

    @Test
    public void sqlTransientOnlySettingTest() throws IOException {
        // (1) compact form
        String settings = "{" +
                "  \"transient\": {" +
                "    \"opendistro.sql.metrics.rollinginterval\": \"80\"" +
                "  }" +
                "}";
        JSONObject actual = updateViaSQLSettingsAPI(settings);
        JSONObject expected = new JSONObject("{" +
                "  \"acknowledged\" : true," +
                "  \"persistent\" : { }," +
                "  \"transient\" : {" +
                "    \"opendistro\" : {" +
                "      \"sql\" : {" +
                "        \"metrics\" : {" +
                "          \"rollinginterval\" : \"80\"" +
                "        }" +
                "      }" +
                "    }" +
                "  }" +
                "}");
        assertTrue(actual.similar(expected));

        // (2) partial expanded form
        settings = "{" +
                "  \"transient\": {" +
                "    \"opendistro\" : {" +
                "      \"sql\" : {" +
                "        \"metrics.rollinginterval\": \"75\"" +
                "      }" +
                "    }" +
                "  }" +
                "}";
        actual = updateViaSQLSettingsAPI(settings);
        expected = new JSONObject("{" +
                "  \"acknowledged\" : true," +
                "  \"persistent\" : { }," +
                "  \"transient\" : {" +
                "    \"opendistro\" : {" +
                "      \"sql\" : {" +
                "        \"metrics\" : {" +
                "          \"rollinginterval\" : \"75\"" +
                "        }" +
                "      }" +
                "    }" +
                "  }" +
                "}");
        assertTrue(actual.similar(expected));


        // (3) full expanded form
        settings = "{" +
                "  \"transient\": {" +
                "    \"opendistro\" : {" +
                "      \"sql\" : {" +
                "        \"metrics\": {" +
                "          \"rollinginterval\": \"65\"" +
                "        }" +
                "      }" +
                "    }" +
                "  }" +
                "}";
        actual = updateViaSQLSettingsAPI(settings);
        expected = new JSONObject("{" +
                "  \"acknowledged\" : true," +
                "  \"persistent\" : { }," +
                "  \"transient\" : {" +
                "    \"opendistro\" : {" +
                "      \"sql\" : {" +
                "        \"metrics\" : {" +
                "          \"rollinginterval\" : \"65\"" +
                "        }" +
                "      }" +
                "    }" +
                "  }" +
                "}");
        assertTrue(actual.similar(expected));
    }

    @Test
    public void sqlPersistentOnlySettingTest() throws IOException  {
        // (1) compact form
        String settings = "{" +
                "  \"persistent\": {" +
                "    \"opendistro.sql.metrics.rollinginterval\": \"80\"" +
                "  }" +
                "}";
        JSONObject actual = updateViaSQLSettingsAPI(settings);
        JSONObject expected = new JSONObject("{" +
                "  \"acknowledged\" : true," +
                "  \"transient\" : { }," +
                "  \"persistent\" : {" +
                "    \"opendistro\" : {" +
                "      \"sql\" : {" +
                "        \"metrics\" : {" +
                "          \"rollinginterval\" : \"80\"" +
                "        }" +
                "      }" +
                "    }" +
                "  }" +
                "}");
        assertTrue(actual.similar(expected));

        // (2) partial expanded form
        settings = "{" +
                "  \"persistent\": {" +
                "    \"opendistro\" : {" +
                "      \"sql\" : {" +
                "        \"metrics.rollinginterval\": \"75\"" +
                "      }" +
                "    }" +
                "  }" +
                "}";
        actual = updateViaSQLSettingsAPI(settings);
        expected = new JSONObject("{" +
                "  \"acknowledged\" : true," +
                "  \"transient\" : { }," +
                "  \"persistent\" : {" +
                "    \"opendistro\" : {" +
                "      \"sql\" : {" +
                "        \"metrics\" : {" +
                "          \"rollinginterval\" : \"75\"" +
                "        }" +
                "      }" +
                "    }" +
                "  }" +
                "}");
        assertTrue(actual.similar(expected));


        // (3) full expanded form
        settings = "{" +
                "  \"persistent\": {" +
                "    \"opendistro\" : {" +
                "      \"sql\" : {" +
                "        \"metrics\": {" +
                "          \"rollinginterval\": \"65\"" +
                "        }" +
                "      }" +
                "    }" +
                "  }" +
                "}";
        actual = updateViaSQLSettingsAPI(settings);
        expected = new JSONObject("{" +
                "  \"acknowledged\" : true," +
                "  \"transient\" : { }," +
                "  \"persistent\" : {" +
                "    \"opendistro\" : {" +
                "      \"sql\" : {" +
                "        \"metrics\" : {" +
                "          \"rollinginterval\" : \"65\"" +
                "        }" +
                "      }" +
                "    }" +
                "  }" +
                "}");
        assertTrue(actual.similar(expected));
    }

    /**
     * Both transient and persistent settings are applied for same settings.
     * This is similiar to _cluster/settings behavior
     */
    @Test
    public void sqlCombinedSettingTest() throws IOException  {
        String settings = "{" +
                "  \"transient\": {" +
                "    \"opendistro.sql.metrics.rollingwindow\": \"3700\"," +
                "    \"opendistro.sql.query.analysis.semantic.suggestion\" : \"false\"" +
                "  }," +
                "  \"persistent\": {" +
                "    \"opendistro.sql.query.analysis.semantic.suggestion\" : \"true\"" +
                "  }" +
                "}" ;
        JSONObject actual = updateViaSQLSettingsAPI(settings);
        JSONObject expected = new JSONObject("{" +
                "  \"acknowledged\" : true," +
                "  \"persistent\" : {" +
                "    \"opendistro\" : {" +
                "      \"sql\" : {" +
                "        \"query\" : {" +
                "          \"analysis\" : {" +
                "            \"semantic\" : {" +
                "              \"suggestion\" : \"true\"" +
                "            }" +
                "          }" +
                "        }" +
                "      }" +
                "    }" +
                "  }," +
                "  \"transient\" : {" +
                "    \"opendistro\" : {" +
                "      \"sql\" : {" +
                "        \"metrics\" : {" +
                "          \"rollingwindow\" : \"3700\"" +
                "        }," +
                "        \"query\" : {" +
                "          \"analysis\" : {" +
                "            \"semantic\" : {" +
                "              \"suggestion\" : \"false\"" +
                "            }" +
                "          }" +
                "        }" +
                "      }" +
                "    }" +
                "  }" +
                "}");
        assertTrue(actual.similar(expected));
    }

    /**
     * Ignore all non opendistro.sql settings.
     * Only settings starting with opendistro.sql. are affected
     */
    @Test
    public void ignoreNonSQLSettingsTest() throws IOException {
        String settings = "{" +
                "  \"transient\": {" +
                "    \"opendistro.sql.metrics.rollingwindow\": \"3700\"," +
                "    \"opendistro.alerting.metrics.rollingwindow\": \"3700\"," +
                "    \"search.max_buckets\": \"10000\"," +
                "    \"search.max_keep_alive\": \"24h\"" +
                "  }," +
                "  \"persistent\": {" +
                "    \"opendistro.sql.query.analysis.semantic.suggestion\": \"true\"," +
                "    \"opendistro.alerting.metrics.rollingwindow\": \"3700\"," +
                "    \"thread_pool.analyze.queue_size\": \"16\"" +
                "  }" +
                "}" ;
        JSONObject actual = updateViaSQLSettingsAPI(settings);
        JSONObject expected = new JSONObject("{" +
                "  \"acknowledged\" : true," +
                "  \"persistent\" : {" +
                "    \"opendistro\" : {" +
                "      \"sql\" : {" +
                "        \"query\" : {" +
                "          \"analysis\" : {" +
                "            \"semantic\" : {" +
                "              \"suggestion\" : \"true\"" +
                "            }" +
                "          }" +
                "        }" +
                "      }" +
                "    }" +
                "  }," +
                "  \"transient\" : {" +
                "    \"opendistro\" : {" +
                "      \"sql\" : {" +
                "        \"metrics\" : {" +
                "          \"rollingwindow\" : \"3700\"" +
                "        }" +
                "      }" +
                "    }" +
                "  }" +
                "}");
        assertTrue(actual.similar(expected));
    }

    @Test
    public void ignoreNonTransientNonPersistentSettingsTest() throws IOException {
        String settings = "{" +
                "  \"transient\": {" +
                "    \"opendistro.sql.query.response.format\": \"jdbc\"" +
                "  }," +
                "  \"persistent\": {" +
                "    \"opendistro.sql.query.slowlog\": \"2\"" +
                "  }," +
                "  \"hello\": {" +
                "    \"world\" : {" +
                "      \"name\" : \"John Doe\"" +
                "    }" +
                "  }" +
                "}" ;
        JSONObject actual = updateViaSQLSettingsAPI(settings);
        JSONObject expected = new JSONObject("{" +
                "  \"acknowledged\" : true," +
                "  \"persistent\" : {" +
                "    \"opendistro\" : {" +
                "      \"sql\" : {" +
                "        \"query\" : {" +
                "          \"slowlog\" : \"2\"" +
                "        }" +
                "      }" +
                "    }" +
                "  }," +
                "  \"transient\" : {" +
                "    \"opendistro\" : {" +
                "      \"sql\" : {" +
                "        \"query\" : {" +
                "          \"response\" : {" +
                "            \"format\" : \"jdbc\"" +
                "          }" +
                "        }" +
                "      }" +
                "    }" +
                "  }" +
                "}");
        assertTrue(actual.similar(expected));
    }

    @Test
    public void sqlCombinedMixedSettingTest() throws IOException {
        String settings = "{" +
                "  \"transient\": {" +
                "    \"opendistro.sql.query.response.format\": \"json\"" +
                "  }," +
                "  \"persistent\": {" +
                "    \"opendistro\": {" +
                "      \"sql\": {" +
                "        \"query\": {" +
                "          \"slowlog\": \"1\"," +
                "          \"response.format\": \"jdbc\"" +
                "        }" +
                "      }" +
                "    }" +
                "  }," +
                "  \"hello\": {" +
                "    \"world\": {" +
                "      \"city\": \"Seattle\"" +
                "    }" +
                "  }" +
                "}" ;
        JSONObject actual = updateViaSQLSettingsAPI(settings);
        JSONObject expected = new JSONObject("{" +
                "  \"acknowledged\" : true," +
                "  \"persistent\" : {" +
                "    \"opendistro\" : {" +
                "      \"sql\" : {" +
                "        \"query\" : {" +
                "          \"slowlog\" : \"1\"," +
                "          \"response\" : {" +
                "            \"format\" : \"jdbc\"" +
                "          }" +
                "        }" +
                "      }" +
                "    }" +
                "  }," +
                "  \"transient\" : {" +
                "    \"opendistro\" : {" +
                "      \"sql\" : {" +
                "        \"query\" : {" +
                "          \"response\" : {" +
                "            \"format\" : \"json\"" +
                "          }" +
                "        }" +
                "      }" +
                "    }" +
                "  }" +
                "}");
        assertTrue(actual.similar(expected));
    }

    @Test
    public void nonRegisteredSQLSettingsThrowException() throws IOException {
        String settings = "{" +
                "  \"transient\": {" +
                "    \"opendistro.sql.query.state.city\": \"Seattle\"" +
                "  }" +
                "}" ;

        JSONObject actual;
        Response response = null;
        try {
            actual = updateViaSQLSettingsAPI(settings);
        } catch (ResponseException ex) {
            response = ex.getResponse();
        }

        actual = new JSONObject(TestUtils.getResponseBody(response));
        assertThat(actual.getInt("status"), equalTo(400));
        assertThat(actual.query("/error/type"), equalTo("illegal_argument_exception"));
        assertThat(
                actual.query("/error/reason"),
                equalTo("transient setting [opendistro.sql.query.state.city], not recognized")
        );
    }

    protected static JSONObject updateViaSQLSettingsAPI(String body) throws IOException {
        Request request = new Request("PUT", "/_opendistro/_sql/settings");
        request.setJsonEntity(body);
        RequestOptions.Builder restOptionsBuilder = RequestOptions.DEFAULT.toBuilder();
        restOptionsBuilder.addHeader("Content-Type", "application/json");
        request.setOptions(restOptionsBuilder);
        Response response = client().performRequest(request);
        return new JSONObject(getResponseBody(response));
    }
}
