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
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.storage.bindingtuple.BindingTuple;
import java.util.List;

/**
 * The minimum aggregator aggregate the value evaluated by the expression.
 * If the expression evaluated result is NULL or MISSING, then the result is NULL.
 */
public class MinAggregator extends Aggregator<MinAggregator.MinState> {

  public MinAggregator(List<Expression> arguments, ExprType returnType) {
    super(BuiltinFunctionName.MIN.getName(), arguments, returnType);
  }


  @Override
  public MinState create() {
    return new MinState(returnType);
  }

  @Override
  public MinState iterate(BindingTuple tuple, MinState state) {
    Expression expression = getArguments().get(0);
    ExprValue value = expression.valueOf(tuple);
    if (value.isNull() || value.isMissing()) {
      state.isNullResult = true;
    } else {
      state.min(value);
    }
    return state;
  }

  @Override
  public String toString() {
    return String.format("min(%s)", format(getArguments()));
  }

  protected class MinState implements AggregationState {
    private final ExprType type;
    private ExprValue minResult;
    private boolean isNullResult;

    public MinState(ExprType type) {
      this.type = type;
      minResult = doubleValue(Double.MAX_VALUE);
      isNullResult = false;
    }

    public void min(ExprValue value) {
      switch (type) {
        case INTEGER:
          minResult = integerValue(Math.min(getIntegerValue(minResult), getIntegerValue(value)));
          break;
        case LONG:
          minResult = longValue(Math.min(getLongValue(minResult), getLongValue(value)));
          break;
        case FLOAT:
          minResult = floatValue(Math.min(getFloatValue(minResult), getFloatValue(value)));
          break;
        case DOUBLE:
          minResult = doubleValue(Math.min(getDoubleValue(minResult), getDoubleValue(value)));
          break;
        default:
          throw new ExpressionEvaluationException(
              String.format("unexpected type [%s] in min aggregation", type));
      }
    }

    @Override
    public ExprValue result() {
      return isNullResult ? ExprNullValue.of() : minResult;
    }
  }
}
