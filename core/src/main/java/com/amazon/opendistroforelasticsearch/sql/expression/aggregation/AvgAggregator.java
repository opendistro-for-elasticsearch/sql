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

import static com.amazon.opendistroforelasticsearch.sql.utils.ExpressionUtils.format;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprNullValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.storage.bindingtuple.BindingTuple;
import java.util.List;
import java.util.Locale;

/**
 * The average aggregator aggregate the value evaluated by the expression.
 * If the expression evaluated result is NULL or MISSING, then the result is NULL.
 */
public class AvgAggregator extends Aggregator<AvgAggregator.AvgState> {

  public AvgAggregator(List<Expression> arguments, ExprCoreType returnType) {
    super(BuiltinFunctionName.AVG.getName(), arguments, returnType);
  }

  @Override
  public AvgState create() {
    return new AvgState();
  }

  @Override
  public AvgState iterate(BindingTuple tuple, AvgState state) {
    Expression expression = getArguments().get(0);
    ExprValue value = expression.valueOf(tuple);
    if (!(value.isNull() || value.isMissing())) {
      state.count++;
      state.total += ExprValueUtils.getDoubleValue(value);
    }
    return state;
  }

  @Override
  public String toString() {
    return String.format(Locale.ROOT, "avg(%s)", format(getArguments()));
  }

  /**
   * Average State.
   */
  protected static class AvgState implements AggregationState {
    private int count;
    private double total;

    AvgState() {
      this.count = 0;
      this.total = 0d;
    }

    @Override
    public ExprValue result() {
      return count == 0 ? ExprNullValue.of() : ExprValueUtils.doubleValue(total / count);
    }
  }
}
