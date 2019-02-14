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

import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.test.ESIntegTestCase;
import org.elasticsearch.test.TestCluster;
import org.json.JSONObject;
import org.junit.Assert;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;

import static com.amazon.opendistroforelasticsearch.sql.plugin.RestSqlAction.QUERY_API_ENDPOINT;

public class SQLIntegTestCase extends ESIntegTestCase {

    @Override
    protected TestCluster buildTestCluster(Scope scope, long seed) throws IOException {
        String clusterAddresses = System.getProperty(TESTS_CLUSTER);
        String[] stringAddresses = clusterAddresses.split(",");
        TransportAddress[] transportAddresses = new TransportAddress[stringAddresses.length];
        int i = 0;
        for (String stringAddress : stringAddresses) {
            URL url = new URL("http://" + stringAddress);
            InetAddress inetAddress = InetAddress.getByName(url.getHost());
            transportAddresses[i++] = new TransportAddress(new InetSocketAddress(inetAddress, url.getPort()));
        }
        return new CustomExternalTestCluster(createTempDir(), externalClusterClientSettings(), transportClientPlugins(), transportAddresses);
    }

    /** Helper methods for loading indices */
    protected void loadOnlineIndex(AdminClient adminClient, Client esClient) throws Exception {
        TestUtils.createTestIndex(adminClient, TestsConstants.TEST_INDEX_ONLINE, "online", null);
        TestUtils.loadBulk(esClient, "src/test/resources/online.json", TestsConstants.TEST_INDEX_ONLINE);
        ensureGreen(TestsConstants.TEST_INDEX_ONLINE);
    }

    protected void loadAccountIndex(AdminClient adminClient, Client esClient) throws Exception {
        TestUtils.createTestIndex(adminClient, TestsConstants.TEST_INDEX_ACCOUNT, "account",
                TestUtils.getAccountIndexMapping());
        TestUtils.loadBulk(esClient, "src/test/resources/accounts.json", TestsConstants.TEST_INDEX_ACCOUNT);
        ensureGreen(TestsConstants.TEST_INDEX_ACCOUNT);
    }

    protected void loadPhraseIndex(AdminClient adminClient, Client esClient) throws Exception {
        TestUtils.createTestIndex(adminClient, TestsConstants.TEST_INDEX_PHRASE, "phrase",
                TestUtils.getPhraseIndexMapping());
        TestUtils.loadBulk(esClient, "src/test/resources/phrases.json", TestsConstants.TEST_INDEX_PHRASE);
        ensureGreen(TestsConstants.TEST_INDEX_PHRASE);
    }

    protected void loadDogIndex(AdminClient adminClient, Client esClient) throws Exception {
        TestUtils.createTestIndex(adminClient, TestsConstants.TEST_INDEX_DOG, "dog",
                TestUtils.getDogIndexMapping());
        TestUtils.loadBulk(esClient, "src/test/resources/dogs.json", TestsConstants.TEST_INDEX_DOG);
        ensureGreen(TestsConstants.TEST_INDEX_DOG);
    }

    protected void loadPeopleIndex(AdminClient adminClient, Client esClient) throws Exception {
        TestUtils.createTestIndex(adminClient, TestsConstants.TEST_INDEX_PEOPLE, "people", null);
        TestUtils.loadBulk(esClient, "src/test/resources/peoples.json", TestsConstants.TEST_INDEX_PEOPLE);
        ensureGreen(TestsConstants.TEST_INDEX_PEOPLE);
    }

    protected void loadGameOfThronesIndex(AdminClient adminClient, Client esClient) throws Exception {
        TestUtils.createTestIndex(adminClient, TestsConstants.TEST_INDEX_GAME_OF_THRONES, "gotCharacters",
                TestUtils.getGameOfThronesIndexMapping());
        TestUtils.loadBulk(esClient, "src/test/resources/game_of_thrones_complex.json",
                TestsConstants.TEST_INDEX_GAME_OF_THRONES);
        ensureGreen(TestsConstants.TEST_INDEX_GAME_OF_THRONES);
    }

