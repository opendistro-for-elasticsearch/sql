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
 * Logical plan node of Project, the interface for building the list of searching fields.
 */
@ToString
@Getter
@EqualsAndHashCode(callSuper = false)
public class Project extends UnresolvedPlan {
  @Setter
  private List<UnresolvedExpression> projectList;
  private List<Argument> argExprList;
  private UnresolvedPlan child;

  public Project(List<UnresolvedExpression> projectList) {
    this.projectList = projectList;
    this.argExprList = Collections.emptyList();
  }

  public Project(List<UnresolvedExpression> projectList, List<Argument> argExprList) {
    this.projectList = projectList;
    this.argExprList = argExprList;
  }

  public boolean hasArgument() {
    return !argExprList.isEmpty();
  }

  /**
   * The Project could been used to exclude fields from the source.
   */
  public boolean isExcluded() {
    if (hasArgument()) {
      Argument argument = argExprList.get(0);
      return (Boolean) argument.getValue().getValue();
    }
    return false;
  }

  @Override
  public Project attach(UnresolvedPlan child) {
    this.child = child;
    return this;
  }

  @Override
  public List<UnresolvedPlan> getChild() {
    return ImmutableList.of(this.child);
  }

  @Override
  public <T, C> T accept(AbstractNodeVisitor<T, C> nodeVisitor, C context) {

    return nodeVisitor.visitProject(this, context);
  }
}
