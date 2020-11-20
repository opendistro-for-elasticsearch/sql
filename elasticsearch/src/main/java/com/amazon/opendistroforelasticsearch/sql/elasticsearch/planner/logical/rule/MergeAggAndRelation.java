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

import com.amazon.opendistroforelasticsearch.sql.elasticsearch.planner.logical.ElasticsearchLogicalIndexAgg;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalAggregation;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalRelation;
import com.amazon.opendistroforelasticsearch.sql.planner.optimizer.Rule;
import com.facebook.presto.matching.Capture;
import com.facebook.presto.matching.Captures;
import com.facebook.presto.matching.Pattern;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Merge Aggregation -- Relation to IndexScanAggregation.
 */
public class MergeAggAndRelation implements Rule<LogicalAggregation> {

  private final Capture<LogicalRelation> relationCapture;

  @Accessors(fluent = true)
  @Getter
  private final Pattern<LogicalAggregation> pattern;

  /**
   * Constructor of MergeAggAndRelation.
   */
  public MergeAggAndRelation() {
    this.relationCapture = Capture.newCapture();
    this.pattern = typeOf(LogicalAggregation.class)
        .with(source().matching(typeOf(LogicalRelation.class).capturedAs(relationCapture)));
  }

  @Override
  public LogicalPlan apply(LogicalAggregation aggregation,
                           Captures captures) {
    LogicalRelation relation = captures.get(relationCapture);
    return ElasticsearchLogicalIndexAgg
        .builder()
        .relationName(relation.getRelationName())
        .aggregatorList(aggregation.getAggregatorList())
        .groupByList(aggregation.getGroupByList())
        .build();
  }
}
