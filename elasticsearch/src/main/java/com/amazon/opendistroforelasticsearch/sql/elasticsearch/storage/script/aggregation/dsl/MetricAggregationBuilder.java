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

import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.serialization.ExpressionSerializer;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.expression.LiteralExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.NamedAggregator;
import java.util.List;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.support.ValuesSourceAggregationBuilder;

/**
 * Build the Metric Aggregation from {@link NamedAggregator}.
 */
public class MetricAggregationBuilder
    extends ExpressionNodeVisitor<AggregationBuilder, Object> {

  private final AggregationBuilderHelper<ValuesSourceAggregationBuilder<?>> helper;

  public MetricAggregationBuilder(
      ExpressionSerializer serializer) {
    this.helper = new AggregationBuilderHelper<>(serializer);
  }

  /**
   * Build AggregatorFactories.Builder from {@link NamedAggregator}.
   *
   * @param aggregatorList aggregator list
   * @return AggregatorFactories.Builder
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
        return make(AggregationBuilders.avg(name), expression);
      case "sum":
        return make(AggregationBuilders.sum(name), expression);
      case "count":
        return make(AggregationBuilders.count(name), replaceStarOrLiteral(expression));
      case "min":
        return make(AggregationBuilders.min(name), expression);
      case "max":
        return make(AggregationBuilders.max(name), expression);
      default:
        throw new IllegalStateException(
            String.format("unsupported aggregator %s", node.getFunctionName().getFunctionName()));
    }
  }

  private ValuesSourceAggregationBuilder<?> make(ValuesSourceAggregationBuilder<?> builder,
                                                  Expression expression) {
    return helper.build(expression, builder::field, builder::script);
  }

  /**
   * Replace star or literal with Elasticsearch metadata field "_index". Because:
   * 1) Analyzer already converts * to string literal, literal check here can handle
   *    both COUNT(*) and COUNT(1).
   * 2) Value count aggregation on _index counts all docs (after filter), therefore
   *    it has same semantics as COUNT(*) or COUNT(1).
   * @param countArg count function argument
   * @return Reference to _index if literal, otherwise return original argument expression
   */
  private Expression replaceStarOrLiteral(Expression countArg) {
    if (countArg instanceof LiteralExpression) {
      return new ReferenceExpression("_index", INTEGER);
    }
    return countArg;
  }

}
