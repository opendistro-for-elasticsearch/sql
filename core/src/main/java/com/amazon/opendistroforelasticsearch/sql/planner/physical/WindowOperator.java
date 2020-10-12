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
import com.amazon.opendistroforelasticsearch.sql.expression.window.CumulativeWindowFrame;
import com.amazon.opendistroforelasticsearch.sql.expression.window.WindowDefinition;
import com.amazon.opendistroforelasticsearch.sql.expression.window.frame.WindowFrame;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Physical operator for window function computation.
 */
@EqualsAndHashCode(callSuper = false)
@ToString
public class WindowOperator extends PhysicalPlan {
  @Getter
  private final PhysicalPlan input;

  @Getter
  private final Expression windowFunction;

  @Getter
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
    this.windowFrame = createWindowFrame();
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
    loadRowsIntoWindowFrame();
    return enrichCurrentRowByWindowFunctionResult();
  }

  /**
   * For now cumulative window frame is returned always. When frame definition is supported:
   *  1. Ranking window functions: ignore frame definition and always operates on entire window.
   *  2. Aggregate window functions: operates on cumulative or sliding window based on definition.
   */
  private WindowFrame createWindowFrame() {
    return new CumulativeWindowFrame(windowDefinition);
  }

  /**
   * For now always load next row into window frame. In future, how/how many rows loaded
   * should be based on window frame type.
   */
  private void loadRowsIntoWindowFrame() {
    windowFrame.add((ExprTupleValue) input.next());
  }

  private ExprValue enrichCurrentRowByWindowFunctionResult() {
    ImmutableMap.Builder<String, ExprValue> mapBuilder = new ImmutableMap.Builder<>();
    preserveAllOriginalColumns(mapBuilder);
    addWindowFunctionResultColumn(mapBuilder);
    return ExprTupleValue.fromExprValueMap(mapBuilder.build());
  }

  private void preserveAllOriginalColumns(ImmutableMap.Builder<String, ExprValue> mapBuilder) {
    ExprTupleValue inputValue = windowFrame.get(windowFrame.currentIndex());
    inputValue.tupleValue().forEach(mapBuilder::put);
  }

  private void addWindowFunctionResultColumn(ImmutableMap.Builder<String, ExprValue> mapBuilder) {
    ExprValue exprValue = windowFunction.valueOf(windowFrame);
    mapBuilder.put(windowFunction.toString(), exprValue);
  }

}
