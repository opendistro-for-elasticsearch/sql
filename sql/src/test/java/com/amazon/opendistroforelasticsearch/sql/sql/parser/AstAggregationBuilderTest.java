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
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.qualifiedName;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;

import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Aggregation;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.UnresolvedPlan;
import com.amazon.opendistroforelasticsearch.sql.common.antlr.CaseInsensitiveCharStream;
import com.amazon.opendistroforelasticsearch.sql.common.antlr.SyntaxAnalysisErrorListener;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLLexer;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.QuerySpecificationContext;
import com.amazon.opendistroforelasticsearch.sql.sql.parser.context.QuerySpecification;
import java.util.Arrays;
import java.util.List;
import org.antlr.v4.runtime.CommonTokenStream;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AstAggregationBuilderTest {

  @Test
  void can_build_explicit_group_by_clause() {
    assertThat(
        buildAggregation("SELECT state, AVG(age) FROM test GROUP BY state"),
        allOf(
            hasGroupByItems(qualifiedName("state")),
            hasAggregators(aggregate("AVG", qualifiedName("age")))));
  }

  @Test
  void can_build_implicit_group_by_clause() {
    assertThat(
        buildAggregation("SELECT AVG(age) FROM test"),
        allOf(
            hasGroupByItems(),
            hasAggregators(aggregate("AVG", qualifiedName("age")))));
  }

  private Matcher<UnresolvedPlan> hasGroupByItems(UnresolvedExpression... exprs) {
    String name = "groupByItems";
    Matcher<List<UnresolvedExpression>> subMatcher =
        (exprs.length == 0) ? equalTo(emptyList()) : equalTo(Arrays.asList(exprs));
    return new FeatureMatcher<UnresolvedPlan, List<UnresolvedExpression>>(subMatcher, name, "") {
      @Override
      protected List<UnresolvedExpression> featureValueOf(UnresolvedPlan agg) {
        return ((Aggregation) agg).getGroupExprList();
      }
    };
  }

  private Matcher<UnresolvedPlan> hasAggregators(UnresolvedExpression... exprs) {
    String name = "aggregators";
    Matcher<List<UnresolvedExpression>> subMatcher = equalTo(Arrays.asList(exprs));
    return new FeatureMatcher<UnresolvedPlan, List<UnresolvedExpression>>(subMatcher, name, "") {
      @Override
      protected List<UnresolvedExpression> featureValueOf(UnresolvedPlan agg) {
        return ((Aggregation) agg).getAggExprList();
      }
    };
  }

  private UnresolvedPlan buildAggregation(String sql) {
    QuerySpecificationContext query = parse(sql);
    QuerySpecification querySpec = collect(query);
    AstAggregationBuilder aggBuilder = new AstAggregationBuilder(querySpec);
    return aggBuilder.visit(query.fromClause().groupByClause());
  }

  private QuerySpecification collect(QuerySpecificationContext query) {
    QuerySpecification querySpec = new QuerySpecification();
    querySpec.collect(query);
    return querySpec;
  }

  private QuerySpecificationContext parse(String query) {
    OpenDistroSQLLexer lexer = new OpenDistroSQLLexer(new CaseInsensitiveCharStream(query));
    OpenDistroSQLParser parser = new OpenDistroSQLParser(new CommonTokenStream(lexer));
    parser.addErrorListener(new SyntaxAnalysisErrorListener());
    return parser.querySpecification();
  }

}