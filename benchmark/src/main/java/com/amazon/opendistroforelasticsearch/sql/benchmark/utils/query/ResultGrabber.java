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

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.results.BenchmarkResult;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.results.BenchmarkResults;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Result grabber class, used to run queries against a specified database.
 */
public class ResultGrabber {
  private final String type;
  private final QueryRunner queryRunner;
  private final Integer scaleFactor;

  /**
   * Constructor for ResultGrabber, assigns QueryRunner.
   * @param type Type of database to get results for.
   * @param scaleFactor Scale factor for data set.
   * @throws Exception Thrown if QueryRunner cannot be found for type.
   */
  public ResultGrabber(final String type, Integer scaleFactor) throws Exception {
    this.type = type;
    this.scaleFactor = scaleFactor;
    this.queryRunner = QueryRunnerFactory.getRunner(type);
  }

  /**
   * Function to run multiple queries and compile results into a single Object.
   * @param queries Queries to run against the specified database.
   * @return BenchmarkResults Object as result of running queries.
   */
  public BenchmarkResults runQueries(final List<String> queries) {
    return new BenchmarkResults(queries.stream()
        .map(this::runQuery).collect(Collectors.toList()), type, scaleFactor);
  }

  /**
   * Function to run a single query against the specified database and get a single result Object.
   * @param query Query to run against the specified database.
   * @return Single BenchmarkResult Object as a result of running queries.
   */
  private BenchmarkResult runQuery(final String query) {
    // TODO: Grab information while running query
    queryRunner.runQuery(query);
    return new BenchmarkResult();
  }
}
