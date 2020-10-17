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
import static com.amazon.opendistroforelasticsearch.sql.utils.ExpressionUtils.format;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.storage.bindingtuple.BindingTuple;
import java.util.List;

public class MaxAggregator extends Aggregator<MaxAggregator.MaxState> {

  public MaxAggregator(List<Expression> arguments, ExprCoreType returnType) {
    super(BuiltinFunctionName.MAX.getName(), arguments, returnType);
  }

  @Override
  public MaxState create() {
    return new MaxState();
  }

  @Override
  public MaxState iterate(BindingTuple tuple, MaxState state) {
    Expression expression = getArguments().get(0);
    ExprValue value = expression.valueOf(tuple);
    if (!(value.isNull() || value.isMissing())) {
      state.max(value);
    }
    return state;
  }

  @Override
  public String toString() {
    return String.format("max(%s)", format(getArguments()));
  }

  protected static class MaxState implements AggregationState {
    private ExprValue maxResult;

    MaxState() {
      maxResult = LITERAL_NULL;
    }

    public void max(ExprValue value) {
      maxResult = maxResult.isNull() ? value
          : (maxResult.compareTo(value) > 0)
          ? maxResult : value;
    }

    @Override
    public ExprValue result() {
      return maxResult;
    }
  }
}
