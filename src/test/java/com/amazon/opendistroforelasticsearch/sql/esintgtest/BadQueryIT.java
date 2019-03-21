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

import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.test.ESIntegTestCase;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_ACCOUNT;
import static com.amazon.opendistroforelasticsearch.sql.plugin.RestSqlAction.QUERY_API_ENDPOINT;

/**
 * Send bad query to test our error handling code.
 */
@ESIntegTestCase.SuiteScopeTestCase
@ThreadLeakScope(ThreadLeakScope.Scope.NONE)
@Ignore
public class BadQueryIT extends SQLIntegTestCase {

    @Override
    public void setupSuiteScopeCluster() throws Exception {
        loadAccountIndex(admin(), ESIntegTestCase.client());
    }

    @Test
    public void typoKeyword() throws IOException {
        assertEquals(400, queryForStatusCode("SELECT * FRON " + TEST_INDEX_ACCOUNT));
    }

    private int queryForStatusCode(String sql) throws IOException {
        RestClient restClient = ESIntegTestCase.getRestClient();
        Request sqlRequest = new Request("POST", QUERY_API_ENDPOINT);
        sqlRequest.setJsonEntity("{\n" +
                                 "  \"query\": \"" + sql + "\"\n" +
                                 "}");
        RequestOptions.Builder restOptionsBuilder = RequestOptions.DEFAULT.toBuilder();
        restOptionsBuilder.addHeader("Content-Type", "application/json");
        sqlRequest.setOptions(restOptionsBuilder);

        Response sqlResponse = restClient.performRequest(sqlRequest);
        return sqlResponse.getStatusLine().getStatusCode();
    }
}
