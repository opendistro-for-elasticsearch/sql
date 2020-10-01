/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.opendistroforelasticsearch.sql.expression.aggregation;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.doubleValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.floatValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.getDoubleValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.getFloatValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.getIntegerValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.getLongValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.integerValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.longValue;
import static com.amazon.opendistroforelasticsearch.sql.utils.ExpressionUtils.format;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprNullValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.SumAggregator.SumState;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.storage.bindingtuple.BindingTuple;
import java.util.List;
import java.util.Locale;

/**
 * The sum aggregator aggregate the value evaluated by the expression.
 * If the expression evaluated result is NULL or MISSING, then the result is NULL.
 */
public class SumAggregator extends Aggregator<SumState> {

  public SumAggregator(List<Expression> arguments, ExprCoreType returnType) {
    super(BuiltinFunctionName.SUM.getName(), arguments, returnType);
  }

  @Override
  public SumState create() {
    return new SumState(returnType);
  }

  @Override
  public SumState iterate(BindingTuple tuple, SumState state) {
    Expression expression = getArguments().get(0);
    ExprValue value = expression.valueOf(tuple);
    if (!(value.isNull() || value.isMissing())) {
      state.isEmptyCollection = false;
      state.add(value);
    }
    return state;
  }

  @Override
  public String toString() {
    return String.format(Locale.ROOT, "sum(%s)", format(getArguments()));
  }

  /**
   * Sum State.
   */
  protected static class SumState implements AggregationState {

    private final ExprCoreType type;
    private ExprValue sumResult;
    private boolean isEmptyCollection;

    SumState(ExprCoreType type) {
      this.type = type;
      sumResult = ExprValueUtils.integerValue(0);
      isEmptyCollection = true;
    }

    /**
     * Add value to current sumResult.
     */
    public void add(ExprValue value) {
      switch (type) {
        case INTEGER:
          sumResult = integerValue(getIntegerValue(sumResult) + getIntegerValue(value));
          break;
        case LONG:
          sumResult = longValue(getLongValue(sumResult) + getLongValue(value));
          break;
        case FLOAT:
          sumResult = floatValue(getFloatValue(sumResult) + getFloatValue(value));
          break;
        case DOUBLE:
          sumResult = doubleValue(getDoubleValue(sumResult) + getDoubleValue(value));
          break;
        default:
          throw new ExpressionEvaluationException(
              String.format("unexpected type [%s] in sum aggregation", type));
      }
    }

    @Override
    public ExprValue result() {
      return isEmptyCollection ? ExprNullValue.of() : sumResult;
    }
  }
}
