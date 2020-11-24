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

import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.named;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.amazon.opendistroforelasticsearch.sql.ast.tree.RareTopN.CommandType;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.SortOption;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.Aggregator;
import com.amazon.opendistroforelasticsearch.sql.expression.window.WindowDefinition;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Todo. Temporary added for UT coverage, Will be removed.
 */
@ExtendWith(MockitoExtension.class)
class LogicalPlanNodeVisitorTest {

  @Mock
  Expression expression;
  @Mock
  ReferenceExpression ref;
  @Mock
  Aggregator aggregator;

  @Test
  public void logicalPlanShouldTraversable() {
    LogicalPlan logicalPlan =
        LogicalPlanDSL.rename(
            LogicalPlanDSL.aggregation(
                LogicalPlanDSL.head(
                    LogicalPlanDSL.rareTopN(
                        LogicalPlanDSL.filter(LogicalPlanDSL.relation("schema"), expression),
                        CommandType.TOP,
                        ImmutableList.of(expression),
                        expression),
                    false, expression, 10),
                ImmutableList.of(DSL.named("avg", aggregator)),
                ImmutableList.of(DSL.named("group", expression))),
            ImmutableMap.of(ref, ref));

    Integer result = logicalPlan.accept(new NodesCount(), null);
    assertEquals(6, result);
  }

  @Test
  public void testAbstractPlanNodeVisitorShouldReturnNull() {
    LogicalPlan relation = LogicalPlanDSL.relation("schema");
    assertNull(relation.accept(new LogicalPlanNodeVisitor<Integer, Object>() {
    }, null));

    LogicalPlan filter = LogicalPlanDSL.filter(relation, expression);
    assertNull(filter.accept(new LogicalPlanNodeVisitor<Integer, Object>() {
    }, null));

    LogicalPlan head = LogicalPlanDSL.head(relation, false, expression, 10);
    assertNull(head.accept(new LogicalPlanNodeVisitor<Integer, Object>() {
    }, null));

    LogicalPlan aggregation =
        LogicalPlanDSL.aggregation(
            filter, ImmutableList.of(DSL.named("avg", aggregator)), ImmutableList.of(DSL.named(
                "group", expression)));
    assertNull(aggregation.accept(new LogicalPlanNodeVisitor<Integer, Object>() {
    }, null));

    LogicalPlan rename = LogicalPlanDSL.rename(aggregation, ImmutableMap.of(ref, ref));
    assertNull(rename.accept(new LogicalPlanNodeVisitor<Integer, Object>() {
    }, null));

    LogicalPlan project = LogicalPlanDSL.project(relation, named("ref", ref));
    assertNull(project.accept(new LogicalPlanNodeVisitor<Integer, Object>() {
    }, null));

    LogicalPlan remove = LogicalPlanDSL.remove(relation, ref);
    assertNull(remove.accept(new LogicalPlanNodeVisitor<Integer, Object>() {
    }, null));

    LogicalPlan eval = LogicalPlanDSL.eval(relation, Pair.of(ref, expression));
    assertNull(eval.accept(new LogicalPlanNodeVisitor<Integer, Object>() {
    }, null));

    LogicalPlan sort = LogicalPlanDSL.sort(relation,
        Pair.of(SortOption.DEFAULT_ASC, expression));
    assertNull(sort.accept(new LogicalPlanNodeVisitor<Integer, Object>() {
    }, null));

    LogicalPlan dedup = LogicalPlanDSL.dedupe(relation, 1, false, false, expression);
    assertNull(dedup.accept(new LogicalPlanNodeVisitor<Integer, Object>() {
    }, null));

    LogicalPlan window = LogicalPlanDSL.window(relation, expression, new WindowDefinition(
        ImmutableList.of(ref), ImmutableList.of(Pair.of(SortOption.DEFAULT_ASC, expression))));
    assertNull(window.accept(new LogicalPlanNodeVisitor<Integer, Object>() {
    }, null));

    LogicalPlan rareTopN = LogicalPlanDSL.rareTopN(
        relation, CommandType.TOP, ImmutableList.of(expression), expression);
    assertNull(rareTopN.accept(new LogicalPlanNodeVisitor<Integer, Object>() {
    }, null));
  }

  private static class NodesCount extends LogicalPlanNodeVisitor<Integer, Object> {
    @Override
    public Integer visitRelation(LogicalRelation plan, Object context) {
      return 1;
    }

    @Override
    public Integer visitFilter(LogicalFilter plan, Object context) {
      return 1
          + plan.getChild().stream()
          .map(child -> child.accept(this, context))
          .collect(Collectors.summingInt(Integer::intValue));
    }

    @Override
    public Integer visitHead(LogicalHead plan, Object context) {
      return 1
          + plan.getChild().stream()
          .map(child -> child.accept(this, context))
          .collect(Collectors.summingInt(Integer::intValue));
    }

    @Override
    public Integer visitAggregation(LogicalAggregation plan, Object context) {
      return 1
          + plan.getChild().stream()
          .map(child -> child.accept(this, context))
          .collect(Collectors.summingInt(Integer::intValue));
    }

    @Override
    public Integer visitRename(LogicalRename plan, Object context) {
      return 1
          + plan.getChild().stream()
          .map(child -> child.accept(this, context))
          .collect(Collectors.summingInt(Integer::intValue));
    }

    @Override
    public Integer visitRareTopN(LogicalRareTopN plan, Object context) {
      return 1
          + plan.getChild().stream()
          .map(child -> child.accept(this, context))
          .collect(Collectors.summingInt(Integer::intValue));
    }
  }
}
