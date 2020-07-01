/*
 *
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.executor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.elasticsearch.executor.protector.ResourceMonitorPlan;
import com.amazon.opendistroforelasticsearch.sql.monitor.ResourceMonitor;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlanNodeVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ResourceMonitorPlanTest {
  @Mock
  private PhysicalPlan plan;

  @Mock
  private ResourceMonitor resourceMonitor;

  @Mock
  private PhysicalPlanNodeVisitor visitor;

  @Mock
  private Object context;

  private ResourceMonitorPlan monitorPlan;

  @BeforeEach
  public void setup() {
    monitorPlan = new ResourceMonitorPlan(plan, resourceMonitor);
  }

  @Test
  void openExceedResourceLimit() {
    when(resourceMonitor.isHealthy()).thenReturn(false);

    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> monitorPlan.open());
    assertEquals("resource is not enough to run the query, quit.", exception.getMessage());
  }

  @Test
  void openSuccess() {
    when(resourceMonitor.isHealthy()).thenReturn(true);

    monitorPlan.open();
    verify(plan, times(1)).open();
  }

  @Test
  void nextSuccess() {
    monitorPlan.next();
    verify(plan, times(1)).next();
  }

  @Test
  void hasNextSuccess() {
    monitorPlan.hasNext();
    verify(plan, times(1)).hasNext();
  }

  @Test
  void closeSuccess() {
    monitorPlan.close();
    verify(plan, times(1)).close();
  }

  @Test
  void getChildSuccess() {
    monitorPlan.getChild();
    verify(plan, times(1)).getChild();
  }

  @Test
  void acceptSuccess() {
    monitorPlan.accept(visitor, context);
    verify(plan, times(1)).accept(visitor, context);
  }
}