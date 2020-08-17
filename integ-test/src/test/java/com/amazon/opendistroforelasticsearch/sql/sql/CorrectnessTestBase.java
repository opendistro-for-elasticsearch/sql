/*
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.sql;

import static java.util.Collections.emptyMap;

import com.amazon.opendistroforelasticsearch.sql.correctness.TestConfig;
import com.amazon.opendistroforelasticsearch.sql.correctness.report.TestReport;
import com.amazon.opendistroforelasticsearch.sql.correctness.report.TestSummary;
import com.amazon.opendistroforelasticsearch.sql.correctness.runner.ComparisonTest;
import com.amazon.opendistroforelasticsearch.sql.correctness.runner.connection.DBConnection;
import com.amazon.opendistroforelasticsearch.sql.correctness.runner.connection.ESConnection;
import com.amazon.opendistroforelasticsearch.sql.correctness.runner.connection.JDBCConnection;
import com.amazon.opendistroforelasticsearch.sql.correctness.testset.TestDataSet;
import com.amazon.opendistroforelasticsearch.sql.correctness.testset.TestQuerySet;
import com.amazon.opendistroforelasticsearch.sql.legacy.RestIntegTestCase;
import com.amazon.opendistroforelasticsearch.sql.legacy.utils.StringUtils;
import com.carrotsearch.randomizedtesting.annotations.ThreadLeakScope;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Assert;

/**
 * SQL integration test base class. This is very similar to CorrectnessIT though
 * enforce the success of all tests rather than report failures only.
 */
@ThreadLeakScope(ThreadLeakScope.Scope.NONE)
public abstract class CorrectnessTestBase extends RestIntegTestCase {

  /**
   * Comparison test runner shared by all methods in this IT class.
   */
  private static ComparisonTest runner;

  @Override
  protected void init() throws Exception {
    if (runner != null) {
      return;
    }

    TestConfig config = new TestConfig(emptyMap());
    runner = new ComparisonTest(getESConnection(),
                                getOtherDBConnections(config));

    runner.connect();
    for (TestDataSet dataSet : config.getTestDataSets()) {
      runner.loadData(dataSet);
    }
  }

  /**
   * Clean up test data and close other database connection.
   */
  @AfterClass
  public static void cleanUp() {
    try {
      TestConfig config = new TestConfig(emptyMap());
      for (TestDataSet dataSet : config.getTestDataSets()) {
        runner.cleanUp(dataSet);
      }

      runner.close();
    } finally {
      runner = null;
    }
  }

  /**
   * Execute the given queries and compare result with other database.
   * The queries will be considered as one test batch.
   */
  protected void verify(String... queries) {
    TestReport result = runner.verify(new TestQuerySet(queries));
    TestSummary summary = result.getSummary();
    Assert.assertEquals(StringUtils.format(
        "Comparison test failed on queries: %s", new JSONObject(result).toString(2)),
        0, summary.getFailure());
  }

  /**
   * Use Elasticsearch cluster initialized by ES Gradle task.
   */
  private DBConnection getESConnection() {
    String esHost = client().getNodes().get(0).getHost().toString();
    return new ESConnection("jdbc:elasticsearch://" + esHost, client());
  }

  /**
   * Create database connection with database name and connect URL.
   */
  private DBConnection[] getOtherDBConnections(TestConfig config) {
    return config.getOtherDbConnectionNameAndUrls()
                 .entrySet().stream()
                 .map(e -> new JDBCConnection(e.getKey(), e.getValue()))
                 .toArray(DBConnection[]::new);
  }

}
