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

import com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.common.antlr.CaseInsensitiveCharStream;
import com.amazon.opendistroforelasticsearch.sql.common.antlr.SyntaxAnalysisErrorListener;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLLexer;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.RuleNode;
import org.junit.jupiter.api.Test;

public class AstQualifiedNameBuilderTest {

  @Test
  public void canBuildRegularIdentifierForSQLStandard() {
    buildFromIdentifier("test").expectQualifiedName("test");
    buildFromIdentifier("test123").expectQualifiedName("test123");
    buildFromIdentifier("test_123").expectQualifiedName("test_123");
  }

  @Test
  public void canBuildRegularIdentifierForElasticsearch() {
    buildFromTableName(".kibana").expectQualifiedName(".kibana");
    //buildFromIdentifier("@timestamp").expectQualifiedName("@timestamp");//TODO: field name
    buildFromIdentifier("logs-2020-01").expectQualifiedName("logs-2020-01");
    buildFromIdentifier("*logs*").expectQualifiedName("*logs*");
  }

  @Test
  public void canBuildDelimitedIdentifier() {
    buildFromIdentifier("\"hello$world\"").expectQualifiedName("hello$world");
    buildFromIdentifier("`logs.2020.01`").expectQualifiedName("logs.2020.01");
  }

  @Test
  public void canBuildQualifiedIdentifier() {
    buildFromQualifiers("account.location.city").expectQualifiedName("account", "location", "city");
  }

  private AstExpressionBuilderAssertion buildFromIdentifier(String expr) {
    return new AstExpressionBuilderAssertion(OpenDistroSQLParser::ident, expr);
  }

  private AstExpressionBuilderAssertion buildFromQualifiers(String expr) {
    return new AstExpressionBuilderAssertion(OpenDistroSQLParser::qualifiedName, expr);
  }

  private AstExpressionBuilderAssertion buildFromTableName(String expr) {
    return new AstExpressionBuilderAssertion(OpenDistroSQLParser::tableName, expr);
  }

  @RequiredArgsConstructor
  private static class AstExpressionBuilderAssertion {
    private final AstExpressionBuilder astExprBuilder = new AstExpressionBuilder();
    private final Function<OpenDistroSQLParser, RuleNode> build;
    private final String actual;

    public void expectQualifiedName(String... expected) {
      assertEquals(AstDSL.qualifiedName(expected), buildExpression(actual));
    }

    private UnresolvedExpression buildExpression(String expr) {
      return build.apply(createParser(expr)).accept(astExprBuilder);
    }

    private OpenDistroSQLParser createParser(String expr) {
      OpenDistroSQLLexer lexer = new OpenDistroSQLLexer(new CaseInsensitiveCharStream(expr));
      OpenDistroSQLParser parser = new OpenDistroSQLParser(new CommonTokenStream(lexer));
      parser.addErrorListener(new SyntaxAnalysisErrorListener());
      return parser;
    }
  }

}
