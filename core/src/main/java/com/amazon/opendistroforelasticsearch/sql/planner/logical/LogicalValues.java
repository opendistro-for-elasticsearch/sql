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

import com.amazon.opendistroforelasticsearch.sql.expression.LiteralExpression;
import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Logical operator which is a sequence of literal rows (like a relation).
 * Basically, Values operator is used to create rows of constant literals
 * "out of nothing" which is corresponding with VALUES clause in SQL.
 * Mostly all rows must have the same number of literals and each column should
 * have same type or can be converted implicitly.
 * In particular, typical use cases include:
 *  1. Project without relation involved.
 *  2. Defining query or insertion without a relation.
 * Take the following logical plan for example:
 *  <pre>
 *  LogicalProject(expr=[log(2),true,1+2])
 *   |_ LogicalValues([[]])  #an empty row so that Project can evaluate its expressions in next()
 *  </pre>
 */
@ToString
@Getter
@EqualsAndHashCode(callSuper = true)
public class LogicalValues extends LogicalPlan {

  private final List<List<LiteralExpression>> values;

  /**
   * Constructor of LogicalValues.
   */
  public LogicalValues(
      List<List<LiteralExpression>> values) {
    super(ImmutableList.of());
    this.values = values;
  }

  @Override
  public <R, C> R accept(LogicalPlanNodeVisitor<R, C> visitor, C context) {
    return visitor.visitValues(this, context);
  }

}
