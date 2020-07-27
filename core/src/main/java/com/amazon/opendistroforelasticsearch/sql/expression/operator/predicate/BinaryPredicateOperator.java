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
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.FLOAT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.LONG;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.expression.operator.OperatorUtils.binaryOperator;
import static com.amazon.opendistroforelasticsearch.sql.utils.OperatorUtils.matches;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprBooleanValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionBuilder;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionDSL;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionResolver;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionSignature;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import java.util.Arrays;
import java.util.Map;
import java.util.function.BiFunction;
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
    FunctionName functionName = BuiltinFunctionName.AND.getName();
    return FunctionResolver.builder()
        .functionName(functionName)
        .functionBundle(new FunctionSignature(functionName,
            Arrays.asList(BOOLEAN, BOOLEAN)), binaryPredicate(functionName,
            andTable, BOOLEAN))
        .build();
  }

  private static FunctionResolver or() {
    FunctionName functionName = BuiltinFunctionName.OR.getName();
    return FunctionResolver.builder()
        .functionName(functionName)
        .functionBundle(new FunctionSignature(functionName,
            Arrays.asList(BOOLEAN, BOOLEAN)), binaryPredicate(functionName,
            orTable, BOOLEAN))
        .build();
  }

  private static FunctionResolver xor() {
    FunctionName functionName = BuiltinFunctionName.XOR.getName();
    return FunctionResolver.builder()
        .functionName(functionName)
        .functionBundle(new FunctionSignature(functionName,
            Arrays.asList(BOOLEAN, BOOLEAN)), binaryPredicate(functionName,
            xorTable, BOOLEAN))
        .build();
  }

  private static FunctionResolver equal() {
    return FunctionDSL.define(BuiltinFunctionName.EQUAL.getName(),
        ExprCoreType.coreTypes().stream()
            .map(type -> FunctionDSL.impl((v1, v2) -> ExprBooleanValue.of(v1.equals(v2)),
                BOOLEAN, type, type))
            .collect(
                Collectors.toList()));
  }

  private static FunctionResolver notEqual() {
    return FunctionDSL
        .define(BuiltinFunctionName.NOTEQUAL.getName(), ExprCoreType.coreTypes().stream()
            .map(type -> FunctionDSL
                .impl((v1, v2) -> ExprBooleanValue.of(!v1.equals(v2)), BOOLEAN, type, type))
            .collect(
                Collectors.toList()));
  }

  private static FunctionResolver less() {
    return new FunctionResolver(
        BuiltinFunctionName.LESS.getName(),
        predicate(
            BuiltinFunctionName.LESS.getName(),
            (v1, v2) -> v1 < v2,
            (v1, v2) -> v1 < v2,
            (v1, v2) -> v1 < v2,
            (v1, v2) -> v1 < v2,
            (v1, v2) -> v1.compareTo(v2) < 0
        )
    );
  }

  private static FunctionResolver lte() {
    return new FunctionResolver(
        BuiltinFunctionName.LTE.getName(),
        predicate(
            BuiltinFunctionName.LTE.getName(),
            (v1, v2) -> v1 <= v2,
            (v1, v2) -> v1 <= v2,
            (v1, v2) -> v1 <= v2,
            (v1, v2) -> v1 <= v2,
            (v1, v2) -> v1.compareTo(v2) <= 0
        )
    );
  }

  private static FunctionResolver greater() {
    return new FunctionResolver(
        BuiltinFunctionName.GREATER.getName(),
        predicate(
            BuiltinFunctionName.GREATER.getName(),
            (v1, v2) -> v1 > v2,
            (v1, v2) -> v1 > v2,
            (v1, v2) -> v1 > v2,
            (v1, v2) -> v1 > v2,
            (v1, v2) -> v1.compareTo(v2) > 0
        )
    );
  }

  private static FunctionResolver gte() {
    return new FunctionResolver(
        BuiltinFunctionName.GTE.getName(),
        predicate(
            BuiltinFunctionName.GTE.getName(),
            (v1, v2) -> v1 >= v2,
            (v1, v2) -> v1 >= v2,
            (v1, v2) -> v1 >= v2,
            (v1, v2) -> v1 >= v2,
            (v1, v2) -> v1.compareTo(v2) >= 0
        )
    );
  }

  private static FunctionResolver like() {
    return new FunctionResolver(
        BuiltinFunctionName.LIKE.getName(),
        predicate(
            BuiltinFunctionName.LIKE.getName(),
            (v1, v2) -> matches(v2, v1)
        )
    );
  }

  /**
   * Util method to generate binary predicate bundles.
   * Applicable for integer, long, float, double, string types of operands
   * Missing/Null value operands follow as {@param table} lists
   */
  private static Map<FunctionSignature, FunctionBuilder> predicate(
      FunctionName functionName,
      BiFunction<Integer, Integer, Boolean> integerFunc,
      BiFunction<Long, Long, Boolean> longFunc,
      BiFunction<Float, Float, Boolean> floatFunc,
      BiFunction<Double, Double, Boolean> doubleFunc,
      BiFunction<String, String, Boolean> stringFunc) {
    ImmutableMap.Builder<FunctionSignature, FunctionBuilder> builder = new ImmutableMap.Builder<>();
    return builder
        .put(
            new FunctionSignature(functionName, Arrays.asList(INTEGER, INTEGER)),
            binaryOperator(
                functionName, integerFunc, ExprValueUtils::getIntegerValue, BOOLEAN))
        .put(
            new FunctionSignature(functionName, Arrays.asList(LONG, LONG)),
            binaryOperator(
                functionName, longFunc, ExprValueUtils::getLongValue, BOOLEAN))
        .put(
            new FunctionSignature(functionName, Arrays.asList(FLOAT, FLOAT)),
            binaryOperator(
                functionName, floatFunc, ExprValueUtils::getFloatValue, BOOLEAN))
        .put(
            new FunctionSignature(functionName, Arrays.asList(DOUBLE, DOUBLE)),
            binaryOperator(
                functionName, doubleFunc, ExprValueUtils::getDoubleValue, BOOLEAN))
        .put(
            new FunctionSignature(functionName, Arrays.asList(STRING, STRING)),
            binaryOperator(
                functionName, stringFunc, ExprValueUtils::getStringValue, BOOLEAN))
        .build();
  }

  /**
   * Util method to generate LIKE predicate bundles.
   * Applicable for string operands.
   */
  private static Map<FunctionSignature, FunctionBuilder> predicate(
      FunctionName functionName,
      BiFunction<String, String, Boolean> stringFunc) {
    ImmutableMap.Builder<FunctionSignature, FunctionBuilder> builder = new ImmutableMap.Builder<>();
    return builder
        .put(new FunctionSignature(functionName, Arrays.asList(STRING, STRING)),
            binaryOperator(functionName, stringFunc, ExprValueUtils::getStringValue,
                BOOLEAN))
        .build();
  }


  /**
   * Building method to construct binary logical predicates AND OR XOR
   * Where operands order does not matter.
   * Special cases for missing/null operands refer to {@param table}.
   */
  private static FunctionBuilder binaryPredicate(FunctionName functionName,
                                                 Table<ExprValue, ExprValue, ExprValue> table,
                                                 ExprCoreType returnType) {
    return arguments -> new FunctionExpression(functionName, arguments) {
      @Override
      public ExprValue valueOf(Environment<Expression, ExprValue> env) {
        ExprValue arg1 = arguments.get(0).valueOf(env);
        ExprValue arg2 = arguments.get(1).valueOf(env);
        if (table.contains(arg1, arg2)) {
          return table.get(arg1, arg2);
        } else {
          return table.get(arg2, arg1);
        }
      }

      @Override
      public ExprType type() {
        return returnType;
      }

      @Override
      public String toString() {
        return String.format("%s %s %s", arguments.get(0).toString(), functionName, arguments
            .get(1).toString());
      }
    };
  }
}
