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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.executor.protector;

import com.amazon.opendistroforelasticsearch.sql.monitor.ResourceMonitor;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.AggregationOperator;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.DedupeOperator;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.EvalOperator;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.FilterOperator;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.HeadOperator;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.ProjectOperator;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.RareTopNOperator;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.RemoveOperator;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.RenameOperator;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.SortOperator;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.ValuesOperator;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.WindowOperator;
import com.amazon.opendistroforelasticsearch.sql.storage.TableScanOperator;
import lombok.RequiredArgsConstructor;

/**
 * Elasticsearch Execution Protector.
 */
@RequiredArgsConstructor
public class ElasticsearchExecutionProtector extends ExecutionProtector {

  /**
   * Elasticsearch resource monitor.
   */
  private final ResourceMonitor resourceMonitor;

  public PhysicalPlan protect(PhysicalPlan physicalPlan) {
    return physicalPlan.accept(this, null);
  }

  @Override
  public PhysicalPlan visitFilter(FilterOperator node, Object context) {
    return new FilterOperator(visitInput(node.getInput(), context), node.getConditions());
  }

  @Override
  public PhysicalPlan visitAggregation(AggregationOperator node, Object context) {
    return new AggregationOperator(visitInput(node.getInput(), context), node.getAggregatorList(),
        node.getGroupByExprList());
  }

  @Override
  public PhysicalPlan visitRareTopN(RareTopNOperator node, Object context) {
    return new RareTopNOperator(visitInput(node.getInput(), context), node.getCommandType(),
        node.getNoOfResults(), node.getFieldExprList(), node.getGroupByExprList());
  }

  @Override
  public PhysicalPlan visitRename(RenameOperator node, Object context) {
    return new RenameOperator(visitInput(node.getInput(), context), node.getMapping());
  }

  /**
   * Decorate with {@link ResourceMonitorPlan}.
   */
  @Override
  public PhysicalPlan visitTableScan(TableScanOperator node, Object context) {
    return new ResourceMonitorPlan(node, resourceMonitor);
  }

  @Override
  public PhysicalPlan visitProject(ProjectOperator node, Object context) {
    return new ProjectOperator(visitInput(node.getInput(), context), node.getProjectList());
  }

  @Override
  public PhysicalPlan visitRemove(RemoveOperator node, Object context) {
    return new RemoveOperator(visitInput(node.getInput(), context), node.getRemoveList());
  }

  @Override
  public PhysicalPlan visitEval(EvalOperator node, Object context) {
    return new EvalOperator(visitInput(node.getInput(), context), node.getExpressionList());
  }

  @Override
  public PhysicalPlan visitDedupe(DedupeOperator node, Object context) {
    return new DedupeOperator(visitInput(node.getInput(), context), node.getDedupeList(),
        node.getAllowedDuplication(), node.getKeepEmpty(), node.getConsecutive());
  }

  @Override
  public PhysicalPlan visitHead(HeadOperator node, Object context) {
    return new HeadOperator(
            visitInput(node.getInput(), context),
            node.getKeepLast(),
            node.getWhileExpr(),
            node.getNumber()
    );
  }

  @Override
  public PhysicalPlan visitWindow(WindowOperator node, Object context) {
    return new WindowOperator(
        visitInput(node.getInput(), context),
        node.getWindowFunction(),
        node.getWindowDefinition());
  }

  /**
   * Decorate with {@link ResourceMonitorPlan}.
   */
  @Override
  public PhysicalPlan visitSort(SortOperator node, Object context) {
    return new ResourceMonitorPlan(
        new SortOperator(
            visitInput(node.getInput(), context),
            node.getSortList()),
        resourceMonitor);
  }

  /**
   * Values are a sequence of rows of literal value in memory
   * which doesn't need memory protection.
   */
  @Override
  public PhysicalPlan visitValues(ValuesOperator node, Object context) {
    return node;
  }

  PhysicalPlan visitInput(PhysicalPlan node, Object context) {
    if (null == node) {
      return node;
    } else {
      return node.accept(this, context);
    }
  }
}
