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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.response;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.composite.InternalComposite;
import org.elasticsearch.search.aggregations.metrics.NumericMetricsAggregation;

public class CompositeAggregationParser {

  @VisibleForTesting
  public static List<Map<String, Object>> flatten(Aggregations aggregations) {
    List<Aggregation> aggregationList = aggregations.asList();
    ImmutableList.Builder<Map<String, Object>> builder = new ImmutableList.Builder<>();

    for (Aggregation aggregation : aggregationList) {
      if (aggregation instanceof InternalComposite) {
        for (InternalComposite.InternalBucket bucket :
            ((InternalComposite) aggregation).getBuckets()) {
          builder.add(CompositeAggregationParser.parse(bucket));
        }
      } else {
        throw new RuntimeException("unsupported aggregation type " + aggregation.getType());
      }

    }
    return builder.build();
  }

  public static Map<String, Object> parse(InternalComposite.InternalBucket bucket) {
    ImmutableMap.Builder<String, Object> mapBuilder = new ImmutableMap.Builder<>();
    // The NodeClient return InternalComposite

    // build <groupKey, value> pair
    mapBuilder.putAll(bucket.getKey());

    // build <aggKey, value> pair
    mapBuilder.putAll(parseAggregation(bucket.getAggregations()));

    return mapBuilder.build();
  }

  private static Map<String, Object> parseAggregation(Aggregations aggregations) {
    Map<String, Object> resultMap = new HashMap<>();
    for (Aggregation aggregation : aggregations.asList()) {
      if (aggregation instanceof NumericMetricsAggregation.SingleValue) {
        resultMap.put(
            aggregation.getName(), ((NumericMetricsAggregation.SingleValue) aggregation).value());
      } else {
        throw new RuntimeException("unsupported aggregation type " + aggregation.getType());
      }
    }
    return resultMap;
  }
}
