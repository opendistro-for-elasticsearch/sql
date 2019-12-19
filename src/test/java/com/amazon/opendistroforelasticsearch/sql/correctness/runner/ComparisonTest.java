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

package com.amazon.opendistroforelasticsearch.sql.correctness.runner;

import com.amazon.opendistroforelasticsearch.sql.correctness.report.ErrorTestCase;
import com.amazon.opendistroforelasticsearch.sql.correctness.report.FailedTestCase;
import com.amazon.opendistroforelasticsearch.sql.correctness.report.SuccessTestCase;
import com.amazon.opendistroforelasticsearch.sql.correctness.report.TestReport;
import com.amazon.opendistroforelasticsearch.sql.correctness.runner.connection.DBConnection;
import com.amazon.opendistroforelasticsearch.sql.correctness.runner.resultset.DBResult;
import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;
import com.google.common.collect.Iterators;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Comparison test runner for query result correctness.
 */
public class ComparisonTest {

    /**
     * Database connection for test data load and query.
     * Assumption is that the first connection is for the database to be targeted such as Elasticsearch.
     */
    private final DBConnection[] connections;

    public ComparisonTest(DBConnection[] connections) {
        this.connections = connections;
    }

    /**
     * Create table and load test data.
     * @param tableName     table name
     * @param schema        schema json in ES mapping format
     * @param testData      test data rows
     */
    public void loadData(String tableName, String schema, List<String[]> testData) {
        for (DBConnection conn : connections) {
            createTestTable(conn, tableName, schema);
            insertTestData(conn, tableName, testData);
        }
    }

    /**
     * Verify queries one by one by comparing between databases.
     * @param queries   SQL queries
     * @return          Test result report
     */
    public TestReport verify(List<String> queries) {
        TestReport report = new TestReport();
        for (String sql : queries) {
            DBResult esResult;
            try {
                esResult = connections[0].select(sql);

                int otherDbWithError = 0;
                StringBuilder reasons = new StringBuilder();
                for (int i = 1; i < connections.length; i++) {
                    try {
                        DBResult otherDbResult = connections[i].select(sql);
                        if (esResult.equals(otherDbResult)) {
                            report.addTestCase(new SuccessTestCase(sql));
                        } else {
                            report.addTestCase(new FailedTestCase(sql, Arrays.asList(esResult, otherDbResult)));
                        }
                        break;
                    } catch (Exception e) {
                        // Ignore
                        otherDbWithError++;
                        reasons.append(extractRootCause(e)).append(";");
                    }
                }

                if (otherDbWithError == connections.length - 1) {
                    report.addTestCase(new ErrorTestCase(sql, "No other databases support this query: " + reasons));
                }
            } catch (Exception e) {
                report.addTestCase(new ErrorTestCase(sql,
                    //StringUtils.format("%s: %s. %s", e.getClass().getSimpleName(), extractRootCause(e), Arrays.toString(e.getStackTrace()))));
                    StringUtils.format("%s: %s", e.getClass().getSimpleName(), extractRootCause(e))));
            }
        }
        return report;
    }

    private void createTestTable(DBConnection conn, String tableName, String schema) {
        conn.create(tableName, schema);
    }

    private void insertTestData(DBConnection conn, String tableName, List<String[]> testData) {
        Iterator<String[]> iterator = testData.iterator();
        String[] fieldNames = iterator.next();
        Iterators.partition(iterator, 100).
            forEachRemaining(batch -> conn.insert(tableName, fieldNames, batch));
    }

    private String extractRootCause(Throwable e) {
        while (e.getCause() != null) {
            e = e.getCause();
        }

        if (e.getLocalizedMessage() != null) {
            return e.getLocalizedMessage();
        }
        if (e.getMessage() != null) {
            return e.getMessage();
        }
        return e.toString();
    }

}
