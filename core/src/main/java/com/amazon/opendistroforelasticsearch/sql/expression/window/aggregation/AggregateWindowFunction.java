/*
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.expression.window.aggregation;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTupleValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.AggregationState;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.Aggregator;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import com.amazon.opendistroforelasticsearch.sql.expression.window.frame.WindowFrame;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

/**
 * Aggregate function adapter that adapts Aggregator for window operator use.
 */
@EqualsAndHashCode
@RequiredArgsConstructor
public class AggregateWindowFunction implements Expression {

  private final Aggregator<AggregationState> aggregator;
  private AggregationState state;

  @Override
  public ExprValue valueOf(Environment<Expression, ExprValue> valueEnv) {
    WindowFrame frame = (WindowFrame) valueEnv;
    if (frame.isNewPartition()) {
      state = aggregator.create();
    }

    ExprTupleValue row = frame.get(frame.currentIndex());
    state = aggregator.iterate(row.bindingTuples(), state);
    return state.result();
  }

  @Override
  public ExprType type() {
    return aggregator.type();
  }

  @Override
  public <T, C> T accept(ExpressionNodeVisitor<T, C> visitor, C context) {
    return aggregator.accept(visitor, context);
  }

  @Override
  public String toString() {
    return aggregator.toString();
  }

}
