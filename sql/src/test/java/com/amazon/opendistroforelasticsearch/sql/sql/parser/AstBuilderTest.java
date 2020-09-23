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

import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.agg;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.aggregate;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.alias;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.booleanLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.doubleLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.filter;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.function;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.intLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.project;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.qualifiedName;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.relation;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.stringLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.values;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amazon.opendistroforelasticsearch.sql.ast.expression.AllFields;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.UnresolvedPlan;
import com.amazon.opendistroforelasticsearch.sql.common.antlr.SyntaxCheckException;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.SQLSyntaxParser;
import com.google.common.collect.ImmutableList;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.jupiter.api.Test;

class AstBuilderTest {

  /**
   * SQL syntax parser that helps prepare parse tree as AstBuilder input.
   */
  private final SQLSyntaxParser parser = new SQLSyntaxParser();

  @Test
  public void can_build_select_literals() {
    assertEquals(
        project(
            values(emptyList()),
            alias("123", intLiteral(123)),
            alias("'hello'", stringLiteral("hello")),
            alias("false", booleanLiteral(false)),
            alias("-4.567", doubleLiteral(-4.567))
        ),
        buildAST("SELECT 123, 'hello', false, -4.567")
    );
  }

  @Test
  public void can_build_select_function_call_with_alias() {
    assertEquals(
        project(
            relation("test"),
            alias(
                "ABS(age)",
                function("ABS", qualifiedName("age")),
                "a"
            )
        ),
        buildAST("SELECT ABS(age) AS a FROM test")
    );
  }

  @Test
  public void can_build_select_all_from_index() {
    assertEquals(
        project(
            relation("test"),
            AllFields.of()
        ),
        buildAST("SELECT * FROM test")
    );

    assertThrows(SyntaxCheckException.class, () -> buildAST("SELECT *"));
  }

  @Test
  public void can_build_select_all_and_fields_from_index() {
    assertEquals(
        project(
            relation("test"),
            AllFields.of(),
            alias("age", qualifiedName("age")),
            alias("age", qualifiedName("age"), "a")
        ),
        buildAST("SELECT *, age, age as a FROM test")
    );
  }

  @Test
  public void can_build_select_fields_from_index() {
    assertEquals(
        project(
            relation("test"),
            alias("age", qualifiedName("age"))
        ),
        buildAST("SELECT age FROM test")
    );
  }

  @Test
  public void can_build_select_fields_with_alias() {
    assertEquals(
        project(
            relation("test"),
            alias("age", qualifiedName("age"), "a")
        ),
        buildAST("SELECT age AS a FROM test")
    );
  }

  @Test
  public void can_build_select_fields_with_alias_quoted() {
    assertEquals(
        project(
            relation("test"),
            alias(
                "name",
                qualifiedName("name"),
                "first name"
            ),
            alias(
                "(age + 10)",
                function("+", qualifiedName("age"), intLiteral(10)),
                "Age_Expr"
            )
        ),
        buildAST("SELECT"
                + " name AS \"first name\", "
                + " (age + 10) AS `Age_Expr` "
                + "FROM test"
        )
    );
  }

  @Test
  public void can_build_from_index_with_alias() {
    assertEquals(
        project(
            filter(
                relation("test", "tt"),
                function("=", qualifiedName("tt", "age"), intLiteral(30))),
            alias("tt.name", qualifiedName("tt", "name"))
        ),
        buildAST("SELECT tt.name FROM test AS tt WHERE tt.age = 30")
    );
  }

  @Test
  public void can_build_from_index_with_alias_quoted() {
    assertEquals(
        project(
            filter(
                relation("test", "t"),
                function("=", qualifiedName("t", "age"), intLiteral(30))),
            alias("`t`.name", qualifiedName("t", "name"))
        ),
        buildAST("SELECT `t`.name FROM test `t` WHERE `t`.age = 30")
    );
  }

  @Test
  public void can_build_where_clause() {
    assertEquals(
        project(
            filter(
                relation("test"),
                function(
                    "=",
                    qualifiedName("name"),
                    stringLiteral("John"))
            ),
            alias("name", qualifiedName("name"))
        ),
        buildAST("SELECT name FROM test WHERE name = 'John'")
    );
  }

  @Test
  public void can_build_group_by_clause() {
    assertEquals(
        project(
            agg(
                relation("test"),
                ImmutableList.of(aggregate("AVG", qualifiedName("age"))),
                emptyList(),
                ImmutableList.of(qualifiedName("name")),
                emptyList()),
            alias("name", qualifiedName("name")),
            alias("AVG(age)", aggregate("AVG", qualifiedName("age")))),
        buildAST("SELECT name, AVG(age) FROM test GROUP BY name"));
  }

  @Test
  public void can_build_implicit_group_by_clause() {
    assertEquals(
        project(
            agg(
                relation("test"),
                ImmutableList.of(aggregate("AVG", qualifiedName("age"))),
                emptyList(),
                emptyList(),
                emptyList()),
            alias("AVG(age)", aggregate("AVG", qualifiedName("age")))),
        buildAST("SELECT AVG(age) FROM test"));
  }

  private UnresolvedPlan buildAST(String query) {
    ParseTree parseTree = parser.parse(query);
    return parseTree.accept(new AstBuilder(query));
  }

}