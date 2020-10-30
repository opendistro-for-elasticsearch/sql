/*
 *
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.aggregation;

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.type.ElasticsearchDataType.ES_TEXT_KEYWORD;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.named;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.ref;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.serialization.ExpressionSerializer;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.NamedExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.AvgAggregator;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.NamedAggregator;
import com.amazon.opendistroforelasticsearch.sql.expression.config.ExpressionConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class AggregationQueryBuilderTest {

  private final DSL dsl = new ExpressionConfig().dsl(new ExpressionConfig().functionRepository());

  @Mock
  private ExpressionSerializer serializer;

  private AggregationQueryBuilder queryBuilder;

  @BeforeEach
  void set_up() {
    queryBuilder = new AggregationQueryBuilder(serializer);
  }

  @Test
  void should_build_composite_aggregation_for_field_reference() {
    assertEquals(
        "{\n"
            + "  \"composite_buckets\" : {\n"
            + "    \"composite\" : {\n"
            + "      \"size\" : 1000,\n"
            + "      \"sources\" : [ {\n"
            + "        \"name\" : {\n"
            + "          \"terms\" : {\n"
            + "            \"field\" : \"name\",\n"
            + "            \"missing_bucket\" : true,\n"
            + "            \"order\" : \"asc\"\n"
            + "          }\n"
            + "        }\n"
            + "      } ]\n"
            + "    },\n"
            + "    \"aggregations\" : {\n"
            + "      \"avg(age)\" : {\n"
            + "        \"avg\" : {\n"
            + "          \"field\" : \"age\"\n"
            + "        }\n"
            + "      }\n"
            + "    }\n"
            + "  }\n"
            + "}",
        buildQuery(
            Arrays.asList(
                named("avg(age)", new AvgAggregator(Arrays.asList(ref("age", INTEGER)), INTEGER))),
            Arrays.asList(named("name", ref("name", STRING)))));
  }

  @Test
  void should_build_type_mapping_for_field_reference() {
    assertThat(
        buildTypeMapping(Arrays.asList(
            named("avg(age)", new AvgAggregator(Arrays.asList(ref("age", INTEGER)), INTEGER))),
            Arrays.asList(named("name", ref("name", STRING)))),
        containsInAnyOrder(
            map("avg(age)", INTEGER),
            map("name", STRING)
        ));
  }

  @Test
  void should_build_composite_aggregation_for_field_reference_of_keyword() {
    assertEquals(
        "{\n"
            + "  \"composite_buckets\" : {\n"
            + "    \"composite\" : {\n"
            + "      \"size\" : 1000,\n"
            + "      \"sources\" : [ {\n"
            + "        \"name\" : {\n"
            + "          \"terms\" : {\n"
            + "            \"field\" : \"name.keyword\",\n"
            + "            \"missing_bucket\" : true,\n"
            + "            \"order\" : \"asc\"\n"
            + "          }\n"
            + "        }\n"
            + "      } ]\n"
            + "    },\n"
            + "    \"aggregations\" : {\n"
            + "      \"avg(age)\" : {\n"
            + "        \"avg\" : {\n"
            + "          \"field\" : \"age\"\n"
            + "        }\n"
            + "      }\n"
            + "    }\n"
            + "  }\n"
            + "}",
        buildQuery(
            Arrays.asList(
                named("avg(age)", new AvgAggregator(Arrays.asList(ref("age", INTEGER)), INTEGER))),
            Arrays.asList(named("name", ref("name", ES_TEXT_KEYWORD)))));
  }

  @Test
  void should_build_type_mapping_for_field_reference_of_keyword() {
    assertThat(
        buildTypeMapping(Arrays.asList(
            named("avg(age)", new AvgAggregator(Arrays.asList(ref("age", INTEGER)), INTEGER))),
            Arrays.asList(named("name", ref("name", ES_TEXT_KEYWORD)))),
        containsInAnyOrder(
            map("avg(age)", INTEGER),
            map("name", ES_TEXT_KEYWORD)
        ));
  }

  @Test
  void should_build_composite_aggregation_for_expression() {
    doAnswer(invocation -> {
      Expression expr = invocation.getArgument(0);
      return expr.toString();
    }).when(serializer).serialize(any());
    assertEquals(
        "{\n"
            + "  \"composite_buckets\" : {\n"
            + "    \"composite\" : {\n"
            + "      \"size\" : 1000,\n"
            + "      \"sources\" : [ {\n"
            + "        \"age\" : {\n"
            + "          \"terms\" : {\n"
            + "            \"script\" : {\n"
            + "              \"source\" : \"asin(age)\",\n"
            + "              \"lang\" : \"opendistro_expression\"\n"
            + "            },\n"
            + "            \"missing_bucket\" : true,\n"
            + "            \"order\" : \"asc\"\n"
            + "          }\n"
            + "        }\n"
            + "      } ]\n"
            + "    },\n"
            + "    \"aggregations\" : {\n"
            + "      \"avg(balance)\" : {\n"
            + "        \"avg\" : {\n"
            + "          \"script\" : {\n"
            + "            \"source\" : \"abs(balance)\",\n"
            + "            \"lang\" : \"opendistro_expression\"\n"
            + "          }\n"
            + "        }\n"
            + "      }\n"
            + "    }\n"
            + "  }\n"
            + "}",
        buildQuery(
            Arrays.asList(
                named("avg(balance)", new AvgAggregator(
                    Arrays.asList(dsl.abs(ref("balance", INTEGER))), INTEGER))),
            Arrays.asList(named("age", dsl.asin(ref("age", INTEGER))))));
  }

  @Test
  void should_build_type_mapping_for_expression() {
    assertThat(
        buildTypeMapping(Arrays.asList(
            named("avg(balance)", new AvgAggregator(
                Arrays.asList(dsl.abs(ref("balance", INTEGER))), INTEGER))),
            Arrays.asList(named("age", dsl.asin(ref("age", INTEGER))))),
        containsInAnyOrder(
            map("avg(balance)", INTEGER),
            map("age", DOUBLE)
        ));
  }

  @Test
  void should_build_aggregation_without_bucket() {
    assertEquals(
        "{\n"
            + "  \"avg(balance)\" : {\n"
            + "    \"avg\" : {\n"
            + "      \"field\" : \"balance\"\n"
            + "    }\n"
            + "  }\n"
            + "}",
        buildQuery(
            Arrays.asList(
                named("avg(balance)", new AvgAggregator(
                    Arrays.asList(ref("balance", INTEGER)), INTEGER))),
            Collections.emptyList()));
  }

  @Test
  void should_build_type_mapping_without_bucket() {
    assertThat(
        buildTypeMapping(Arrays.asList(
            named("avg(balance)", new AvgAggregator(
                Arrays.asList(ref("balance", INTEGER)), INTEGER))),
            Collections.emptyList()),
        containsInAnyOrder(
            map("avg(balance)", INTEGER)
        ));
  }

  @SneakyThrows
  private String buildQuery(List<NamedAggregator> namedAggregatorList,
                            List<NamedExpression> groupByList) {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readTree(
        queryBuilder.buildAggregationBuilder(namedAggregatorList, groupByList).get(0).toString())
        .toPrettyString();
  }

  private Set<Map.Entry<String, ExprType>> buildTypeMapping(
      List<NamedAggregator> namedAggregatorList,
      List<NamedExpression> groupByList) {
    return queryBuilder.buildTypeMapping(namedAggregatorList, groupByList).entrySet();
  }

  private Map.Entry<String, ExprType> map(String name, ExprType type) {
    return new AbstractMap.SimpleEntry<String, ExprType>(name, type);
  }
}