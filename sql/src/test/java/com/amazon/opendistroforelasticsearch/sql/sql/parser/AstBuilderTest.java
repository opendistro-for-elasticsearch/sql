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

import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.booleanLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.doubleLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.intLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.project;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.relation;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.stringLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.values;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amazon.opendistroforelasticsearch.sql.ast.tree.UnresolvedPlan;
import com.amazon.opendistroforelasticsearch.sql.common.antlr.SyntaxCheckException;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.SQLSyntaxParser;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

class AstBuilderTest {

  /**
   * SQL syntax parser that helps prepare parse tree as AstBuilder input.
   */
  private final SQLSyntaxParser parser = new SQLSyntaxParser();

  /**
   * AST builder class that being tested.
   */
  private final AstBuilder astBuilder = new AstBuilder();

  @Test
  public void canBuildSelectLiterals() {
    assertEquals(
        project(
            values(emptyList()),
            intLiteral(123),
            stringLiteral("hello"),
            booleanLiteral(false),
            doubleLiteral(-4.567)
        ),
        buildAST("SELECT 123, 'hello', false, -4.567")
    );
  }

  @Test
  public void canBuildSelectAllFromIndex() {
    assertEquals(
        relation("test"),
        buildAST("SELECT * FROM test")
    );

    assertThrows(SyntaxCheckException.class, () -> buildAST("SELECT *"));
  }

  @Test
  public void buildSelectFieldsFromIndex() { // TODO: change to select fields later
    assertEquals(
        project(relation("test"), intLiteral(1)),
        buildAST("SELECT 1 FROM test")
    );
  }

  private UnresolvedPlan buildAST(String query) {
    ParseTree parseTree = parser.parse(query);
    return parseTree.accept(astBuilder);
  }

}