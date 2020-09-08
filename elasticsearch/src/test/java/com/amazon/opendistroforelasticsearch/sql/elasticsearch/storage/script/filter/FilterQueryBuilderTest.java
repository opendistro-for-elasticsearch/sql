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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.filter;

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.type.ElasticsearchDataType.ES_TEXT_KEYWORD;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.literal;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.ref;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.serialization.ExpressionSerializer;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.config.ExpressionConfig;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class FilterQueryBuilderTest {

  private final DSL dsl = new ExpressionConfig().dsl(new ExpressionConfig().functionRepository());

  @Mock
  private ExpressionSerializer serializer;

  private FilterQueryBuilder filterQueryBuilder;

  @BeforeEach
  void set_up() {
    filterQueryBuilder = new FilterQueryBuilder(serializer);
  }

  @Test
  void should_build_term_query_for_equality_expression() {
    assertEquals(
        "{\n"
            + "  \"term\" : {\n"
            + "    \"name\" : {\n"
            + "      \"value\" : \"John\",\n"
            + "      \"boost\" : 1.0\n"
            + "    }\n"
            + "  }\n"
            + "}",
        buildQuery(
            dsl.equal(
                ref("name", STRING), literal("John"))));
  }

  @Test
  void should_build_range_query_for_comparison_expression() {
    Expression[] params = {ref("age", INTEGER), literal(30)};
    Map<Expression, Object[]> ranges = ImmutableMap.of(
        dsl.less(params), new Object[]{null, 30, true, false},
        dsl.greater(params), new Object[]{30, null, false, true},
        dsl.lte(params), new Object[]{null, 30, true, true},
        dsl.gte(params), new Object[]{30, null, true, true});

    ranges.forEach((expr, range) ->
        assertEquals(
            "{\n"
                + "  \"range\" : {\n"
                + "    \"age\" : {\n"
                + "      \"from\" : " + range[0] + ",\n"
                + "      \"to\" : " + range[1] + ",\n"
                + "      \"include_lower\" : " + range[2] + ",\n"
                + "      \"include_upper\" : " + range[3] + ",\n"
                + "      \"boost\" : 1.0\n"
                + "    }\n"
                + "  }\n"
                + "}",
            buildQuery(expr)));
  }

  @Test
  void should_build_wildcard_query_for_like_expression() {
    assertEquals(
        "{\n"
            + "  \"wildcard\" : {\n"
            + "    \"name\" : {\n"
            + "      \"wildcard\" : \"*John?\",\n"
            + "      \"boost\" : 1.0\n"
            + "    }\n"
            + "  }\n"
            + "}",
        buildQuery(
            dsl.like(
                ref("name", STRING), literal("%John_"))));
  }

  @Test
  void should_build_script_query_for_function_expression() {
    doAnswer(invocation -> {
      Expression expr = invocation.getArgument(0);
      return expr.toString();
    }).when(serializer).serialize(any());

    assertEquals(
        "{\n"
            + "  \"script\" : {\n"
            + "    \"script\" : {\n"
            + "      \"source\" : \"=(abs(age), 30)\",\n"
            + "      \"lang\" : \"opendistro_expression\"\n"
            + "    },\n"
            + "    \"boost\" : 1.0\n"
            + "  }\n"
            + "}",
        buildQuery(
            dsl.equal(
                dsl.abs(ref("age", INTEGER)), literal(30))));
  }

  @Test
  void should_build_script_query_for_comparison_between_fields() {
    doAnswer(invocation -> {
      Expression expr = invocation.getArgument(0);
      return expr.toString();
    }).when(serializer).serialize(any());

    assertEquals(
        "{\n"
            + "  \"script\" : {\n"
            + "    \"script\" : {\n"
            + "      \"source\" : \"=(age1, age2)\",\n"
            + "      \"lang\" : \"opendistro_expression\"\n"
            + "    },\n"
            + "    \"boost\" : 1.0\n"
            + "  }\n"
            + "}",
        buildQuery(
            dsl.equal(
                ref("age1", INTEGER), ref("age2", INTEGER))));
  }

  @Test
  void should_build_bool_query_for_and_or_expression() {
    String[] names = { "filter", "should" };
    FunctionExpression expr1 = dsl.equal(ref("name", STRING), literal("John"));
    FunctionExpression expr2 = dsl.equal(ref("age", INTEGER), literal(30));
    Expression[] exprs = {
        dsl.and(expr1, expr2),
        dsl.or(expr1, expr2)
    };

    for (int i = 0; i < names.length; i++) {
      assertEquals(
          "{\n"
              + "  \"bool\" : {\n"
              + "    \"" + names[i] + "\" : [\n"
              + "      {\n"
              + "        \"term\" : {\n"
              + "          \"name\" : {\n"
              + "            \"value\" : \"John\",\n"
              + "            \"boost\" : 1.0\n"
              + "          }\n"
              + "        }\n"
              + "      },\n"
              + "      {\n"
              + "        \"term\" : {\n"
              + "          \"age\" : {\n"
              + "            \"value\" : 30,\n"
              + "            \"boost\" : 1.0\n"
              + "          }\n"
              + "        }\n"
              + "      }\n"
              + "    ],\n"
              + "    \"adjust_pure_negative\" : true,\n"
              + "    \"boost\" : 1.0\n"
              + "  }\n"
              + "}",
          buildQuery(exprs[i]));
    }
  }

  @Test
  void should_build_bool_query_for_not_expression() {
    assertEquals(
        "{\n"
            + "  \"bool\" : {\n"
            + "    \"must_not\" : [\n"
            + "      {\n"
            + "        \"term\" : {\n"
            + "          \"age\" : {\n"
            + "            \"value\" : 30,\n"
            + "            \"boost\" : 1.0\n"
            + "          }\n"
            + "        }\n"
            + "      }\n"
            + "    ],\n"
            + "    \"adjust_pure_negative\" : true,\n"
            + "    \"boost\" : 1.0\n"
            + "  }\n"
            + "}",
        buildQuery(
            dsl.not(
                dsl.equal(
                    ref("age", INTEGER), literal(30)))));
  }

  @Test
  void should_use_keyword_for_multi_field_in_equality_expression() {
    assertEquals(
        "{\n"
            + "  \"term\" : {\n"
            + "    \"name.keyword\" : {\n"
            + "      \"value\" : \"John\",\n"
            + "      \"boost\" : 1.0\n"
            + "    }\n"
            + "  }\n"
            + "}",
        buildQuery(
            dsl.equal(
                ref("name", ES_TEXT_KEYWORD), literal("John"))));
  }

  @Test
  void should_use_keyword_for_multi_field_in_like_expression() {
    assertEquals(
        "{\n"
            + "  \"wildcard\" : {\n"
            + "    \"name.keyword\" : {\n"
            + "      \"wildcard\" : \"John*\",\n"
            + "      \"boost\" : 1.0\n"
            + "    }\n"
            + "  }\n"
            + "}",
        buildQuery(
            dsl.like(
                ref("name", ES_TEXT_KEYWORD), literal("John%"))));
  }

  private String buildQuery(Expression expr) {
    return filterQueryBuilder.build(expr).toString();
  }

}