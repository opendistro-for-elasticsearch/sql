/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.planner.logical;

import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.argument;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.booleanLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.defaultSortFieldArgs;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.defaultSortOptions;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.exprList;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.field;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.nullLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.relation;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.sort;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.sortOptions;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;

import com.amazon.opendistroforelasticsearch.sql.analysis.AnalyzerTestBase;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.SortOption;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.config.ExpressionConfig;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Configuration
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ExpressionConfig.class, AnalyzerTestBase.class})
class LogicalSortTest extends AnalyzerTestBase {
  @Test
  public void analyze_sort_with_two_field_with_default_option() {
    assertAnalyzeEqual(
        LogicalPlanDSL.sort(
            LogicalPlanDSL.relation("schema"),
            1000,
            ImmutablePair.of(SortOption.DEFAULT_ASC, DSL.ref("integer_value", INTEGER)),
            ImmutablePair.of(SortOption.DEFAULT_ASC, DSL.ref("double_value", DOUBLE))),
        sort(
            relation("schema"),
            defaultSortOptions(),
            field("integer_value", defaultSortFieldArgs()),
            field("double_value", defaultSortFieldArgs())));
  }

  @Test
  public void analyze_sort_with_two_field() {
    assertAnalyzeEqual(
        LogicalPlanDSL.sort(
            LogicalPlanDSL.relation("schema"),
            100,
            ImmutablePair.of(SortOption.DEFAULT_DESC, DSL.ref("integer_value", INTEGER)),
            ImmutablePair.of(SortOption.DEFAULT_ASC, DSL.ref("double_value", DOUBLE))),
        sort(
            relation("schema"),
            sortOptions(100),
            field(
                "integer_value",
                exprList(argument("asc", booleanLiteral(false)), argument("type", nullLiteral()))),
            field("double_value", defaultSortFieldArgs())));
  }
}
