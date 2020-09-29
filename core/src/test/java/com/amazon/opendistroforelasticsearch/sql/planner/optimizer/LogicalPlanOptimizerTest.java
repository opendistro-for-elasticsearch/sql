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
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.opendistroforelasticsearch.sql.analysis.AnalyzerTestBase;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.config.ExpressionConfig;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalIndexScan;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalIndexScanAggregation;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL;
import com.amazon.opendistroforelasticsearch.sql.planner.optimizer.rule.MergeAggAndIndexScan;
import com.amazon.opendistroforelasticsearch.sql.planner.optimizer.rule.MergeAggAndRelation;
import com.amazon.opendistroforelasticsearch.sql.planner.optimizer.rule.MergeFilterAndFilter;
import com.amazon.opendistroforelasticsearch.sql.planner.optimizer.rule.MergeFilterAndRelation;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
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
  public void project_filter_merge_with_relation() {
    assertEquals(
        LogicalPlanDSL.project(
            new LogicalIndexScan("schema",
                dsl.equal(DSL.ref("integer_value", INTEGER), DSL.literal(integerValue(1)))),
            DSL.named("i", DSL.ref("integer_value", INTEGER))
        ),
        optimize(
            LogicalPlanDSL.project(
                LogicalPlanDSL.filter(
                    LogicalPlanDSL.relation("schema"),
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
  public void filter_merge_filter() {
    assertEquals(
        new LogicalIndexScan("schema",
            dsl.and(dsl.equal(DSL.ref("integer_value", INTEGER), DSL.literal(integerValue(2))),
                dsl.equal(DSL.ref("integer_value", INTEGER), DSL.literal(integerValue(1))))),
        optimize(
            LogicalPlanDSL.filter(
                LogicalPlanDSL.filter(
                    LogicalPlanDSL.relation("schema"),
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
  public void aggregation_merge_relation() {
    assertEquals(
        LogicalPlanDSL.project(
            new LogicalIndexScanAggregation("schema", ImmutableList
                .of(DSL.named("AVG(integer_value)",
                    dsl.avg(DSL.ref("integer_value", INTEGER)))),
                ImmutableList.of(DSL.named("long_value",
                    dsl.abs(DSL.ref("long_value", LONG))))),
            DSL.named("AVG(integer_value)", DSL.ref("AVG(integer_value)", DOUBLE))),
        optimize(
            LogicalPlanDSL.project(
                LogicalPlanDSL.aggregation(
                    LogicalPlanDSL.relation("schema"),
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
  public void aggregation_merge_filter_relation() {
    assertEquals(
        LogicalPlanDSL.project(
            new LogicalIndexScanAggregation("schema",
                dsl.equal(DSL.ref("integer_value", INTEGER), DSL.literal(integerValue(1))),
                ImmutableList
                    .of(DSL.named("AVG(integer_value)",
                        dsl.avg(DSL.ref("integer_value", INTEGER)))),
                ImmutableList.of(DSL.named("long_value",
                    dsl.abs(DSL.ref("long_value", LONG))))),
            DSL.named("AVG(integer_value)", DSL.ref("AVG(integer_value)", DOUBLE))),
        optimize(
            LogicalPlanDSL.project(
                LogicalPlanDSL.aggregation(
                    LogicalPlanDSL.filter(
                        LogicalPlanDSL.relation("schema"),
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

  public LogicalPlan optimize(LogicalPlan plan) {
    final LogicalPlanOptimizer optimizer =
        new LogicalPlanOptimizer(Arrays.asList(
            new MergeFilterAndRelation(),
            new MergeAggAndIndexScan(),
            new MergeAggAndRelation(),
            new MergeFilterAndFilter(dsl)));
    final LogicalPlan optimize = optimizer.optimize(plan);
    return optimize;
  }
}