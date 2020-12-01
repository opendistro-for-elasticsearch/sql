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

import com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort;
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine.ExplainResponse;
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine.ExplainResponseNode;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.AggregationOperator;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.DedupeOperator;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.EvalOperator;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.FilterOperator;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.HeadOperator;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlanNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.ProjectOperator;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.RareTopNOperator;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.RemoveOperator;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.RenameOperator;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.SortOperator;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.ValuesOperator;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.WindowOperator;
import com.amazon.opendistroforelasticsearch.sql.storage.TableScanOperator;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;

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
  public ExplainResponseNode visitProject(ProjectOperator node, Object context) {
    return explain(node, context, explainNode -> explainNode.setDescription(ImmutableMap.of(
        "fields", node.getProjectList().toString())));
  }

  @Override
  public ExplainResponseNode visitFilter(FilterOperator node, Object context) {
    return explain(node, context, explainNode -> explainNode.setDescription(ImmutableMap.of(
        "conditions", node.getConditions().toString())));
  }

  @Override
  public ExplainResponseNode visitSort(SortOperator node, Object context) {
    return explain(node, context, explainNode -> explainNode.setDescription(ImmutableMap.of(
        "sortList", describeSortList(node.getSortList()))));
  }

  @Override
  public ExplainResponseNode visitTableScan(TableScanOperator node, Object context) {
    return explain(node, context, explainNode -> explainNode.setDescription(ImmutableMap.of(
        "request", node.toString())));
  }

  @Override
  public ExplainResponseNode visitAggregation(AggregationOperator node, Object context) {
    return explain(node, context, explainNode -> explainNode.setDescription(ImmutableMap.of(
        "aggregators", node.getAggregatorList().toString(),
        "groupBy", node.getGroupByExprList().toString())));
  }

  @Override
  public ExplainResponseNode visitWindow(WindowOperator node, Object context) {
    return explain(node, context, explainNode -> explainNode.setDescription(ImmutableMap.of(
        "function", node.getWindowFunction().toString(),
        "definition", ImmutableMap.of(
            "partitionBy", node.getWindowDefinition().getPartitionByList().toString(),
            "sortList", describeSortList(node.getWindowDefinition().getSortList())))));
  }

  @Override
  public ExplainResponseNode visitRename(RenameOperator node, Object context) {
    Map<String, String> renameMappingDescription =
        node.getMapping()
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                e -> e.getKey().toString(),
                e -> e.getValue().toString()));

    return explain(node, context, explainNode -> explainNode.setDescription(ImmutableMap.of(
        "mapping", renameMappingDescription)));
  }

  @Override
  public ExplainResponseNode visitRemove(RemoveOperator node, Object context) {
    return explain(node, context, explainNode -> explainNode.setDescription(ImmutableMap.of(
        "removeList", node.getRemoveList().toString())));
  }

  @Override
  public ExplainResponseNode visitEval(EvalOperator node, Object context) {
    return explain(node, context, explainNode -> explainNode.setDescription(ImmutableMap.of(
        "expressions", convertPairListToMap(node.getExpressionList()))));
  }

  @Override
  public ExplainResponseNode visitDedupe(DedupeOperator node, Object context) {
    return explain(node, context, explainNode -> explainNode.setDescription(ImmutableMap.of(
        "dedupeList", node.getDedupeList().toString(),
        "allowedDuplication", node.getAllowedDuplication(),
        "keepEmpty", node.getKeepEmpty(),
        "consecutive", node.getConsecutive())));
  }

  @Override
  public ExplainResponseNode visitRareTopN(RareTopNOperator node, Object context) {
    return explain(node, context, explainNode -> explainNode.setDescription(ImmutableMap.of(
        "commandType", node.getCommandType(),
        "noOfResults", node.getNoOfResults(),
        "fields", node.getFieldExprList().toString(),
        "groupBy", node.getGroupByExprList().toString()
    )));
  }

  @Override
  public ExplainResponseNode visitHead(HeadOperator node, Object context) {
    return explain(node, context, explainNode -> explainNode.setDescription(ImmutableMap.of(
        "keepLast", node.getKeepLast(),
        "whileExpr", node.getWhileExpr().toString(),
        "number", node.getNumber()
    )));
  }

  @Override
  public ExplainResponseNode visitValues(ValuesOperator node, Object context) {
    return explain(node, context, explainNode -> explainNode.setDescription(ImmutableMap.of(
        "values", node.getValues())));
  }

  protected ExplainResponseNode explain(PhysicalPlan node, Object context,
                                        Consumer<ExplainResponseNode> doExplain) {
    ExplainResponseNode explainNode = new ExplainResponseNode(getOperatorName(node));

    List<ExplainResponseNode> children = new ArrayList<>();
    for (PhysicalPlan child : node.getChild()) {
      children.add(child.accept(this, context));
    }
    explainNode.setChildren(children);

    doExplain.accept(explainNode);
    return explainNode;
  }

  private String getOperatorName(PhysicalPlan node) {
    return node.getClass().getSimpleName();
  }

  private <T, U> Map<String, String> convertPairListToMap(List<Pair<T, U>> pairs) {
    return pairs.stream()
                .collect(Collectors.toMap(
                    p -> p.getLeft().toString(),
                    p -> p.getRight().toString()));
  }

  private Map<String, Map<String, String>> describeSortList(
      List<Pair<Sort.SortOption, Expression>> sortList) {
    return sortList.stream()
                   .collect(Collectors.toMap(
                       p -> p.getRight().toString(),
                       p -> ImmutableMap.of(
                           "sortOrder", p.getLeft().getSortOrder().toString(),
                           "nullOrder", p.getLeft().getNullOrder().toString())));
  }

}
