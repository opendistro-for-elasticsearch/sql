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

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.UNKNOWN;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprNullValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionName;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * A CASE clause is very different from a regular function. Functions have well-defined signature,
 * though CASE clause is more like a function implementation which requires type check "manually".
 */
@EqualsAndHashCode(callSuper = false)
@Getter
@ToString
public class CaseClause extends FunctionExpression {

  /**
   * List of WHEN clauses.
   */
  private final List<WhenClause> whenClauses;

  /**
   * Default result if none of WHEN conditions match.
   */
  private final Expression defaultResult;

  /**
   * Initialize case clause.
   */
  public CaseClause(List<WhenClause> whenClauses, Expression defaultResult) {
    super(FunctionName.of("case"), concatArgs(whenClauses, defaultResult));
    this.whenClauses = whenClauses;
    this.defaultResult = defaultResult;
  }

  @Override
  public ExprValue valueOf(Environment<Expression, ExprValue> valueEnv) {
    for (WhenClause when : whenClauses) {
      if (when.isTrue(valueEnv)) {
        return when.valueOf(valueEnv);
      }
    }
    return (defaultResult == null) ? ExprNullValue.of() : defaultResult.valueOf(valueEnv);
  }

  @Override
  public ExprType type() {
    List<ExprType> types = allResultTypes();

    // Return unknown if all WHEN/ELSE return NULL
    return types.isEmpty() ? UNKNOWN : types.get(0);
  }

  @Override
  public <T, C> T accept(ExpressionNodeVisitor<T, C> visitor, C context) {
    return visitor.visitCase(this, context);
  }

  /**
   * Get types of each result in WHEN clause and ELSE clause.
   * Exclude UNKNOWN type from NULL literal which means NULL in THEN or ELSE clause
   * is not included in result.
   * @return all result types. Use list so caller can generate friendly error message.
   */
  public List<ExprType> allResultTypes() {
    List<ExprType> types = whenClauses.stream()
                                      .map(WhenClause::type)
                                      .collect(Collectors.toList());
    if (defaultResult != null) {
      types.add(defaultResult.type());
    }

    types.removeIf(type -> (type == UNKNOWN));
    return types;
  }

  private static List<Expression> concatArgs(List<WhenClause> whenClauses,
                                             Expression defaultResult) {
    ImmutableList.Builder<Expression> args = ImmutableList.builder();
    whenClauses.forEach(args::add);

    if (defaultResult != null) {
      args.add(defaultResult);
    }
    return args.build();
  }

}
