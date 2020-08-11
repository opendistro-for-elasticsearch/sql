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

import com.amazon.opendistroforelasticsearch.sql.analysis.ExpressionAnalyzer;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionImplementation;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionName;
import com.amazon.opendistroforelasticsearch.sql.storage.bindingtuple.BindingTuple;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Aggregator which will iterate on the {@link BindingTuple}s to aggregate the result.
 * The Aggregator is not well fit into Expression, because it has side effect.
 * But we still want to make it implement {@link Expression} interface to make
 * {@link ExpressionAnalyzer} easier.
 */
@EqualsAndHashCode
@RequiredArgsConstructor
public abstract class Aggregator<S extends AggregationState>
    implements FunctionImplementation, Expression {
  @Getter
  private final FunctionName functionName;
  @Getter
  private final List<Expression> arguments;
  protected final ExprCoreType returnType;

  /**
   * Create an {@link AggregationState} which will be used for aggregation.
   */
  public abstract S create();

  /**
   * Iterate on the {@link BindingTuple}.
   *
   * @param tuple {@link BindingTuple}
   * @param state {@link AggregationState}
   * @return {@link AggregationState}
   */
  public abstract S iterate(BindingTuple tuple, S state);

  @Override
  public ExprValue valueOf(Environment<Expression, ExprValue> valueEnv) {
    throw new ExpressionEvaluationException(
        String.format("can't evaluate on aggregator: %s", functionName));
  }

  @Override
  public ExprType type() {
    return returnType;
  }

  @Override
  public <T, C> T accept(ExpressionNodeVisitor<T, C> visitor, C context) {
    return visitor.visitAggregator(this, context);
  }

}
