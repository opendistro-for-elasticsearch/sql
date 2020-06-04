/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.field;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.analysis.AnalyzerTestBase;
import com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LogicalEvalTest extends AnalyzerTestBase {
  @Mock
  private Environment<Expression, ExprType> environment;

  @Test
  public void analyze_eval_with_one_field() {
    when(environment.resolve(DSL.ref("integer_value"))).thenReturn(ExprType.INTEGER);

    assertAnalyzeEqual(
        LogicalPlanDSL.eval(
            LogicalPlanDSL.relation("schema"),
            ImmutablePair.of(DSL.ref("absValue"), dsl.abs(environment, DSL.ref("integer_value")))),
        AstDSL.eval(
            AstDSL.relation("schema"),
            AstDSL.let(AstDSL.field("absValue"), AstDSL.function("abs", field("integer_value")))));
  }

  @Test
  public void analyze_eval_with_two_field() {
    when(environment.resolve(DSL.ref("integer_value"))).thenReturn(ExprType.INTEGER);
    when(environment.resolve(DSL.ref("absValue"))).thenReturn(ExprType.INTEGER);

    assertAnalyzeEqual(
        LogicalPlanDSL.eval(
            LogicalPlanDSL.relation("schema"),
            ImmutablePair.of(DSL.ref("absValue"), dsl.abs(environment, DSL.ref("integer_value"))),
            ImmutablePair.of(DSL.ref("iValue"), dsl.abs(environment, DSL.ref("absValue")))),
        AstDSL.eval(
            AstDSL.relation("schema"),
            AstDSL.let(AstDSL.field("absValue"), AstDSL.function("abs", field("integer_value"))),
            AstDSL.let(AstDSL.field("iValue"), AstDSL.function("abs", field("absValue")))));
  }
}
