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
import org.elasticsearch.client.RestClient;
import org.elasticsearch.test.ESIntegTestCase;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static com.amazon.opendistroforelasticsearch.sql.intgtest.TestsConstants.TEST_INDEX_ACCOUNT;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils.SettingType;
import static org.hamcrest.Matchers.equalTo;

public class PluginIT extends SQLIntegTestCase {

    @Override
    protected void init() throws Exception {
        loadIndex(Index.ACCOUNT);
    }

    @Test
    public void sqlEnableSettingsTest() throws IOException {
        updateClusterSettings(SettingType.PERSISTENT, "opendistro.sql.enabled", "true");
        JSONObject queryResult = executeQuery(String.format("SELECT firstname FROM %s/account WHERE account_number=1", TEST_INDEX_ACCOUNT));
        System.out.println(queryResult);
        Assert.assertThat(getHits(queryResult).length(), equalTo(1));

        updateClusterSettings(SettingType.PERSISTENT, "opendistro.sql.enabled", "false");
        Response response = null;
        try {
            queryResult = executeQuery(String.format("SELECT firstname FROM %s/account WHERE account_number = 1", TEST_INDEX_ACCOUNT));
            System.out.println(queryResult);
        } catch (ResponseException ex) {
            response = ex.getResponse();
        }

        queryResult = new JSONObject(TestUtils.getResponseBody(response));
        Assert.assertThat(queryResult.getInt("status"), equalTo(400));
        JSONObject error = queryResult.getJSONObject("error");
        Assert.assertThat(error.getString("reason"), equalTo("Invalid SQL query"));
        Assert.assertThat(error.getString("details"), equalTo("Either opendistro.sql.enabled or rest.action.multi.allow_explicit_index setting is false"));
        Assert.assertThat(error.getString("type"), equalTo("SQLFeatureDisabledException"));
        resetClusterSettings(SettingType.PERSISTENT, "opendistro.sql.enabled");
    }

    private JSONObject updateClusterSettings(SettingType settingType, String setting , String value) throws IOException {
        Request request = new Request("PUT", "/_cluster/settings?pretty");
        String persistentSetting = "{\""+ settingType.val() +"\": {\"" + setting + "\": " + value + "}}";
        request.setJsonEntity(persistentSetting);
        RequestOptions.Builder restOptionsBuilder = RequestOptions.DEFAULT.toBuilder();
        restOptionsBuilder.addHeader("Content-Type", "application/json");
        request.setOptions(restOptionsBuilder);
        JSONObject result = new JSONObject(executeRequest(request));
        System.out.println(result);
        return result;
    }

    /**
     * We need to reset all the cluster settings, otherwise ESIntegTestCase will throw
     * java.lang.AssertionError: test leaves persistent cluster metadata behind
     *
     * TODO: Look for a way to reset all the persistent and transient settings instead of
     *       individually resetting of each such setting
     */
    private JSONObject resetClusterSettings(SettingType settingType, String setting) throws IOException {
        return updateClusterSettings(settingType, setting, "null");
    }

    private String executeRequest(final Request request) throws IOException {
        RestClient restClient = ESIntegTestCase.getRestClient();
        Response response = restClient.performRequest(request);
        return TestUtils.getResponseBody(response);
    }
}
