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

import com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.aggregation.dsl.BucketAggregationBuilder;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.aggregation.dsl.MetricAggregationBuilder;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.serialization.ExpressionSerializer;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.expression.NamedExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.NamedAggregator;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.sort.SortOrder;

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
  public List<AggregationBuilder> buildAggregationBuilder(
      List<NamedAggregator> namedAggregatorList,
      List<NamedExpression> groupByList,
      List<Pair<Sort.SortOption, Expression>> sortList) {
    if (groupByList.isEmpty()) {
      // no bucket
      return ImmutableList
          .copyOf(metricBuilder.build(namedAggregatorList).getAggregatorFactories());
    } else {
      final GroupSortOrder groupSortOrder = new GroupSortOrder(sortList);
      return Collections.singletonList(AggregationBuilders.composite("composite_buckets",
          bucketBuilder
              .build(groupByList.stream().sorted(groupSortOrder).map(expr -> Pair.of(expr,
                  groupSortOrder.apply(expr))).collect(Collectors.toList())))
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

  @VisibleForTesting
  public static class GroupSortOrder implements Comparator<NamedExpression>,
      Function<NamedExpression, SortOrder> {

    /**
     * The default order of group field.
     * The order is ASC NULL_FIRST.
     * The field should be the last one in the group list.
     */
    private static final Pair<SortOrder, Integer> DEFAULT_ORDER =
        Pair.of(SortOrder.ASC, Integer.MAX_VALUE);

    /**
     * The mapping betwen {@link Sort.SortOption} and {@link SortOrder}.
     */
    private static final Map<Sort.SortOption, SortOrder> SORT_MAP =
        new ImmutableMap.Builder<Sort.SortOption, SortOrder>()
            .put(Sort.SortOption.DEFAULT_ASC, SortOrder.ASC)
            .put(Sort.SortOption.DEFAULT_DESC, SortOrder.DESC).build();

    private final Map<String, Pair<SortOrder, Integer>> map = new HashMap<>();

    /**
     * Constructor of GroupSortOrder.
     */
    public GroupSortOrder(List<Pair<Sort.SortOption, Expression>> sortList) {
      if (null == sortList) {
        return;
      }
      int pos = 0;
      for (Pair<Sort.SortOption, Expression> sortPair : sortList) {
        map.put(((ReferenceExpression) sortPair.getRight()).getAttr(),
            Pair.of(SORT_MAP.getOrDefault(sortPair.getLeft(), SortOrder.ASC), pos++));
      }
    }

    /**
     * Compare the two expressions. The comparison is based on the pos in the sort list.
     * If the expression is defined in the sort list. then the order of the expression is the pos
     * in sort list.
     * If the expression isn't defined in the sort list. the the order of the expression is the
     * Integer.MAX_VALUE. you can think it is at the end of the sort list.
     *
     * @param o1 NamedExpression
     * @param o2 NamedExpression
     * @return -1, o1 before o2. 1, o1 after o2. 0 o1, o2 has same position.
     */
    @Override
    public int compare(NamedExpression o1, NamedExpression o2) {
      final Pair<SortOrder, Integer> o1Value =
          map.getOrDefault(o1.getName(), DEFAULT_ORDER);
      final Pair<SortOrder, Integer> o2Value =
          map.getOrDefault(o2.getName(), DEFAULT_ORDER);
      return o1Value.getRight().compareTo(o2Value.getRight());
    }

    /**
     * Get the {@link SortOrder} for expression.
     * By default, the {@link SortOrder} is ASC.
     */
    @Override
    public SortOrder apply(NamedExpression expression) {
      return map.getOrDefault(expression.getName(), DEFAULT_ORDER).getLeft();
    }
  }
}
