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

package com.amazon.opendistroforelasticsearch.sql.analysis;

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.config.ExpressionConfig;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Configuration
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ExpressionConfig.class, AnalyzerTest.class})
class ExpressionReferenceOptimizerTest extends AnalyzerTestBase {

  @Test
  void expression_without_aggregation_should_not_be_replaced() {
    assertEquals(
        dsl.subtract(DSL.ref("age", INTEGER), DSL.literal(1)),
        optimize(dsl.subtract(DSL.ref("age", INTEGER), DSL.literal(1)))
    );
  }

  @Test
  void group_expression_should_be_replaced() {
    assertEquals(
        DSL.ref("abs(balance)", INTEGER),
        optimize(dsl.abs(DSL.ref("balance", INTEGER)))
    );
  }

  @Test
  void aggregation_expression_should_be_replaced() {
    assertEquals(
        DSL.ref("AVG(age)", DOUBLE),
        optimize(dsl.avg(DSL.ref("age", INTEGER)))
    );
  }

  @Test
  void aggregation_in_expression_should_be_replaced() {
    assertEquals(
        dsl.subtract(DSL.ref("AVG(age)", DOUBLE), DSL.literal(1)),
        optimize(dsl.subtract(dsl.avg(DSL.ref("age", INTEGER)), DSL.literal(1)))
    );
  }

  Expression optimize(Expression expression) {
    final ExpressionReferenceOptimizer optimizer =
        new ExpressionReferenceOptimizer(functionRepository, logicalPlan());
    return optimizer.optimize(expression, new AnalysisContext());
  }

  LogicalPlan logicalPlan() {
    return LogicalPlanDSL.aggregation(
        LogicalPlanDSL.relation("schema"),
        ImmutableList
            .of(DSL.named("AVG(age)", dsl.avg(DSL.ref("age", INTEGER))),
                DSL.named("SUM(age)", dsl.sum(DSL.ref("age", INTEGER)))),
        ImmutableList.of(DSL.named("balance", DSL.ref("balance", INTEGER)),
            DSL.named("abs(balance)", dsl.abs(DSL.ref("balance", INTEGER))))
    );
  }
}