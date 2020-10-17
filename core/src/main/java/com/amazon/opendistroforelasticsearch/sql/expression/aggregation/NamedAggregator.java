/*
 *
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.expression.aggregation;

import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.storage.bindingtuple.BindingTuple;
import com.google.common.base.Strings;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * NamedAggregator expression that represents expression with name.
 * Please see more details in associated unresolved expression operator
 * {@link com.amazon.opendistroforelasticsearch.sql.ast.expression.Alias}.
 */
@EqualsAndHashCode(callSuper = false)
public class NamedAggregator extends Aggregator<AggregationState> {

  /**
   * Aggregator name.
   */
  private final String name;

  /**
   * Aggregator that being named.
   */
  @Getter
  private final Aggregator<AggregationState> delegated;

  /**
   * NamedAggregator.
   *
   * @param name name
   * @param delegated delegated
   */
  public NamedAggregator(
      String name,
      Aggregator<AggregationState> delegated) {
    super(delegated.getFunctionName(), delegated.getArguments(), delegated.returnType);
    this.name = name;
    this.delegated = delegated;
  }

  @Override
  public AggregationState create() {
    return delegated.create();
  }

  @Override
  public AggregationState iterate(BindingTuple tuple, AggregationState state) {
    return delegated.iterate(tuple, state);
  }

  /**
   * Get expression name using name or its alias (if it's present).
   * @return  expression name
   */
  public String getName() {
    return name;
  }

  @Override
  public <T, C> T accept(ExpressionNodeVisitor<T, C> visitor, C context) {
    return visitor.visitNamedAggregator(this, context);
  }

  @Override
  public String toString() {
    return getName();
  }

}
