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

package com.amazon.opendistroforelasticsearch.sql.sql.parser.context;

import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.aggregate;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.function;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.qualifiedName;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.opendistroforelasticsearch.sql.common.antlr.CaseInsensitiveCharStream;
import com.amazon.opendistroforelasticsearch.sql.common.antlr.SyntaxAnalysisErrorListener;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLLexer;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.QuerySpecificationContext;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class QuerySpecificationTest {

  @Test
  void can_collect_group_by_items_in_group_by_clause() {
    QuerySpecification querySpec = collect(
        "SELECT name, ABS(age) FROM test GROUP BY name, ABS(age)");

    assertEquals(
        ImmutableList.of(
            qualifiedName("name"),
            function("ABS", qualifiedName("age"))),
        querySpec.getGroupByItems());
  }

  @Test
  void can_collect_select_items_in_select_clause() {
    QuerySpecification querySpec = collect(
        "SELECT name, ABS(age) FROM test");

    assertEquals(
        ImmutableList.of(
            qualifiedName("name"),
            function("ABS", qualifiedName("age"))),
        querySpec.getSelectItems());
  }

  @Test
  void can_collect_aggregators_in_select_clause() {
    QuerySpecification querySpec = collect(
        "SELECT name, AVG(age), SUM(balance) FROM test GROUP BY name");

    assertEquals(
        ImmutableSet.of(
            aggregate("AVG", qualifiedName("age")),
            aggregate("SUM", qualifiedName("balance"))),
        querySpec.getAggregators());
  }

  @Test
  void can_collect_nested_aggregators_in_select_clause() {
    QuerySpecification querySpec = collect(
        "SELECT name, ABS(1 + AVG(age)) FROM test GROUP BY name");

    assertEquals(
        ImmutableSet.of(
            aggregate("AVG", qualifiedName("age"))),
        querySpec.getAggregators());
  }

  @Test
  void can_collect_alias_in_select_clause() {
    QuerySpecification querySpec = collect(
        "SELECT name AS n FROM test GROUP BY n");

    assertEquals(
        ImmutableMap.of("n", qualifiedName("name")),
        querySpec.getSelectItemsByAlias());
  }

  @Test
  void should_deduplicate_same_aggregators() {
    QuerySpecification querySpec = collect(
        "SELECT AVG(age), AVG(balance), AVG(age) FROM test GROUP BY name");

    assertEquals(
        ImmutableSet.of(
            aggregate("AVG", qualifiedName("age")),
            aggregate("AVG", qualifiedName("balance"))),
        querySpec.getAggregators());
  }

  private QuerySpecification collect(String query) {
    QuerySpecification querySpec = new QuerySpecification();
    querySpec.collect(parse(query));
    return querySpec;
  }

  private QuerySpecificationContext parse(String query) {
    OpenDistroSQLLexer lexer = new OpenDistroSQLLexer(new CaseInsensitiveCharStream(query));
    OpenDistroSQLParser parser = new OpenDistroSQLParser(new CommonTokenStream(lexer));
    parser.addErrorListener(new SyntaxAnalysisErrorListener());
    return parser.querySpecification();
  }

}