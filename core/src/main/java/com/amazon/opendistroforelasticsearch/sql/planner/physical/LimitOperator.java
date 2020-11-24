/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.planner.physical;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class LimitOperator extends PhysicalPlan {
  private final PhysicalPlan input;
  private final Integer limit;
  private final Integer offset;
  private Iterator<ExprValue> iterator;

  @Override
  public void open() {
    super.open();

    List<ExprValue> results = new LinkedList<>();
    while (input.hasNext()) {
      results.add(input.next());
    }
    iterator = Iterators.limit(offset(results.iterator(), offset), limit);
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public ExprValue next() {
    return iterator.next();
  }

  @Override
  public <R, C> R accept(PhysicalPlanNodeVisitor<R, C> visitor, C context) {
    return visitor.visitLimit(this, context);
  }

  @Override
  public List<PhysicalPlan> getChild() {
    return ImmutableList.of(input);
  }

  /**
   * Util method to skip first rows of an {offset} amount from the starting of the result set.
   */
  private Iterator<ExprValue> offset(Iterator<ExprValue> iterator, Integer offset) {
    if (offset > 0 && iterator.hasNext()) {
      iterator.next();
      return offset(iterator, offset - 1);
    }
    return iterator;
  }

}
