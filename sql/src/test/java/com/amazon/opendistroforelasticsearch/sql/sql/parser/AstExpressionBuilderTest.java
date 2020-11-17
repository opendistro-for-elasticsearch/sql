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

import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.and;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.booleanLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.caseWhen;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.dateLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.doubleLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.function;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.intLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.intervalLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.not;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.nullLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.or;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.qualifiedName;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.stringLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.timeLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.timestampLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.when;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.window;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.opendistroforelasticsearch.sql.ast.Node;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.DataType;
import com.amazon.opendistroforelasticsearch.sql.common.antlr.CaseInsensitiveCharStream;
import com.amazon.opendistroforelasticsearch.sql.common.antlr.SyntaxAnalysisErrorListener;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLLexer;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser;
import com.google.common.collect.ImmutableList;
import org.antlr.v4.runtime.CommonTokenStream;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.Test;

class AstExpressionBuilderTest {

  private final AstExpressionBuilder astExprBuilder = new AstExpressionBuilder();

  @Test
  public void canBuildStringLiteral() {
    assertEquals(
        stringLiteral("hello"),
        buildExprAst("'hello'")
    );
  }

  @Test
  public void canBuildIntegerLiteral() {
    assertEquals(
        intLiteral(123),
        buildExprAst("123")
    );
  }

  @Test
  public void canBuildNegativeRealLiteral() {
    assertEquals(
        doubleLiteral(-4.567),
        buildExprAst("-4.567")
    );
  }

  @Test
  public void canBuildBooleanLiteral() {
    assertEquals(
        booleanLiteral(true),
        buildExprAst("true")
    );
  }

  @Test
  public void canBuildDateLiteral() {
    assertEquals(
        dateLiteral("2020-07-07"),
        buildExprAst("DATE '2020-07-07'")
    );
  }

  @Test
  public void canBuildTimeLiteral() {
    assertEquals(
        timeLiteral("11:30:45"),
        buildExprAst("TIME '11:30:45'")
    );
  }

  @Test
  public void canBuildTimestampLiteral() {
    assertEquals(
        timestampLiteral("2020-07-07 11:30:45"),
        buildExprAst("TIMESTAMP '2020-07-07 11:30:45'")
    );
  }

  @Test
  public void canBuildIntervalLiteral() {
    assertEquals(
        intervalLiteral(1, DataType.INTEGER, "day"),
        buildExprAst("interval 1 day")
    );
  }

  @Test
  public void canBuildArithmeticExpression() {
    assertEquals(
        function("+", intLiteral(1), intLiteral(2)),
        buildExprAst("1 + 2")
    );
  }

  @Test
  public void canBuildFunctionWithoutArguments() {
    assertEquals(
        function("PI"),
        buildExprAst("PI()")
    );
  }

  @Test
  public void canBuildExpressionWithParentheses() {
    assertEquals(
        function("*",
            function("+", doubleLiteral(-1.0), doubleLiteral(2.3)),
            function("-", intLiteral(3), intLiteral(1))
        ),
        buildExprAst("(-1.0 + 2.3) * (3 - 1)")
    );
  }

  @Test
  public void canBuildFunctionCall() {
    assertEquals(
        function("abs", intLiteral(-1)),
        buildExprAst("abs(-1)")
    );
  }

  @Test
  public void canBuildNestedFunctionCall() {
    assertEquals(
        function("abs",
            function("*",
              function("abs", intLiteral(-5)),
              intLiteral(-1)
            )
        ),
        buildExprAst("abs(abs(-5) * -1)")
    );
  }

  @Test
  public void canBuildDateAndTimeFunctionCall() {
    assertEquals(
        function("dayofmonth", dateLiteral("2020-07-07")),
        buildExprAst("dayofmonth(DATE '2020-07-07')")
    );
  }

  @Test
  public void canBuildComparisonExpression() {
    assertEquals(
        function("!=", intLiteral(1), intLiteral(2)),
        buildExprAst("1 != 2")
    );

    assertEquals(
        function("!=", intLiteral(1), intLiteral(2)),
        buildExprAst("1 <> 2")
    );
  }

