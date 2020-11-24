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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.utils;

import com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.planner.logical.ElasticsearchLogicalIndexAgg;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.planner.logical.ElasticsearchLogicalIndexScan;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.NamedExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.NamedAggregator;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import java.util.Arrays;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;

@UtilityClass
public class Utils {

  /**
   * Build ElasticsearchLogicalIndexScan.
   */
  public static LogicalPlan indexScan(String tableName, Expression filter) {
    return ElasticsearchLogicalIndexScan.builder().relationName(tableName).filter(filter).build();
  }

  /**
   * Build ElasticsearchLogicalIndexScan.
   */
  public static LogicalPlan indexScan(String tableName,
                                      Pair<Sort.SortOption, Expression>... sorts) {
    return ElasticsearchLogicalIndexScan.builder().relationName(tableName)
        .sortList(Arrays.asList(sorts))
        .build();
  }

  /**
   * Build ElasticsearchLogicalIndexScan.
   */
  public static LogicalPlan indexScan(String tableName,
                                      Expression filter,
                                      Pair<Sort.SortOption, Expression>... sorts) {
    return ElasticsearchLogicalIndexScan.builder().relationName(tableName)
        .filter(filter)
        .sortList(Arrays.asList(sorts))
        .build();
  }

  /**
   * Build ElasticsearchLogicalIndexAgg.
   */
  public static LogicalPlan indexScanAgg(String tableName, List<NamedAggregator> aggregators,
                                         List<NamedExpression> groupByList) {
    return ElasticsearchLogicalIndexAgg.builder().relationName(tableName)
        .aggregatorList(aggregators).groupByList(groupByList).build();
  }

  /**
   * Build ElasticsearchLogicalIndexAgg.
   */
  public static LogicalPlan indexScanAgg(String tableName,
                                         Expression filter,
                                         List<NamedAggregator> aggregators,
                                         List<NamedExpression> groupByList) {
    return ElasticsearchLogicalIndexAgg.builder().relationName(tableName).filter(filter)
        .aggregatorList(aggregators).groupByList(groupByList).build();
  }
}
