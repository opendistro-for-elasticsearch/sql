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

package com.amazon.opendistroforelasticsearch.sql.ast.expression;

import com.amazon.opendistroforelasticsearch.sql.ast.AbstractNodeVisitor;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class Interval extends UnresolvedExpression {

  private final UnresolvedExpression value;
  private final IntervalUnit unit;

  public Interval(UnresolvedExpression value, String unit) {
    this.value = value;
    this.unit = IntervalUnit.of(unit);
  }

  @Override
  public List<UnresolvedExpression> getChild() {
    return Collections.singletonList(value);
  }

  @Override
  public <R, C> R accept(AbstractNodeVisitor<R, C> nodeVisitor, C context) {
    return nodeVisitor.visitInterval(this, context);
  }
}
