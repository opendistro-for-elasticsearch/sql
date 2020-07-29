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

/**
 * Idea from guava {@link Ordering}. The only difference is the special logic to handle {@link
 * com.amazon.opendistroforelasticsearch.sql.data.model.ExprNullValue} and {@link
 * com.amazon.opendistroforelasticsearch.sql.data.model.ExprMissingValue}
 */
public class NaturalExprValueOrdering extends ExprValueOrdering {
  static final ExprValueOrdering INSTANCE = new NaturalExprValueOrdering();

  private transient ExprValueOrdering nullsFirst;
  private transient ExprValueOrdering nullsLast;

  @Override
  public int compare(ExprValue left, ExprValue right) {
    return left.compareTo(right);
  }

  @Override
  public ExprValueOrdering nullsFirst() {
    ExprValueOrdering result = nullsFirst;
    if (result == null) {
      result = nullsFirst = super.nullsFirst();
    }
    return result;
  }

  @Override
  public ExprValueOrdering nullsLast() {
    ExprValueOrdering result = nullsLast;
    if (result == null) {
      result = nullsLast = super.nullsLast();
    }
    return result;
  }

  @Override
  public ExprValueOrdering reverse() {
    return super.reverse();
  }
}
