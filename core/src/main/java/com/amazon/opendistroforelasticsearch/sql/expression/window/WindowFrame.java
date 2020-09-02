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
public class WindowFrame implements Environment<Expression, ExprValue> {
  private final List<Expression> sortList;

  private int rowCount;
  private ExprTupleValue current;

  @Override
  public ExprValue resolve(Expression var) {
    return var.valueOf(current.bindingTuples());
  }

  public boolean isNewPartition() {
    return (rowCount == 1);
  }

  public boolean isPrecedingDifferent() {
    return false;
  }

  public void add(ExprTupleValue row) {
    rowCount++;
    current = row;
  }

  public void reset() {
    rowCount = 0;
    current = null;
  }

}
