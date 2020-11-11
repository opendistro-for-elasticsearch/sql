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

package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.query.mysql;

import com.amazon.opendistroforelasticsearch.sql.benchmark.BenchmarkService;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.mysql.MysqlTpchSchema;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.query.QueryRunner;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Query runner for MySQL databases.
 */
public class MysqlQueryRunner extends QueryRunner {

  private static final String URL = "jdbc:mysql://localhost/";
  private String authUrl;
  private Connection connection = null;
  private Statement statement = null;
  private ResultSet result = null;
  private String query;

  /**
   * Function to run queries against MySQL database.
   */
  @Override
  public void runQuery() throws Exception {
    result = statement.executeQuery(query);
  }

  /**
   * Function to prepare query runner against MySQL database.
   *
   * @param query Query to run against MySQL database.
   */
  @Override
  public void prepareQueryRunner(String query) throws Exception {
    authUrl = URL + "?user=" + BenchmarkService.mysqlUsername + "&password="
        + BenchmarkService.mysqlPassword;
    Class.forName("com.mysql.cj.jdbc.Driver");
    connection = DriverManager.getConnection(authUrl);
    statement = connection.createStatement();
    statement.executeUpdate("use " + MysqlTpchSchema.databaseName);
    this.query = query;
  }

  /**
   * Function to check query execution status against MySQL database.
   */
  @Override
  public void checkQueryExecutionStatus(String benchmarkPath) throws Exception {
    if (!result.next()) {
      File benchmarkDirectory = new File(benchmarkPath);
      if (benchmarkDirectory.exists() && benchmarkDirectory.isDirectory()) {
        try (BufferedWriter bufferedWriter = new BufferedWriter(
            new FileWriter(benchmarkPath + "/mysql_failed_queries.txt", true))) {
          bufferedWriter.write(query);
          bufferedWriter.newLine();
        }
      } else {
        throw new FileNotFoundException("Invalid Directory");
      }
    }
  }
}
