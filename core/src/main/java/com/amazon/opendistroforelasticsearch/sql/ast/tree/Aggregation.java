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

package com.amazon.opendistroforelasticsearch.sql.ast.tree;

import com.amazon.opendistroforelasticsearch.sql.ast.AbstractNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Argument;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Logical plan node of Aggregation, the interface for building aggregation actions in queries.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = false)
public class Aggregation extends UnresolvedPlan {
  private List<UnresolvedExpression> aggExprList;
  private List<UnresolvedExpression> sortExprList;
  private List<UnresolvedExpression> groupExprList;
  private List<Argument> argExprList;
  private UnresolvedPlan child;

  /**
   * Aggregation Constructor without argument.
   */
  public Aggregation(List<UnresolvedExpression> aggExprList,
                     List<UnresolvedExpression> sortExprList,
                     List<UnresolvedExpression> groupExprList) {
    this(aggExprList, sortExprList, groupExprList, Collections.emptyList());
  }

  /**
   * Aggregation Constructor.
   */
  public Aggregation(List<UnresolvedExpression> aggExprList,
                     List<UnresolvedExpression> sortExprList,
                     List<UnresolvedExpression> groupExprList,
                     List<Argument> argExprList) {
    this.aggExprList = aggExprList;
    this.sortExprList = sortExprList;
    this.groupExprList = groupExprList;
    this.argExprList = argExprList;
  }

  public boolean hasArgument() {
    return !aggExprList.isEmpty();
  }

  @Override
  public Aggregation attach(UnresolvedPlan child) {
    this.child = child;
    return this;
  }

  @Override
  public List<UnresolvedPlan> getChild() {
    return ImmutableList.of(this.child);
  }

  @Override
  public <T, C> T accept(AbstractNodeVisitor<T, C> nodeVisitor, C context) {
    return nodeVisitor.visitAggregation(this, context);
  }
}
