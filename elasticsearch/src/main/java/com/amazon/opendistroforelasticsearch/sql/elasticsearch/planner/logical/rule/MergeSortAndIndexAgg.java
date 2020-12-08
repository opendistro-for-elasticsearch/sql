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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.planner.logical.rule;

import static com.amazon.opendistroforelasticsearch.sql.planner.optimizer.pattern.Patterns.source;
import static com.facebook.presto.matching.Pattern.typeOf;

import com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.planner.logical.ElasticsearchLogicalIndexAgg;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.NamedAggregator;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalSort;
import com.amazon.opendistroforelasticsearch.sql.planner.optimizer.Rule;
import com.facebook.presto.matching.Capture;
import com.facebook.presto.matching.Captures;
import com.facebook.presto.matching.Pattern;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Merge Sort -- IndexScanAggregation to IndexScanAggregation.
 */
public class MergeSortAndIndexAgg implements Rule<LogicalSort> {

  private final Capture<ElasticsearchLogicalIndexAgg> indexAggCapture;

  @Accessors(fluent = true)
  @Getter
  private final Pattern<LogicalSort> pattern;

  /**
   * Constructor of MergeAggAndIndexScan.
   */
  public MergeSortAndIndexAgg() {
    this.indexAggCapture = Capture.newCapture();
    final AtomicReference<LogicalSort> sortRef = new AtomicReference<>();

    this.pattern = typeOf(LogicalSort.class)
        .matching(OptimizationRuleUtils::sortByFieldsOnly)
        .matching(OptimizationRuleUtils::sortByDefaultOptionOnly)
        .matching(sort -> {
          sortRef.set(sort);
          return true;
        })
        .with(source().matching(typeOf(ElasticsearchLogicalIndexAgg.class)
            .matching(indexAgg -> !hasAggregatorInSortBy(sortRef.get(), indexAgg))
            .capturedAs(indexAggCapture)));
  }

  @Override
  public LogicalPlan apply(LogicalSort sort,
                           Captures captures) {
    ElasticsearchLogicalIndexAgg indexAgg = captures.get(indexAggCapture);
    return ElasticsearchLogicalIndexAgg.builder()
        .relationName(indexAgg.getRelationName())
        .filter(indexAgg.getFilter())
        .groupByList(indexAgg.getGroupByList())
        .aggregatorList(indexAgg.getAggregatorList())
        .sortList(sort.getSortList())
        .build();
  }

  private boolean hasAggregatorInSortBy(LogicalSort sort, ElasticsearchLogicalIndexAgg agg) {
    final Set<String> aggregatorNames =
        agg.getAggregatorList().stream().map(NamedAggregator::getName).collect(Collectors.toSet());
    for (Pair<Sort.SortOption, Expression> sortPair : sort.getSortList()) {
      if (aggregatorNames.contains(((ReferenceExpression) sortPair.getRight()).getAttr())) {
        return true;
      }
    }
    return false;
  }
}
