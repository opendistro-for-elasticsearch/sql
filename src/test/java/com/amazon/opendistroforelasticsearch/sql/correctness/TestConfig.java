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

import com.amazon.opendistroforelasticsearch.sql.correctness.testfile.TestDataSet;
import com.amazon.opendistroforelasticsearch.sql.correctness.testfile.TestQuerySet;

import java.util.HashMap;
import java.util.Map;

/**
 * Test configuration parse the following information from command line arguments:
 *
 *  1) Test data and schema
 *  2) Test queries
 *  3) Elasticsearch connection URL
 *  4) Other database connection URLs
 *
 * This class is only focused on parsing and return simple data structure such as url string.
 * It doesn't convert it to internal class like DBConnection.
 */
public class TestConfig {

    private static final TestDataSet[] DEFAULT_TEST_DATA_SET = {
        new TestDataSet("kibana_sample_data_flights", "kibana_sample_data_flights.json", "kibana_sample_data_flights.csv"),
        new TestDataSet("kibana_sample_data_ecommerce", "kibana_sample_data_ecommerce.json", "kibana_sample_data_ecommerce.csv"),
    };

    private static final String DEFAULT_TEST_QUERIES = "tableau_integration_tests_full.txt";

    private final TestDataSet[] testDataSets = DEFAULT_TEST_DATA_SET; //TODO: always use default for now

    private final TestQuerySet testQuerySet;

    private final String esHostUrl;

    private final Map<String, String> otherDbConnectionUrlByNames = new HashMap<>();

    public TestConfig(Map<String, String> cliArgs) {
        testQuerySet = new TestQuerySet(cliArgs.getOrDefault("queries", DEFAULT_TEST_QUERIES));
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

    @Override
    public String toString() {
        return "================================="
            + " ES Host Url     : " + esHostUrl + '\n'
            + " Other Databases : " + otherDbConnectionUrlByNames + '\n'
            + "=================================";
    }
}
