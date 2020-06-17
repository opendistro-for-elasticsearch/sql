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

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprType.STRUCT;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTupleValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Remove the fields specified in {@link RemoveOperator#removeList} from input.
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class RemoveOperator extends PhysicalPlan {
  private final PhysicalPlan input;
  private final Set<Expression> removeList;

  @Override
  public <R, C> R accept(PhysicalPlanNodeVisitor<R, C> visitor, C context) {
    return visitor.visitRemove(this, context);
  }

  @Override
  public List<PhysicalPlan> getChild() {
    return Collections.singletonList(input);
  }

  @Override
  public boolean hasNext() {
    return input.hasNext();
  }

  @Override
  public ExprValue next() {
    ExprValue inputValue = input.next();
    if (STRUCT == inputValue.type()) {
      ImmutableMap.Builder<String, ExprValue> mapBuilder = new Builder<>();
      Map<String, ExprValue> tupleValue = ExprValueUtils.getTupleValue(inputValue);
      for (Entry<String, ExprValue> valueEntry : tupleValue.entrySet()) {
        if (!removeList.contains(DSL.ref(valueEntry.getKey()))) {
          mapBuilder.put(valueEntry);
        }
      }
      return ExprTupleValue.fromExprValueMap(mapBuilder.build());
    } else {
      return inputValue;
    }
  }
}
