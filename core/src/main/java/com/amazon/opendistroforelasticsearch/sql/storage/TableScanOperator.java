/*
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.storage;

import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlanNodeVisitor;
import java.util.Collections;
import java.util.List;

/**
 * Abstract table scan class for different storage to implement.
 * This is also to avoid "polluting" physical plan visitor by concrete table scan implementation.
 */
public abstract class TableScanOperator extends PhysicalPlan {

  @Override
  public <R, C> R accept(PhysicalPlanNodeVisitor<R, C> visitor, C context) {
    return visitor.visitTableScan(this, context);
  }

  @Override
  public List<PhysicalPlan> getChild() {
    return Collections.emptyList();
  }

  /**
   * Explain the execution plan.
   *
   * @return execution plan.
   */
  public abstract String explain();
}
