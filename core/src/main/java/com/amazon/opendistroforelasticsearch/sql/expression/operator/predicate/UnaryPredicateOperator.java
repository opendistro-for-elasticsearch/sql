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

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_NULL;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_TRUE;

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.BOOLEAN;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.UNKNOWN;

import static com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionDSL.impl;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprBooleanValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionBuilder;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionDSL;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionResolver;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionSignature;
import com.amazon.opendistroforelasticsearch.sql.expression.function.SerializableFunction;

import java.util.Arrays;
import java.util.List;
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
    repository.register(isNotNull());
    repository.register(ifNull());
    repository.register(nullIf());
    repository.register(isNull(BuiltinFunctionName.IS_NULL));
    repository.register(isNull(BuiltinFunctionName.ISNULL));
    repository.register(ifFunction());
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

  private static FunctionResolver isNull(BuiltinFunctionName funcName) {
    return FunctionDSL
        .define(funcName.getName(), Arrays.stream(ExprCoreType.values())
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

  private static FunctionResolver ifFunction() {
    FunctionName functionName = BuiltinFunctionName.IF.getName();
    List<ExprCoreType> typeList = ExprCoreType.coreTypes();

    List<SerializableFunction<FunctionName, org.apache.commons.lang3.tuple.Pair<FunctionSignature,
            FunctionBuilder>>> functionsOne = typeList.stream().map(v ->
            impl((UnaryPredicateOperator::exprIf), v, BOOLEAN, v, v))
            .collect(Collectors.toList());

    FunctionResolver functionResolver = FunctionDSL.define(functionName, functionsOne);
    return functionResolver;
  }

  private static FunctionResolver ifNull() {
    FunctionName functionName = BuiltinFunctionName.IFNULL.getName();
    List<ExprCoreType> typeList = ExprCoreType.coreTypes();

    List<SerializableFunction<FunctionName, org.apache.commons.lang3.tuple.Pair<FunctionSignature,
            FunctionBuilder>>> functionsOne = typeList.stream().map(v ->
            impl((UnaryPredicateOperator::exprIfNull), v, v, v))
            .collect(Collectors.toList());

    FunctionResolver functionResolver = FunctionDSL.define(functionName, functionsOne);
    return functionResolver;
  }

  private static FunctionResolver nullIf() {
    FunctionName functionName = BuiltinFunctionName.NULLIF.getName();
    List<ExprCoreType> typeList = ExprCoreType.coreTypes();

    FunctionResolver functionResolver =
        FunctionDSL.define(functionName,
            typeList.stream().map(v ->
              impl((UnaryPredicateOperator::exprNullIf), v, v, v))
              .collect(Collectors.toList()));
    return functionResolver;
  }

  /** v2 if v1 is null.
   * @param v1 varable 1
   * @param v2 varable 2
   * @return v2 if v1 is null
   */
  public static ExprValue exprIfNull(ExprValue v1, ExprValue v2) {
    return (v1.isNull() || v1.isMissing()) ? v2 : v1;
  }

  /** return null if v1 equls to v2.
   * @param v1 varable 1
   * @param v2 varable 2
   * @return null if v1 equls to v2
   */
  public static ExprValue exprNullIf(ExprValue v1, ExprValue v2) {
    return v1.equals(v2) ? LITERAL_NULL : v1;
  }

  public static ExprValue exprIf(ExprValue v1, ExprValue v2, ExprValue v3) {
    return !v1.isNull() && !v1.isMissing() && LITERAL_TRUE.equals(v1) ? v2 : v3;
  }

}
