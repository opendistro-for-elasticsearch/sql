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
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.longValue;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.LONG;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.filter;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.relation;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.sort;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.opendistroforelasticsearch.sql.analysis.AnalyzerTestBase;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.config.ExpressionConfig;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import org.apache.commons.lang3.tuple.Pair;
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
   * Filter - Filter --> Filter.
   */
  @Test
  void filter_merge_filter() {
    assertEquals(
        filter(
            relation("schema"),
            dsl.and(dsl.equal(DSL.ref("integer_value", INTEGER), DSL.literal(integerValue(2))),
                dsl.equal(DSL.ref("integer_value", INTEGER), DSL.literal(integerValue(1))))
        ),
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
   * Filter - Sort --> Sort - Filter.
   */
  @Test
  void push_filter_under_sort() {
    assertEquals(
        sort(
            filter(
                relation("schema"),
                dsl.equal(DSL.ref("intV", INTEGER), DSL.literal(integerValue(1)))
            ),
            Pair.of(Sort.SortOption.DEFAULT_ASC, DSL.ref("longV", LONG))
        ),
        optimize(
            filter(
                sort(
                    relation("schema"),
                    Pair.of(Sort.SortOption.DEFAULT_ASC, DSL.ref("longV", LONG))
                ),
                dsl.equal(DSL.ref("intV", INTEGER), DSL.literal(integerValue(1)))
            )
        )
    );
  }

  /**
   * Filter - Sort --> Sort - Filter.
   */
  @Test
  void multiple_filter_should_eventually_be_merged() {
    assertEquals(
        sort(
            filter(
                relation("schema"),
                dsl.and(dsl.equal(DSL.ref("intV", INTEGER), DSL.literal(integerValue(1))),
                    dsl.less(DSL.ref("longV", INTEGER), DSL.literal(longValue(1L))))
            ),
            Pair.of(Sort.SortOption.DEFAULT_ASC, DSL.ref("longV", LONG))
        ),
        optimize(
            filter(
                sort(
                    filter(
                        relation("schema"),
                        dsl.less(DSL.ref("longV", INTEGER), DSL.literal(longValue(1L)))
                    ),
                    Pair.of(Sort.SortOption.DEFAULT_ASC, DSL.ref("longV", LONG))
                ),
                dsl.equal(DSL.ref("intV", INTEGER), DSL.literal(integerValue(1)))
            )
        )
    );
  }

  private LogicalPlan optimize(LogicalPlan plan) {
    final LogicalPlanOptimizer optimizer = LogicalPlanOptimizer.create(dsl);
    final LogicalPlan optimize = optimizer.optimize(plan);
    return optimize;
  }
}