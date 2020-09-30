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

package com.amazon.opendistroforelasticsearch.sql.planner.logical;

import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import java.util.Collections;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Logical Evaluation represent the evaluation operation. The {@link LogicalEval#expressions} is a
 * list assignment operation. e.g. velocity = distance/speed, then the Pair is (velocity,
 * distance/speed).
 */
@ToString
@EqualsAndHashCode(callSuper = true)
public class LogicalEval extends LogicalPlan {

  @Getter
  private final List<Pair<ReferenceExpression, Expression>> expressions;

  /**
   * Constructor of LogicalEval.
   */
  public LogicalEval(
      LogicalPlan child,
      List<Pair<ReferenceExpression, Expression>> expressions) {
    super(Collections.singletonList(child));
    this.expressions = expressions;
  }

  @Override
  public <R, C> R accept(LogicalPlanNodeVisitor<R, C> visitor, C context) {
    return visitor.visitEval(this, context);
  }
}
