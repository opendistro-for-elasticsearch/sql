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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.executor;

import static com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.SortOption.DEFAULT_ASC;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.literal;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.named;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.ref;
import static com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlanDSL.filter;
import static com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlanDSL.sort;
import static com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlanDSL.values;
import static com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlanDSL.window;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.ast.tree.RareTopN.CommandType;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort;
import com.amazon.opendistroforelasticsearch.sql.common.setting.Settings;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprBooleanValue;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.client.ElasticsearchClient;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.value.ElasticsearchExprValueFactory;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.executor.protector.ElasticsearchExecutionProtector;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.executor.protector.ResourceMonitorPlan;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.setting.ElasticsearchSettings;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.ElasticsearchIndexScan;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.NamedExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.AvgAggregator;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.NamedAggregator;
import com.amazon.opendistroforelasticsearch.sql.expression.window.WindowDefinition;
import com.amazon.opendistroforelasticsearch.sql.expression.window.ranking.RankFunction;
import com.amazon.opendistroforelasticsearch.sql.monitor.ResourceMonitor;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlanDSL;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ElasticsearchExecutionProtectorTest {

  @Mock
  private ElasticsearchClient client;

  @Mock
  private ResourceMonitor resourceMonitor;

  @Mock
  private ElasticsearchExprValueFactory exprValueFactory;

  @Mock
  private ElasticsearchSettings settings;

  private ElasticsearchExecutionProtector executionProtector;

  @BeforeEach
  public void setup() {
    executionProtector = new ElasticsearchExecutionProtector(resourceMonitor);
  }

  @Test
  public void testProtectIndexScan() {
    when(settings.getSettingValue(Settings.Key.QUERY_SIZE_LIMIT)).thenReturn(200);

    String indexName = "test";
    NamedExpression include = named("age", ref("age", INTEGER));
    ReferenceExpression exclude = ref("name", STRING);
    ReferenceExpression dedupeField = ref("name", STRING);
    ReferenceExpression topField = ref("name", STRING);
    List<Expression> topExprs = Arrays.asList(ref("age", INTEGER));
    Expression filterExpr = literal(ExprBooleanValue.of(true));
    Expression whileExpr = literal(ExprBooleanValue.of(true));
    Boolean keepLast = false;
    Integer headNumber = 5;
    List<NamedExpression> groupByExprs = Arrays.asList(named("age", ref("age", INTEGER)));
    List<NamedAggregator> aggregators =
        Arrays.asList(named("avg(age)", new AvgAggregator(Arrays.asList(ref("age", INTEGER)),
            DOUBLE)));
    Map<ReferenceExpression, ReferenceExpression> mappings =
        ImmutableMap.of(ref("name", STRING), ref("lastname", STRING));
    Pair<ReferenceExpression, Expression> newEvalField =
        ImmutablePair.of(ref("name1", STRING), ref("name", STRING));
    Integer sortCount = 100;
    Pair<Sort.SortOption, Expression> sortField =
        ImmutablePair.of(DEFAULT_ASC, ref("name1", STRING));

    assertEquals(
        PhysicalPlanDSL.project(
            PhysicalPlanDSL.dedupe(
                PhysicalPlanDSL.rareTopN(
                    resourceMonitor(
                    PhysicalPlanDSL.sort(
                        PhysicalPlanDSL.eval(
                            PhysicalPlanDSL.remove(
                                PhysicalPlanDSL.rename(
                                    PhysicalPlanDSL.agg(
                                        PhysicalPlanDSL.head(
                                            filter(
                                                resourceMonitor(
                                                new ElasticsearchIndexScan(
                                                    client, settings, indexName, exprValueFactory)),
                                                filterExpr),
                                            keepLast,
                                            whileExpr,
                                            headNumber),
                                        aggregators,
                                        groupByExprs),
                                    mappings),
                                exclude),
                            newEvalField),
                        sortCount,
                        sortField)),
                    CommandType.TOP,
                    topExprs,
                    topField),
                dedupeField),
            include),
        executionProtector.protect(
            PhysicalPlanDSL.project(
                PhysicalPlanDSL.dedupe(
                    PhysicalPlanDSL.rareTopN(
                        PhysicalPlanDSL.sort(
                            PhysicalPlanDSL.eval(
                                PhysicalPlanDSL.remove(
                                    PhysicalPlanDSL.rename(
                                        PhysicalPlanDSL.agg(
                                            PhysicalPlanDSL.head(
                                                filter(
                                                        new ElasticsearchIndexScan(
                                                            client, settings, indexName,
                                                            exprValueFactory),
                                                    filterExpr),
                                                keepLast,
                                                whileExpr,
                                                headNumber),
                                            aggregators,
                                            groupByExprs),
                                        mappings),
                                    exclude),
                                newEvalField),
                            sortCount,
                            sortField),
                        CommandType.TOP,
                        topExprs,
                        topField),
                    dedupeField),
                include)));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testProtectSortForWindowOperator() {
    Expression rank = mock(RankFunction.class);
    Pair<Sort.SortOption, Expression> sortItem =
        ImmutablePair.of(DEFAULT_ASC, DSL.ref("age", INTEGER));
    WindowDefinition windowDefinition =
        new WindowDefinition(emptyList(), ImmutableList.of(sortItem));

    assertEquals(
        window(
            resourceMonitor(
                sort(
                    values(emptyList()),
                    0,
                    sortItem)),
            rank,
            windowDefinition),
        executionProtector.protect(
            window(
                sort(
                    values(emptyList()),
                    0,
                    sortItem
                ),
                rank,
                windowDefinition)));
  }

  @Test
  public void testWithoutProtection() {
    Expression filterExpr = literal(ExprBooleanValue.of(true));

    assertEquals(
        filter(
            filter(null, filterExpr),
            filterExpr),
        executionProtector.protect(
            filter(
                filter(null, filterExpr),
                filterExpr)
        )
    );
  }

  PhysicalPlan resourceMonitor(PhysicalPlan input) {
    return new ResourceMonitorPlan(input, resourceMonitor);
  }
}