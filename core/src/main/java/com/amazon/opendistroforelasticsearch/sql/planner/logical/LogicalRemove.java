/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.planner.logical;

import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Remove field specified by the {@link LogicalRemove#removeList}.
 */
@ToString
@EqualsAndHashCode(callSuper = true)
public class LogicalRemove extends LogicalPlan {

  @Getter
  private final Set<ReferenceExpression> removeList;

  /**
   * Constructor of LogicalRemove.
   */
  public LogicalRemove(
      LogicalPlan child,
      Set<ReferenceExpression> removeList) {
    super(Collections.singletonList(child));
    this.removeList = removeList;
  }

  @Override
  public <R, C> R accept(LogicalPlanNodeVisitor<R, C> visitor, C context) {
    return visitor.visitRemove(this, context);
  }
}
