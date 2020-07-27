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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage;

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.literal;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.ref;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.aggregation;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.eval;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.filter;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.project;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.relation;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.remove;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.rename;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.sort;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprBooleanValue;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.client.ElasticsearchClient;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.type.ElasticsearchDataType;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.value.ElasticsearchExprValueFactory;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.mapping.IndexMapping;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.Aggregator;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.AvgAggregator;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlanDSL;
import com.amazon.opendistroforelasticsearch.sql.storage.Table;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ElasticsearchIndexTest {

  @Mock
  private ElasticsearchClient client;

  @Mock
  private ElasticsearchExprValueFactory exprValueFactory;

  @Test
  void getFieldTypes() {
    when(client.getIndexMappings("test"))
        .thenReturn(
            ImmutableMap.of(
                "test",
                new IndexMapping(
                    ImmutableMap.<String, String>builder()
                        .put("name", "keyword")
                        .put("address", "text")
                        .put("age", "integer")
                        .put("account_number", "long")
                        .put("balance1", "float")
                        .put("balance2", "double")
                        .put("gender", "boolean")
                        .put("family", "nested")
                        .put("employer", "object")
                        .put("birthday", "date")
                        .build())));

    Table index = new ElasticsearchIndex(client, "test");
    Map<String, ExprType> fieldTypes = index.getFieldTypes();
    assertThat(
        fieldTypes,
        allOf(
            aMapWithSize(10),
            hasEntry("name", (ExprType) ExprCoreType.STRING),
            hasEntry("address", (ExprType) ElasticsearchDataType.ES_TEXT),
            hasEntry("age", (ExprType) ExprCoreType.INTEGER),
            hasEntry("account_number", ExprCoreType.LONG),
            hasEntry("balance1", (ExprType) ExprCoreType.FLOAT),
            hasEntry("balance2", (ExprType) ExprCoreType.DOUBLE),
            hasEntry("gender", (ExprType) ExprCoreType.BOOLEAN),
            hasEntry("family", (ExprType) ExprCoreType.ARRAY),
            hasEntry("employer", (ExprType) ExprCoreType.STRUCT),
            hasEntry("birthday", (ExprType) ExprCoreType.TIMESTAMP)));
  }

  @Test
  void implementRelationOperatorOnly() {
    String indexName = "test";
    LogicalPlan plan = relation(indexName);
    Table index = new ElasticsearchIndex(client, indexName);
    assertEquals(
        new ElasticsearchIndexScan(client, indexName, exprValueFactory), index.implement(plan));
  }

  @Test
  void implementOtherLogicalOperators() {
    String indexName = "test";
    ReferenceExpression include = ref("age", INTEGER);
    ReferenceExpression exclude = ref("name", STRING);
    ReferenceExpression dedupeField = ref("name", STRING);
    Expression filterExpr = literal(ExprBooleanValue.of(true));
    List<Expression> groupByExprs = Arrays.asList(ref("age", INTEGER));
    List<Aggregator> aggregators = Arrays.asList(new AvgAggregator(groupByExprs, DOUBLE));
    Map<ReferenceExpression, ReferenceExpression> mappings =
        ImmutableMap.of(ref("name", STRING), ref("lastname", STRING));
    Pair<ReferenceExpression, Expression> newEvalField =
        ImmutablePair.of(ref("name1", STRING), ref("name", STRING));
    Integer sortCount = 100;
    Pair<Sort.SortOption, Expression> sortField =
        ImmutablePair.of(Sort.SortOption.PPL_ASC, ref("name1", STRING));

    LogicalPlan plan =
        project(
            LogicalPlanDSL.dedupe(
                sort(
                    eval(
                        remove(
                            rename(
                                aggregation(
                                    filter(relation(indexName), filterExpr),
                                    aggregators,
                                    groupByExprs),
                                mappings),
                            exclude),
                        newEvalField),
                    sortCount,
                    sortField),
                dedupeField),
            include);

    Table index = new ElasticsearchIndex(client, indexName);
    assertEquals(
        PhysicalPlanDSL.project(
            PhysicalPlanDSL.dedupe(
                PhysicalPlanDSL.sort(
                    PhysicalPlanDSL.eval(
                        PhysicalPlanDSL.remove(
                            PhysicalPlanDSL.rename(
                                PhysicalPlanDSL.agg(
                                        PhysicalPlanDSL.filter(
                                                new ElasticsearchIndexScan(
                                                        client, indexName, exprValueFactory),
                                                filterExpr),
                                        aggregators,
                                        groupByExprs),
                                mappings),
                            exclude),
                        newEvalField),
                    sortCount,
                    sortField),
                dedupeField),
            include),
        index.implement(plan));
  }
}