    protected void loadOdbcIndex(AdminClient adminClient, Client esClient) throws Exception {
        TestUtils.createTestIndex(adminClient, TestsConstants.TEST_INDEX_ODBC, "odbc",
                TestUtils.getOdbcIndexMapping());
        TestUtils.loadBulk(esClient, "src/test/resources/odbc-date-formats.json",
                TestsConstants.TEST_INDEX_ODBC);
        ensureGreen(TestsConstants.TEST_INDEX_ODBC);
    }

    protected void loadLocationIndex(AdminClient adminClient, Client esClient) throws Exception {
        TestUtils.createTestIndex(adminClient, TestsConstants.TEST_INDEX_LOCATION, "location",
                TestUtils.getLocationIndexMapping("location"));
        TestUtils.loadBulk(esClient, "src/test/resources/locations.json", TestsConstants.TEST_INDEX_LOCATION);
        ensureGreen(TestsConstants.TEST_INDEX_LOCATION);
    }

    protected void loadNestedTypeIndex(AdminClient adminClient, Client esClient) throws Exception {
        TestUtils.createTestIndex(adminClient, TestsConstants.TEST_INDEX_NESTED_TYPE, "nestedType",
                TestUtils.getNestedTypeIndexMapping());
        TestUtils.loadBulk(esClient, "src/test/resources/nested_objects.json",
                TestsConstants.TEST_INDEX_NESTED_TYPE);
        ensureGreen(TestsConstants.TEST_INDEX_NESTED_TYPE);
    }

    protected void loadJoinTypeIndex(AdminClient adminClient, Client esClient) throws Exception {
        TestUtils.createTestIndex(adminClient, TestsConstants.TEST_INDEX_JOIN_TYPE, "joinType",
                TestUtils.getJoinTypeIndexMapping());
        TestUtils.loadBulk(esClient, "src/test/resources/join_objects.json",
                TestsConstants.TEST_INDEX_JOIN_TYPE);
        ensureGreen(TestsConstants.TEST_INDEX_JOIN_TYPE);
    }

    protected void loadBankIndex(AdminClient adminClient, Client esClient) throws Exception {
        TestUtils.createTestIndex(adminClient, TestsConstants.TEST_INDEX_BANK, "account",
                TestUtils.getBankIndexMapping("account"));
        TestUtils.loadBulk(esClient, "src/test/resources/bank.json", TestsConstants.TEST_INDEX_BANK);
        ensureGreen(TestsConstants.TEST_INDEX_BANK);
    }

    protected void loadBankTwoIndex(AdminClient adminClient, Client esClient) throws Exception {
        TestUtils.createTestIndex(adminClient, TestsConstants.TEST_INDEX_BANK_TWO, "account_two",
                TestUtils.getBankIndexMapping("account_two"));
        TestUtils.loadBulk(esClient, "src/test/resources/bank_two.json", TestsConstants.TEST_INDEX_BANK_TWO);
        ensureGreen(TestsConstants.TEST_INDEX_BANK_TWO);
    }

    protected JSONObject query(String request) throws IOException {
        RestClient restClient = ESIntegTestCase.getRestClient();
        Request sqlRequest = new Request("POST", QUERY_API_ENDPOINT);
        sqlRequest.setJsonEntity(request);
        RequestOptions.Builder restOptionsBuilder = RequestOptions.DEFAULT.toBuilder();
        restOptionsBuilder.addHeader("Content-Type", "application/json");
        sqlRequest.setOptions(restOptionsBuilder);

        Response sqlResponse = restClient.performRequest(sqlRequest);

        Assert.assertTrue(sqlResponse.getStatusLine().getStatusCode() == 200);

        InputStream is = sqlResponse.getEntity().getContent();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line = null;
            while((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        return new JSONObject(sb.toString());
    }
}
