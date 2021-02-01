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

import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalFilter;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalSort;
import com.amazon.opendistroforelasticsearch.sql.planner.optimizer.Rule;
import com.facebook.presto.matching.Capture;
import com.facebook.presto.matching.Captures;
import com.facebook.presto.matching.Pattern;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * Push Filter under Sort.
 * Filter - Sort - Child --> Sort - Filter - Child
 */
public class PushFilterUnderSort implements Rule<LogicalFilter> {

  private final Capture<LogicalSort> capture;

  @Accessors(fluent = true)
  @Getter
  private final Pattern<LogicalFilter> pattern;

  /**
   * Constructor of PushFilterUnderSort.
   */
  public PushFilterUnderSort() {
    this.capture = Capture.newCapture();
    this.pattern = typeOf(LogicalFilter.class)
        .with(source().matching(typeOf(LogicalSort.class).capturedAs(capture)));
  }

  @Override
  public LogicalPlan apply(LogicalFilter filter,
                           Captures captures) {
    LogicalSort sort = captures.get(capture);
    return new LogicalSort(
        filter.replaceChildPlans(sort.getChild()),
        sort.getSortList()
    );
  }
}
