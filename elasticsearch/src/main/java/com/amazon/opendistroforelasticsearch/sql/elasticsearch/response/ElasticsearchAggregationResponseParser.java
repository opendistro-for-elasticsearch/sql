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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.experimental.UtilityClass;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.composite.CompositeAggregation;
import org.elasticsearch.search.aggregations.metrics.NumericMetricsAggregation;

/**
 * AggregationResponseParser.
 */
@UtilityClass
public class ElasticsearchAggregationResponseParser {

  /**
   * Parse Aggregations as a list of field and value map.
   *
   * @param aggregations aggregations
   * @return a list of field and value map
   */
  public static List<Map<String, Object>> parse(Aggregations aggregations) {
    List<Aggregation> aggregationList = aggregations.asList();
    ImmutableList.Builder<Map<String, Object>> builder = new ImmutableList.Builder<>();
    Map<String, Object> noBucketMap = new HashMap<>();

    for (Aggregation aggregation : aggregationList) {
      if (aggregation instanceof CompositeAggregation) {
        for (CompositeAggregation.Bucket bucket :
            ((CompositeAggregation) aggregation).getBuckets()) {
          builder.add(parse(bucket));
        }
      } else {
        noBucketMap.putAll(parseInternal(aggregation));
      }

    }
    // Todo, there is no better way to difference the with/without bucket from aggregations result.
    return noBucketMap.isEmpty() ? builder.build() : Collections.singletonList(noBucketMap);
  }

  private static Map<String, Object> parse(CompositeAggregation.Bucket bucket) {
    Map<String, Object> resultMap = new HashMap<>();
    // The NodeClient return InternalComposite

    // build <groupKey, value> pair
    resultMap.putAll(bucket.getKey());

    // build <aggKey, value> pair
    for (Aggregation aggregation : bucket.getAggregations()) {
      resultMap.putAll(parseInternal(aggregation));
    }

    return resultMap;
  }

  private static Map<String, Object> parseInternal(Aggregation aggregation) {
    Map<String, Object> resultMap = new HashMap<>();
    if (aggregation instanceof NumericMetricsAggregation.SingleValue) {
      resultMap.put(
          aggregation.getName(),
          handleNanValue(((NumericMetricsAggregation.SingleValue) aggregation).value()));
    } else {
      throw new IllegalStateException("unsupported aggregation type " + aggregation.getType());
    }
    return resultMap;
  }

  @VisibleForTesting
  protected static Object handleNanValue(double value) {
    return Double.isNaN(value) ? null : value;
  }
}
