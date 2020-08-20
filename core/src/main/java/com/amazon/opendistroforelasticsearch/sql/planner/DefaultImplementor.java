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

package com.amazon.opendistroforelasticsearch.sql.planner;

import com.amazon.opendistroforelasticsearch.sql.planner.logical.*;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.*;

/**
 * Default implementor for implementing logical to physical translation. "Default" here means all
 * logical operator will be translated to correspondent physical operator to pipeline operations
 * in post-processing style in memory.
 * Different storage can override methods here to optimize default pipelining operator, for example
 * a storage has the flexibility to override visitFilter and visitRelation to push down filtering
 * operation and return a single physical index scan operator.
 *
 * @param <C>   context type
 */
public class DefaultImplementor<C> extends LogicalPlanNodeVisitor<PhysicalPlan, C> {

  @Override
  public PhysicalPlan visitDedupe(LogicalDedupe node, C context) {
    return new DedupeOperator(
        visitChild(node, context),
        node.getDedupeList(),
        node.getAllowedDuplication(),
        node.getKeepEmpty(),
        node.getConsecutive());
  }

  @Override
  public PhysicalPlan visitHead(LogicalHead node, C context) {
    return new HeadOperator(
            visitChild(node, context),
            node.getNumber(),
            node.getKeeplast()
    );
  }

  @Override
  public PhysicalPlan visitProject(LogicalProject node, C context) {
    return new ProjectOperator(visitChild(node, context), node.getProjectList());
  }

  @Override
  public PhysicalPlan visitRemove(LogicalRemove node, C context) {
    return new RemoveOperator(visitChild(node, context), node.getRemoveList());
  }

  @Override
  public PhysicalPlan visitEval(LogicalEval node, C context) {
    return new EvalOperator(visitChild(node, context), node.getExpressions());
  }

  @Override
  public PhysicalPlan visitSort(LogicalSort node, C context) {
    return new SortOperator(visitChild(node, context), node.getCount(), node.getSortList());
  }

  @Override
  public PhysicalPlan visitRename(LogicalRename node, C context) {
    return new RenameOperator(visitChild(node, context), node.getRenameMap());
  }

  @Override
  public PhysicalPlan visitAggregation(LogicalAggregation node, C context) {
    return new AggregationOperator(
        visitChild(node, context), node.getAggregatorList(), node.getGroupByList());
  }

  @Override
  public PhysicalPlan visitFilter(LogicalFilter node, C context) {
    return new FilterOperator(visitChild(node, context), node.getCondition());
  }

  @Override
  public PhysicalPlan visitValues(LogicalValues node, C context) {
    return new ValuesOperator(node.getValues());
  }

  @Override
  public PhysicalPlan visitRelation(LogicalRelation node, C context) {
    throw new UnsupportedOperationException("Storage engine is responsible for "
        + "implementing and optimizing logical plan with relation involved");
  }

  protected PhysicalPlan visitChild(LogicalPlan node, C context) {
    // Logical operators visited here must have a single child
    return node.getChild().get(0).accept(this, context);
  }

}
