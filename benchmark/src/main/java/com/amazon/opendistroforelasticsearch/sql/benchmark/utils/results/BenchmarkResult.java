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

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

/**
 * Class to hold benchmark results.
 */
@Getter
public class BenchmarkResult {
  private List<Double> cpuUsage = new ArrayList<>();
  private List<Double> memoryUsage = new ArrayList<>();
  private List<Double> timeSlots;
  private Long executionTimeMilliseconds;
  private String query;
  private String type;

  private static final Double BYTE_TO_GIGABYTE = (double)(1024L * 1024L * 1024L);

  /**
   * Constructor for BenchmarkResult.
   * @param cpuUsage List of Doubles representing the CPU usage.
   * @param memoryUsage List of Doubles representing the memory usage.
   * @param executionTimeMilliseconds Execution time for the query in milliseconds.
   * @param query Query as a string.
   * @param type Type database for measurement.
   * @param timeSlots List of Doubles representing the time slots for measurements.
   */
  public BenchmarkResult(
      final List<Double> cpuUsage, final List<Double> memoryUsage,
      final Long executionTimeMilliseconds, final String query, final String type,
      final List<Double> timeSlots) {
    cpuUsage.forEach(x -> this.cpuUsage.add(x * 100));
    memoryUsage.forEach(x -> this.memoryUsage.add(x / BYTE_TO_GIGABYTE));
    this.executionTimeMilliseconds = executionTimeMilliseconds;
    this.query = query;
    this.type = type;
    this.timeSlots = timeSlots;
  }
}
