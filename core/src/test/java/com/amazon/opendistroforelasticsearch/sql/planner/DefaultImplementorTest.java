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

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.literal;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.named;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.ref;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.aggregation;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.eval;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.filter;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.limit;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.project;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.rareTopN;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.remove;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.rename;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.sort;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.values;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.window;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amazon.opendistroforelasticsearch.sql.ast.tree.RareTopN.CommandType;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprBooleanValue;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.NamedExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.AvgAggregator;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.NamedAggregator;
import com.amazon.opendistroforelasticsearch.sql.expression.window.WindowDefinition;
import com.amazon.opendistroforelasticsearch.sql.expression.window.ranking.RowNumberFunction;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalRelation;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlanDSL;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DefaultImplementorTest {

  @Mock
  private Expression filter;

  @Mock
  private NamedAggregator aggregator;

  @Mock
  private NamedExpression groupBy;

  private final DefaultImplementor<Object> implementor = new DefaultImplementor<>();

  @Test
  public void visitShouldReturnDefaultPhysicalOperator() {
    String indexName = "test";
    NamedExpression include = named("age", ref("age", INTEGER));
    ReferenceExpression exclude = ref("name", STRING);
    ReferenceExpression dedupeField = ref("name", STRING);
    Expression filterExpr = literal(ExprBooleanValue.of(true));
    List<NamedExpression> groupByExprs = Arrays.asList(DSL.named("age", ref("age", INTEGER)));
    List<Expression> aggExprs = Arrays.asList(ref("age", INTEGER));
    ReferenceExpression rareTopNField = ref("age", INTEGER);
    List<Expression> topByExprs = Arrays.asList(ref("age", INTEGER));
    List<NamedAggregator> aggregators =
        Arrays.asList(DSL.named("avg(age)", new AvgAggregator(aggExprs, ExprCoreType.DOUBLE)));
    Map<ReferenceExpression, ReferenceExpression> mappings =
        ImmutableMap.of(ref("name", STRING), ref("lastname", STRING));
    Pair<ReferenceExpression, Expression> newEvalField =
        ImmutablePair.of(ref("name1", STRING), ref("name", STRING));
    Pair<Sort.SortOption, Expression> sortField =
        ImmutablePair.of(Sort.SortOption.DEFAULT_ASC, ref("name1", STRING));
    Integer limit = 1;
    Integer offset = 1;

    LogicalPlan plan =
        project(
            limit(
                LogicalPlanDSL.dedupe(
                    rareTopN(
                        sort(
                            eval(
                                remove(
                                    rename(
                                        aggregation(
                                            filter(values(emptyList()), filterExpr),
                                            aggregators,
                                            groupByExprs),
                                        mappings),
                                    exclude),
                                newEvalField),
                            sortField),
                        CommandType.TOP,
                        topByExprs,
                        rareTopNField),
                    dedupeField),
                limit,
                offset),
            include);

    PhysicalPlan actual = plan.accept(implementor, null);

    assertEquals(
        PhysicalPlanDSL.project(
            PhysicalPlanDSL.limit(
                PhysicalPlanDSL.dedupe(
                    PhysicalPlanDSL.rareTopN(
                        PhysicalPlanDSL.sort(
                            PhysicalPlanDSL.eval(
                                PhysicalPlanDSL.remove(
                                    PhysicalPlanDSL.rename(
                                        PhysicalPlanDSL.agg(
                                            PhysicalPlanDSL.filter(
                                                PhysicalPlanDSL.values(emptyList()),
                                                filterExpr),
                                            aggregators,
                                            groupByExprs),
                                        mappings),
                                    exclude),
                                newEvalField),
                            sortField),
                        CommandType.TOP,
                        topByExprs,
                        rareTopNField),
                    dedupeField),
                limit,
                offset),
            include),
        actual);
  }

  @Test
  public void visitRelationShouldThrowException() {
    assertThrows(UnsupportedOperationException.class,
        () -> new LogicalRelation("test").accept(implementor, null));
  }

  @SuppressWarnings({"rawtypes", "unchecked"})
  @Test
  public void visitWindowOperatorShouldReturnPhysicalWindowOperator() {
    NamedExpression windowFunction = named(new RowNumberFunction());
    WindowDefinition windowDefinition = new WindowDefinition(
        Collections.singletonList(ref("state", STRING)),
        Collections.singletonList(
            ImmutablePair.of(Sort.SortOption.DEFAULT_DESC, ref("age", INTEGER))));

    NamedExpression[] projectList = {
        named("state", ref("state", STRING)),
        named("row_number", ref("row_number", INTEGER))
    };
    Pair[] sortList = {
        ImmutablePair.of(Sort.SortOption.DEFAULT_ASC, ref("state", STRING)),
        ImmutablePair.of(Sort.SortOption.DEFAULT_DESC, ref("age", STRING))
    };

    LogicalPlan logicalPlan =
        project(
            window(
                sort(
                    values(),
                    sortList),
                windowFunction,
                windowDefinition),
            projectList);

    PhysicalPlan physicalPlan =
        PhysicalPlanDSL.project(
            PhysicalPlanDSL.window(
                PhysicalPlanDSL.sort(
                    PhysicalPlanDSL.values(),
                    sortList),
                windowFunction,
                windowDefinition),
            projectList);

    assertEquals(physicalPlan, logicalPlan.accept(implementor, null));
  }
}