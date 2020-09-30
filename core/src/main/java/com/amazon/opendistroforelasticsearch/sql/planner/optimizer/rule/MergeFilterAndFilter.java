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

package com.amazon.opendistroforelasticsearch.sql.planner.optimizer.rule;

import static com.amazon.opendistroforelasticsearch.sql.planner.optimizer.pattern.Patterns.source;
import static com.facebook.presto.matching.Pattern.typeOf;

import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalFilter;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.optimizer.Rule;
import com.facebook.presto.matching.Capture;
import com.facebook.presto.matching.Captures;
import com.facebook.presto.matching.Pattern;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Merge Filter --> Filter to the single Filter condition.
 */
public class MergeFilterAndFilter implements Rule<LogicalFilter> {

  private final Capture<LogicalFilter> capture;

  private final DSL dsl;

  @Accessors(fluent = true)
  @Getter
  private final Pattern<LogicalFilter> pattern;

  /**
   * Constructor of MergeFilterAndFilter.
   */
  public MergeFilterAndFilter(DSL dsl) {
    this.dsl = dsl;
    this.capture = Capture.newCapture();
    this.pattern = typeOf(LogicalFilter.class)
        .with(source().matching(typeOf(LogicalFilter.class).capturedAs(capture)));
  }

  @Override
  public LogicalPlan apply(LogicalFilter filter,
                           Captures captures) {
    LogicalFilter childFilter = captures.get(capture);
    return new LogicalFilter(
        childFilter.getChild().get(0),
        dsl.and(filter.getCondition(), childFilter.getCondition())
    );
  }
}
