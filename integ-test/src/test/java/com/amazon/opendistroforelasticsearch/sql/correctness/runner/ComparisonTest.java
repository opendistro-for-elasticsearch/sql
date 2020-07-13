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

import static com.google.common.collect.ObjectArrays.concat;

import com.amazon.opendistroforelasticsearch.sql.correctness.report.ErrorTestCase;
import com.amazon.opendistroforelasticsearch.sql.correctness.report.FailedTestCase;
import com.amazon.opendistroforelasticsearch.sql.correctness.report.SuccessTestCase;
import com.amazon.opendistroforelasticsearch.sql.correctness.report.TestCaseReport;
import com.amazon.opendistroforelasticsearch.sql.correctness.report.TestReport;
import com.amazon.opendistroforelasticsearch.sql.correctness.runner.connection.DBConnection;
import com.amazon.opendistroforelasticsearch.sql.correctness.runner.resultset.DBResult;
import com.amazon.opendistroforelasticsearch.sql.correctness.testset.TestDataSet;
import com.amazon.opendistroforelasticsearch.sql.correctness.testset.TestQuerySet;
import com.amazon.opendistroforelasticsearch.sql.legacy.utils.StringUtils;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Comparison test runner for query result correctness.
 */
public class ComparisonTest implements AutoCloseable {

  /**
   * Next id for test case
   */
  private int testCaseId = 1;

  /**
   * Connection for database being tested
   */
  private final DBConnection thisConnection;

  /**
   * Database connections for reference databases
   */
  private final DBConnection[] otherDbConnections;

  public ComparisonTest(DBConnection thisConnection, DBConnection[] otherDbConnections) {
    this.thisConnection = thisConnection;
    this.otherDbConnections = otherDbConnections;

    // Guarantee ordering of other database in comparison test
    Arrays.sort(this.otherDbConnections, Comparator.comparing(DBConnection::getDatabaseName));
  }

  /**
   * Open database connection.
   */
  public void connect() {
    for (DBConnection conn : concat(thisConnection, otherDbConnections)) {
      conn.connect();
    }
  }

  /**
   * Create table and load test data.
   *
   * @param dataSet test data set
   */
  public void loadData(TestDataSet dataSet) {
    for (DBConnection conn : concat(thisConnection, otherDbConnections)) {
      conn.create(dataSet.getTableName(), dataSet.getSchema());
      insertTestDataInBatch(conn, dataSet.getTableName(), dataSet.getDataRows());
    }
  }

  /**
   * Verify queries one by one by comparing between databases.
   *
   * @param querySet SQL queries
   * @return Test result report
   */
  public TestReport verify(TestQuerySet querySet) {
    TestReport report = new TestReport();
    for (String sql : querySet) {
      try {
        DBResult esResult = thisConnection.select(sql);
        report.addTestCase(compareWithOtherDb(sql, esResult));
      } catch (Exception e) {
        report.addTestCase(new ErrorTestCase(nextId(), sql,
            StringUtils.format("%s: %s", e.getClass().getSimpleName(), extractRootCause(e))));
      }
    }
    return report;
  }

  /**
   * Clean up test table.
   *
   * @param dataSet test data set
   */
  public void cleanUp(TestDataSet dataSet) {
    for (DBConnection conn : concat(thisConnection, otherDbConnections)) {
      conn.drop(dataSet.getTableName());
    }
  }

  @Override
  public void close() {
    for (DBConnection conn : concat(thisConnection, otherDbConnections)) {
      try {
        conn.close();
      } catch (Exception e) {
        // Ignore
      }
    }
  }

  /**
   * Execute the query and compare with current result
   */
  private TestCaseReport compareWithOtherDb(String sql, DBResult esResult) {
    List<DBResult> mismatchResults = Lists.newArrayList(esResult);
    StringBuilder reasons = new StringBuilder();
    for (int i = 0; i < otherDbConnections.length; i++) {
      try {
        DBResult otherDbResult = otherDbConnections[i].select(sql);
        if (esResult.equals(otherDbResult)) {
          return new SuccessTestCase(nextId(), sql);
        }

        mismatchResults.add(otherDbResult);

      } catch (Exception e) {
        // Ignore and move on to next database
        reasons.append(extractRootCause(e)).append(";");
      }
    }

    if (mismatchResults.size() == 1) { // Only ES result on list. Cannot find other database support this query
      return new ErrorTestCase(nextId(), sql, "No other databases support this query: " + reasons);
    }
    return new FailedTestCase(nextId(), sql, mismatchResults, reasons.toString());
  }

  private int nextId() {
    return testCaseId++;
  }

  private void insertTestDataInBatch(DBConnection conn, String tableName, List<String[]> testData) {
    Iterator<String[]> iterator = testData.iterator();
    String[] fieldNames = iterator.next(); // first row is header of column names
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
