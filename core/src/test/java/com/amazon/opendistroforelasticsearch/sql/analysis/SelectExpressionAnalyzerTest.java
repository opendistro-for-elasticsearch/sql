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

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.FLOAT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRUCT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.opendistroforelasticsearch.sql.analysis.symbol.Namespace;
import com.amazon.opendistroforelasticsearch.sql.analysis.symbol.Symbol;
import com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.NamedExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.config.ExpressionConfig;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Configuration
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ExpressionConfig.class, SelectExpressionAnalyzerTest.class})
public class SelectExpressionAnalyzerTest extends AnalyzerTestBase {

  @Test
  public void named_expression() {
    assertAnalyzeEqual(
        DSL.named("integer_value", DSL.ref("integer_value", INTEGER)),
        AstDSL.alias("integer_value", AstDSL.qualifiedName("integer_value"))
    );
  }

  @Test
  public void named_expression_with_alias() {
    assertAnalyzeEqual(
        DSL.named("integer_value", DSL.ref("integer_value", INTEGER), "int"),
        AstDSL.alias("integer_value", AstDSL.qualifiedName("integer_value"), "int")
    );
  }

  @Test
  public void named_expression_with_delegated_expression_defined_in_symbol_table() {
    analysisContext.push();
    analysisContext.peek().define(new Symbol(Namespace.FIELD_NAME, "AVG(integer_value)"), FLOAT);

    assertAnalyzeEqual(
        DSL.named("AVG(integer_value)", DSL.ref("AVG(integer_value)", FLOAT)),
        AstDSL.alias("AVG(integer_value)",
            AstDSL.aggregate("AVG", AstDSL.qualifiedName("integer_value")))
    );
  }

  @Test
  public void field_name_with_qualifier() {
    analysisContext.peek().define(new Symbol(Namespace.INDEX_NAME, "index_alias"), STRUCT);
    assertAnalyzeEqual(
        DSL.named("integer_value", DSL.ref("integer_value", INTEGER)),
        AstDSL.alias("integer_alias.integer_value",
            AstDSL.qualifiedName("index_alias", "integer_value"))
    );
  }

  @Test
  public void field_name_with_qualifier_quoted() {
    analysisContext.peek().define(new Symbol(Namespace.INDEX_NAME, "index_alias"), STRUCT);
    assertAnalyzeEqual(
        DSL.named("integer_value", DSL.ref("integer_value", INTEGER)),
        AstDSL.alias("`integer_alias`.integer_value", // qualifier in SELECT is quoted originally
            AstDSL.qualifiedName("index_alias", "integer_value"))
    );
  }

  @Test
  public void field_name_in_expression_with_qualifier() {
    analysisContext.peek().define(new Symbol(Namespace.INDEX_NAME, "index_alias"), STRUCT);
    assertAnalyzeEqual(
        DSL.named("abs(index_alias.integer_value)", dsl.abs(DSL.ref("integer_value", INTEGER))),
        AstDSL.alias("abs(index_alias.integer_value)",
            AstDSL.function("abs", AstDSL.qualifiedName("index_alias", "integer_value")))
    );
  }

  protected List<NamedExpression> analyze(UnresolvedExpression unresolvedExpression) {

    return new SelectExpressionAnalyzer(expressionAnalyzer)
        .analyze(Arrays.asList(unresolvedExpression),
            analysisContext);
  }

  protected void assertAnalyzeEqual(NamedExpression expected,
                                    UnresolvedExpression unresolvedExpression) {
    assertEquals(Arrays.asList(expected), analyze(unresolvedExpression));
  }
}
