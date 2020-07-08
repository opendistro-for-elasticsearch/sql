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

import com.amazon.opendistroforelasticsearch.sql.common.setting.Settings;
import com.amazon.opendistroforelasticsearch.sql.monitor.ResourceMonitor;
import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import java.util.function.Supplier;
import lombok.extern.log4j.Log4j2;
import org.elasticsearch.common.unit.ByteSizeValue;

/**
 * {@link ResourceMonitor} implementation on Elasticsearch. When the heap memory usage exceeds
 * certain threshold, the monitor is not healthy.
 * Todo, add metrics.
 */
@Log4j2
public class ElasticsearchResourceMonitor extends ResourceMonitor {
  private final Settings settings;
  private final Retry retry;
  private final ElasticsearchMemoryHealthy memoryMonitor;

  /**
   * Constructor of ElasticsearchCircuitBreaker.
   */
  public ElasticsearchResourceMonitor(
      Settings settings,
      ElasticsearchMemoryHealthy memoryMonitor) {
    this.settings = settings;
    RetryConfig config =
        RetryConfig.custom()
            .maxAttempts(3)
            .intervalFunction(IntervalFunction.ofExponentialRandomBackoff(1000))
            .retryExceptions(ElasticsearchMemoryHealthy.MemoryUsageExceedException.class)
            .ignoreExceptions(
                ElasticsearchMemoryHealthy.MemoryUsageExceedFastFailureException.class)
            .build();
    retry = Retry.of("mem", config);
    this.memoryMonitor = memoryMonitor;
  }

  /**
   * Is Healthy.
   *
   * @return true if healthy, otherwise return false.
   */
  @Override
  public boolean isHealthy() {
    try {
      ByteSizeValue limit = settings.getSettingValue(Settings.Key.PPL_QUERY_MEMORY_LIMIT);
      Supplier<Boolean> booleanSupplier =
          Retry.decorateSupplier(retry,
              () -> memoryMonitor
                  .isMemoryHealthy(limit.getBytes()));
      return booleanSupplier.get();
    } catch (Exception e) {
      return false;
    }
  }
}
