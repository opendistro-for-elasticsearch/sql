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

package com.amazon.opendistroforelasticsearch.sql.planner.physical;

import com.amazon.opendistroforelasticsearch.sql.storage.TableScanOperator;

/**
 * The visitor of {@link PhysicalPlan}.
 *
 * @param <R> return object type.
 * @param <C> context type.
 */
public abstract class PhysicalPlanNodeVisitor<R, C> {

  protected R visitNode(PhysicalPlan node, C context) {
    return null;
  }

  public R visitFilter(FilterOperator node, C context) {
    return visitNode(node, context);
  }

  public R visitAggregation(AggregationOperator node, C context) {
    return visitNode(node, context);
  }

  public R visitRename(RenameOperator node, C context) {
    return visitNode(node, context);
  }

  public R visitTableScan(TableScanOperator node, C context) {
    return visitNode(node, context);
  }

  public R visitProject(ProjectOperator node, C context) {
    return visitNode(node, context);
  }

  public R visitWindow(WindowOperator node, C context) {
    return visitNode(node, context);
  }

  public R visitRemove(RemoveOperator node, C context) {
    return visitNode(node, context);
  }

  public R visitEval(EvalOperator node, C context) {
    return visitNode(node, context);
  }

  public R visitDedupe(DedupeOperator node, C context) {
    return visitNode(node, context);
  }

  public R visitValues(ValuesOperator node, C context) {
    return visitNode(node, context);
  }

  public R visitSort(SortOperator node, C context) {
    return visitNode(node, context);
  }

  public R visitHead(HeadOperator node, C context) {
    return visitNode(node, context);
  }
  
  public R visitRareTopN(RareTopNOperator node, C context) {
    return visitNode(node, context);
  }

  public R visitLimit(LimitOperator node, C context) {
    return visitNode(node, context);
  }

}
