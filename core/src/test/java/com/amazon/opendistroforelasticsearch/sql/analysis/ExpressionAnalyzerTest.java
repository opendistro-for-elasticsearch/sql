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

package com.amazon.opendistroforelasticsearch.sql.analysis;

import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.field;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_TRUE;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.integerValue;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.BOOLEAN;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.common.antlr.SyntaxCheckException;
import com.amazon.opendistroforelasticsearch.sql.exception.SemanticCheckException;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.config.ExpressionConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Configuration
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ExpressionConfig.class, AnalyzerTestBase.class})
class ExpressionAnalyzerTest extends AnalyzerTestBase {

  @Test
  public void equal() {
    assertAnalyzeEqual(
        dsl.equal(DSL.ref("integer_value", INTEGER), DSL.literal(integerValue(1))),
        AstDSL.equalTo(AstDSL.unresolvedAttr("integer_value"), AstDSL.intLiteral(1))
    );
  }

  @Test
  public void and() {
    assertAnalyzeEqual(
        dsl.and(DSL.ref("boolean_value", BOOLEAN), DSL.literal(LITERAL_TRUE)),
        AstDSL.and(AstDSL.unresolvedAttr("boolean_value"), AstDSL.booleanLiteral(true))
    );
  }

  @Test
  public void or() {
    assertAnalyzeEqual(
        dsl.or(DSL.ref("boolean_value", BOOLEAN), DSL.literal(LITERAL_TRUE)),
        AstDSL.or(AstDSL.unresolvedAttr("boolean_value"), AstDSL.booleanLiteral(true))
    );
  }

  @Test
  public void xor() {
    assertAnalyzeEqual(
        dsl.xor(DSL.ref("boolean_value", BOOLEAN), DSL.literal(LITERAL_TRUE)),
        AstDSL.xor(AstDSL.unresolvedAttr("boolean_value"), AstDSL.booleanLiteral(true))
    );
  }

  @Test
  public void not() {
    assertAnalyzeEqual(
        dsl.not(DSL.ref("boolean_value", BOOLEAN)),
        AstDSL.not(AstDSL.unresolvedAttr("boolean_value"))
    );
  }

  @Test
  public void qualified_name() {
    assertAnalyzeEqual(
        DSL.ref("integer_value", INTEGER),
        AstDSL.qualifiedName("integer_value")
    );
  }

  @Test
  public void skip_identifier_with_qualifier() {
    SyntaxCheckException exception =
        assertThrows(SyntaxCheckException.class,
            () -> analyze(AstDSL.qualifiedName("index_alias", "integer_value")));

    assertEquals(
        "Qualified name [index_alias.integer_value] is not supported yet",
        exception.getMessage()
    );
  }

  @Test
  public void skip_struct_data_type() {
    SyntaxCheckException exception =
        assertThrows(SyntaxCheckException.class,
            () -> analyze(AstDSL.qualifiedName("struct_value")));
    assertEquals(
        "Identifier [struct_value] of type [STRUCT] is not supported yet",
        exception.getMessage()
    );
  }

  @Test
  public void skip_array_data_type() {
    SyntaxCheckException exception =
        assertThrows(SyntaxCheckException.class,
            () -> analyze(AstDSL.qualifiedName("array_value")));
    assertEquals(
        "Identifier [array_value] of type [ARRAY] is not supported yet",
        exception.getMessage()
    );
  }

  @Test
  public void undefined_var_semantic_check_failed() {
    SemanticCheckException exception = assertThrows(SemanticCheckException.class,
        () -> analyze(
            AstDSL.and(AstDSL.unresolvedAttr("undefined_field"), AstDSL.booleanLiteral(true))));
    assertEquals("can't resolve Symbol(namespace=FIELD_NAME, name=undefined_field) in type env",
        exception.getMessage());
  }

  @Test
  public void undefined_aggregation_function() {
    SemanticCheckException exception = assertThrows(SemanticCheckException.class,
        () -> analyze(AstDSL.aggregate("ESTDC_ERROR", field("integer_value"))));
    assertEquals("Unsupported aggregation function ESTDC_ERROR", exception.getMessage());
  }

  protected Expression analyze(UnresolvedExpression unresolvedExpression) {
    return expressionAnalyzer.analyze(unresolvedExpression, analysisContext);
  }

  protected void assertAnalyzeEqual(Expression expected,
                                    UnresolvedExpression unresolvedExpression) {
    assertEquals(expected, analyze(unresolvedExpression));
  }
}