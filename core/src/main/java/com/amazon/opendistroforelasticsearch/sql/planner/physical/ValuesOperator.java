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

package com.amazon.opendistroforelasticsearch.sql.planner.physical;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprCollectionValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.expression.LiteralExpression;
import com.google.common.collect.ImmutableList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Physical operator for Values.
 */
@ToString
@EqualsAndHashCode(callSuper = false, of = "values")
public class ValuesOperator extends PhysicalPlan {

  /**
   * Original values list for print and equality check.
   */
  @Getter
  private final List<List<LiteralExpression>> values;

  /**
   * Values iterator.
   */
  private final Iterator<List<LiteralExpression>> valuesIterator;

  public ValuesOperator(List<List<LiteralExpression>> values) {
    this.values = values;
    this.valuesIterator = values.iterator();
  }

  @Override
  public <R, C> R accept(PhysicalPlanNodeVisitor<R, C> visitor, C context) {
    return visitor.visitValues(this, context);
  }

  @Override
  public List<PhysicalPlan> getChild() {
    return ImmutableList.of();
  }

  @Override
  public boolean hasNext() {
    return valuesIterator.hasNext();
  }

  @Override
  public ExprValue next() {
    List<ExprValue> values = valuesIterator.next().stream()
                                           .map(expr -> expr.valueOf(null))
                                           .collect(Collectors.toList());
    return new ExprCollectionValue(values);
  }

}
