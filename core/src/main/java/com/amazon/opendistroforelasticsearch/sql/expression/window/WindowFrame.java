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

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import java.util.ArrayDeque;
import java.util.Deque;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@EqualsAndHashCode
@RequiredArgsConstructor
@ToString
public class WindowFrame implements Environment<Expression, ExprValue> {
  private final Deque<ExprValue> rows = new ArrayDeque<>();

  @Override
  public ExprValue resolve(Expression var) {
    //return var.valueOf(current().bindingTuples());
    return null;
  }

  /*
  public ExprValue preceding(Expression var, int offset) {
    return var.valueOf()
  }
  */

  public void add(ExprValue row) {
    rows.push(row);
  }

  public ExprValue get(int offset) {
    return rows.peekLast();
  }
}
