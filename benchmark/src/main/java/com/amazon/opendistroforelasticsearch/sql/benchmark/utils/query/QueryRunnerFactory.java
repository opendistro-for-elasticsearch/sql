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

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.BenchmarkConstants;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.query.cassandra.CassandraQueryRunner;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.query.elasticsearch.ElasticsearchQueryRunner;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.query.mock.MockQueryRunner;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Factory to get query runner for specified database type.
 */
public class QueryRunnerFactory {
  private static Map<String, QueryRunner> QUERY_RUNNER_MAP = ImmutableMap.of(
      BenchmarkConstants.ELASTICSEARCH, new ElasticsearchQueryRunner(),
      BenchmarkConstants.CASSANDRA, new CassandraQueryRunner(),
      BenchmarkConstants.MOCK1, new MockQueryRunner(),
      BenchmarkConstants.MOCK2, new MockQueryRunner(),
      BenchmarkConstants.MOCK3, new MockQueryRunner());

  /**
   * Empty private constructor since this is a factory.
   */
  private QueryRunnerFactory() {
  }

  /**
   * Function to get a QueryRunner of specified type.
   * @param type Type of query runner to get.
   * @return QueryRunner Object for specific type.
   * @throws Exception Thrown if no QueryRunner can be generated for the specified type.
   */
  public static QueryRunner getRunner(final String type) throws Exception {
    if (type == null) {
      throw new Exception("TODO: Proper exceptions.");
    }
    if (!QUERY_RUNNER_MAP.containsKey(type)) {
      throw new Exception("TODO: Proper exceptions.");
    }
    return QUERY_RUNNER_MAP.get(type);
  }
}
