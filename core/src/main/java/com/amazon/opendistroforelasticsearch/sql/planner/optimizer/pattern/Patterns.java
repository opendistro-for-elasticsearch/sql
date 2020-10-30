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

package com.amazon.opendistroforelasticsearch.sql.planner.optimizer.pattern;

import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.facebook.presto.matching.Property;
import java.util.Optional;
import lombok.experimental.UtilityClass;

/**
 * Pattern helper class.
 */
@UtilityClass
public class Patterns {

  /**
   * LogicalPlan source {@link Property}.
   */
  public static Property<LogicalPlan, LogicalPlan> source() {
    return Property.optionalProperty("source", plan -> plan.getChild().size() == 1
        ? Optional.of(plan.getChild().get(0))
        : Optional.empty());
  }
}
