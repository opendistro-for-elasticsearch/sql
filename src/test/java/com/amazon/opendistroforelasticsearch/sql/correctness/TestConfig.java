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

    private final TestDataSet[] testDataSets;

    private final TestQuerySet testQuerySet;

    private final String esHostUrl;

    private final Map<String, String> otherDbConnectionUrlByNames = new HashMap<>();

    public TestConfig(Map<String, String> cliArgs) {
        testDataSets = getDefaultTestDataSet(); // TODO: parse test data set argument
        testQuerySet = new TestQuerySet(readFile(cliArgs.getOrDefault("queries", DEFAULT_TEST_QUERIES)));
        esHostUrl = cliArgs.getOrDefault("esHost", "");

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

    public Map<String, String> getOtherDbConnectionNameAndUrls() {
        return otherDbConnectionUrlByNames;
    }

    private TestDataSet[] getDefaultTestDataSet() {
        return new TestDataSet[]{
            new TestDataSet("kibana_sample_data_flights",
                            readFile("kibana_sample_data_flights.json"),
                            readFile("kibana_sample_data_flights.csv")),
            new TestDataSet("kibana_sample_data_ecommerce",
                            readFile("kibana_sample_data_ecommerce.json"),
                            readFile("kibana_sample_data_ecommerce.csv")),
        };
    }

    private void parseOtherDbConnectionInfo(Map<String, String> cliArgs) {
        String dbUrls = cliArgs.getOrDefault("dbUrls",
                                             "H2=jdbc:h2:mem:test;DB_CLOSE_DELAY=-1,"
                                             + "SQLite=jdbc:sqlite::memory:");

        for (String dbNameAndUrl : dbUrls.split(",")) {
            int firstEquity = dbNameAndUrl.indexOf('=');
            String dbName = dbNameAndUrl.substring(0, firstEquity);
            String dbUrl = dbNameAndUrl.substring(firstEquity + 1);
            otherDbConnectionUrlByNames.put(dbName, dbUrl);
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
            + "ES Host Url      : " + esHostUrlToString() + '\n'
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
        return esHostUrl.isEmpty() ? "(Use internal Elasticsearch in workspace)" : esHostUrl;
    }

    private String otherDbConnectionInfoToString() {
        return otherDbConnectionUrlByNames.entrySet().stream().
                                           map(e -> StringUtils.format(" %s = %s", e.getKey(), e.getValue())).
                                           collect(joining("\n"));
    }

}
