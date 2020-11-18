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

package com.amazon.opendistroforelasticsearch.sql.expression.conditional.cases;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * WHEN clause that consists of a condition and a result corresponding.
 */
@EqualsAndHashCode
@Getter
@RequiredArgsConstructor
@ToString
public class WhenClause implements Expression {

  /**
   * Condition that must be a predicate.
   */
  private final Expression condition;

  /**
   * Result to return if condition is evaluated to true.
   */
  private final Expression result;

  public boolean isTrue(Environment<Expression, ExprValue> valueEnv) {
    return condition.valueOf(valueEnv).booleanValue();
  }

  @Override
  public ExprValue valueOf(Environment<Expression, ExprValue> valueEnv) {
    return result.valueOf(valueEnv);
  }

  @Override
  public ExprType type() {
    return result.type();
  }

  @Override
  public <T, C> T accept(ExpressionNodeVisitor<T, C> visitor, C context) {
    return visitor.visitWhen(this, context);
  }

}
