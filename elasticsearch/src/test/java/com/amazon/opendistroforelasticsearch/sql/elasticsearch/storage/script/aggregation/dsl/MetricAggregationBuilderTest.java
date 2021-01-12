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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.aggregation.dsl;

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.literal;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.named;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.ref;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.serialization.ExpressionSerializer;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.AvgAggregator;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.CountAggregator;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.MaxAggregator;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.MinAggregator;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.NamedAggregator;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.SumAggregator;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionName;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
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
class MetricAggregationBuilderTest {

  @Mock
  private ExpressionSerializer serializer;

  @Mock
  private NamedAggregator aggregator;

  private MetricAggregationBuilder aggregationBuilder;

  @BeforeEach
  void set_up() {
    aggregationBuilder = new MetricAggregationBuilder(serializer);
  }

  @Test
  void should_build_avg_aggregation() {
    assertEquals(
        "{\n"
            + "  \"avg(age)\" : {\n"
            + "    \"avg\" : {\n"
            + "      \"field\" : \"age\"\n"
            + "    }\n"
            + "  }\n"
            + "}",
        buildQuery(
            Arrays.asList(
                named("avg(age)",
                    new AvgAggregator(Arrays.asList(ref("age", INTEGER)), INTEGER)))));
  }

  @Test
  void should_build_sum_aggregation() {
    assertEquals(
        "{\n"
            + "  \"sum(age)\" : {\n"
            + "    \"sum\" : {\n"
            + "      \"field\" : \"age\"\n"
            + "    }\n"
            + "  }\n"
            + "}",
        buildQuery(
            Arrays.asList(
                named("sum(age)",
                    new SumAggregator(Arrays.asList(ref("age", INTEGER)), INTEGER)))));
  }

  @Test
  void should_build_count_aggregation() {
    assertEquals(
        "{\n"
            + "  \"count(age)\" : {\n"
            + "    \"value_count\" : {\n"
            + "      \"field\" : \"age\"\n"
            + "    }\n"
            + "  }\n"
            + "}",
        buildQuery(
            Arrays.asList(
                named("count(age)",
                    new CountAggregator(Arrays.asList(ref("age", INTEGER)), INTEGER)))));
  }

  @Test
  void should_build_count_star_aggregation() {
    assertEquals(
        "{\n"
            + "  \"count(*)\" : {\n"
            + "    \"value_count\" : {\n"
            + "      \"field\" : \"_index\"\n"
            + "    }\n"
            + "  }\n"
            + "}",
        buildQuery(
            Arrays.asList(
                named("count(*)",
                    new CountAggregator(Arrays.asList(literal("*")), INTEGER)))));
  }

  @Test
  void should_build_count_other_literal_aggregation() {
    assertEquals(
        "{\n"
            + "  \"count(1)\" : {\n"
            + "    \"value_count\" : {\n"
            + "      \"field\" : \"_index\"\n"
            + "    }\n"
            + "  }\n"
            + "}",
        buildQuery(
            Arrays.asList(
                named("count(1)",
                    new CountAggregator(Arrays.asList(literal(1)), INTEGER)))));
  }

  @Test
  void should_build_min_aggregation() {
    assertEquals(
        "{\n"
            + "  \"min(age)\" : {\n"
            + "    \"min\" : {\n"
            + "      \"field\" : \"age\"\n"
            + "    }\n"
            + "  }\n"
            + "}",
        buildQuery(
            Arrays.asList(
                named("min(age)",
                    new MinAggregator(Arrays.asList(ref("age", INTEGER)), INTEGER)))));
  }

  @Test
  void should_build_max_aggregation() {
    assertEquals(
        "{\n"
            + "  \"max(age)\" : {\n"
            + "    \"max\" : {\n"
            + "      \"field\" : \"age\"\n"
            + "    }\n"
            + "  }\n"
            + "}",
        buildQuery(
            Arrays.asList(
                named("max(age)",
                    new MaxAggregator(Arrays.asList(ref("age", INTEGER)), INTEGER)))));
  }

  @Test
  void should_throw_exception_for_unsupported_aggregator() {
    when(aggregator.getFunctionName()).thenReturn(new FunctionName("unsupported_agg"));
    when(aggregator.getArguments()).thenReturn(Arrays.asList(ref("age", INTEGER)));

    IllegalStateException exception =
        assertThrows(IllegalStateException.class,
            () -> buildQuery(Arrays.asList(named("unsupported_agg(age)", aggregator))));
    assertEquals("unsupported aggregator unsupported_agg", exception.getMessage());
  }

  @Test
  void should_throw_exception_for_unsupported_exception() {
    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> buildQuery(Arrays.asList(
            named("count(age)",
                new CountAggregator(Arrays.asList(named("age", ref("age", INTEGER))), INTEGER)))));
    assertEquals(
        "metric aggregation doesn't support expression age",
        exception.getMessage());
  }

  @SneakyThrows
  private String buildQuery(List<NamedAggregator> namedAggregatorList) {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readTree(
        aggregationBuilder.build(namedAggregatorList).toString())
        .toPrettyString();
  }
}