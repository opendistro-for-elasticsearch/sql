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
 * Logical operator which is a sequence of literal list.
 */
@ToString
@Getter
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class LogicalValues extends LogicalPlan {

  private final List<List<LiteralExpression>> values;

  @Override
  public <R, C> R accept(LogicalPlanNodeVisitor<R, C> visitor, C context) {
    return visitor.visitValues(this, context);
  }

  @Override
  public List<LogicalPlan> getChild() {
    return ImmutableList.of();
  }

}
