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
import java.util.Arrays;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * Argument.
 */
@Getter
@ToString
@EqualsAndHashCode(callSuper = false)
public class UnresolvedArgument extends UnresolvedExpression {
  private final String argName;
  private final UnresolvedExpression value;

  public UnresolvedArgument(String argName, UnresolvedExpression value) {
    this.argName = argName;
    this.value = value;
  }

  @Override
  public List<UnresolvedExpression> getChild() {
    return Arrays.asList(value);
  }

  @Override
  public <R, C> R accept(AbstractNodeVisitor<R, C> nodeVisitor, C context) {
    return nodeVisitor.visitUnresolvedArgument(this, context);
  }
}
