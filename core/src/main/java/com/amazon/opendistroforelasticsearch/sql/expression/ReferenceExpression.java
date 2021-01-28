/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.expression;

import static com.amazon.opendistroforelasticsearch.sql.utils.ExpressionUtils.PATH_SEP;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTupleValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import java.util.Arrays;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@EqualsAndHashCode
@RequiredArgsConstructor
public class ReferenceExpression implements Expression {
  @Getter
  private final String attr;

  @Getter
  private final List<String> paths;

  private final ExprType type;

  /**
   * Constructor of ReferenceExpression.
   * @param ref the field name. e.g. addr.state/addr.
   * @param type type.
   */
  public ReferenceExpression(String ref, ExprType type) {
    this.attr = ref;
    // Todo. the define of paths need to be redefined after adding multiple index/variable support.
    this.paths = Arrays.asList(ref.split("\\."));
    this.type = type;
  }

  @Override
  public ExprValue valueOf(Environment<Expression, ExprValue> env) {
    return env.resolve(this);
  }

  @Override
  public ExprType type() {
    return type;
  }

  @Override
  public <T, C> T accept(ExpressionNodeVisitor<T, C> visitor, C context) {
    return visitor.visitReference(this, context);
  }

  @Override
  public String toString() {
    return attr;
  }

  /**
   * Resolve the ExprValue from {@link ExprTupleValue}.
   * @param value {@link ExprTupleValue}.
   * @return {@link ExprTupleValue}.
   */
  public ExprValue resolve(ExprTupleValue value) {
    return resolve(value, paths);
  }

  private ExprValue resolve(ExprValue value, List<String> paths) {
    final ExprValue wholePathValue = value.keyValue(String.join(PATH_SEP, paths));
    if (!wholePathValue.isMissing() || paths.size() == 1) {
      return wholePathValue;
    } else {
      return resolve(value.keyValue(paths.get(0)), paths.subList(1, paths.size()));
    }
  }
}
