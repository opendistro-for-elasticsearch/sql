/*
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

package com.amazon.opendistroforelasticsearch.sql.planner.logical;

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.aggregation;
import static com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlanDSL.relation;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazon.opendistroforelasticsearch.sql.analysis.AnalyzerTestBase;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.Aggregator;
import com.amazon.opendistroforelasticsearch.sql.expression.config.ExpressionConfig;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Configuration
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ExpressionConfig.class, AnalyzerTestBase.class})
public class LogicalAggregationTest extends AnalyzerTestBase {

  @Test
  void has_filter() {
    Aggregator avgAggregator = dsl.avg(DSL.ref("intV", INTEGER))
        .condition(dsl.greater(DSL.ref("intV", INTEGER), DSL.literal(1)));
    LogicalAggregation agg = (LogicalAggregation) aggregation(
        relation("test"),
        ImmutableList.of(
            DSL.named("AVG(intV) FILTER(WHERE intV > 1)", avgAggregator)), emptyList());
    assertTrue(agg.hasFilterFunction());
  }

  @Test
  void no_filter() {
    Aggregator avgAggregator = dsl.avg(DSL.ref("intV", INTEGER));
    LogicalAggregation agg = (LogicalAggregation) aggregation(
        relation("test"),
        ImmutableList.of(
            DSL.named("AVG(intV) FILTER(WHERE intV > 1)", avgAggregator)), emptyList());
    assertFalse(agg.hasFilterFunction());
  }
}