  @Test
  public void canBuildNullTestExpression() {
    assertEquals(
        function("is null", intLiteral(1)),
        buildExprAst("1 is NULL")
    );

    assertEquals(
        function("is not null", intLiteral(1)),
        buildExprAst("1 IS NOT null")
    );
  }

  @Test
  public void canBuildNullTestExpressionWithNULLLiteral() {
    assertEquals(
        function("is null", nullLiteral()),
        buildExprAst("NULL is NULL")
    );

    assertEquals(
        function("is not null", nullLiteral()),
        buildExprAst("NULL IS NOT null")
    );
  }

  @Test
  public void canBuildLikeExpression() {
    assertEquals(
        function("like", stringLiteral("str"), stringLiteral("st%")),
        buildExprAst("'str' like 'st%'")
    );

    assertEquals(
        function("not like", stringLiteral("str"), stringLiteral("st%")),
        buildExprAst("'str' not like 'st%'")
    );
  }

  @Test
  public void canBuildRegexpExpression() {
    assertEquals(
        function("regexp", stringLiteral("str"), stringLiteral(".*")),
        buildExprAst("'str' regexp '.*'")
    );
  }

  @Test
  public void canBuildLogicalExpression() {
    assertEquals(
        and(booleanLiteral(true), booleanLiteral(false)),
        buildExprAst("true AND false")
    );

    assertEquals(
        or(booleanLiteral(true), booleanLiteral(false)),
        buildExprAst("true OR false")
    );

    assertEquals(
        not(booleanLiteral(false)),
        buildExprAst("NOT false")
    );
  }

  @Test
  public void canBuildWindowFunction() {
    assertEquals(
        window(
            function("RANK"),
            ImmutableList.of(qualifiedName("state")),
            ImmutableList.of(ImmutablePair.of("ASC", qualifiedName("age")))),
        buildExprAst("RANK() OVER (PARTITION BY state ORDER BY age)"));
  }

  @Test
  public void canBuildWindowFunctionWithoutPartitionBy() {
    assertEquals(
        window(
            function("DENSE_RANK"),
            ImmutableList.of(),
            ImmutableList.of(ImmutablePair.of("DESC", qualifiedName("age")))),
        buildExprAst("DENSE_RANK() OVER (ORDER BY age DESC)"));
  }

  @Test
  public void canBuildWindowFunctionWithoutOrderBy() {
    assertEquals(
        window(
            function("RANK"),
            ImmutableList.of(qualifiedName("state")),
            ImmutableList.of()),
        buildExprAst("RANK() OVER (PARTITION BY state)"));
  }

  @Test
  public void canBuildCaseConditionStatement() {
    assertEquals(
        caseWhen(
            null, // no else statement
            when(
                function(">", qualifiedName("age"), intLiteral(30)),
                stringLiteral("age1"))),
        buildExprAst("CASE WHEN age > 30 THEN 'age1' END")
    );
  }

  @Test
  public void canBuildCaseValueStatement() {
    assertEquals(
        caseWhen(
            qualifiedName("age"),
            stringLiteral("age2"),
            when(intLiteral(30), stringLiteral("age1"))),
        buildExprAst("CASE age WHEN 30 THEN 'age1' ELSE 'age2' END")
    );
  }

  @Test
  public void canBuildKeywordsAsIdentifiers() {
    assertEquals(
        qualifiedName("timestamp"),
        buildExprAst("timestamp")
    );
  }

  private Node buildExprAst(String expr) {
    OpenDistroSQLLexer lexer = new OpenDistroSQLLexer(new CaseInsensitiveCharStream(expr));
    OpenDistroSQLParser parser = new OpenDistroSQLParser(new CommonTokenStream(lexer));
    parser.addErrorListener(new SyntaxAnalysisErrorListener());
    return parser.expression().accept(astExprBuilder);
  }

}