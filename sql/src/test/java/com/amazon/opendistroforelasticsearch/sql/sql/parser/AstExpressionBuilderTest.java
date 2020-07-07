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

package com.amazon.opendistroforelasticsearch.sql.sql.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.opendistroforelasticsearch.sql.ast.Node;
import com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL;
import com.amazon.opendistroforelasticsearch.sql.common.antlr.CaseInsensitiveCharStream;
import com.amazon.opendistroforelasticsearch.sql.common.antlr.SyntaxAnalysisErrorListener;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLLexer;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

class AstExpressionBuilderTest {

  private final AstExpressionBuilder astExprBuilder = new AstExpressionBuilder();

  @Test
  public void canBuildExprAstForQualifiedName() {
    assertEquals(
        AstDSL.qualifiedName("test"),
        buildQualifiedName("test")
    );

    assertEquals(
        AstDSL.qualifiedName("hello world"),
        buildQualifiedName("`hello world`")
    );

    assertEquals(
        AstDSL.qualifiedName("log.2020.01"),
        buildQualifiedName("`log.2020.01`")
    );
  }

  @Test
  public void canBuildExprAstForStringLiteral() {
    assertEquals(
        AstDSL.stringLiteral("hello"),
        buildExprAst("'hello'")
    );
  }

  @Test
  public void canBuildExprAstForIntegerLiteral() {
    assertEquals(
        AstDSL.intLiteral(123),
        buildExprAst("123")
    );
  }

  @Test
  public void canBuildExprAstForNegativeRealLiteral() {
    assertEquals(
        AstDSL.doubleLiteral(-4.567),
        buildExprAst("-4.567")
    );
  }

  @Test
  public void canBuildExprAstForBooleanLiteral() {
    assertEquals(
        AstDSL.booleanLiteral(true),
        buildExprAst("true")
    );
  }

  private Node buildQualifiedName(String expr) {
    return createParser(expr).qualifiedName().accept(astExprBuilder);
  }

  private Node buildExprAst(String expr) {
    return createParser(expr).constant().accept(astExprBuilder);
  }

  private OpenDistroSQLParser createParser(String expr) {
    OpenDistroSQLLexer lexer = new OpenDistroSQLLexer(new CaseInsensitiveCharStream(expr));
    OpenDistroSQLParser parser = new OpenDistroSQLParser(new CommonTokenStream(lexer));
    parser.addErrorListener(new SyntaxAnalysisErrorListener());
    return parser;
  }

}