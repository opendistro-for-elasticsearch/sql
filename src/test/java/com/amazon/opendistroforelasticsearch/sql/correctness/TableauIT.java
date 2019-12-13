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

import com.amazon.opendistroforelasticsearch.sql.correctness.report.TestReport;
import com.amazon.opendistroforelasticsearch.sql.correctness.runner.ComparisonTest;
import com.amazon.opendistroforelasticsearch.sql.correctness.runner.connection.DBConnection;
import com.amazon.opendistroforelasticsearch.sql.correctness.runner.connection.ESConnection;
import com.amazon.opendistroforelasticsearch.sql.correctness.runner.connection.JDBCConnection;
import com.amazon.opendistroforelasticsearch.sql.esintgtest.SQLIntegTestCase;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.elasticsearch.client.Node;
import org.junit.Test;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Testing for Tableau integration.
 */
public class TableauIT extends SQLIntegTestCase {

    @Test
    public void testQueriesForTableauIntegrationWithSQLPlugin() {
        ComparisonTest test = new ComparisonTest(getDBConnections());
        test.loadData("kibana_sample_data_flights",
            new TestFile("kibana_sample_data_flights.json").content(),
            new TestFile("kibana_sample_data_flights.csv").splitLines(",")
        );
        TestReport result = test.verify(new TestFile("tableau_integration_tests_full.txt").lines());
        test.report(result);
    }

    private static class TestFile {

        /** File content string */
        private final String content;

        private TestFile(String relativePath) {
            try {
                URL url = Resources.getResource("correctness/" + relativePath);
                this.content = Resources.toString(url, Charsets.UTF_8);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to read test file [" + relativePath + "]");
            }
        }

        public String content() {
            return content;
        }

        public List<String> lines() {
            return Arrays.asList(content.split("\\r?\\n"));
        }

        public List<String[]> splitLines(String separator) {
            return lines().stream().
                           map(line -> line.split(separator)).
                           collect(Collectors.toList());
        }
    }

    private DBConnection[] getDBConnections() {
        Node node = getRestClient().getNodes().get(0);
        return new DBConnection[]{
            new ESConnection("jdbc:elasticsearch://" + node.getHost(), client()),
            new JDBCConnection("H2", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"),
            new JDBCConnection("SQLite", "jdbc:sqlite:memory:myDb"),
            //new JDBCConnection("Apache Derby", "jdbc:derby:memory:myDb;create=true"),
        };
    }

}
