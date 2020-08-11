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
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.literal;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.ref;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.serialization.ExpressionSerializer;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.config.ExpressionConfig;
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
    doAnswer(invocation -> {
      Expression expr = invocation.getArgument(0);
      return expr.toString();
    }).when(serializer).serialize(any());

    filterQueryBuilder = new FilterQueryBuilder(serializer);
  }

  @Test
  void should_return_null_if_exception() {
    reset(serializer);
    when(serializer.serialize(any())).thenThrow(IllegalStateException.class);

    assertNull(
        filterQueryBuilder.build(
            dsl.equal(ref("age", INTEGER), literal(30))));
  }

  @Test
  void can_build_query_for_comparison_expression() {
    assertEquals(
        "{\n"
            + "  \"script\" : {\n"
            + "    \"script\" : {\n"
            + "      \"source\" : \"age > 30\",\n"
            + "      \"lang\" : \"opendistro_expression\"\n"
            + "    },\n"
            + "    \"boost\" : 1.0\n"
            + "  }\n"
            + "}",
        buildQuery(
            dsl.greater(
                ref("age", INTEGER), literal(30))));
  }

  @Test
  void can_build_query_for_and_or_expression() {
    String[] names = { "must", "should" };
    Expression[] exprs = {
        dsl.and(
            dsl.equal(ref("name", STRING), literal("John")),
            dsl.greater(ref("age", INTEGER), literal(30))),
        dsl.or(
            dsl.equal(ref("name", STRING), literal("John")),
            dsl.greater(ref("age", INTEGER), literal(30)))
    };

    for (int i = 0; i < names.length; i++) {
      assertEquals(
          "{\n"
              + "  \"bool\" : {\n"
              + "    \"" + names[i] + "\" : [\n"
              + "      {\n"
              + "        \"script\" : {\n"
              + "          \"script\" : {\n"
              + "            \"source\" : \"name = \\\"John\\\"\",\n"
              + "            \"lang\" : \"opendistro_expression\"\n"
              + "          },\n"
              + "          \"boost\" : 1.0\n"
              + "        }\n"
              + "      },\n"
              + "      {\n"
              + "        \"script\" : {\n"
              + "          \"script\" : {\n"
              + "            \"source\" : \"age > 30\",\n"
              + "            \"lang\" : \"opendistro_expression\"\n"
              + "          },\n"
              + "          \"boost\" : 1.0\n"
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
  void can_build_query_for_or_expression() {
    assertEquals(
        "{\n"
            + "  \"bool\" : {\n"
            + "    \"must_not\" : [\n"
            + "      {\n"
            + "        \"script\" : {\n"
            + "          \"script\" : {\n"
            + "            \"source\" : \"age > 30\",\n"
            + "            \"lang\" : \"opendistro_expression\"\n"
            + "          },\n"
            + "          \"boost\" : 1.0\n"
            + "        }\n"
            + "      }\n"
            + "    ],\n"
            + "    \"adjust_pure_negative\" : true,\n"
            + "    \"boost\" : 1.0\n"
            + "  }\n"
            + "}",
        buildQuery(
            dsl.not(
                dsl.greater(
                    ref("age", INTEGER), literal(30)))));
  }

  private String buildQuery(Expression expr) {
    return filterQueryBuilder.build(expr).toString();
  }

}