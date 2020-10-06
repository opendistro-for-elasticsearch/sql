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

package com.amazon.opendistroforelasticsearch.sql.planner.optimizer;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.integerValue;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.LONG;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.aggregation;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.filter;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.indexScan;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.indexScanAgg;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.project;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.relation;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.opendistroforelasticsearch.sql.analysis.AnalyzerTestBase;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.config.ExpressionConfig;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalIndexScan;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Configuration
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ExpressionConfig.class, AnalyzerTestBase.class})
class LogicalPlanOptimizerTest extends AnalyzerTestBase {
  /**
   * SELECT integer_value as i FROM schema WHERE integer_value = 1.
   */
  @Test
  void project_filter_merge_with_relation() {
    assertEquals(
        project(
            indexScan("schema",
                dsl.equal(DSL.ref("integer_value", INTEGER), DSL.literal(integerValue(1)))),
            DSL.named("i", DSL.ref("integer_value", INTEGER))
        ),
        optimize(
            project(
                filter(
                    relation("schema"),
                    dsl.equal(DSL.ref("integer_value", INTEGER), DSL.literal(integerValue(1)))
                ),
                DSL.named("i", DSL.ref("integer_value", INTEGER)))
        )
    );
  }

  /**
   * Filter - Filter --> Filter.
   */
  @Test
  void filter_merge_filter() {
    assertEquals(
        indexScan("schema",
            dsl.and(dsl.equal(DSL.ref("integer_value", INTEGER), DSL.literal(integerValue(2))),
                dsl.equal(DSL.ref("integer_value", INTEGER), DSL.literal(integerValue(1))))),
        optimize(
            filter(
                filter(
                    relation("schema"),
                    dsl.equal(DSL.ref("integer_value", INTEGER), DSL.literal(integerValue(1)))
                ),
                dsl.equal(DSL.ref("integer_value", INTEGER), DSL.literal(integerValue(2)))
            )
        )
    );
  }

  /**
   * SELECT avg(integer_value) FROM schema GROUP BY string_value.
   */
  @Test
  void aggregation_merge_relation() {
    assertEquals(
        project(
            indexScanAgg("schema", ImmutableList
                    .of(DSL.named("AVG(integer_value)",
                        dsl.avg(DSL.ref("integer_value", INTEGER)))),
                ImmutableList.of(DSL.named("long_value",
                    dsl.abs(DSL.ref("long_value", LONG))))),
            DSL.named("AVG(integer_value)", DSL.ref("AVG(integer_value)", DOUBLE))),
        optimize(
            project(
                aggregation(
                    relation("schema"),
                    ImmutableList
                        .of(DSL.named("AVG(integer_value)",
                            dsl.avg(DSL.ref("integer_value", INTEGER)))),
                    ImmutableList.of(DSL.named("long_value",
                        dsl.abs(DSL.ref("long_value", LONG))))),
                DSL.named("AVG(integer_value)", DSL.ref("AVG(integer_value)", DOUBLE)))
        )
    );
  }

  /**
   * SELECT avg(integer_value) FROM schema WHERE integer_value = 1 GROUP BY string_value.
   */
  @Test
  void aggregation_merge_filter_relation() {
    assertEquals(
        project(
            indexScanAgg("schema",
                dsl.equal(DSL.ref("integer_value", INTEGER), DSL.literal(integerValue(1))),
                ImmutableList
                    .of(DSL.named("AVG(integer_value)",
                        dsl.avg(DSL.ref("integer_value", INTEGER)))),
                ImmutableList.of(DSL.named("long_value",
                    dsl.abs(DSL.ref("long_value", LONG))))),
            DSL.named("AVG(integer_value)", DSL.ref("AVG(integer_value)", DOUBLE))),
        optimize(
            project(
                aggregation(
                    filter(
                        relation("schema"),
                        dsl.equal(DSL.ref("integer_value", INTEGER), DSL.literal(integerValue(1)))
                    ),
                    ImmutableList
                        .of(DSL.named("AVG(integer_value)",
                            dsl.avg(DSL.ref("integer_value", INTEGER)))),
                    ImmutableList.of(DSL.named("long_value",
                        dsl.abs(DSL.ref("long_value", LONG))))),
                DSL.named("AVG(integer_value)", DSL.ref("AVG(integer_value)", DOUBLE)))
        )
    );
  }

  @Test
  void aggregation_cant_merge_indexScan_with_project() {
    assertEquals(
        aggregation(
            new LogicalIndexScan("schema",
                dsl.equal(DSL.ref("integer_value", INTEGER), DSL.literal(integerValue(1))),
                Collections.singletonList(DSL.named("i", DSL.ref("integer_value", INTEGER)))),
            ImmutableList
                .of(DSL.named("AVG(integer_value)",
                    dsl.avg(DSL.ref("integer_value", INTEGER)))),
            ImmutableList.of(DSL.named("long_value",
                dsl.abs(DSL.ref("long_value", LONG))))),
        optimize(
            aggregation(
                new LogicalIndexScan("schema",
                    dsl.equal(DSL.ref("integer_value", INTEGER), DSL.literal(integerValue(1))),
                    Collections.singletonList(DSL.named("i", DSL.ref("integer_value", INTEGER)))),
                ImmutableList
                    .of(DSL.named("AVG(integer_value)",
                        dsl.avg(DSL.ref("integer_value", INTEGER)))),
                ImmutableList.of(DSL.named("long_value",
                    dsl.abs(DSL.ref("long_value", LONG))))))
    );
  }

  private LogicalPlan optimize(LogicalPlan plan) {
    final LogicalPlanOptimizer optimizer = LogicalPlanOptimizer.create(dsl);
    final LogicalPlan optimize = optimizer.optimize(plan);
    return optimize;
  }
}