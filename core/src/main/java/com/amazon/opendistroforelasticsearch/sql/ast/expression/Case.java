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

package com.amazon.opendistroforelasticsearch.sql.ast.expression;

import com.amazon.opendistroforelasticsearch.sql.ast.AbstractNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.ast.Node;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * AST node that represents CASE clause similar as switch statement in programming language.
 */
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Getter
@ToString
public class Case extends UnresolvedExpression {

  /**
   * Value to be compared by WHEN statements. Null in the case of CASE WHEN conditions.
   */
  private final UnresolvedExpression caseValue;

  /**
   * Expression list that represents WHEN statements. Each is a mapping from condition
   * to its result.
   */
  private final List<When> whenStatements;

  /**
   * Expression that represents ELSE statement result.
   */
  private final UnresolvedExpression elseStatement;

  @Override
  public List<? extends Node> getChild() {
    return Collections.emptyList(); //TODO: AST when is required?
  }

  @Override
  public <T, C> T accept(AbstractNodeVisitor<T, C> nodeVisitor, C context) {
    return nodeVisitor.visitCase(this, context);
  }

}
