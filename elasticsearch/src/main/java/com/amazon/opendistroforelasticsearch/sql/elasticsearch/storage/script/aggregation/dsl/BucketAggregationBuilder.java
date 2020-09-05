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
import com.amazon.opendistroforelasticsearch.sql.expression.NamedExpression;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeValuesSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.TermsValuesSourceBuilder;

/**
 * Bucket Aggregation Builder.
 */
public class BucketAggregationBuilder {

  private final AggregationBuilderHelper<CompositeValuesSourceBuilder<?>> helper;

  public BucketAggregationBuilder(
      ExpressionSerializer serializer) {
    this.helper = new AggregationBuilderHelper<>(serializer);
  }

  /**
   * Build the list of CompositeValuesSourceBuilder.
   */
  public List<CompositeValuesSourceBuilder<?>> build(List<NamedExpression> expressions) {
    ImmutableList.Builder<CompositeValuesSourceBuilder<?>> resultBuilder =
        new ImmutableList.Builder<>();
    for (NamedExpression expression : expressions) {
      TermsValuesSourceBuilder valuesSourceBuilder =
          new TermsValuesSourceBuilder(expression.getName()).missingBucket(true);
      resultBuilder
          .add(helper.build(expression.getDelegated(), valuesSourceBuilder::field,
              valuesSourceBuilder::script));
    }
    return resultBuilder.build();
  }
}
