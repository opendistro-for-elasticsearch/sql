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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * The limit operator sets a window, to and block the rows out of the window
 * and allow only the result subset within this window to the output.
 *
 * <p>The result subset is enframed from original result with {@link LimitOperator#offset}
 * as the offset and {@link LimitOperator#limit} as the size, thus the output
 * is the subset of the original result set that has indices from {index + 1} to {index + limit}.
 * Special cases might occur where the result subset has a size smaller than expected {limit},
 * it occurs when the original result set has a size smaller than {index + limit},
 * or even not greater than the offset. The latter results in an empty output.</p>
 */
@RequiredArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class LimitOperator extends PhysicalPlan {
  private final PhysicalPlan input;
  private final Integer limit;
  private final Integer offset;
  private Integer count = 0;

  @Override
  public void open() {
    super.open();

    // skip the leading rows of offset size
    while (input.hasNext() && count < offset) {
      count++;
      input.next();
    }
  }

  @Override
  public boolean hasNext() {
    return input.hasNext() && count < offset + limit;
  }

  @Override
  public ExprValue next() {
    count++;
    return input.next();
  }

  @Override
  public <R, C> R accept(PhysicalPlanNodeVisitor<R, C> visitor, C context) {
    return visitor.visitLimit(this, context);
  }

  @Override
  public List<PhysicalPlan> getChild() {
    return ImmutableList.of(input);
  }

}
