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

package com.amazon.opendistroforelasticsearch.sql.expression.window.frame;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTupleValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;

/**
 * Window frame that represents a subset of a window which is all data accessible to
 * the window function when calculation. Basically there are 3 types of window frame:
 *  1) Entire window frame that holds all data of the window
 *  2) Cumulative window frame that accumulates one row by another
 *  3) Sliding window frame that maintains a sliding window of fixed size
 * Note that which type of window frame is used is determined by both window function itself
 * and frame definition in a window definition.
 */
public interface WindowFrame extends Environment<Expression, ExprValue> {

  @Override
  default ExprValue resolve(Expression var) {
    return var.valueOf(get(currentIndex()).bindingTuples());
  }

  /**
   * Check is current row the beginning of a new partition according to window definition.
   * @return  true if a new partition begins here, otherwise false.
   */
  boolean isNewPartition();

  /**
   * Get current row index in the frame.
   * @return index
   */
  int currentIndex();

  /**
   * Add a row to the window frame.
   * @param row   data row
   */
  void add(ExprTupleValue row);

  /**
   * Get a data rows within the frame by offset.
   * @param index  index starting from 0 to upper boundary
   * @return data row
   */
  ExprTupleValue get(int index);

}
