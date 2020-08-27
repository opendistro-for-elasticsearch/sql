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

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_NULL;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.doubleValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.floatValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.getDoubleValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.getFloatValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.getIntegerValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.getLongValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.getStringValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.integerValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.longValue;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.FLOAT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.LONG;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.SHORT;
import static com.amazon.opendistroforelasticsearch.sql.utils.ExpressionUtils.format;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprNullValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.storage.bindingtuple.BindingTuple;
import java.util.Arrays;
import java.util.List;

/**
 * The minimum aggregator aggregate the value evaluated by the expression.
 * If the expression evaluated result is NULL or MISSING, then the result is NULL.
 */
public class MinAggregator extends Aggregator<MinAggregator.MinState> {

  public MinAggregator(List<Expression> arguments, ExprCoreType returnType) {
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
    if (!(value.isNull() || value.isMissing())) {
      state.isEmptyCollection = false;
      state.min(value);
    }
    return state;
  }

  @Override
  public String toString() {
    return String.format("min(%s)", format(getArguments()));
  }

  protected static class MinState implements AggregationState {
    private ExprValue minResult;
    private boolean isEmptyCollection;

    MinState(ExprCoreType type) {
      minResult = isNumber(type) ? doubleValue(Double.MAX_VALUE) : LITERAL_NULL;
      isEmptyCollection = true;
    }

    public void min(ExprValue value) {
      minResult = minResult.isNull() ? value : minResult.compareTo(value) < 0 ? minResult : value;
    }

    @Override
    public ExprValue result() {
      return isEmptyCollection ? ExprNullValue.of() : minResult;
    }


    private boolean isNumber(ExprCoreType type) {
      return Arrays.asList(SHORT, INTEGER, LONG, FLOAT, DOUBLE).contains(type);
    }
  }
}