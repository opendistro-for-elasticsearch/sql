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

import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.serialization.ExpressionSerializer;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.NamedAggregator;
import java.util.List;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.AggregatorFactories;

/**
 * Todo.
 */
public class MetricAggregationBuilder
    extends ExpressionNodeVisitor<AggregationBuilder, Object> {

  private final ValuesSourceAggregationMaker maker;

  public MetricAggregationBuilder(
      ExpressionSerializer serializer) {
    this.maker = new ValuesSourceAggregationMaker(serializer);
  }

  /**
   * Todo.
   * @param aggregatorList aggregator list
   * @return
   */
  public AggregatorFactories.Builder build(List<NamedAggregator> aggregatorList) {
    AggregatorFactories.Builder builder = new AggregatorFactories.Builder();
    for (NamedAggregator aggregator : aggregatorList) {
      builder.addAggregator(aggregator.accept(this, null));
    }
    return builder;
  }

  @Override
  public AggregationBuilder visitNamedAggregator(NamedAggregator node,
                                                 Object context) {
    Expression expression = node.getArguments().get(0);
    String name = node.getName();

    switch (node.getFunctionName().getFunctionName()) {
      case "avg":
        return maker.build(AggregationBuilders.avg(name), expression);
      case "sum":
        return maker.build(AggregationBuilders.sum(name), expression);
      case "count":
        return maker.build(AggregationBuilders.count(name), expression);
      default:
        throw new RuntimeException("visitNamedAggregator exception");
    }
  }
}
