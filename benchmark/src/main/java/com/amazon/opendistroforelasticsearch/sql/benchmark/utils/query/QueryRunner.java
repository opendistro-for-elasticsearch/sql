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

package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.query;

/**
 * Abstract query runner class.
 */
public abstract class QueryRunner {

  /**
   * Abstract function definition for query running.
   */
  public abstract void runQuery() throws Exception;

  /**
   * Abstract function definition to prepare query runner.
   *
   * @param query Query to run against the specified database.
   */
  public abstract void prepareQueryRunner(String query) throws Exception;

  /**
   * Abstract function definition to check query execution status.
   */
  public abstract void checkQueryExecutionStatus(final String benchmarkPath)
      throws Exception;
}
