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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.planner.logical;

import com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.NamedExpression;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanNodeVisitor;
import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Elasticsearch Logical Index Scan Operation.
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class ElasticsearchLogicalIndexScan extends LogicalPlan {

  /**
   * Relation Name.
   */
  private final String relationName;

  /**
   * Filter Condition.
   */
  @Setter
  private Expression filter;

  /**
   * Projection List.
   */
  @Setter
  private List<NamedExpression> projectList;

  /**
   * Sort List.
   */
  @Setter
  private List<Pair<Sort.SortOption, Expression>> sortList;

  @Setter
  private Integer offset;

  @Setter
  private Integer limit;

  /**
   * ElasticsearchLogicalIndexScan Constructor.
   */
  @Builder
  public ElasticsearchLogicalIndexScan(
      String relationName,
      Expression filter,
      List<NamedExpression> projectList,
      List<Pair<Sort.SortOption, Expression>> sortList,
      Integer limit, Integer offset) {
    super(ImmutableList.of());
    this.relationName = relationName;
    this.filter = filter;
    this.projectList = projectList;
    this.sortList = sortList;
    this.limit = limit;
    this.offset = offset;
  }

  @Override
  public <R, C> R accept(LogicalPlanNodeVisitor<R, C> visitor, C context) {
    return visitor.visitNode(this, context);
  }

  public boolean hasLimit() {
    return limit != null;
  }
}
