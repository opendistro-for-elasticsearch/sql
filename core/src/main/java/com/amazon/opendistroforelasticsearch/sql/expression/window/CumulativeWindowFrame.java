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

package com.amazon.opendistroforelasticsearch.sql.expression.window;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTupleValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import com.amazon.opendistroforelasticsearch.sql.expression.window.frame.WindowFrame;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Conceptually, window function has access to all the data in the frame defined by
 * window definition. However, this needs to hold all preceding and following rows.
 * So here we only hold current row and assume window function maintains the previous
 * state. This workaround may not work for window functions that needs access to following
 * rows such as LAG.
 */
@EqualsAndHashCode
@Getter
@RequiredArgsConstructor
@ToString
public class CumulativeWindowFrame implements WindowFrame {

  private final WindowDefinition windowDefinition;

  private ExprTupleValue previous;
  private ExprTupleValue current;

  @Override
  public boolean isNewPartition() {
    Objects.requireNonNull(current);

    if (previous == null) {
      return true;
    }

    List<ExprValue> preValues = resolve(windowDefinition.getPartitionByList(), previous);
    List<ExprValue> curValues = resolve(windowDefinition.getPartitionByList(), current);
    return !preValues.equals(curValues);
  }

  @Override
  public int currentIndex() {
    return 1;
  }

  /**
   * Update frame with a new row.
   * @param row   data row
   */
  @Override
  public void add(ExprTupleValue row) {
    previous = current;
    current = row;
  }

  @Override
  public ExprTupleValue get(int index) {
    if (index != 0 && index != 1) {
      throw new IndexOutOfBoundsException("Index is out of boundary of window frame: " + index);
    }
    return (index == 0) ? previous : current;
  }

  private List<ExprValue> resolve(List<Expression> expressions, ExprTupleValue row) {
    Environment<Expression, ExprValue> valueEnv = row.bindingTuples();
    return expressions.stream()
                      .map(expr -> expr.valueOf(valueEnv))
                      .collect(Collectors.toList());
  }

}
