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
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.config.ExpressionConfig;
import com.amazon.opendistroforelasticsearch.sql.expression.window.WindowDefinition;
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

  @Test
  void case_clause_should_be_replaced() {
    Expression caseClause = DSL.cases(
        null,
        DSL.when(
            dsl.equal(DSL.ref("age", INTEGER), DSL.literal(30)),
            DSL.literal("true")));

    LogicalPlan logicalPlan =
        LogicalPlanDSL.aggregation(
            LogicalPlanDSL.relation("test"),
            emptyList(),
            ImmutableList.of(DSL.named(
                "CaseClause(whenClauses=[WhenClause(condition==(age, 30), result=\"true\")],"
                    + " defaultResult=null)",
                caseClause)));

    assertEquals(
        DSL.ref(
            "CaseClause(whenClauses=[WhenClause(condition==(age, 30), result=\"true\")],"
                + " defaultResult=null)", STRING),
        optimize(caseClause, logicalPlan));
  }

  @Test
  void aggregation_in_case_when_clause_should_be_replaced() {
    Expression caseClause = DSL.cases(
        null,
        DSL.when(
            dsl.equal(dsl.avg(DSL.ref("age", INTEGER)), DSL.literal(30)),
            DSL.literal("true")));

    LogicalPlan logicalPlan =
        LogicalPlanDSL.aggregation(
            LogicalPlanDSL.relation("test"),
            ImmutableList.of(DSL.named("AVG(age)", dsl.avg(DSL.ref("age", INTEGER)))),
            ImmutableList.of(DSL.named("name", DSL.ref("name", STRING))));

    assertEquals(
        DSL.cases(
            null,
            DSL.when(
                dsl.equal(DSL.ref("AVG(age)", DOUBLE), DSL.literal(30)),
                DSL.literal("true"))),
        optimize(caseClause, logicalPlan));
  }

  @Test
  void aggregation_in_case_else_clause_should_be_replaced() {
    Expression caseClause = DSL.cases(
        dsl.avg(DSL.ref("age", INTEGER)),
        DSL.when(
            dsl.equal(DSL.ref("age", INTEGER), DSL.literal(30)),
            DSL.literal("true")));

    LogicalPlan logicalPlan =
        LogicalPlanDSL.aggregation(
            LogicalPlanDSL.relation("test"),
            ImmutableList.of(DSL.named("AVG(age)", dsl.avg(DSL.ref("age", INTEGER)))),
            ImmutableList.of(DSL.named("name", DSL.ref("name", STRING))));

    assertEquals(
        DSL.cases(
            DSL.ref("AVG(age)", DOUBLE),
            DSL.when(
                dsl.equal(DSL.ref("age", INTEGER), DSL.literal(30)),
                DSL.literal("true"))),
        optimize(caseClause, logicalPlan));
  }

  @Test
  void window_expression_should_be_replaced() {
    LogicalPlan logicalPlan =
        LogicalPlanDSL.window(
            LogicalPlanDSL.window(
                LogicalPlanDSL.relation("test"),
                dsl.rank(),
                new WindowDefinition(emptyList(), emptyList())),
            dsl.denseRank(),
            new WindowDefinition(emptyList(), emptyList()));

    assertEquals(
        DSL.ref("rank()", INTEGER),
        optimize(dsl.rank(), logicalPlan));
    assertEquals(
        DSL.ref("dense_rank()", INTEGER),
        optimize(dsl.denseRank(), logicalPlan));
  }

  Expression optimize(Expression expression) {
    return optimize(expression, logicalPlan());
  }

  Expression optimize(Expression expression, LogicalPlan logicalPlan) {
    final ExpressionReferenceOptimizer optimizer =
        new ExpressionReferenceOptimizer(functionRepository, logicalPlan);
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