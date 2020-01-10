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

package com.amazon.opendistroforelasticsearch.sql.correctness;

import com.amazon.opendistroforelasticsearch.sql.correctness.testset.TestDataSet;
import com.amazon.opendistroforelasticsearch.sql.correctness.testset.TestQuerySet;
import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static java.util.stream.Collectors.joining;

/**
 * Test configuration parse the following information from command line arguments:
 *  1) Test schema and data
 *  2) Test queries
 *  3) Elasticsearch connection URL
 *  4) Other database connection URLs
 */
public class TestConfig {

    private static final String DEFAULT_TEST_QUERIES = "tableau_integration_tests.txt";
    private static final String DEFAULT_OTHER_DB_URLS = "H2=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1," + "SQLite=jdbc:sqlite::memory:";

    private final TestDataSet[] testDataSets;

    private final TestQuerySet testQuerySet;

    private final String esHostUrl;

    /** Test against some database rather than Elasticsearch via our JDBC driver */
    private final String dbConnectionUrl;

    private final Map<String, String> otherDbConnectionNameAndUrls = new HashMap<>();

    public TestConfig(Map<String, String> cliArgs) {
        testDataSets = buildDefaultTestDataSet(); // TODO: parse test data set argument
        testQuerySet = buildTestQuerySet(cliArgs);
        esHostUrl = cliArgs.getOrDefault("esHost", "");
        dbConnectionUrl = cliArgs.getOrDefault("dbUrl", "");

        parseOtherDbConnectionInfo(cliArgs);
    }

    public TestDataSet[] getTestDataSets() {
        return testDataSets;
    }

    public TestQuerySet getTestQuerySet() {
        return testQuerySet;
    }

    public String getESHostUrl() {
        return esHostUrl;
    }

    public String getDbConnectionUrl() {
        return dbConnectionUrl;
    }

    public Map<String, String> getOtherDbConnectionNameAndUrls() {
        return otherDbConnectionNameAndUrls;
    }

    private TestDataSet[] buildDefaultTestDataSet() {
        return new TestDataSet[]{
            new TestDataSet("kibana_sample_data_flights",
                            readFile("kibana_sample_data_flights.json"),
                            readFile("kibana_sample_data_flights.csv")),
            new TestDataSet("kibana_sample_data_ecommerce",
                            readFile("kibana_sample_data_ecommerce.json"),
                            readFile("kibana_sample_data_ecommerce.csv")),
        };
    }

    private TestQuerySet buildTestQuerySet(Map<String, String> cliArgs) {
        String queryFilePath = cliArgs.getOrDefault("queries", ""); // Gradle set it empty always
        if (queryFilePath.isEmpty()) {
            queryFilePath = DEFAULT_TEST_QUERIES;
        }
        return new TestQuerySet(readFile(queryFilePath));
    }

    private void parseOtherDbConnectionInfo(Map<String, String> cliArgs) {
        String otherDbUrls = cliArgs.getOrDefault("otherDbUrls", "");
        if (otherDbUrls.isEmpty()) {
            otherDbUrls = DEFAULT_OTHER_DB_URLS;
        }

        for (String dbNameAndUrl : otherDbUrls.split(",")) {
            int firstEq = dbNameAndUrl.indexOf('=');
            String dbName = dbNameAndUrl.substring(0, firstEq);
            String dbUrl = dbNameAndUrl.substring(firstEq + 1);
            otherDbConnectionNameAndUrls.put(dbName, dbUrl);
        }
    }

    private static String readFile(String relativePath) {
        try {
            URL url = Resources.getResource("correctness/" + relativePath);
            return Resources.toString(url, Charsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to read test file [" + relativePath + "]");
        }
    }

    @Override
    public String toString() {
        return "\n=================================\n"
            + "Tested Database  : " + esHostUrlToString() + '\n'
            + "Other Databases  :\n" + otherDbConnectionInfoToString() + '\n'
            + "Test data set(s) :\n" + testDataSetsToString() + '\n'
            + "Test query set   : " + testQuerySet + '\n'
            + "=================================\n";
    }

    private String testDataSetsToString() {
        return Arrays.stream(testDataSets).
                      map(TestDataSet::toString).
                      collect(joining("\n"));
    }

    private String esHostUrlToString() {
        if (!dbConnectionUrl.isEmpty()) {
            return dbConnectionUrl;
        }
        return esHostUrl.isEmpty() ? "(Use internal Elasticsearch in workspace)" : esHostUrl;
    }

    private String otherDbConnectionInfoToString() {
        return otherDbConnectionNameAndUrls.entrySet().stream().
                                           map(e -> StringUtils.format(" %s = %s", e.getKey(), e.getValue())).
                                           collect(joining("\n"));
    }

}
