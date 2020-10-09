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

package com.amazon.opendistroforelasticsearch.sql.planner.logical;

import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.window.WindowDefinition;
import java.util.Collections;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Logical operator for window function generated from project list. Logically, each window operator
 * has to work with a Sort operator to ensure input data is sorted as required by window definition.
 * However, the Sort operator may be removed after logical optimization.
 */
@EqualsAndHashCode(callSuper = true)
@Getter
@ToString
public class LogicalWindow extends LogicalPlan {
  private final Expression windowFunction;
  private final WindowDefinition windowDefinition;

  /**
   * Constructor of logical window.
   */
  public LogicalWindow(
      LogicalPlan child,
      Expression windowFunction,
      WindowDefinition windowDefinition) {
    super(Collections.singletonList(child));
    this.windowFunction = windowFunction;
    this.windowDefinition = windowDefinition;
  }

  @Override
  public <R, C> R accept(LogicalPlanNodeVisitor<R, C> visitor, C context) {
    return visitor.visitWindow(this, context);
  }

}
