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

package com.amazon.opendistroforelasticsearch.sql.ast.expression;

import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum  IntervalUnit {
  UNKNOWN,

  MICROSECOND,
  SECOND,
  MINUTE,
  HOUR,
  DAY,
  WEEK,
  MONTH,
  QUARTER,
  YEAR,
  SECOND_MICROSECOND,
  MINUTE_MICROSECOND,
  MINUTE_SECOND,
  HOUR_MICROSECOND,
  HOUR_SECOND,
  HOUR_MINUTE,
  DAY_MICROSECOND,
  DAY_SECOND,
  DAY_MINUTE,
  DAY_HOUR,
  YEAR_MONTH;

  private static final List<IntervalUnit> INTERVAL_UNITS;

  static {
    ImmutableList.Builder<IntervalUnit> builder = new ImmutableList.Builder<>();
    INTERVAL_UNITS = builder.add(IntervalUnit.values()).build();
  }

  /**
   * Util method to get interval unit given the unit name.
   */
  public static IntervalUnit of(String unit) {
    return INTERVAL_UNITS.stream()
        .filter(v -> unit.equalsIgnoreCase(v.name()))
        .findFirst()
        .orElse(IntervalUnit.UNKNOWN);
  }
}
