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

package com.amazon.opendistroforelasticsearch.sql.expression.operator.predicate;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_FALSE;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_MISSING;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_NULL;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_TRUE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.BOOLEAN;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprBooleanValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionDSL;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionResolver;
import com.amazon.opendistroforelasticsearch.sql.utils.OperatorUtils;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

/**
 * The definition of binary predicate function
 * and, Accepts two Boolean values and produces a Boolean.
 * or,  Accepts two Boolean values and produces a Boolean.
 * xor, Accepts two Boolean values and produces a Boolean.
 * equalTo, Compare the left expression and right expression and produces a Boolean.
 */
@UtilityClass
public class BinaryPredicateOperator {
  /**
   * Register Binary Predicate Function.
   *
   * @param repository {@link BuiltinFunctionRepository}.
   */
  public static void register(BuiltinFunctionRepository repository) {
    repository.register(and());
    repository.register(or());
    repository.register(xor());
    repository.register(equal());
    repository.register(notEqual());
    repository.register(less());
    repository.register(lte());
    repository.register(greater());
    repository.register(gte());
    repository.register(like());
    repository.register(notLike());
  }

  /**
   * The and logic.
   * A       B       A AND B
   * TRUE    TRUE    TRUE
   * TRUE    FALSE   FALSE
   * TRUE    NULL    NULL
   * TRUE    MISSING MISSING
   * FALSE   FALSE   FALSE
   * FALSE   NULL    FALSE
   * FALSE   MISSING FALSE
   * NULL    NULL    NULL
   * NULL    MISSING MISSING
   * MISSING MISSING MISSING
   */
  private static Table<ExprValue, ExprValue, ExprValue> andTable =
      new ImmutableTable.Builder<ExprValue, ExprValue, ExprValue>()
          .put(LITERAL_TRUE, LITERAL_TRUE, LITERAL_TRUE)
          .put(LITERAL_TRUE, LITERAL_FALSE, LITERAL_FALSE)
          .put(LITERAL_TRUE, LITERAL_NULL, LITERAL_NULL)
          .put(LITERAL_TRUE, LITERAL_MISSING, LITERAL_MISSING)
          .put(LITERAL_FALSE, LITERAL_FALSE, LITERAL_FALSE)
          .put(LITERAL_FALSE, LITERAL_NULL, LITERAL_FALSE)
          .put(LITERAL_FALSE, LITERAL_MISSING, LITERAL_FALSE)
          .put(LITERAL_NULL, LITERAL_NULL, LITERAL_NULL)
          .put(LITERAL_NULL, LITERAL_MISSING, LITERAL_MISSING)
          .put(LITERAL_MISSING, LITERAL_MISSING, LITERAL_MISSING)
          .build();

  /**
   * The or logic.
   * A       B       A AND B
   * TRUE    TRUE    TRUE
   * TRUE    FALSE   TRUE
   * TRUE    NULL    TRUE
   * TRUE    MISSING TRUE
   * FALSE   FALSE   FALSE
   * FALSE   NULL    NULL
   * FALSE   MISSING MISSING
   * NULL    NULL    NULL
   * NULL    MISSING NULL
   * MISSING MISSING MISSING
   */
  private static Table<ExprValue, ExprValue, ExprValue> orTable =
      new ImmutableTable.Builder<ExprValue, ExprValue, ExprValue>()
          .put(LITERAL_TRUE, LITERAL_TRUE, LITERAL_TRUE)
          .put(LITERAL_TRUE, LITERAL_FALSE, LITERAL_TRUE)
          .put(LITERAL_TRUE, LITERAL_NULL, LITERAL_TRUE)
          .put(LITERAL_TRUE, LITERAL_MISSING, LITERAL_TRUE)
          .put(LITERAL_FALSE, LITERAL_FALSE, LITERAL_FALSE)
          .put(LITERAL_FALSE, LITERAL_NULL, LITERAL_NULL)
          .put(LITERAL_FALSE, LITERAL_MISSING, LITERAL_MISSING)
          .put(LITERAL_NULL, LITERAL_NULL, LITERAL_NULL)
          .put(LITERAL_NULL, LITERAL_MISSING, LITERAL_NULL)
          .put(LITERAL_MISSING, LITERAL_MISSING, LITERAL_MISSING)
          .build();

  /**
   * The xor logic.
   * A       B       A AND B
   * TRUE    TRUE    FALSE
   * TRUE    FALSE   TRUE
   * TRUE    NULL    TRUE
   * TRUE    MISSING TRUE
   * FALSE   FALSE   FALSE
   * FALSE   NULL    NULL
   * FALSE   MISSING MISSING
   * NULL    NULL    NULL
   * NULL    MISSING NULL
   * MISSING MISSING MISSING
   */
  private static Table<ExprValue, ExprValue, ExprValue> xorTable =
      new ImmutableTable.Builder<ExprValue, ExprValue, ExprValue>()
          .put(LITERAL_TRUE, LITERAL_TRUE, LITERAL_FALSE)
          .put(LITERAL_TRUE, LITERAL_FALSE, LITERAL_TRUE)
          .put(LITERAL_TRUE, LITERAL_NULL, LITERAL_TRUE)
          .put(LITERAL_TRUE, LITERAL_MISSING, LITERAL_TRUE)
          .put(LITERAL_FALSE, LITERAL_FALSE, LITERAL_FALSE)
          .put(LITERAL_FALSE, LITERAL_NULL, LITERAL_NULL)
          .put(LITERAL_FALSE, LITERAL_MISSING, LITERAL_MISSING)
          .put(LITERAL_NULL, LITERAL_NULL, LITERAL_NULL)
          .put(LITERAL_NULL, LITERAL_MISSING, LITERAL_NULL)
          .put(LITERAL_MISSING, LITERAL_MISSING, LITERAL_MISSING)
          .build();

