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

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import com.amazon.opendistroforelasticsearch.sql.expression.window.WindowDefinition;
import com.google.common.collect.PeekingIterator;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Conceptually, cumulative window frame should hold all seen rows till next partition.
 * This class is actually an optimized version that only hold previous and current row. This is
 * efficient and sufficient for ranking and aggregate window function support for now, though need
 * to add "real" cumulative frame implementation in future as needed.
 */
@EqualsAndHashCode
@RequiredArgsConstructor
@ToString
public class CurrentRowWindowFrame implements WindowFrame {

  @Getter
  private final WindowDefinition windowDefinition;

  private ExprValue previous;
  private ExprValue current;

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
  public void load(PeekingIterator<ExprValue> it) {
    previous = current;
    current = it.next();
  }

  @Override
  public ExprValue current() {
    return current;
  }

  public ExprValue previous() {
    return previous;
  }

  private List<ExprValue> resolve(List<Expression> expressions, ExprValue row) {
    Environment<Expression, ExprValue> valueEnv = row.bindingTuples();
    return expressions.stream()
                      .map(expr -> expr.valueOf(valueEnv))
                      .collect(Collectors.toList());
  }

  /**
   * Current row window frame won't pre-fetch any row ahead.
   * So always return false as nothing "cached" in frame.
   */
  @Override
  public boolean hasNext() {
    return false;
  }

  @Override
  public List<ExprValue> next() {
    return Collections.emptyList();
  }

}
