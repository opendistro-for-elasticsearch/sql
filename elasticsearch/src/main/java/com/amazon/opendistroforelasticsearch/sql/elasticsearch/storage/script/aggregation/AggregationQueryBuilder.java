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

import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.aggregation.dsl.BucketAggregationBuilder;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.aggregation.dsl.MetricAggregationBuilder;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.serialization.ExpressionSerializer;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.expression.NamedExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.NamedAggregator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;

/**
 * Build the AggregationBuilder from the list of {@link NamedAggregator}
 * and list of {@link NamedExpression}.
 */
@RequiredArgsConstructor
public class AggregationQueryBuilder extends ExpressionNodeVisitor<AggregationBuilder, Object> {

  /**
   * How many composite buckets should be returned.
   */
  public static final int AGGREGATION_BUCKET_SIZE = 1000;

  /**
   * Bucket Aggregation builder.
   */
  private final BucketAggregationBuilder bucketBuilder;

  /**
   * Metric Aggregation builder.
   */
  private final MetricAggregationBuilder metricBuilder;

  public AggregationQueryBuilder(
      ExpressionSerializer serializer) {
    this.bucketBuilder = new BucketAggregationBuilder(serializer);
    this.metricBuilder = new MetricAggregationBuilder(serializer);
  }

  /**
   * Build AggregationBuilder.
   */
  public List<AggregationBuilder> buildAggregationBuilder(List<NamedAggregator> namedAggregatorList,
                                                          List<NamedExpression> groupByList) {
    if (groupByList.isEmpty()) {
      // no bucket
      return ImmutableList
          .copyOf(metricBuilder.build(namedAggregatorList).getAggregatorFactories());
    } else {
      return Collections.singletonList(AggregationBuilders.composite("composite_buckets",
          bucketBuilder.build(groupByList))
          .subAggregations(metricBuilder.build(namedAggregatorList))
          .size(AGGREGATION_BUCKET_SIZE));
    }
  }

  /**
   * Build ElasticsearchExprValueFactory.
   */
  public Map<String, ExprType> buildTypeMapping(
      List<NamedAggregator> namedAggregatorList,
      List<NamedExpression> groupByList) {
    ImmutableMap.Builder<String, ExprType> builder = new ImmutableMap.Builder<>();
    namedAggregatorList.forEach(agg -> builder.put(agg.getName(), agg.type()));
    groupByList.forEach(group -> builder.put(group.getName(), group.type()));
    return builder.build();
  }
}
