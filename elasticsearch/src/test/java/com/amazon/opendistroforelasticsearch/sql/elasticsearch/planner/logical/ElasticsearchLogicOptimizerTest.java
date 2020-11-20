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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.planner.logical;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.integerValue;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.LONG;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.aggregation;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.filter;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.project;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.relation;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.NamedExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.NamedAggregator;
import com.amazon.opendistroforelasticsearch.sql.expression.config.ExpressionConfig;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.optimizer.LogicalPlanOptimizer;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


class ElasticsearchLogicOptimizerTest {

  private final DSL dsl = new ExpressionConfig().dsl(new ExpressionConfig().functionRepository());

  /**
   * SELECT intV as i FROM schema WHERE intV = 1.
   */
  @Test
  void project_filter_merge_with_relation() {
    assertEquals(
        project(
            indexScan("schema",
                dsl.equal(DSL.ref("intV", INTEGER), DSL.literal(integerValue(1)))),
            DSL.named("i", DSL.ref("intV", INTEGER))
        ),
        optimize(
            project(
                filter(
                    relation("schema"),
                    dsl.equal(DSL.ref("intV", INTEGER), DSL.literal(integerValue(1)))
                ),
                DSL.named("i", DSL.ref("intV", INTEGER)))
        )
    );
  }

  /**
   * SELECT avg(intV) FROM schema GROUP BY string_value.
   */
  @Test
  void aggregation_merge_relation() {
    assertEquals(
        project(
            indexScanAgg("schema", ImmutableList
                    .of(DSL.named("AVG(intV)",
                        dsl.avg(DSL.ref("intV", INTEGER)))),
                ImmutableList.of(DSL.named("longV",
                    dsl.abs(DSL.ref("longV", LONG))))),
            DSL.named("AVG(intV)", DSL.ref("AVG(intV)", DOUBLE))),
        optimize(
            project(
                aggregation(
                    relation("schema"),
                    ImmutableList
                        .of(DSL.named("AVG(intV)",
                            dsl.avg(DSL.ref("intV", INTEGER)))),
                    ImmutableList.of(DSL.named("longV",
                        dsl.abs(DSL.ref("longV", LONG))))),
                DSL.named("AVG(intV)", DSL.ref("AVG(intV)", DOUBLE)))
        )
    );
  }

  /**
   * SELECT avg(intV) FROM schema WHERE intV = 1 GROUP BY string_value.
   */
  @Test
  void aggregation_merge_filter_relation() {
    assertEquals(
        project(
            indexScanAgg("schema",
                dsl.equal(DSL.ref("intV", INTEGER), DSL.literal(integerValue(1))),
                ImmutableList
                    .of(DSL.named("AVG(intV)",
                        dsl.avg(DSL.ref("intV", INTEGER)))),
                ImmutableList.of(DSL.named("longV",
                    dsl.abs(DSL.ref("longV", LONG))))),
            DSL.named("AVG(intV)", DSL.ref("AVG(intV)", DOUBLE))),
        optimize(
            project(
                aggregation(
                    filter(
                        relation("schema"),
                        dsl.equal(DSL.ref("intV", INTEGER), DSL.literal(integerValue(1)))
                    ),
                    ImmutableList
                        .of(DSL.named("AVG(intV)",
                            dsl.avg(DSL.ref("intV", INTEGER)))),
                    ImmutableList.of(DSL.named("longV",
                        dsl.abs(DSL.ref("longV", LONG))))),
                DSL.named("AVG(intV)", DSL.ref("AVG(intV)", DOUBLE)))
        )
    );
  }

  @Disabled
  @Test
  void aggregation_cant_merge_indexScan_with_project() {
    assertEquals(
        aggregation(
            ElasticsearchLogicalIndexScan.builder().relationName("schema")
                .filter(dsl.equal(DSL.ref("intV", INTEGER), DSL.literal(integerValue(1))))
                .projectList(Collections.singletonList(DSL.named("i", DSL.ref("intV", INTEGER))))
                .build(),
            ImmutableList
                .of(DSL.named("AVG(intV)",
                    dsl.avg(DSL.ref("intV", INTEGER)))),
            ImmutableList.of(DSL.named("longV",
                dsl.abs(DSL.ref("longV", LONG))))),
        optimize(
            aggregation(
                ElasticsearchLogicalIndexScan.builder().relationName("schema")
                    .filter(dsl.equal(DSL.ref("intV", INTEGER), DSL.literal(integerValue(1))))
                    .projectList(
                        Collections.singletonList(DSL.named("i", DSL.ref("intV", INTEGER))))
                    .build(),
                ImmutableList
                    .of(DSL.named("AVG(intV)",
                        dsl.avg(DSL.ref("intV", INTEGER)))),
                ImmutableList.of(DSL.named("longV",
                    dsl.abs(DSL.ref("longV", LONG))))))
    );
  }

  private LogicalPlan optimize(LogicalPlan plan) {
    final LogicalPlanOptimizer optimizer = ElasticsearchLogicalPlanOptimizerFactory.create();
    final LogicalPlan optimize = optimizer.optimize(plan);
    return optimize;
  }

  public static LogicalPlan indexScan(String tableName, Expression filter) {
    return ElasticsearchLogicalIndexScan.builder().relationName(tableName).filter(filter).build();
  }

  public static LogicalPlan indexScanAgg(String tableName, List<NamedAggregator> aggregators,
                                         List<NamedExpression> groupByList) {
    return ElasticsearchLogicalIndexAgg.builder().relationName(tableName)
        .aggregatorList(aggregators).groupByList(groupByList).build();
  }

  public static LogicalPlan indexScanAgg(String tableName,
                                         Expression filter,
                                         List<NamedAggregator> aggregators,
                                         List<NamedExpression> groupByList) {
    return ElasticsearchLogicalIndexAgg.builder().relationName(tableName).filter(filter)
        .aggregatorList(aggregators).groupByList(groupByList).build();
  }
}