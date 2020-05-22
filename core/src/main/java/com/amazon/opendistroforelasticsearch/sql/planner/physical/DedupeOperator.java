/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.storage.bindingtuple.BindingTuple;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

/**
 * Dedupe operator. Dedupe the input {@link ExprValue} by using the {@link
 * DedupeOperator#dedupeList}
 * The result order follow the input order.
 *
 */
public class DedupeOperator extends PhysicalPlan {
  private final PhysicalPlan input;
  private final List<Expression> dedupeList;
  private final Decider decider;
  private Integer allowedDuplication = 1;
  private Boolean keepEmpty = false;
  private Boolean consecutive = false;
  private ExprValue next;

  private static final Predicate<ExprValue> NULL_OR_MISSING = v -> v.isNull() || v.isMissing();

  public DedupeOperator(PhysicalPlan input, List<Expression> dedupeList) {
    this.input = input;
    this.dedupeList = dedupeList;
    this.decider = new Dedupe();
  }

  public DedupeOperator(
      PhysicalPlan input,
      List<Expression> dedupeList,
      Integer allowedDuplication,
      Boolean keepEmpty,
      Boolean consecutive) {
    this.input = input;
    this.dedupeList = dedupeList;
    this.allowedDuplication = allowedDuplication;
    this.keepEmpty = keepEmpty;
    this.consecutive = consecutive;
    this.decider = this.consecutive ? new ConsecutiveDedupe() : new Dedupe();
  }

  @Override
  public <R, C> R accept(PhysicalPlanNodeVisitor<R, C> visitor, C context) {
    return visitor.visitDedupe(this, context);
  }

  @Override
  public List<PhysicalPlan> getChild() {
    return Collections.singletonList(input);
  }

  @Override
  public boolean hasNext() {
    while (input.hasNext()) {
      ExprValue next = input.next();
      if (decider.keep(next)) {
        this.next = next;
        return true;
      }
    }
    return false;
  }

  @Override
  public ExprValue next() {
    return this.next;
  }

  /**
   * The Decider test the {@link ExprValue} should be keep (return true) or ignored (return false).
   *
   * <p>If any value evaluted by {@link DedupeOperator#dedupeList} is NULL or MISSING, then the
   * return value is decided by keepEmpty option, default value is ignore.
   */
  abstract class Decider {
    public boolean keep(ExprValue value) {
      BindingTuple bindingTuple = value.bindingTuples();
      ImmutableList.Builder<ExprValue> dedupeKeyBuilder = new ImmutableList.Builder<>();
      for (Expression expression : dedupeList) {
        ExprValue exprValue = expression.valueOf(bindingTuple);
        if (NULL_OR_MISSING.test(exprValue)) {
          return keepEmpty;
        }
        dedupeKeyBuilder.add(exprValue);
      }
      List<ExprValue> dedupeKey = dedupeKeyBuilder.build();
      int seenTimes = seenTimes(dedupeKey);
      return seenTimes <= allowedDuplication;
    }

    /**
     * Return how many times the dedupeKey has been seen before. The side effect is the seen times
     * will add 1 times after calling this function.
     */
    abstract int seenTimes(List<ExprValue> dedupeKey);
  }

  class Dedupe extends Decider {
    private final Map<List<ExprValue>, Integer> seenMap = new ConcurrentHashMap<>();

    @Override
    int seenTimes(List<ExprValue> dedupeKey) {
      seenMap.putIfAbsent(dedupeKey, 0);
      return seenMap.computeIfPresent(dedupeKey, (k, v) -> v + 1);
    }
  }

  class ConsecutiveDedupe extends Decider {
    private List<ExprValue> lastSeenDedupeKey = null;
    private Integer consecutiveCount = 0;

    @Override
    int seenTimes(List<ExprValue> dedupeKey) {
      if (dedupeKey.equals(lastSeenDedupeKey)) {
        return ++consecutiveCount;
      } else {
        lastSeenDedupeKey = dedupeKey;
        consecutiveCount = 1;
        return consecutiveCount;
      }
    }
  }
}
