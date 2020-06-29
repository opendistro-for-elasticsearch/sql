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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.common.setting.Settings;
import org.elasticsearch.common.unit.ByteSizeValue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ElasticsearchResourceMonitorTest {

  @Mock
  private Settings settings;

  @Mock
  private ElasticsearchMemoryHealthy memoryMonitor;

  @BeforeEach
  public void setup() {
    when(settings.getSettingValue(Settings.Key.PPL_QUERY_MEMORY_LIMIT))
        .thenReturn(new ByteSizeValue(10L));
  }

  @Test
  void isHealthy() {
    when(memoryMonitor.isMemoryHealthy(anyLong())).thenReturn(true);

    ElasticsearchResourceMonitor resourceMonitor =
        new ElasticsearchResourceMonitor(settings, memoryMonitor);
    assertTrue(resourceMonitor.isHealthy());
  }

  @Test
  void notHealthyFastFailure() {
    when(memoryMonitor.isMemoryHealthy(anyLong())).thenThrow(
        ElasticsearchMemoryHealthy.MemoryUsageExceedFastFailureException.class);

    ElasticsearchResourceMonitor resourceMonitor =
        new ElasticsearchResourceMonitor(settings, memoryMonitor);
    assertFalse(resourceMonitor.isHealthy());
    verify(memoryMonitor, times(1)).isMemoryHealthy(anyLong());
  }

  @Test
  void notHealthyWithRetry() {
    when(memoryMonitor.isMemoryHealthy(anyLong())).thenThrow(
        ElasticsearchMemoryHealthy.MemoryUsageExceedException.class);

    ElasticsearchResourceMonitor resourceMonitor =
        new ElasticsearchResourceMonitor(settings, memoryMonitor);
    assertFalse(resourceMonitor.isHealthy());
    verify(memoryMonitor, times(3)).isMemoryHealthy(anyLong());
  }

  @Test
  void healthyWithRetry() {

    when(memoryMonitor.isMemoryHealthy(anyLong())).thenThrow(
        ElasticsearchMemoryHealthy.MemoryUsageExceedException.class).thenReturn(true);

    ElasticsearchResourceMonitor resourceMonitor =
        new ElasticsearchResourceMonitor(settings, memoryMonitor);
    assertTrue(resourceMonitor.isHealthy());
    verify(memoryMonitor, times(2)).isMemoryHealthy(anyLong());
  }
}
