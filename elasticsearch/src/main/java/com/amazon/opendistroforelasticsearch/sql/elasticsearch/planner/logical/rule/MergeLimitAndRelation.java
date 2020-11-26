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
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalRelation;
import com.amazon.opendistroforelasticsearch.sql.planner.optimizer.Rule;
import com.facebook.presto.matching.Capture;
import com.facebook.presto.matching.Captures;
import com.facebook.presto.matching.Pattern;
import lombok.Getter;
import lombok.experimental.Accessors;

@Getter
public class MergeLimitAndRelation implements Rule<LogicalLimit> {

  private final Capture<LogicalRelation> relationCapture;

  @Accessors(fluent = true)
  private final Pattern<LogicalLimit> pattern;

  /**
   * Constructor of MergeLimitAndRelation.
   */
  public MergeLimitAndRelation() {
    this.relationCapture = Capture.newCapture();
    this.pattern = typeOf(LogicalLimit.class)
        .with(source().matching(typeOf(LogicalRelation.class).capturedAs(relationCapture)));
  }

  @Override
  public LogicalPlan apply(LogicalLimit plan, Captures captures) {
    LogicalRelation relation = captures.get(relationCapture);
    return ElasticsearchLogicalIndexScan.builder()
        .relationName(relation.getRelationName())
        .offset(plan.getOffset())
        .limit(plan.getLimit())
        .build();
  }
}
