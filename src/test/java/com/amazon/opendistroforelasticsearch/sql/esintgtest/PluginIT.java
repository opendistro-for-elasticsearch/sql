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

import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.elasticsearch.client.Request;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Locale;

import static com.amazon.opendistroforelasticsearch.sql.intgtest.TestsConstants.TEST_INDEX_ACCOUNT;
import static org.hamcrest.Matchers.equalTo;

public class PluginIT extends SQLIntegTestCase {

    private static final String PERSISTENT = "persistent";

    @Override
    protected void init() throws Exception {
        loadIndex(Index.ACCOUNT);
    }

    @Test
    public void sqlEnableSettingsTest() throws IOException {
        updateClusterSettings(PERSISTENT, "opendistro.sql.enabled", "true");
        String query = String.format(Locale.ROOT, "SELECT firstname FROM %s/account WHERE account_number=1", TEST_INDEX_ACCOUNT);
        JSONObject queryResult = executeQuery(query);
        Assert.assertThat(getHits(queryResult).length(), equalTo(1));

        updateClusterSettings(PERSISTENT, "opendistro.sql.enabled", "false");
        Response response = null;
        try {
            queryResult = executeQuery(query);
        } catch (ResponseException ex) {
            response = ex.getResponse();
        }

        queryResult = new JSONObject(TestUtils.getResponseBody(response));
        Assert.assertThat(queryResult.getInt("status"), equalTo(400));
        JSONObject error = queryResult.getJSONObject("error");
        Assert.assertThat(error.getString("reason"), equalTo("Invalid SQL query"));
        Assert.assertThat(error.getString("details"), equalTo("Either opendistro.sql.enabled or rest.action.multi.allow_explicit_index setting is false"));
        Assert.assertThat(error.getString("type"), equalTo("SQLFeatureDisabledException"));
        resetClusterSettings(PERSISTENT, "opendistro.sql.enabled");
    }

    private JSONObject updateClusterSettings(String settingType, String setting , String value) throws IOException {
        Request request = new Request("PUT", "/_cluster/settings?pretty");
        String persistentSetting = String.format(Locale.ROOT, "{\"%s\": {\"%s\": %s}}", settingType, setting, value);
        request.setJsonEntity(persistentSetting);
        RequestOptions.Builder restOptionsBuilder = RequestOptions.DEFAULT.toBuilder();
        restOptionsBuilder.addHeader("Content-Type", "application/json");
        request.setOptions(restOptionsBuilder);
        JSONObject result = new JSONObject(executeRequest(request));
        return result;
    }

    /**
     * We need to reset all the cluster settings, otherwise ESIntegTestCase will throw
     * java.lang.AssertionError: test leaves persistent cluster metadata behind
     *
     * TODO: Look for a way to reset all the persistent and transient settings instead of
     *       individually resetting of each such setting
     */
    private JSONObject resetClusterSettings(String settingType, String setting) throws IOException {
        return updateClusterSettings(settingType, setting, "null");
    }
}
