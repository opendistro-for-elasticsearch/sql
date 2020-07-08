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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.executor.protector;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.monitor.ResourceMonitor;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlanNodeVisitor;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * A PhysicalPlan which will run the delegate plan in resource protection manner.
 */
@ToString
@RequiredArgsConstructor
@EqualsAndHashCode
public class ResourceMonitorPlan extends PhysicalPlan {
  /**
   * Delegated PhysicalPlan.
   */
  private final PhysicalPlan delegate;

  /**
   * ResourceMonitor.
   */
  @ToString.Exclude
  private final ResourceMonitor monitor;

  @Override
  public <R, C> R accept(PhysicalPlanNodeVisitor<R, C> visitor, C context) {
    return delegate.accept(visitor, context);
  }

  @Override
  public void open() {
    if (!this.monitor.isHealthy()) {
      throw new IllegalStateException("resource is not enough to run the query, quit.");
    }
    delegate.open();
  }

  @Override
  public void close() {
    delegate.close();
  }

  @Override
  public List<PhysicalPlan> getChild() {
    return delegate.getChild();
  }

  @Override
  public boolean hasNext() {
    return delegate.hasNext();
  }

  @Override
  public ExprValue next() {
    return delegate.next();
  }
}
