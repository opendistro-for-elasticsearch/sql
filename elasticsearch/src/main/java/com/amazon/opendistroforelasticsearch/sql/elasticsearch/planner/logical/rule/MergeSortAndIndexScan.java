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
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.planner.logical.ElasticsearchLogicalIndexScan;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalSort;
import com.amazon.opendistroforelasticsearch.sql.planner.optimizer.Rule;
import com.facebook.presto.matching.Capture;
import com.facebook.presto.matching.Captures;
import com.facebook.presto.matching.Pattern;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Merge Sort with IndexScan only when Sort by fields.
 */
public class MergeSortAndIndexScan implements Rule<LogicalSort> {

  private final Capture<ElasticsearchLogicalIndexScan> indexScanCapture;
  private final Pattern<LogicalSort> pattern;

  /**
   * Constructor of MergeSortAndRelation.
   */
  public MergeSortAndIndexScan() {
    this.indexScanCapture = Capture.newCapture();
    this.pattern = typeOf(LogicalSort.class).matching(OptimizationRuleUtils::sortByFieldsOnly)
        .with(source()
            .matching(typeOf(ElasticsearchLogicalIndexScan.class).capturedAs(indexScanCapture)));
  }

  @Override
  public Pattern<LogicalSort> pattern() {
    return pattern;
  }

  @Override
  public LogicalPlan apply(LogicalSort sort,
                           Captures captures) {
    ElasticsearchLogicalIndexScan indexScan = captures.get(indexScanCapture);

    return ElasticsearchLogicalIndexScan
        .builder()
        .relationName(indexScan.getRelationName())
        .filter(indexScan.getFilter())
        .sortList(mergeSortList(indexScan.getSortList(), sort.getSortList()))
        .build();
  }

  private List<Pair<Sort.SortOption, Expression>> mergeSortList(List<Pair<Sort.SortOption,
      Expression>> l1, List<Pair<Sort.SortOption, Expression>> l2) {
    if (null == l1) {
      return l2;
    } else {
      return Stream.concat(l1.stream(), l2.stream()).collect(Collectors.toList());
    }
  }
}
