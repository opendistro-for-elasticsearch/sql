/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.opendistroforelasticsearch.sql.ast.expression;

import com.amazon.opendistroforelasticsearch.sql.ast.AbstractNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.common.utils.StringUtils;
import java.util.Collections;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Expression node of aggregate functions.
 * Params include aggregate function name (AVG, SUM, MAX etc.) and the field to aggregate.
 */
@Getter
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class AggregateFunction extends UnresolvedExpression {
  private final String funcName;
  private final UnresolvedExpression field;
  private final List<UnresolvedExpression> argList;

  /**
   * Constructor.
   * @param funcName function name.
   * @param field {@link UnresolvedExpression}.
   */
  public AggregateFunction(String funcName, UnresolvedExpression field) {
    this.funcName = funcName;
    this.field = field;
    this.argList = Collections.emptyList();
  }

  @Override
  public List<UnresolvedExpression> getChild() {
    return Collections.singletonList(field);
  }

  @Override
  public <R, C> R accept(AbstractNodeVisitor<R, C> nodeVisitor, C context) {
    return nodeVisitor.visitAggregateFunction(this, context);
  }

  @Override
  public String toString() {
    return StringUtils.format("%s(%s)", funcName, field);
  }
}
