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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 * Expression node of scalar function.
 * Params include function name (@funcName) and function arguments (@funcArgs)
 */
@Getter
@EqualsAndHashCode(callSuper = false)
@RequiredArgsConstructor
public class Function extends UnresolvedExpression {
  private final String funcName;
  private final List<UnresolvedExpression> funcArgs;

  @Override
  public List<UnresolvedExpression> getChild() {
    return Collections.unmodifiableList(funcArgs);
  }

  @Override
  public <R, C> R accept(AbstractNodeVisitor<R, C> nodeVisitor, C context) {
    return nodeVisitor.visitFunction(this, context);
  }

  @Override
  public String toString() {
    return String.format("%s(%s)", funcName,
        funcArgs.stream()
            .map(Object::toString)
            .collect(Collectors.joining(", ")));
  }
}
