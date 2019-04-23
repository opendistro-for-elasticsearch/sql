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

import com.amazon.opendistroforelasticsearch.sql.metrics.MetricType;
import com.amazon.opendistroforelasticsearch.sql.metrics.Metrics;
import com.google.common.io.Files;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.test.ESIntegTestCase;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static com.amazon.opendistroforelasticsearch.sql.intgtest.TestsConstants.TEST_INDEX_ACCOUNT;
import static com.amazon.opendistroforelasticsearch.sql.intgtest.TestsConstants.TEST_INDEX_DOG;
import static com.amazon.opendistroforelasticsearch.sql.intgtest.TestsConstants.TEST_INDEX_LOCATION;
import static com.amazon.opendistroforelasticsearch.sql.intgtest.TestsConstants.TEST_INDEX_NESTED_TYPE;
import static com.amazon.opendistroforelasticsearch.sql.intgtest.TestsConstants.TEST_INDEX_PEOPLE;
import static com.amazon.opendistroforelasticsearch.sql.intgtest.TestsConstants.TEST_INDEX_PHRASE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class MetricsIT extends SQLIntegTestCase {

    private RestClient restClient = ESIntegTestCase.getRestClient();

    @Override
    protected void init() throws Exception {
        loadIndex(Index.ACCOUNT);
        loadIndex(Index.DOG);
    }

    @Test
    public void requestCount() throws IOException, InterruptedException {
        multiQueries(3);
        TimeUnit.SECONDS.sleep(2L);
        JSONObject res = new JSONObject(executeStatRequest(makeStatRequest()));
    }

    private void multiQueries(int n) throws IOException {
        for (int i=0; i<n; ++i) {
            executeQuery(String.format("SELECT COUNT(*) FROM %s/dog", TEST_INDEX_DOG));
        }
    }

    private Request makeStatRequest() {
        return new Request(
                "GET", "/_opendistro/_sql/stats"
        );
    }

    private String executeStatRequest(final Request request) throws IOException {

        RestClient restClient = ESIntegTestCase.getRestClient();
        Response sqlResponse = restClient.performRequest(request);

        Assert.assertTrue(sqlResponse.getStatusLine().getStatusCode() == 200);

        InputStream is = sqlResponse.getEntity().getContent();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line = null;
            while((line = br.readLine()) != null) {
                sb.append(line);
            }
        }

        System.out.println("[jing]: " + sb.toString());
        return sb.toString();
    }

}
