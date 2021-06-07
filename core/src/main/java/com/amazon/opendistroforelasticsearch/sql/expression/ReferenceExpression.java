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
   * Resolve the ExprValue from {@link ExprTupleValue} using paths.
   * Considering the following sample data.
   * {
   *   "name": "bob smith"
   *   "project.year": 1990,
   *   "project": {
   *     "year": "2020"
   *   }
   *   "address": {
   *     "state": "WA",
   *     "city": "seattle",
   *     "project.year": 1990
   *   }
   *   "address.local": {
   *     "state": "WA",
   *   }
   * }
   * The paths could be
   * 1. top level, e.g. "name", which will be resolved as "bob smith"
   * 2. multiple paths, e.g. "name.address.state", which will be resolved as "WA"
   * 3. special case, the "." is the path separator, but it is possible that the path include
   * ".", for handling this use case, we define the resolve rule as bellow, e.g. "project.year" is
   * resolved as 1990 instead of 2020. Note. This logic only applied top level none object field.
   * e.g. "address.local.state" been resolved to Missing. but "address.project.year" could been
   * resolved as 1990.
   *
   * <p>Resolve Rule
   * 1. Resolve the full name by combine the paths("x"."y"."z") as whole ("x.y.z").
   * 2. Resolve the path recursively through ExprValue.
   *
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
