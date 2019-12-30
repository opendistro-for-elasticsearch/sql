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
import com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import org.elasticsearch.client.Node;
import org.junit.Test;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

/**
 * Testing for Tableau integration. To simplify testing, the IT itself manages all dependencies on file system,
 * including where to load test data and where to store the final report. {@link ComparisonTest} is only
 * interacted by simple data structure like string and list.
 */
public class TableauIT extends SQLIntegTestCase {

    @Test
    public void testQueriesForTableauIntegrationWithSQLPlugin() {
        DBConnection[] connections = {};
        try {
            ComparisonTest test = new ComparisonTest(getESConnection(), getOtherDBConnections());
            test.loadData("kibana_sample_data_flights",
                new TestFile("kibana_sample_data_flights.json").content(),
                new TestFile("kibana_sample_data_flights.csv").splitBy(",")
            );
            test.loadData("kibana_sample_data_ecommerce",
                new TestFile("kibana_sample_data_ecommerce.json").content(),
                new TestFile("kibana_sample_data_ecommerce.csv").splitBy(",")
            );

            List<String> queries = new TestFile("tableau_integration_tests_full.txt").lines();
            TestReport report = test.verify(queries);
            store(report);
        } finally {
            for (DBConnection conn : connections) {
                conn.close();
            }
        }
    }

    /** Abstraction for test related file reading */
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

        public List<String[]> splitBy(String separator) {
            List<String[]> rows = lines().stream().
                                          map(line -> line.split(separator)).
                                          collect(Collectors.toList());

            // Separator in quote is escaped
            List<String[]> result = new ArrayList<>();
            for (String[] row : rows) {
                List<String> escaped = new ArrayList<>();
                boolean isQuote = false;
                for (int i = 0; i < row.length; i++) {
                    if (row[i].startsWith("\"")) {
                        isQuote = true;
                        escaped.add(row[i]);
                    } else {
                        if (isQuote) {
                            escaped.set(escaped.size() - 1, escaped.get(escaped.size() - 1) + row[i]);
                        } else {
                            escaped.add(row[i]);
                        }

                        if (row[i].endsWith("\"")) {
                            isQuote = false;
                        }
                    }
                }
                result.add(escaped.toArray(new String[0]));
            }
            return result;
        }
    }

    private void store(TestReport report) {
        try {
            String relFilePath = "reports/" + reportFileName();
            String absFilePath = TestUtils.getResourceFilePath(relFilePath);
            byte[] content = report.report().getBytes();
            Files.write(Paths.get(absFilePath), content);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private String reportFileName() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH");
        df.setTimeZone(TimeZone.getTimeZone("GMT"));
        String dateTime = df.format(new Date());
        return "report_" + dateTime + ".json";
    }

    private DBConnection getESConnection() {
        Node node = getRestClient().getNodes().get(0);
        return new ESConnection("jdbc:elasticsearch://" + node.getHost(), client());
    }

    private DBConnection[] getOtherDBConnections() {
        return new DBConnection[]{
            new JDBCConnection("H2", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1"),
            new JDBCConnection("SQLite", "jdbc:sqlite::memory:"),
            //new JDBCConnection("Apache Derby", "jdbc:derby:memory:myDb;create=true"),
        };
    }

}
