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

package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.results;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Class to hold a list of benchmark results and the associated database type.
 */
@Getter
@AllArgsConstructor
public class BenchmarkResults {
  private final List<BenchmarkResult> benchmarkResults;
  private final String benchmarkType;
  private final Double scaleFactor;
  private final Long totalMemory;

  /**
   * Function to get the BenchmarkResult belonging to a given query.
   * @param query Query to search for.
   * @return BenchmarkResult belonging to query.
   * @throws Exception Thrown if BenchmarkResult could nto be found.
   */
  public BenchmarkResult getByQuery(final String query) throws Exception {
    for (final BenchmarkResult benchmarkResult: benchmarkResults) {
      if (benchmarkResult.getQuery().equals(query)) {
        return benchmarkResult;
      }
    }
    throw new Exception(String.format("Failed to find a BenchmarkResult with query '%s'", query));
  }
}
