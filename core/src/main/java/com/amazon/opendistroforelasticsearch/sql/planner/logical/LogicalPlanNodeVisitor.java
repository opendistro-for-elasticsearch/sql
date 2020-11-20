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

/**
 * The visitor of {@link LogicalPlan}.
 *
 * @param <R> return object type.
 * @param <C> context type.
 */
public abstract class LogicalPlanNodeVisitor<R, C> {

  public R visitNode(LogicalPlan plan, C context) {
    return null;
  }

  public R visitRelation(LogicalRelation plan, C context) {
    return visitNode(plan, context);
  }

  public R visitFilter(LogicalFilter plan, C context) {
    return visitNode(plan, context);
  }

  public R visitAggregation(LogicalAggregation plan, C context) {
    return visitNode(plan, context);
  }

  public R visitDedupe(LogicalDedupe plan, C context) {
    return visitNode(plan, context);
  }

  public R visitHead(LogicalHead plan, C context) {
    return visitNode(plan, context);
  }

  public R visitRename(LogicalRename plan, C context) {
    return visitNode(plan, context);
  }

  public R visitProject(LogicalProject plan, C context) {
    return visitNode(plan, context);
  }

  public R visitWindow(LogicalWindow plan, C context) {
    return visitNode(plan, context);
  }

  public R visitRemove(LogicalRemove plan, C context) {
    return visitNode(plan, context);
  }

  public R visitEval(LogicalEval plan, C context) {
    return visitNode(plan, context);
  }

  public R visitSort(LogicalSort plan, C context) {
    return visitNode(plan, context);
  }

  public R visitValues(LogicalValues plan, C context) {
    return visitNode(plan, context);
  }

  public R visitRareTopN(LogicalRareTopN plan, C context) {
    return visitNode(plan, context);
  }
}