  private static FunctionResolver and() {
    return FunctionDSL.define(BuiltinFunctionName.AND.getName(), FunctionDSL
        .impl((v1, v2) -> lookupTableFunction(v1, v2, andTable), BOOLEAN, BOOLEAN,
            BOOLEAN));
  }

  private static FunctionResolver or() {
    return FunctionDSL.define(BuiltinFunctionName.OR.getName(), FunctionDSL
        .impl((v1, v2) -> lookupTableFunction(v1, v2, orTable), BOOLEAN, BOOLEAN,
            BOOLEAN));
  }

  private static FunctionResolver xor() {
    return FunctionDSL.define(BuiltinFunctionName.XOR.getName(), FunctionDSL
        .impl((v1, v2) -> lookupTableFunction(v1, v2, xorTable), BOOLEAN, BOOLEAN,
            BOOLEAN));
  }

  private static FunctionResolver equal() {
    return FunctionDSL.define(BuiltinFunctionName.EQUAL.getName(),
        ExprCoreType.coreTypes().stream()
            .map(type -> FunctionDSL.impl(
                FunctionDSL.nullMissingHandling((v1, v2) -> ExprBooleanValue.of(v1.equals(v2))),
                BOOLEAN, type, type))
            .collect(
                Collectors.toList()));
  }

  private static FunctionResolver notEqual() {
    return FunctionDSL
        .define(BuiltinFunctionName.NOTEQUAL.getName(), ExprCoreType.coreTypes().stream()
            .map(type -> FunctionDSL
                .impl(
                    FunctionDSL
                        .nullMissingHandling((v1, v2) -> ExprBooleanValue.of(!v1.equals(v2))),
                    BOOLEAN,
                    type,
                    type))
            .collect(
                Collectors.toList()));
  }

  private static FunctionResolver less() {
    return FunctionDSL
        .define(BuiltinFunctionName.LESS.getName(), ExprCoreType.coreTypes().stream()
            .map(type -> FunctionDSL
                .impl(FunctionDSL
                        .nullMissingHandling((v1, v2) -> ExprBooleanValue.of(v1.compareTo(v2) < 0)),
                    BOOLEAN,
                    type, type))
            .collect(
                Collectors.toList()));
  }

  private static FunctionResolver lte() {
    return FunctionDSL
        .define(BuiltinFunctionName.LTE.getName(), ExprCoreType.coreTypes().stream()
            .map(type -> FunctionDSL
                .impl(
                    FunctionDSL
                        .nullMissingHandling(
                            (v1, v2) -> ExprBooleanValue.of(v1.compareTo(v2) <= 0)),
                    BOOLEAN,
                    type, type))
            .collect(
                Collectors.toList()));
  }

  private static FunctionResolver greater() {
    return FunctionDSL
        .define(BuiltinFunctionName.GREATER.getName(), ExprCoreType.coreTypes().stream()
            .map(type -> FunctionDSL
                .impl(FunctionDSL
                        .nullMissingHandling((v1, v2) -> ExprBooleanValue.of(v1.compareTo(v2) > 0)),
                    BOOLEAN, type, type))
            .collect(
                Collectors.toList()));
  }

  private static FunctionResolver gte() {
    return FunctionDSL
        .define(BuiltinFunctionName.GTE.getName(), ExprCoreType.coreTypes().stream()
            .map(type -> FunctionDSL
                .impl(
                    FunctionDSL.nullMissingHandling(
                        (v1, v2) -> ExprBooleanValue.of(v1.compareTo(v2) >= 0)),
                    BOOLEAN,
                    type, type))
            .collect(
                Collectors.toList()));
  }

  private static FunctionResolver like() {
    return FunctionDSL.define(BuiltinFunctionName.LIKE.getName(), FunctionDSL
        .impl(FunctionDSL.nullMissingHandling(OperatorUtils::matches), BOOLEAN, STRING,
            STRING));
  }

  private static FunctionResolver notLike() {
    return FunctionDSL.define(BuiltinFunctionName.NOT_LIKE.getName(), FunctionDSL
        .impl(FunctionDSL.nullMissingHandling(
            (v1, v2) -> UnaryPredicateOperator.not(OperatorUtils.matches(v1, v2))),
            BOOLEAN,
            STRING,
            STRING));
  }

  private static ExprValue lookupTableFunction(ExprValue arg1, ExprValue arg2,
                                               Table<ExprValue, ExprValue, ExprValue> table) {
    if (table.contains(arg1, arg2)) {
      return table.get(arg1, arg2);
    } else {
      return table.get(arg2, arg1);
    }
  }
}
