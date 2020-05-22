/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.data.utils;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.google.common.collect.Ordering;
import lombok.RequiredArgsConstructor;

/**
 * Idea from guava {@link Ordering}. The only difference is the special logic to handle {@link
 * com.amazon.opendistroforelasticsearch.sql.data.model.ExprNullValue} and {@link
 * com.amazon.opendistroforelasticsearch.sql.data.model.ExprMissingValue}
 */
@RequiredArgsConstructor
public class NullsLastExprValueOrdering extends ExprValueOrdering {
  private final ExprValueOrdering ordering;

  @Override
  public int compare(ExprValue left, ExprValue right) {
    if (left == right) {
      return 0;
    }
    if (left.isNull() || left.isMissing()) {
      return LEFT_IS_GREATER;
    }
    if (right.isNull() || right.isMissing()) {
      return RIGHT_IS_GREATER;
    }
    return ordering.compare(left, right);
  }

  @Override
  public ExprValueOrdering reverse() {
    return ordering.reverse().nullsFirst();
  }

  @Override
  public ExprValueOrdering nullsFirst() {
    return ordering.nullsFirst();
  }

  @Override
  public ExprValueOrdering nullsLast() {
    return this;
  }
}
