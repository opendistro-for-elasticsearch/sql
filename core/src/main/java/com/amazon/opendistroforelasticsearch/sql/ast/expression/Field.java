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
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class Field extends UnresolvedExpression {
  private UnresolvedExpression field;
  private List<Argument> fieldArgs = Collections.emptyList();

  public Field(QualifiedName field) {
    this.field = field;
  }

  public Field(String field) {
    this.field = new QualifiedName(field);
  }

  public Field(String field, List<Argument> fieldArgs) {
    this.field = new QualifiedName(field);
    this.fieldArgs = fieldArgs;
  }

  public boolean hasArgument() {
    return !fieldArgs.isEmpty();
  }

  @Override
  public List<UnresolvedExpression> getChild() {
    return ImmutableList.of(this.field);
  }

  @Override
  public <R, C> R accept(AbstractNodeVisitor<R, C> nodeVisitor, C context) {
    return nodeVisitor.visitField(this, context);
  }
}
