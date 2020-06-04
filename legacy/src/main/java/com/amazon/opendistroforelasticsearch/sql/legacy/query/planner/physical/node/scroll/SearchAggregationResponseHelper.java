/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.node.scroll;

import com.amazon.opendistroforelasticsearch.sql.legacy.expression.domain.BindingTuple;
import com.google.common.annotations.VisibleForTesting;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.metrics.NumericMetricsAggregation;
import org.elasticsearch.search.aggregations.metrics.Percentile;
import org.elasticsearch.search.aggregations.metrics.Percentiles;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * The definition of Search {@link Aggregations} parser helper class.
 */
public class SearchAggregationResponseHelper {
    public static List<BindingTupleRow> populateSearchAggregationResponse(Aggregations aggs) {
        List<Map<String, Object>> flatten = flatten(aggs);
        List<BindingTupleRow> bindingTupleList = flatten.stream()
                .map(BindingTuple::from)
                .map(bindingTuple -> new BindingTupleRow(bindingTuple))
                .collect(Collectors.toList());
        return bindingTupleList;
    }

    @VisibleForTesting
    public static List<Map<String, Object>> flatten(Aggregations aggregations) {
        List<Aggregation> aggregationList = aggregations.asList();
        List<Map<String, Object>> resultList = new ArrayList<>();
        Map<String, Object> resultMap = new HashMap<>();
        for (Aggregation aggregation : aggregationList) {
            if (aggregation instanceof Terms) {
                for (Terms.Bucket bucket : ((Terms) aggregation).getBuckets()) {
                    List<Map<String, Object>> internalBucketList = flatten(bucket.getAggregations());
                    fillResultListWithInternalBucket(resultList, internalBucketList, aggregation.getName(),
                                                     bucket.getKey());
                }
            } else if (aggregation instanceof NumericMetricsAggregation.SingleValue) {
                resultMap.put(aggregation.getName(), ((NumericMetricsAggregation.SingleValue) aggregation).value());
            } else if (aggregation instanceof Percentiles) {
                Percentiles percentiles = (Percentiles) aggregation;
                resultMap.putAll((Map<String, Double>) StreamSupport.stream(percentiles.spliterator(), false)
                        .collect(Collectors.toMap(
                                (percentile) -> String.format("%s_%s", percentiles.getName(), percentile.getPercent()),
                                Percentile::getValue, (v1, v2) -> {
                                    throw new IllegalArgumentException(
                                            String.format("Duplicate key for values %s and %s", v1, v2));
                                }, HashMap::new)));
            } else if (aggregation instanceof Histogram) {
                for (Histogram.Bucket bucket : ((Histogram) aggregation).getBuckets()) {
                    List<Map<String, Object>> internalBucketList = flatten(bucket.getAggregations());
                    fillResultListWithInternalBucket(resultList, internalBucketList, aggregation.getName(),
                                                     bucket.getKeyAsString());
                }
            } else {
                throw new RuntimeException("unsupported aggregation type " + aggregation.getType());
            }
        }
        if (!resultMap.isEmpty()) {
            resultList.add(resultMap);
        }
        return resultList;
    }

    private static void fillResultListWithInternalBucket(List<Map<String, Object>> resultList,
                                                         List<Map<String, Object>> internalBucketList,
                                                         String aggregationName, Object bucketKey) {
        if (internalBucketList.isEmpty()) {
            resultList.add(new HashMap<String, Object>() {{
                put(aggregationName, bucketKey);
            }});
        } else {
            for (Map<String, Object> map : internalBucketList) {
                map.put(aggregationName, bucketKey);
            }
            resultList.addAll(internalBucketList);
        }
    }
}
