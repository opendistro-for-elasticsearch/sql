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

package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.results.html;

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.results.BenchmarkResult;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Class to contain plan to render HTML output file.
 */
@AllArgsConstructor
public class HtmlPlan {
  @Getter
  private final String query;
  private List<BenchmarkResult> benchmarkResults;

  /**
   * Function to convert BenchmarkResult List to TableRow List.
   * @return TableRow List.
   */
  public List<TableRow> getTableResults() {
    return benchmarkResults.stream().map(TableRow::new).collect(Collectors.toList());
  }

  /**
   * Class to hold information required to render rows in HTML file tables.
   */
  @AllArgsConstructor
  class TableRow {
    private final BenchmarkResult benchmarkResult;

    /**
     * Function to turn double to string with 1 decimal.
     * @param in Input double.
     * @return Formatted String.
     */
    private String asString(final double in) {
      return String.format("%.1f", in);
    }

    /**
     * Function to get minimum of Double List.
     * @param list List of Doubles.
     * @return Minimum value of Doubles with proper formatting.
     */
    private String getMin(List<Double> list) {
      return asString(list.stream().mapToDouble(a -> a).min().getAsDouble());
    }

    /**
     * Function to get maximum of Double List.
     * @param list List of Doubles.
     * @return Maximum value of Doubles with proper formatting.
     */
    private String getMax(List<Double> list) {
      return asString(list.stream().mapToDouble(a -> a).max().getAsDouble());
    }

    /**
     * Function to get average of Double List.
     * @param list List of Doubles.
     * @return Average value of Doubles with proper formatting.
     */
    private String getAvg(List<Double> list) {
      return asString(list.stream().mapToDouble(a -> a).average().getAsDouble());
    }

    /**
     * Function to get database for result.
     * @return String of Database name for result.
     */
    String getDatabase() {
      return benchmarkResult.getType();
    }

    /**
     * Function to return average CPU usage.
     * @return String with average CPU usage.
     */
    String getAvgCpu() {
      return getAvg(benchmarkResult.getCpuUsage());
    }

    /**
     * Function to return maximum CPU usage.
     * @return String with maximum CPU usage.
     */
    String getMaxCpu() {
      return getMax(benchmarkResult.getCpuUsage());
    }

    /**
     * Function to return minimum CPU usage.
     * @return String with minimum CPU usage.
     */
    String getMinCpu() {
      return getMin(benchmarkResult.getCpuUsage());
    }

    /**
     * Function to return average memory usage.
     * @return String with average memory usage.
     */
    String getAvgMem() {
      return getAvg(benchmarkResult.getMemoryUsage());
    }

    /**
     * Function to return maximum memory usage.
     * @return String with maximum memory usage.
     */
    String getMaxMem() {
      return getMax(benchmarkResult.getMemoryUsage());
    }

    /**
     * Function to return minimum memory usage.
     * @return String with minimum memory usage.
     */
    String getMinMem() {
      return getMin(benchmarkResult.getMemoryUsage());
    }

    /**
     * Function to get execution time of result.
     * @return String with execution time of result.
     */
    String getExecutionTime() {
      return benchmarkResult.getExecutionTimeMilliseconds().toString();
    }
  }
}
