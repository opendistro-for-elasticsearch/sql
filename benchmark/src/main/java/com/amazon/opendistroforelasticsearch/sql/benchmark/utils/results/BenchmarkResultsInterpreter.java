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

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.results.html.HtmlRenderer;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.results.plot.PlotRenderer;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class to interpret the benchmark results.
 */
public class BenchmarkResultsInterpreter {

  /**
   * Function to interpret results and generate a report.
   * @param benchmarkResults Results to use for report.
   */
  public void interpretResults(
      final List<BenchmarkResults> benchmarkResults) throws Exception {
    final List<String> queryInfos = validateBenchmarkResultsGetQueries(benchmarkResults);
    PlotRenderer.render(benchmarkResults, queryInfos);
    HtmlRenderer.render(benchmarkResults, queryInfos);
  }

  private List<String> validateBenchmarkResultsGetQueries(
      final List<BenchmarkResults> benchmarkResultsList) throws Exception {
    if (benchmarkResultsList.isEmpty()) {
      throw new Exception("BenchmarkResults list is empty, cannot generate any data.");
    }
    final int size = benchmarkResultsList.get(0).getBenchmarkResults().size();
    if (size == 0) {
      throw new Exception("Inner BenchmarkResult is empty.");
    }
    for (final BenchmarkResults benchmarkResults: benchmarkResultsList) {
      if (benchmarkResults.getBenchmarkResults().size() != size) {
        throw new Exception("Inner BenchmarkResult size mismatch.");
      }
    }
    return benchmarkResultsList.get(0).getBenchmarkResults().stream()
        .map(BenchmarkResult::getQuery).collect(Collectors.toList());
  }
}
