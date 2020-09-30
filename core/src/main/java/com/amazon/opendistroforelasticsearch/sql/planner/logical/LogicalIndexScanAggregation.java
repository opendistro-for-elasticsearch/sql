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
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.NamedAggregator;
import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Logical Index Scan Aggregation Operation.
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class LogicalIndexScanAggregation extends LogicalPlan {

  private final String relationName;

  private Expression filter;

  private List<NamedAggregator> aggregatorList;

  private List<NamedExpression> groupByList;

  /**
   * Construct {@link LogicalIndexScanAggregation} with Filter and Aggregation.
   */
  public LogicalIndexScanAggregation(String relationName, Expression filter,
                          List<NamedAggregator> aggregatorList,
                          List<NamedExpression> groupByList) {
    super(ImmutableList.of());
    this.filter = filter;
    this.relationName = relationName;
    this.aggregatorList = aggregatorList;
    this.groupByList = groupByList;
  }

  /**
   * Construct {@link LogicalIndexScanAggregation} with Aggregation without Filter.
   */
  public LogicalIndexScanAggregation(String relationName,
                                     List<NamedAggregator> aggregatorList,
                                     List<NamedExpression> groupByList) {
    this(relationName, null, aggregatorList, groupByList);
  }

  @Override
  public <R, C> R accept(LogicalPlanNodeVisitor<R, C> visitor, C context) {
    return visitor.visitIndexScanAggregation(this, context);
  }
}
