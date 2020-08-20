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

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
        DSL.named("int", DSL.ref("integer_value", INTEGER)),
        AstDSL.alias("int", AstDSL.qualifiedName("integer_value"))
    );
  }

  @Test
  public void named_expression_with_alias() {
    assertAnalyzeEqual(
        DSL.named("integer", DSL.ref("integer_value", INTEGER), "int"),
        AstDSL.alias("integer", AstDSL.qualifiedName("integer_value"), "int")
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
