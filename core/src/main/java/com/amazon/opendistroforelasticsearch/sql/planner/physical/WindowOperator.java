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

package com.amazon.opendistroforelasticsearch.sql.planner.physical;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTupleValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.window.WindowDefinition;
import com.amazon.opendistroforelasticsearch.sql.expression.window.WindowFrame;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * Physical operator for window function computation.
 */
@EqualsAndHashCode(callSuper = false)
@ToString
public class WindowOperator extends PhysicalPlan {
  private final PhysicalPlan input;
  private final Expression windowFunction;
  private final WindowDefinition windowDefinition;

  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private final WindowFrame windowFrame;

  /**
   * Initialize window operator.
   * @param input             child operator
   * @param windowFunction    window function
   * @param windowDefinition  window definition
   */
  public WindowOperator(PhysicalPlan input,
                        Expression windowFunction,
                        WindowDefinition windowDefinition) {
    this.input = input;
    this.windowFunction = windowFunction;
    this.windowDefinition = windowDefinition;
    this.windowFrame = new WindowFrame(windowDefinition);
  }

  @Override
  public <R, C> R accept(PhysicalPlanNodeVisitor<R, C> visitor, C context) {
    return visitor.visitWindow(this, context);
  }

  @Override
  public List<PhysicalPlan> getChild() {
    return Collections.singletonList(input);
  }

  @Override
  public boolean hasNext() {
    return input.hasNext();
  }

  @Override
  public ExprValue next() {
    // Preserve all columns in tuple
    ExprTupleValue inputValue = (ExprTupleValue) input.next();
    ImmutableMap.Builder<String, ExprValue> mapBuilder = new ImmutableMap.Builder<>();
    inputValue.tupleValue().forEach(mapBuilder::put);

    // Enrich by adding window function calculated result
    windowFrame.add(inputValue);
    ExprValue exprValue = windowFunction.valueOf(windowFrame);
    mapBuilder.put(windowFunction.toString(), exprValue);
    return ExprTupleValue.fromExprValueMap(mapBuilder.build());
  }

}
