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
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.results.BenchmarkResult;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.results.BenchmarkResults;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

/**
 * Class to test ResultGrabber.
 */
public class ResultGrabberTest {
  private static final List<String> QUERIES = ImmutableList.of("1", "2", "3");

  @Test
  public void testResultGrab() throws Exception {
    final ResultGrabber resultGrabber = new ResultGrabber(BenchmarkConstants.MOCK, 10);
    final BenchmarkResults results = resultGrabber.runQueries(QUERIES);
    final List<BenchmarkResult> resultList = results.getBenchmarkResults();
    final Long totalMemory = results.getTotalMemory();
    Assert.assertTrue(totalMemory > 0);
    System.out.println("Total Memory: " + totalMemory);
    for (BenchmarkResult result: resultList) {
      final List<Double> cpuUsage = result.getCpuUsage();
      cpuUsage.forEach(cpu -> {
        Assert.assertTrue(cpu > 0.0);
      });
      Assert.assertTrue(cpuUsage.size() > 0);
      final List<Long> memoryUsage = result.getMemoryUsage();
      memoryUsage.forEach(mem -> {
        Assert.assertTrue(mem > 0);
      });
      Assert.assertTrue(memoryUsage.size() > 0);
      final Long executionTime = result.getExecutionTimeMilliseconds();
      Assert.assertTrue(executionTime > 0);
      System.out.println("Cpu Usage: " + cpuUsage.toString());
      System.out.println("Memory usage: " + memoryUsage.toString());
      System.out.println("Total time: " + executionTime.toString());
    }
  }
}

