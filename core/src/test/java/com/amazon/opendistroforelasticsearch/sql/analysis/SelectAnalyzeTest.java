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

import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.argument;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.booleanLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.field;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;

import com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.AllFields;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.config.ExpressionConfig;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Configuration
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ExpressionConfig.class, SelectAnalyzeTest.class})
public class SelectAnalyzeTest extends AnalyzerTestBase {

  @Override
  protected Map<String, ExprType> typeMapping() {
    return new ImmutableMap.Builder<String, ExprType>()
        .put("integer_value", ExprCoreType.INTEGER)
        .put("double_value", ExprCoreType.DOUBLE)
        .put("string_value", ExprCoreType.STRING)
        .build();
  }

  @Test
  public void project_all_from_source() {
    assertAnalyzeEqual(
        LogicalPlanDSL.project(
            LogicalPlanDSL.relation("schema"),
            DSL.named("integer_value", DSL.ref("integer_value", INTEGER)),
            DSL.named("double_value", DSL.ref("double_value", DOUBLE)),
            DSL.named("string_value", DSL.ref("string_value", STRING)),
            DSL.named("integer_value", DSL.ref("integer_value", INTEGER)),
            DSL.named("double_value", DSL.ref("double_value", DOUBLE))
        ),
        AstDSL.projectWithArg(
            AstDSL.relation("schema"),
            AstDSL.defaultFieldsArgs(),
            AstDSL.field("integer_value"), // Field not wrapped by Alias
            AstDSL.alias("double_value", AstDSL.field("double_value")),
            AllFields.of()));
  }

  @Test
  public void select_and_project_all() {
    assertAnalyzeEqual(
        LogicalPlanDSL.project(
            LogicalPlanDSL.project(
                LogicalPlanDSL.relation("schema"),
                DSL.named("integer_value", DSL.ref("integer_value", INTEGER)),
                DSL.named("double_value", DSL.ref("double_value", DOUBLE))
            ),
            DSL.named("integer_value", DSL.ref("integer_value", INTEGER)),
            DSL.named("double_value", DSL.ref("double_value", DOUBLE))
        ),
        AstDSL.projectWithArg(
            AstDSL.projectWithArg(
                AstDSL.relation("schema"),
                AstDSL.defaultFieldsArgs(),
                AstDSL.field("integer_value"),
                AstDSL.field("double_value")),
            AstDSL.defaultFieldsArgs(),
            AllFields.of()
        ));
  }

  @Test
  public void remove_and_project_all() {
    assertAnalyzeEqual(
        LogicalPlanDSL.project(
            LogicalPlanDSL.remove(
                LogicalPlanDSL.relation("schema"),
                DSL.ref("integer_value", INTEGER),
                DSL.ref("double_value", DOUBLE)
            ),
            DSL.named("string_value", DSL.ref("string_value", STRING))
        ),
        AstDSL.projectWithArg(
            AstDSL.projectWithArg(
                AstDSL.relation("schema"),
                AstDSL.exprList(argument("exclude", booleanLiteral(true))),
                AstDSL.field("integer_value"),
                AstDSL.field("double_value")),
            AstDSL.defaultFieldsArgs(),
            AllFields.of()
        ));
  }

  @Test
  public void stats_and_project_all() {
    assertAnalyzeEqual(
        LogicalPlanDSL.project(
            LogicalPlanDSL.aggregation(
                LogicalPlanDSL.relation("schema"),
                ImmutableList.of(DSL
                    .named("avg(integer_value)", dsl.avg(DSL.ref("integer_value", INTEGER)))),
                ImmutableList.of(DSL.named("string_value", DSL.ref("string_value", STRING)))),
            DSL.named("string_value", DSL.ref("string_value", STRING)),
            DSL.named("avg(integer_value)", DSL.ref("avg(integer_value)", DOUBLE))
        ),
        AstDSL.projectWithArg(
            AstDSL.agg(
                AstDSL.relation("schema"),
                AstDSL.exprList(AstDSL.alias("avg(integer_value)", AstDSL.aggregate("avg",
                    field("integer_value")))),
                null,
                ImmutableList.of(AstDSL.alias("string_value", field("string_value"))),
                AstDSL.defaultStatsArgs()), AstDSL.defaultFieldsArgs(),
            AllFields.of()));
  }

  @Test
  public void rename_and_project_all() {
    assertAnalyzeEqual(
        LogicalPlanDSL.project(
            LogicalPlanDSL.rename(
                LogicalPlanDSL.relation("schema"),
                ImmutableMap.of(DSL.ref("integer_value", INTEGER), DSL.ref("ivalue", INTEGER))),
            DSL.named("ivalue", DSL.ref("ivalue", INTEGER)),
            DSL.named("string_value", DSL.ref("string_value", STRING)),
            DSL.named("double_value", DSL.ref("double_value", DOUBLE))
        ),
        AstDSL.projectWithArg(
            AstDSL.rename(
                AstDSL.relation("schema"),
                AstDSL.map(AstDSL.field("integer_value"), AstDSL.field("ivalue"))),
            AstDSL.defaultFieldsArgs(),
            AllFields.of()
        ));
  }
}
