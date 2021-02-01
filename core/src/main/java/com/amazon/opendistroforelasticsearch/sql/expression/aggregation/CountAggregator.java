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

package com.amazon.opendistroforelasticsearch.sql.expression.aggregation;

import static com.amazon.opendistroforelasticsearch.sql.utils.ExpressionUtils.format;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.CountAggregator.CountState;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.storage.bindingtuple.BindingTuple;
import java.util.List;
import java.util.Locale;

public class CountAggregator extends Aggregator<CountState> {

  public CountAggregator(List<Expression> arguments, ExprCoreType returnType) {
    super(BuiltinFunctionName.COUNT.getName(), arguments, returnType);
  }

  @Override
  public CountAggregator.CountState create() {
    return new CountState();
  }

  @Override
  protected CountState iterate(ExprValue value, CountState state) {
    state.count++;
    return state;
  }

  @Override
  public String toString() {
    return String.format(Locale.ROOT, "count(%s)", format(getArguments()));
  }

  /**
   * Count State.
   */
  protected static class CountState implements AggregationState {
    private int count;

    CountState() {
      this.count = 0;
    }

    @Override
    public ExprValue result() {
      return ExprValueUtils.integerValue(count);
    }
  }
}
