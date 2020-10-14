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

import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.aggregate;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.alias;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.function;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.intLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.qualifiedName;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Aggregation;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.UnresolvedPlan;
import com.amazon.opendistroforelasticsearch.sql.common.antlr.CaseInsensitiveCharStream;
import com.amazon.opendistroforelasticsearch.sql.common.antlr.SyntaxAnalysisErrorListener;
import com.amazon.opendistroforelasticsearch.sql.exception.SemanticCheckException;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLLexer;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.QuerySpecificationContext;
import com.amazon.opendistroforelasticsearch.sql.sql.parser.context.QuerySpecification;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import org.antlr.v4.runtime.CommonTokenStream;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AstAggregationBuilderTest {

  @Test
  void can_build_group_by_clause() {
    assertThat(
        buildAggregation("SELECT state, AVG(age) FROM test GROUP BY state"),
        allOf(
            hasGroupByItems(alias("state", qualifiedName("state"))),
            hasAggregators(alias("AVG(age)", aggregate("AVG", qualifiedName("age"))))));
  }

  @Test
  void can_build_group_by_clause_with_scalar_expression() {
    assertThat(
        buildAggregation("SELECT ABS(age + 1) FROM test GROUP BY ABS(age + 1)"),
        allOf(
            hasGroupByItems(
                alias("ABS(+(age, 1))", function("ABS",
                    function("+",
                        qualifiedName("age"),
                        intLiteral(1))))),
            hasAggregators()));
  }

  @Test
  void can_build_group_by_clause_with_complicated_aggregators() {
    assertThat(
        buildAggregation("SELECT state, ABS(2 * AVG(age)) FROM test GROUP BY state"),
        allOf(
            hasGroupByItems(alias("state", qualifiedName("state"))),
            hasAggregators(alias("AVG(age)", aggregate("AVG", qualifiedName("age"))))));
  }

  @Test
  void can_build_group_by_clause_without_aggregators() {
    assertThat(
        buildAggregation("SELECT state FROM test GROUP BY state"),
        allOf(
            hasGroupByItems(alias("state", qualifiedName("state"))),
            hasAggregators()));
  }

  @Test
  void can_build_implicit_group_by_clause() {
    assertThat(
        buildAggregation("SELECT AVG(age), SUM(balance) FROM test"),
        allOf(
            hasGroupByItems(),
            hasAggregators(
                alias("AVG(age)", aggregate("AVG", qualifiedName("age"))),
                alias("SUM(balance)", aggregate("SUM", qualifiedName("balance"))))));
  }

  @Test
  void should_build_nothing_if_no_group_by_and_no_aggregators_in_select() {
    assertNull(buildAggregation("SELECT name FROM test"));
  }

  @Test
  void should_replace_group_by_alias_by_expression_in_select_clause() {
    assertThat(
        buildAggregation("SELECT state AS s, name FROM test GROUP BY s, name"),
        hasGroupByItems(alias("state", qualifiedName("state")),
            alias("name", qualifiedName("name"))));

    assertThat(
        buildAggregation("SELECT ABS(age) AS a FROM test GROUP BY a"),
        hasGroupByItems(alias("ABS(age)", function("ABS", qualifiedName("age")))));
  }

  @Test
  void should_replace_group_by_ordinal_by_expression_in_select_clause() {
    assertThat(
        buildAggregation("SELECT state AS s FROM test GROUP BY 1"),
        hasGroupByItems(alias("state", qualifiedName("state"))));

    assertThat(
        buildAggregation("SELECT name, ABS(age) AS a FROM test GROUP BY name, 2"),
        hasGroupByItems(
            alias("name", qualifiedName("name")),
            alias("ABS(age)", function("ABS", qualifiedName("age")))));
  }

  @Test
  void should_report_error_for_non_integer_ordinal_in_group_by() {
    SemanticCheckException error = assertThrows(SemanticCheckException.class, () ->
        buildAggregation("SELECT state AS s FROM test GROUP BY 1.5"));
    assertEquals(
        "Non-integer constant [1.5] found in GROUP BY clause",
        error.getMessage());
  }

  @Disabled("This validation is supposed to be in analyzing phase")
  @Test
  void should_report_error_for_mismatch_between_select_and_group_by_items() {
    SemanticCheckException error1 = assertThrows(SemanticCheckException.class, () ->
        buildAggregation("SELECT name FROM test GROUP BY state"));
    assertEquals(
        "Expression [name] that contains non-aggregated column is not present in group by clause",
        error1.getMessage());

    SemanticCheckException error2 = assertThrows(SemanticCheckException.class, () ->
        buildAggregation("SELECT ABS(name + 1) FROM test GROUP BY name"));
    assertEquals(
        "Expression [Function(funcName=ABS, funcArgs=[Function(funcName=+, "
            + "funcArgs=[name, Literal(value=1, type=INTEGER)])])] that contains "
            + "non-aggregated column is not present in group by clause",
        error2.getMessage());
  }

  @Test
  void should_report_error_for_non_aggregated_item_in_select_if_no_group_by() {
    SemanticCheckException error1 = assertThrows(SemanticCheckException.class, () ->
        buildAggregation("SELECT age, AVG(balance) FROM tests"));
    assertEquals(
        "Explicit GROUP BY clause is required because expression [age] "
            + "contains non-aggregated column",
        error1.getMessage());

    SemanticCheckException error2 = assertThrows(SemanticCheckException.class, () ->
        buildAggregation("SELECT ABS(age + 1), AVG(balance) FROM tests"));
    assertEquals(
        "Explicit GROUP BY clause is required because expression [ABS(+(age, 1))] "
            + "contains non-aggregated column",
        error2.getMessage());
  }

  @Test
  void should_report_error_for_group_by_ordinal_out_of_bound_of_select_list() {
    SemanticCheckException error1 = assertThrows(SemanticCheckException.class, () ->
        buildAggregation("SELECT age, AVG(balance) FROM tests GROUP BY 0"));
    assertEquals("Group by ordinal [0] is out of bound of select item list", error1.getMessage());

    SemanticCheckException error2 = assertThrows(SemanticCheckException.class, () ->
        buildAggregation("SELECT age, AVG(balance) FROM tests GROUP BY 3"));
    assertEquals("Group by ordinal [3] is out of bound of select item list", error2.getMessage());
  }

  private Matcher<UnresolvedPlan> hasGroupByItems(UnresolvedExpression... exprs) {
    return featureValueOf("groupByItems", Aggregation::getGroupExprList, exprs);
  }

  private Matcher<UnresolvedPlan> hasAggregators(UnresolvedExpression... exprs) {
    return featureValueOf("aggregators", Aggregation::getAggExprList, exprs);
  }

  private Matcher<UnresolvedPlan> featureValueOf(String name,
                                                 Function<Aggregation,
                                                     List<UnresolvedExpression>> getter,
                                                 UnresolvedExpression... exprs) {
    Matcher<List<UnresolvedExpression>> subMatcher =
        (exprs.length == 0) ? equalTo(emptyList()) : equalTo(Arrays.asList(exprs));
    return new FeatureMatcher<UnresolvedPlan, List<UnresolvedExpression>>(subMatcher, name, "") {
      @Override
      protected List<UnresolvedExpression> featureValueOf(UnresolvedPlan agg) {
        return getter.apply((Aggregation) agg);
      }
    };
  }

  private UnresolvedPlan buildAggregation(String sql) {
    QuerySpecificationContext query = parse(sql);
    QuerySpecification querySpec = collect(query, sql);
    AstAggregationBuilder aggBuilder = new AstAggregationBuilder(querySpec);
    return aggBuilder.visit(query.fromClause().groupByClause());
  }

  private QuerySpecification collect(QuerySpecificationContext query, String sql) {
    QuerySpecification querySpec = new QuerySpecification();
    querySpec.collect(query, sql);
    return querySpec;
  }

  private QuerySpecificationContext parse(String query) {
    OpenDistroSQLLexer lexer = new OpenDistroSQLLexer(new CaseInsensitiveCharStream(query));
    OpenDistroSQLParser parser = new OpenDistroSQLParser(new CommonTokenStream(lexer));
    parser.addErrorListener(new SyntaxAnalysisErrorListener());
    return parser.querySpecification();
  }

}