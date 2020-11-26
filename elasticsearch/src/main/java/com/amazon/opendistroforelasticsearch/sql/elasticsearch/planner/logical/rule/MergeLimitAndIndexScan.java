/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.planner.logical.rule;

import static com.amazon.opendistroforelasticsearch.sql.planner.optimizer.pattern.Patterns.source;
import static com.facebook.presto.matching.Pattern.typeOf;

import com.amazon.opendistroforelasticsearch.sql.elasticsearch.planner.logical.ElasticsearchLogicalIndexScan;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalLimit;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.optimizer.Rule;
import com.facebook.presto.matching.Capture;
import com.facebook.presto.matching.Captures;
import com.facebook.presto.matching.Pattern;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
public class MergeLimitAndIndexScan implements Rule<LogicalLimit> {

  private final Capture<ElasticsearchLogicalIndexScan> indexScanCapture;

  @Accessors(fluent = true)
  private final Pattern<LogicalLimit> pattern;

  /**
   * Constructor of MergeLimitAndIndexScan.
   */
  public MergeLimitAndIndexScan() {
    this.indexScanCapture = Capture.newCapture();
    this.pattern = typeOf(LogicalLimit.class)
        .with(source()
            .matching(typeOf(ElasticsearchLogicalIndexScan.class).capturedAs(indexScanCapture)));
  }

  @Override
  public LogicalPlan apply(LogicalLimit plan, Captures captures) {
    ElasticsearchLogicalIndexScan indexScan = captures.get(indexScanCapture);
    ElasticsearchLogicalIndexScan.ElasticsearchLogicalIndexScanBuilder builder =
        ElasticsearchLogicalIndexScan.builder();
    builder.relationName(indexScan.getRelationName())
        .filter(indexScan.getFilter())
        .offset(plan.getOffset())
        .limit(plan.getLimit());
    if (indexScan.getSortList() != null) {
      builder.sortList(indexScan.getSortList());
    }
    return builder.build();
  }
}
