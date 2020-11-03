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

package com.amazon.opendistroforelasticsearch.sql.expression.conditional.cases;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprNullValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@EqualsAndHashCode
@Getter
@ToString
public class CaseValue implements Expression {

  private final List<When> whenClauses;
  private Expression defaultResult;

  @Override
  public ExprValue valueOf(Environment<Expression, ExprValue> valueEnv) {
    for (When when : whenClauses) {
      if (when.isTrue(valueEnv)) {
        return when.valueOf(valueEnv);
      }
    }
    return (defaultResult == null) ? ExprNullValue.of() : defaultResult.valueOf(valueEnv);
  }

  @Override
  public ExprType type() {
    return whenClauses.get(0).type();
  }

  @Override
  public <T, C> T accept(ExpressionNodeVisitor<T, C> visitor, C context) {
    return visitor.visitCase(this, context);
  }

}
