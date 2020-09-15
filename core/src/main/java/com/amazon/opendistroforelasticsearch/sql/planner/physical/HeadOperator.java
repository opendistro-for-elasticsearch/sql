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

package com.amazon.opendistroforelasticsearch.sql.planner.physical;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprBooleanValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.LiteralExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.operator.predicate.BinaryPredicateOperator;
import java.util.Collections;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

/**
 * The Head operator returns the first {@link HeadOperator#number} number of results until the
 * {@link HeadOperator#whileExpr} evaluates to true. If {@link HeadOperator#keepLast} is true then
 * first result which evalutes {@link HeadOperator#whileExpr} to false is also returned.
 * The NULL and MISSING are handled by the logic defined in {@link BinaryPredicateOperator}.
 */
@Getter
@EqualsAndHashCode
public class HeadOperator extends PhysicalPlan {

  @Getter
  private final PhysicalPlan input;
  @Getter
  private final Boolean keepLast;
  @Getter
  private final Expression whileExpr;
  @Getter
  private final Integer number;

  private static final Integer DEFAULT_LIMIT = 10;
  private static final Boolean IGNORE_LAST = false;

  @EqualsAndHashCode.Exclude
  private int recordCount = 0;
  @EqualsAndHashCode.Exclude
  private boolean foundFirstFalse = false;
  @EqualsAndHashCode.Exclude
  private ExprValue next;

  @NonNull
  public HeadOperator(PhysicalPlan input) {
    this(input, IGNORE_LAST, new LiteralExpression(ExprBooleanValue.of(true)), DEFAULT_LIMIT);
  }

  /**
   * HeadOperator Constructor.
   *
   * @param input     Input {@link PhysicalPlan}
   * @param keepLast  Controls whether the last result in the result set is retained. The last
   *                  result returned is the result that caused the whileExpr to evaluate to false
   *                  or NULL.
   * @param whileExpr The search returns results until this expression evaluates to false
   * @param number    Number of specified results
   */
  @NonNull
  public HeadOperator(PhysicalPlan input, Boolean keepLast, Expression whileExpr, Integer number) {
    this.input = input;
    this.keepLast = keepLast;
    this.whileExpr = whileExpr;
    this.number = number;
  }

  @Override
  public <R, C> R accept(PhysicalPlanNodeVisitor<R, C> visitor, C context) {
    return visitor.visitHead(this, context);
  }

  @Override
  public List<PhysicalPlan> getChild() {
    return Collections.singletonList(input);
  }

  @Override
  public boolean hasNext() {
    if (!input.hasNext() || foundFirstFalse || (recordCount >= number)) {
      return false;
    }
    ExprValue inputVal = input.next();
    ExprValue exprValue = whileExpr.valueOf(inputVal.bindingTuples());
    if (exprValue.isNull() || exprValue.isMissing() || !(exprValue.booleanValue())) {
      // First false is when we decide whether to keep the last value
      foundFirstFalse = true;
      if (!keepLast) {
        return false;
      }
    }
    this.next = inputVal;
    recordCount++;
    return true;
  }

  @Override
  public ExprValue next() {
    return this.next;
  }
}
