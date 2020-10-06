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
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.NamedAggregator;
import java.util.Collections;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Logical Aggregation.
 */
@ToString
@EqualsAndHashCode(callSuper = true)
public class LogicalAggregation extends LogicalPlan {

  @Getter
  private final List<NamedAggregator> aggregatorList;

  @Getter
  private final List<NamedExpression> groupByList;

  /**
   * Constructor of LogicalAggregation.
   */
  public LogicalAggregation(
      LogicalPlan child,
      List<NamedAggregator> aggregatorList,
      List<NamedExpression> groupByList) {
    super(Collections.singletonList(child));
    this.aggregatorList = aggregatorList;
    this.groupByList = groupByList;
  }

  @Override
  public <R, C> R accept(LogicalPlanNodeVisitor<R, C> visitor, C context) {
    return visitor.visitAggregation(this, context);
  }
}
