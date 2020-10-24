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
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sun.management.OperatingSystemMXBean;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Result grabber class, used to run queries against a specified database.
 */
public class ResultGrabber {
  private final String type;
  private final QueryRunner queryRunner;
  private final Double scaleFactor;

  private static final OperatingSystemMXBean INFO_GRABBER =
      (com.sun.management.OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
  private static int RESULT_GRABBER_IDX = 1;
  private ExecutorService executorService;
  private static final long STOP_TIMEOUT_SECONDS = 5L;
  private static final long WAIT_TIME_MILLISECONDS = 100L;
  private static final long INFO_SAMPLE_PERIOD_MILLISECONDS = 500L;
  private static final long MAX_WAIT_TIME_MILLISECONDS = 5000L;
  private static final double INVALID_CPU = -1.0;
  private static final double MAX_CPU = 1.0;
  private volatile boolean grabInProgress = false;
  private volatile boolean shutdownFlag = false;
  private List<Double> cpuUsage = new ArrayList<>();
  private List<Double> memoryUsage = new ArrayList<>();
  private List<Double> currentTime = new ArrayList<>();
  private Long executionTimeStart = -1L;
  private Long executionTimeEnd = -1L;

  /**
   * Constructor for ResultGrabber, assigns QueryRunner.
   * @param type Type of database to get results for.
   * @param scaleFactor Scale factor for data set.
   * @throws Exception Thrown if QueryRunner cannot be found for type.
   */
  public ResultGrabber(final String type, Double scaleFactor) throws Exception {
    this.type = type;
    this.scaleFactor = scaleFactor;
    this.queryRunner = QueryRunnerFactory.getRunner(type);
  }

  /**
   * Function to run multiple queries and compile results into a single Object.
   * @param queries Queries to run against the specified database.
   * @return BenchmarkResults Object as result of running queries.
   */
  public BenchmarkResults runQueries(final List<String> queries, final String benchmarkPath)
      throws Exception {
    final List<BenchmarkResult> results = new ArrayList<>();
    for (String query: queries) {
      results.add(grabResult(query, benchmarkPath));
    }
    return new BenchmarkResults(
        results, type, scaleFactor, INFO_GRABBER.getTotalPhysicalMemorySize());
  }

  /**
   * Function to grab a single result.
   * @param query Query to run.
   * @return Single BenchmarkResult as a result of running the query.
   * @throws Exception If grabbing the result or running the query fails.
   */
  private BenchmarkResult grabResult(final String query, final String benchmarkPath)
      throws Exception {
    queryRunner.prepareQueryRunner(query);
    startGrab();
    queryRunner.runQuery();
    BenchmarkResult result = stopGrab(query);
    queryRunner.checkQueryExecutionStatus(benchmarkPath);
    return result;
  }

  /**
   * Function to pause the system until benchmarking results can be obtained.
   * @throws Exception If benchmarking results cannot be obtained.
   */
  private static void waitUntilReady() throws Exception {
    // Results are invalid for a little bit after the JVM boots, need to give it time to update.
    final long startTime = System.currentTimeMillis();
    double cpuLoad = -1.0;
    while ((cpuLoad == INVALID_CPU) || (cpuLoad == MAX_CPU)) {
      if ((System.currentTimeMillis() - startTime) > MAX_WAIT_TIME_MILLISECONDS) {
        throw new Exception(
            String.format("Failed to get valid performance metrics within %d milliseconds.",
                MAX_WAIT_TIME_MILLISECONDS));
      }
      sleepForTime(WAIT_TIME_MILLISECONDS);
      cpuLoad = INFO_GRABBER.getSystemCpuLoad();
    }

  }

  /**
   * Function to start grabbing results.
   * @throws Exception If the attempt to start grabbing results fails.
   */
  private void startGrab() throws Exception {
    if (grabInProgress) {
      throw new Exception("Cannot start a grab when another grab is in progress.");
    }
    grabInProgress = true;
    shutdownFlag = false;
    waitUntilReady();
    startGrabDaemon();
    executionTimeStart = System.currentTimeMillis();
  }

  /**
   * Function to stop grabbing results.
   * @return A single BenchmarkResult Object using all the data grabbed during the query.
   * @throws Exception If the attempt to stop grabbing results fails.
   */
  private BenchmarkResult stopGrab(String query) throws Exception {
    if (!grabInProgress) {
      throw new Exception("Cannot stop a grab when no grab is in progress.");
    }
    executionTimeEnd = System.currentTimeMillis();
    shutdownFlag = true;
    stopGrabDaemon();
    BenchmarkResult result = generateNewBenchmarkResult(query);
    grabInProgress = false;
    return result;
  }

  /**
   * Function to launch a daemon that will grab results.
   */
  private void startGrabDaemon() {
    executorService = Executors.newSingleThreadExecutor(
        new ThreadFactoryBuilder().setNameFormat(
            String.format("Result-Grabbed-Thread-%d", RESULT_GRABBER_IDX++))
            .setDaemon(true).build());
    executorService.execute(() -> {
      final long startTime = System.currentTimeMillis();
      while (!shutdownFlag) {
        currentTime.add((double)System.currentTimeMillis() - (double)startTime);
        memoryUsage.add((double)(
            INFO_GRABBER.getTotalPhysicalMemorySize() - INFO_GRABBER.getFreePhysicalMemorySize()));
        cpuUsage.add(INFO_GRABBER.getSystemCpuLoad());
        sleepForTime(INFO_SAMPLE_PERIOD_MILLISECONDS);
      }
    });
  }

  /**
   * Function to shut down the daemon that is grabbing results.
   */
  private void stopGrabDaemon() throws Exception {
    executorService.shutdown();
    try {
      executorService.awaitTermination(STOP_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    if (!executorService.isTerminated()) {
      throw new Exception("Failed to stop system info grabber.");
    }
  }

  /**
   * Function to sleep for specified time (in milliseconds).
   * @param time Amount of time to sleep for (in milliseconds).
   */
  private static void sleepForTime(final long time) {
    try {
      Thread.sleep(time);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Function to latch the current results into a result.
   * @return BenchmarkResult Object, generated with the results.
   * @throws Exception If the current results are invalid.
   */
  private BenchmarkResult generateNewBenchmarkResult(String query) throws Exception {
    if ((executionTimeStart == -1L) || (executionTimeEnd == -1L)
        || cpuUsage.isEmpty() || memoryUsage.isEmpty() || currentTime.isEmpty()) {
      throw new Exception(
          String.format("Failed to get performance metrics. "
                  + "Start: %d, End: %d, Cpu usage count: %d, Memory usage count: %d"
                  + "Current time count: %d",
              executionTimeStart, executionTimeEnd,
              cpuUsage.size(), memoryUsage.size(), currentTime.size()));
    }
    final BenchmarkResult result = new BenchmarkResult(
        cpuUsage, memoryUsage, executionTimeEnd - executionTimeStart, query, type, currentTime);
    cpuUsage = new ArrayList<>();
    memoryUsage = new ArrayList<>();
    currentTime = new ArrayList<>();
    executionTimeStart = -1L;
    executionTimeEnd = -1L;
    return result;
  }
}
