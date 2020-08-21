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

package com.amazon.opendistroforelasticsearch.sql.executor;

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.literal;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.named;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.ref;
import static com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlanDSL.filter;
import static com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlanDSL.project;
import static com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlanDSL.rename;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine.ExplainResponse;
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine.ExplainResponseNode;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionTestBase;
import com.amazon.opendistroforelasticsearch.sql.expression.NamedExpression;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.RenameOperator;
import com.amazon.opendistroforelasticsearch.sql.storage.TableScanOperator;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ExplainTest extends ExpressionTestBase {

  private final Explain explain = new Explain();

  @Test
  void can_explain_plan_with_project_filter_table_scan() {
    Expression filterExpr =
        dsl.and(
            dsl.equal(ref("balance", INTEGER), literal(10000)),
            dsl.greater(ref("age", INTEGER), literal(30)));
    NamedExpression[] projectList = {
        named("full_name", ref("full_name", STRING), "name"),
        named("age", ref("age", INTEGER))
    };

    PhysicalPlan plan =
        project(
            filter(
                new FakeTableScan(),
                filterExpr),
            projectList);

    assertEquals(
        new ExplainResponse(
            new ExplainResponseNode(
                "ProjectOperator",
                ImmutableMap.of("fields", Arrays.asList("name", "age")),
                new ExplainResponseNode(
                    "FilterOperator",
                    ImmutableMap.of("conditions", "balance = 10000 and age > 30"),
                    new ExplainResponseNode(
                        "FakeTableScan",
                        ImmutableMap.of("request", "Fake DSL request"),
                        null)))),
        explain.apply(plan)
    );
  }

  @Test
  void should_have_empty_description_for_unimplemented_operators() {
    RenameOperator plan = rename(new FakeTableScan(),
        ImmutableMap.of(ref("full_name", STRING), ref("name", STRING)));
    assertEquals(
        new ExplainResponse(
            new ExplainResponseNode(
                "RenameOperator",
                ImmutableMap.of(),
                new ExplainResponseNode(
                    "FakeTableScan",
                    ImmutableMap.of("request", "Fake DSL request"),
                    null))),
        explain.apply(plan)
    );
  }

  private static class FakeTableScan extends TableScanOperator {
    @Override
    public boolean hasNext() {
      return false;
    }

    @Override
    public ExprValue next() {
      return null;
    }

    @Override
    public String toString() {
      return "Fake DSL request";
    }
  }

}