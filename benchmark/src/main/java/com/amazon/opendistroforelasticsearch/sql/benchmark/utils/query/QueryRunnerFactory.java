package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.query;

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.BenchmarkConstants;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.query.elasticsearch.ElasticsearchQueryRunner;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Factory to get query runner for specified database type.
 */
public class QueryRunnerFactory {
  private static Map<String, QueryRunner> QUERY_RUNNER_MAP = ImmutableMap.of(
      BenchmarkConstants.ELASTICSEARCH, new ElasticsearchQueryRunner());

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
