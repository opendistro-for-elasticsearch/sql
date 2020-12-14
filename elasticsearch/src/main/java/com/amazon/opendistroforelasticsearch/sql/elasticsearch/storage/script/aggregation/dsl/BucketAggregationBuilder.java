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
import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeValuesSourceBuilder;
import org.elasticsearch.search.aggregations.bucket.composite.TermsValuesSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

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
  public List<CompositeValuesSourceBuilder<?>> build(
      List<Pair<NamedExpression, SortOrder>> groupList) {
    ImmutableList.Builder<CompositeValuesSourceBuilder<?>> resultBuilder =
        new ImmutableList.Builder<>();
    for (Pair<NamedExpression, SortOrder> groupPair : groupList) {
      TermsValuesSourceBuilder valuesSourceBuilder =
          new TermsValuesSourceBuilder(groupPair.getLeft().getNameOrAlias())
              .missingBucket(true)
              .order(groupPair.getRight());
      resultBuilder
          .add(helper.build(groupPair.getLeft().getDelegated(), valuesSourceBuilder::field,
              valuesSourceBuilder::script));
    }
    return resultBuilder.build();
  }
}
