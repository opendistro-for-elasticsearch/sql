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

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.query.QueryRunner;

/**
 * Query runner for MySQL databases.
 */
public class MysqlQueryRunner extends QueryRunner {

  /**
   * Function to run queries against MySQL database.
   */
  @Override
  public void runQuery() throws Exception {

  }

  /**
   * Function to prepare query runner against MySQL database.
   *
   * @param query Query to run against MySQL database.
   */
  @Override
  public void prepareQueryRunner(String query) throws Exception {

  }

  /**
   * Function to check query execution status against MySQL database.
   */
  @Override
  public void checkQueryExecutionStatus(String benchmarkPath) throws Exception {

  }
}
