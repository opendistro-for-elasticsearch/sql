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
import com.amazon.opendistroforelasticsearch.sql.expression.NamedExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.window.WindowDefinition;
import com.amazon.opendistroforelasticsearch.sql.expression.window.WindowFunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.window.frame.WindowFrame;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
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
  private final NamedExpression windowFunction;

  @Getter
  private final WindowDefinition windowDefinition;

  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private final WindowFrame windowFrame;

  /**
   * Peeking iterator that can peek next element which is required
   * by window frame such as peer frame to prefetch all rows related
   * to same peer (of same sorting key).
   */
  @EqualsAndHashCode.Exclude
  @ToString.Exclude
  private final PeekingIterator<ExprValue> peekingIterator;

  /**
   * Initialize window operator.
   * @param input             child operator
   * @param windowFunction    window function
   * @param windowDefinition  window definition
   */
  public WindowOperator(PhysicalPlan input,
                        NamedExpression windowFunction,
                        WindowDefinition windowDefinition) {
    this.input = input;
    this.windowFunction = windowFunction;
    this.windowDefinition = windowDefinition;
    this.windowFrame = createWindowFrame();
    this.peekingIterator = Iterators.peekingIterator(input);
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
    return peekingIterator.hasNext() || windowFrame.hasNext();
  }

  @Override
  public ExprValue next() {
    windowFrame.load(peekingIterator);
    return enrichCurrentRowByWindowFunctionResult();
  }

  private WindowFrame createWindowFrame() {
    return ((WindowFunctionExpression) windowFunction.getDelegated())
        .createWindowFrame(windowDefinition);
  }

  private ExprValue enrichCurrentRowByWindowFunctionResult() {
    ImmutableMap.Builder<String, ExprValue> mapBuilder = new ImmutableMap.Builder<>();
    preserveAllOriginalColumns(mapBuilder);
    addWindowFunctionResultColumn(mapBuilder);
    return ExprTupleValue.fromExprValueMap(mapBuilder.build());
  }

  private void preserveAllOriginalColumns(ImmutableMap.Builder<String, ExprValue> mapBuilder) {
    ExprValue inputValue = windowFrame.current();
    inputValue.tupleValue().forEach(mapBuilder::put);
  }

  private void addWindowFunctionResultColumn(ImmutableMap.Builder<String, ExprValue> mapBuilder) {
    ExprValue exprValue = windowFunction.valueOf(windowFrame);
    mapBuilder.put(windowFunction.getName(), exprValue);
  }

}
