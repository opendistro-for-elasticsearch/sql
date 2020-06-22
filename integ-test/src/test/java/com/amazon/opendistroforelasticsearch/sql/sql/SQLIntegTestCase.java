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
import com.google.common.collect.Maps;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.Assert;

/**
 * SQL integration test base class.
 */
@ThreadLeakScope(ThreadLeakScope.Scope.NONE)
public abstract class SQLIntegTestCase extends RestIntegTestCase {

  /**
   * Comparison test runner shared by all methods in this IT class.
   */
  private static ComparisonTest runner;

  @Override
  protected void init() throws Exception {
    if (runner != null) {
      return;
    }

    TestConfig config = new TestConfig(getCmdLineArgs());
    runner = new ComparisonTest(getThisDBConnection(config),
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

    /*TestConfig config = new TestConfig(getCmdLineArgs());
    for (TestDataSet dataSet : config.getTestDataSets()) {
      runner.cleanUp(dataSet);
    }*/

    try {
      runner.close();
    } finally {
      runner = null;
    }
  }

  /**
   * Execute the given query and compare result with other database.
   */
  protected void verify(String query) {
    TestReport result = runner.verify(new TestQuerySet(query));
    TestSummary summary = result.getSummary();
    Assert.assertEquals(StringUtils.format(
        "Comparison test failed on query [%s]: %s", query, result), 0, summary.getFailure());
  }

  private static Map<String, String> getCmdLineArgs() {
    return Maps.fromProperties(System.getProperties());
  }

  private DBConnection getThisDBConnection(TestConfig config) {
    String dbUrl = config.getDbConnectionUrl();
    if (dbUrl.isEmpty()) {
      return getESConnection();
    }
    return new JDBCConnection("DB Tested", dbUrl);
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
