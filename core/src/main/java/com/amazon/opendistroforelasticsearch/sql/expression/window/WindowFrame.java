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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.tuple.Pair;

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
public class WindowFrame implements Environment<Expression, ExprValue> {

  private final WindowDefinition windowDefinition;

  private ExprTupleValue previous;
  private ExprTupleValue current;

  @Override
  public ExprValue resolve(Expression var) {
    return var.valueOf(current.bindingTuples());
  }

  private List<ExprValue> resolve(List<Expression> expressions, ExprTupleValue row) {
    Environment<Expression, ExprValue> valueEnv = row.bindingTuples();
    return expressions.stream()
                      .map(expr -> expr.valueOf(valueEnv))
                      .collect(Collectors.toList());
  }

  /**
   * Check is current row the beginning of a new partition according to window definition.
   * @return  true if a new partition begins here, otherwise false.
   */
  public boolean isNewPartition() {
    Objects.requireNonNull(current);

    if (previous == null) {
      return true;
    }

    List<ExprValue> preValues = resolve(windowDefinition.getPartitionByList(), previous);
    List<ExprValue> curValues = resolve(windowDefinition.getPartitionByList(), current);
    return !preValues.equals(curValues);
  }

  /**
   * Update frame with a new row.
   * @param row   data row
   */
  public void add(ExprTupleValue row) {
    previous = current;
    current = row;
  }

  /**
   * Resolve sort item values on certain position.
   * @param offset offset
   * @return       value list
   */
  public List<ExprValue> resolveSortItemValues(int offset) {
    List<Expression> sortItems =
        windowDefinition.getSortList().stream().map(Pair::getRight).collect(Collectors.toList());

    return (offset == 0) ? resolve(sortItems, current) : resolve(sortItems, previous);
  }

}
