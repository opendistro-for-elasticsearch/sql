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

import com.amazon.opendistroforelasticsearch.sql.expression.NamedExpression;
import java.util.Collections;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Project field specified by the {@link LogicalProject#projectList}.
 */
@ToString
@EqualsAndHashCode(callSuper = true)
public class LogicalProject extends LogicalPlan {

  @Getter
  private final List<NamedExpression> projectList;

  /**
   * Constructor of LogicalProject.
   */
  public LogicalProject(
      LogicalPlan child,
      List<NamedExpression> projectList) {
    super(Collections.singletonList(child));
    this.projectList = projectList;
  }

  @Override
  public <R, C> R accept(LogicalPlanNodeVisitor<R, C> visitor, C context) {
    return visitor.visitProject(this, context);
  }
}
