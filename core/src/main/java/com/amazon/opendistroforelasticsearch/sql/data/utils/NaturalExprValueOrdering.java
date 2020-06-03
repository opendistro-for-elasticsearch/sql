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

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.getBooleanValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.getCollectionValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.getDoubleValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.getFloatValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.getIntegerValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.getLongValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.getStringValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.getTupleValue;
import static com.amazon.opendistroforelasticsearch.sql.expression.scalar.OperatorUtils.COMPARE_WITH_NULL_OR_MISSING;
import static com.amazon.opendistroforelasticsearch.sql.expression.scalar.OperatorUtils.LIST_COMPARATOR;
import static com.amazon.opendistroforelasticsearch.sql.expression.scalar.OperatorUtils.MAP_COMPARATOR;
import static com.amazon.opendistroforelasticsearch.sql.expression.scalar.OperatorUtils.STRING_COMPARATOR;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
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
    if (COMPARE_WITH_NULL_OR_MISSING.test(left, right)) {
      throw new ExpressionEvaluationException("compare with null or missing value is invalid");
    }
    if (!left.type().equals(right.type())) {
      throw new ExpressionEvaluationException(
          String.format(
              "compare expected value have same type, but with [%s, %s]",
              left.type(), right.type()));
    }
    switch (left.type()) {
      case DOUBLE:
        return Double.compare(getDoubleValue(left), getDoubleValue(right));
      case FLOAT:
        return Float.compare(getFloatValue(left), getFloatValue(right));
      case LONG:
        return Long.compare(getLongValue(left), getLongValue(right));
      case INTEGER:
        return Integer.compare(getIntegerValue(left), getIntegerValue(right));
      case BOOLEAN:
        return Boolean.compare(getBooleanValue(left), getBooleanValue(right));
      case STRING:
        return STRING_COMPARATOR.apply(getStringValue(left), getStringValue(right));
      case STRUCT:
        return MAP_COMPARATOR.apply(getTupleValue(left), getTupleValue(right));
      case ARRAY:
        return LIST_COMPARATOR.apply(getCollectionValue(left), getCollectionValue(right));
      default:
        throw new ExpressionEvaluationException(
            String.format("compare doesn't support type [%s]", left.type()));
    }
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
