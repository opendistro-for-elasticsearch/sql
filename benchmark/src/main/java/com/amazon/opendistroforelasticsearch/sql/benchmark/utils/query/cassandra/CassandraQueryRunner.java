/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.query.cassandra;

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.query.QueryRunner;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;

/**
 * Query runner for Cassandra databases.
 */
public class CassandraQueryRunner extends QueryRunner {
  private boolean queryWasSuccessful;
  private static final String NODE = "127.0.0.1";
  private Cluster cluster;
  private Session session;
  private String query;

  /**
   * Function to run queries against Cassandra database.
   */
  @Override
  public void runQuery() {
    try {
      session.execute(query);
    } catch (Exception e) {
      queryWasSuccessful = false;
    }
  }

  /**
   * Function to prepare query runner against Cassandra database.
   *
   * @param query Query to run against Cassandra database.
   */
  @Override
  public void prepareQueryRunner(String query) {
    cluster = Cluster.builder()
            .addContactPoint(NODE)
            .build();
    session = cluster.connect();
    this.query = query;
    queryWasSuccessful = true;
  }

  /**
   * Function to check query execution status against Cassandra database.
   */
  @Override
  public void checkQueryExecutionStatus(String benchmarkPath) throws Exception {
    session.close();
    cluster.close();

    if (queryWasSuccessful) {
      return;
    }

    File benchmarkDirectory = new File(benchmarkPath);
    if (benchmarkDirectory.exists() && benchmarkDirectory.isDirectory()) {
      BufferedWriter bufferedWriter = new BufferedWriter(
              new FileWriter(benchmarkPath + "/cassandra_failed_queries.txt", true));
      bufferedWriter.write(query);
      bufferedWriter.newLine();
      bufferedWriter.close();
    } else {
      throw new FileNotFoundException("Invalid Directory");
    }
  }
}
