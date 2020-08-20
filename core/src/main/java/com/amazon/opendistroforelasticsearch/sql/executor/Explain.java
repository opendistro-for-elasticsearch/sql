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

package com.amazon.opendistroforelasticsearch.sql.executor;

import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine.ExplainResponse;
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine.ExplainResponseNode;
import com.amazon.opendistroforelasticsearch.sql.expression.NamedExpression;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.FilterOperator;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlanNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.ProjectOperator;
import com.amazon.opendistroforelasticsearch.sql.storage.TableScanOperator;
import com.google.common.collect.ImmutableMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Visitor that explains a physical plan to JSON format.
 */
public class Explain extends PhysicalPlanNodeVisitor<ExplainResponseNode, Object>
                     implements Function<PhysicalPlan, ExplainResponse> {

  @Override
  public ExplainResponse apply(PhysicalPlan plan) {
    return new ExplainResponse(plan.accept(this, null));
  }

  @Override
  protected ExplainResponseNode visitNode(PhysicalPlan node, Object context) {
    return explain(node, context, explainNode -> explainNode.setDescription(ImmutableMap.of()));
  }

  @Override
  public ExplainResponseNode visitProject(ProjectOperator node, Object context) {
    return explain(node, context, explainNode -> {
      String projectList = node.getProjectList()
                               .stream()
                               .map(NamedExpression::getName)
                               .collect(Collectors.joining(", "));
      explainNode.setDescription(ImmutableMap.of("fields", projectList));
    });
  }

  @Override
  public ExplainResponseNode visitFilter(FilterOperator node, Object context) {
    return explain(node, context, explainNode ->
        explainNode.setDescription(ImmutableMap.of("conditions", node.getConditions().toString())));
  }

  @Override
  public ExplainResponseNode visitTableScan(TableScanOperator node, Object context) {
    return explain(node, context, explainNode ->
        explainNode.setDescription(ImmutableMap.of("request", node.toString())));
  }

  private ExplainResponseNode explain(PhysicalPlan node, Object context,
                           Consumer<ExplainResponseNode> doExplain) {
    ExplainResponseNode explainNode = new ExplainResponseNode(getOperatorName(node));
    explainNode.setChild(explainChild(node, context));
    doExplain.accept(explainNode);
    return explainNode;
  }

  private ExplainResponseNode explainChild(PhysicalPlan node, Object context) {
    if (node.getChild().isEmpty()) {
      return null;
    }

    PhysicalPlan child = node.getChild().get(0);
    return child.accept(this, context);
  }

  private String getOperatorName(PhysicalPlan node) {
    return node.getClass().getSimpleName();
  }

}
