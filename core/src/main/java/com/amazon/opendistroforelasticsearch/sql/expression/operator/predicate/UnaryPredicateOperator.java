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

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.BOOLEAN;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprBooleanValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionDSL;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionResolver;
import java.util.Arrays;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

/**
 * The definition of unary predicate function
 * not, Accepts one Boolean value and produces a Boolean.
 */
@UtilityClass
public class UnaryPredicateOperator {
  /**
   * Register Unary Predicate Function.
   */
  public static void register(BuiltinFunctionRepository repository) {
    repository.register(not());
    repository.register(isNull());
    repository.register(isNotNull());
  }

  private static FunctionResolver not() {
    return FunctionDSL.define(BuiltinFunctionName.NOT.getName(), FunctionDSL
        .impl(UnaryPredicateOperator::not, BOOLEAN, BOOLEAN));
  }

  /**
   * The not logic.
   * A       NOT A
   * TRUE    FALSE
   * FALSE   TRUE
   * NULL    NULL
   * MISSING MISSING
   */
  public ExprValue not(ExprValue v) {
    if (v.isMissing() || v.isNull()) {
      return v;
    } else {
      return ExprBooleanValue.of(!v.booleanValue());
    }
  }

  private static FunctionResolver isNull() {

    return FunctionDSL
        .define(BuiltinFunctionName.IS_NULL.getName(), Arrays.stream(ExprCoreType.values())
            .map(type -> FunctionDSL
                .impl((v) -> ExprBooleanValue.of(v.isNull()), BOOLEAN, type))
            .collect(
                Collectors.toList()));
  }

  private static FunctionResolver isNotNull() {
    return FunctionDSL
        .define(BuiltinFunctionName.IS_NOT_NULL.getName(), Arrays.stream(ExprCoreType.values())
            .map(type -> FunctionDSL
                .impl((v) -> ExprBooleanValue.of(!v.isNull()), BOOLEAN, type))
            .collect(
                Collectors.toList()));
  }
}
