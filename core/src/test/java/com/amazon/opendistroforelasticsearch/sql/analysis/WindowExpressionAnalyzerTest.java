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

package com.amazon.opendistroforelasticsearch.sql.analysis;

import static com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.SortOption.DEFAULT_ASC;
import static com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.SortOption.DEFAULT_DESC;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.config.ExpressionConfig;
import com.amazon.opendistroforelasticsearch.sql.expression.window.WindowDefinition;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalRelation;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Configuration
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ExpressionConfig.class, SelectExpressionAnalyzerTest.class})
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class WindowExpressionAnalyzerTest extends AnalyzerTestBase {

  private final LogicalPlan child = new LogicalRelation("test");

  private WindowExpressionAnalyzer analyzer;

  @BeforeEach
  void setUp() {
    analyzer = new WindowExpressionAnalyzer(expressionAnalyzer, child);
  }

  @SuppressWarnings("unchecked")
  @Test
  void should_wrap_child_with_window_and_sort_operator_if_project_item_windowed() {
    assertEquals(
        LogicalPlanDSL.window(
            LogicalPlanDSL.sort(
                LogicalPlanDSL.relation("test"),
                ImmutablePair.of(DEFAULT_ASC, DSL.ref("string_value", STRING)),
                ImmutablePair.of(DEFAULT_DESC, DSL.ref("integer_value", INTEGER))),
            dsl.rowNumber(),
            new WindowDefinition(
                ImmutableList.of(DSL.ref("string_value", STRING)),
                ImmutableList.of(
                    ImmutablePair.of(DEFAULT_DESC, DSL.ref("integer_value", INTEGER))))),
        analyzer.analyze(
            AstDSL.alias(
                "row_number",
                AstDSL.window(
                    AstDSL.function("row_number"),
                    ImmutableList.of(AstDSL.qualifiedName("string_value")),
                    ImmutableList.of(
                        ImmutablePair.of("DESC", AstDSL.qualifiedName("integer_value"))))),
            analysisContext));
  }

  @Test
  void should_return_original_child_if_project_item_not_windowed() {
    assertEquals(
        child,
        analyzer.analyze(
            AstDSL.alias(
                "string_value",
                AstDSL.qualifiedName("string_value")),
            analysisContext));
  }

}