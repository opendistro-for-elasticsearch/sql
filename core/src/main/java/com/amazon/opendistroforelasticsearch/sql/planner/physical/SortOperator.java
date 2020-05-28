/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.planner.physical;

import static com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.NullOrder.NULL_FIRST;
import static com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.SortOrder.ASC;

import com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.SortOption;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.utils.ExprValueOrdering;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.SortOperator.Sorter.SorterBuilder;
import com.google.common.collect.Iterators;
import com.google.common.collect.TreeMultiset;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Singular;
import lombok.ToString;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Sort Operator. The input data is sorted by the sort fields in the {@link SortOperator#sortList}.
 * The sort field is specified by the {@link Expression} with {@link SortOption}. The count indicate
 * how many sorted result should been return.
 */
@ToString
@EqualsAndHashCode
public class SortOperator extends PhysicalPlan {
  private final PhysicalPlan input;
  private final Integer count;
  private final List<Pair<SortOption, Expression>> sortList;
  @EqualsAndHashCode.Exclude private final Sorter sorter;
  @EqualsAndHashCode.Exclude private Iterator<ExprValue> iterator;

  public SortOperator(
      PhysicalPlan input, Integer count, List<Pair<SortOption, Expression>> sortList) {
    this.input = input;
    this.count = count;
    this.sortList = sortList;
    SorterBuilder sorterBuilder = Sorter.builder();
    for (Pair<SortOption, Expression> pair : sortList) {
      SortOption option = pair.getLeft();
      ExprValueOrdering ordering =
          ASC.equals(option.getSortOrder())
              ? ExprValueOrdering.natural()
              : ExprValueOrdering.natural().reverse();
      ordering =
          NULL_FIRST.equals(option.getNullOrder()) ? ordering.nullsFirst() : ordering.nullsLast();
      sorterBuilder.comparator(Pair.of(pair.getRight(), ordering));
    }
    this.sorter = sorterBuilder.build();
  }

  @Override
  public <R, C> R accept(PhysicalPlanNodeVisitor<R, C> visitor, C context) {
    return null;
  }

  @Override
  public void open() {
    super.open();
    PriorityQueue<ExprValue> sorted = new PriorityQueue<>(1, sorter::compare);
    while (input.hasNext()) {
      sorted.add(input.next());
    }
    iterator = Iterators.limit(new Iterator<ExprValue>() {
      @Override
      public boolean hasNext() {
        return !sorted.isEmpty();
      }

      @Override
      public ExprValue next() {
        return sorted.poll();
      }
    }, count);
  }

  @Override
  public List<PhysicalPlan> getChild() {
    return Collections.singletonList(input);
  }

  @Override
  public boolean hasNext() {
    return iterator.hasNext();
  }

  @Override
  public ExprValue next() {
    return iterator.next();
  }

  @Builder
  public static class Sorter implements Comparator<ExprValue> {
    @Singular private final List<Pair<Expression, Comparator<ExprValue>>> comparators;

    @Override
    public int compare(ExprValue o1, ExprValue o2) {
      for (Pair<Expression, Comparator<ExprValue>> comparator : comparators) {
        Expression expression = comparator.getKey();
        int result =
            comparator
                .getValue()
                .compare(
                    expression.valueOf(o1.bindingTuples()), expression.valueOf(o2.bindingTuples()));
        if (result != 0) {
          return result;
        }
      }
      return 0;
    }
  }
}
