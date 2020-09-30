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

package com.amazon.opendistroforelasticsearch.sql.planner.logical;

import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.NamedExpression;
import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Logical Index Scan Operation which could include Filter conditions ans Project Lists.
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class LogicalIndexScan extends LogicalPlan {

  /**
   * Relation Name.
   */
  private final String relationName;

  /**
   * Filter Condition.
   */
  private Expression filter;

  /**
   * Select List.
   */
  private List<NamedExpression> projectList;

  /**
   * Construct the {@link LogicalIndexScan} with relationName and filter condition.
   */
  public LogicalIndexScan(String relationName, Expression filter) {
    super(ImmutableList.of());
    this.relationName = relationName;
    this.filter = filter;
  }

  /**
   * Construct the {@link LogicalIndexScan} with relationName and project list.
   */
  public LogicalIndexScan(String relationName, List<NamedExpression> projectList) {
    super(ImmutableList.of());
    this.relationName = relationName;
    this.projectList = projectList;
  }

  /**
   * Construct the {@link LogicalIndexScan} with relationName, filter condition and project list.
   */
  public LogicalIndexScan(String relationName, Expression filter,
                          List<NamedExpression> projectList) {
    super(ImmutableList.of());
    this.relationName = relationName;
    this.filter = filter;
    this.projectList = projectList;
  }

  @Override
  public <R, C> R accept(LogicalPlanNodeVisitor<R, C> visitor, C context) {
    return visitor.visitIndexScan(this, context);
  }

  /**
   * Has Projects.
   */
  public boolean hasProjects() {
    return projectList != null && !projectList.isEmpty();
  }

  /**
   * Has Filter.
   */
  public boolean hasFilter() {
    return filter != null;
  }
}
