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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.monitor;

import com.google.common.annotations.VisibleForTesting;
import java.util.concurrent.ThreadLocalRandom;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;

/**
 * Elasticsearch Memory Monitor.
 */
@Log4j2
public class ElasticsearchMemoryHealthy {
  private final RandomFail randomFail;
  private final MemoryUsage memoryUsage;

  public ElasticsearchMemoryHealthy() {
    randomFail = new RandomFail();
    memoryUsage = new MemoryUsage();
  }

  @VisibleForTesting
  public ElasticsearchMemoryHealthy(
      RandomFail randomFail,
      MemoryUsage memoryUsage) {
    this.randomFail = randomFail;
    this.memoryUsage = memoryUsage;
  }

  /**
   * Is Memory Healthy. Calculate based on the current heap memory usage.
   */
  public boolean isMemoryHealthy(long limitBytes) {
    final long memoryUsage = this.memoryUsage.usage();
    log.debug("Memory usage:{}, limit:{}", memoryUsage, limitBytes);
    if (memoryUsage < limitBytes) {
      return true;
    } else {
      log.warn("Memory usage:{} exceed limit:{}", memoryUsage, limitBytes);
      if (randomFail.shouldFail()) {
        log.warn("Fast failure the current request");
        throw new MemoryUsageExceedFastFailureException();
      } else {
        throw new MemoryUsageExceedException();
      }
    }
  }

  static class RandomFail {
    public boolean shouldFail() {
      return ThreadLocalRandom.current().nextBoolean();
    }
  }

  static class MemoryUsage {
    public long usage() {
      final long freeMemory = Runtime.getRuntime().freeMemory();
      final long totalMemory = Runtime.getRuntime().totalMemory();
      return totalMemory - freeMemory;
    }
  }

  @NoArgsConstructor
  public static class MemoryUsageExceedFastFailureException extends RuntimeException {

  }

  @NoArgsConstructor
  public static class MemoryUsageExceedException extends RuntimeException {

  }
}
