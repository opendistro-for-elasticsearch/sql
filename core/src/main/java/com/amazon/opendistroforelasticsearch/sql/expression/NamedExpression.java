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

package com.amazon.opendistroforelasticsearch.sql.expression;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

/**
 * Named expression that represents expression with name. This is to preserve
 * original expression name or alias in query and avoid inferring its name
 * by reconstructing in toString() method.
 */
@AllArgsConstructor
@EqualsAndHashCode
@RequiredArgsConstructor
@ToString
public class NamedExpression implements Expression {

  /**
   * Expression name.
   */
  @Getter
  private final String name;

  /**
   * Expression that being named.
   */
  private final Expression delegation;

  /**
   * Optional alias.
   */
  private String alias;

  @Override
  public ExprValue valueOf(Environment<Expression, ExprValue> valueEnv) {
    return delegation.valueOf(valueEnv);
  }

  @Override
  public ExprType type() {
    return delegation.type();
  }

}
