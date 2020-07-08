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
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.ARRAY;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.BOOLEAN;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.FLOAT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.LONG;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRUCT;

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
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionResolver;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionSignature;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
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

  /**
   * The equalTo logic.
   * A       B       A == B
   * NULL    NULL    TRUE
   * NULL    MISSING FALSE
   * MISSING NULL    FALSE
   * MISSING MISSING TRUE
   */
  private static Table<ExprValue, ExprValue, ExprValue> equalTable =
      new ImmutableTable.Builder<ExprValue, ExprValue, ExprValue>()
          .put(LITERAL_NULL, LITERAL_NULL, LITERAL_TRUE)
          .put(LITERAL_NULL, LITERAL_MISSING, LITERAL_FALSE)
          .put(LITERAL_MISSING, LITERAL_NULL, LITERAL_FALSE)
          .put(LITERAL_MISSING, LITERAL_MISSING, LITERAL_TRUE)
          .build();

  /**
   * The notEqualTo logic.
   * A       B       A != B
   * NULL    NULL    FALSE
   * NULL    MISSING TRUE
   * MISSING NULL    TRUE
   * MISSING MISSING FALSE
   */
  private static Table<ExprValue, ExprValue, ExprValue> notEqualTable =
      new ImmutableTable.Builder<ExprValue, ExprValue, ExprValue>()
          .put(LITERAL_NULL, LITERAL_NULL, LITERAL_FALSE)
          .put(LITERAL_NULL, LITERAL_MISSING, LITERAL_TRUE)
          .put(LITERAL_MISSING, LITERAL_NULL, LITERAL_TRUE)
          .put(LITERAL_MISSING, LITERAL_MISSING, LITERAL_FALSE)
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
    return new FunctionResolver(
        BuiltinFunctionName.EQUAL.getName(),
        predicate(
            BuiltinFunctionName.EQUAL.getName(),
            equalTable,
            LITERAL_FALSE,
            Integer::equals,
            Long::equals,
            Float::equals,
            Double::equals,
            String::equals,
            Boolean::equals,
            List::equals,
            Map::equals
        )
    );
  }

  private static FunctionResolver notEqual() {
    return new FunctionResolver(
        BuiltinFunctionName.NOTEQUAL.getName(),
        predicate(
            BuiltinFunctionName.NOTEQUAL.getName(),
            notEqualTable,
            LITERAL_TRUE,
            (v1, v2) -> ! v1.equals(v2),
            (v1, v2) -> ! v1.equals(v2),
            (v1, v2) -> ! v1.equals(v2),
            (v1, v2) -> ! v1.equals(v2),
            (v1, v2) -> ! v1.equals(v2),
            (v1, v2) -> ! v1.equals(v2),
            (v1, v2) -> ! v1.equals(v2),
            (v1, v2) -> ! v1.equals(v2)
        )
    );
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

  private static Map<FunctionSignature, FunctionBuilder> predicate(
      FunctionName functionName,
      Table<ExprValue, ExprValue, ExprValue> table,
      ExprValue defaultValue,
      BiFunction<Integer, Integer, Boolean> integerFunc,
      BiFunction<Long, Long, Boolean> longFunc,
      BiFunction<Float, Float, Boolean> floatFunc,
      BiFunction<Double, Double, Boolean> doubleFunc,
      BiFunction<String, String, Boolean> stringFunc,
      BiFunction<Boolean, Boolean, Boolean> booleanFunc,
      BiFunction<List, List, Boolean> listFunc,
      BiFunction<Map, Map, Boolean> mapFunc) {
    ImmutableMap.Builder<FunctionSignature, FunctionBuilder> builder = new ImmutableMap.Builder<>();
    return builder
        .put(new FunctionSignature(functionName, Arrays.asList(INTEGER, INTEGER)),
            equalPredicate(functionName, table, integerFunc, ExprValueUtils::getIntegerValue,
                defaultValue, BOOLEAN))
        .put(new FunctionSignature(functionName, Arrays.asList(LONG, LONG)),
            equalPredicate(functionName, table, longFunc, ExprValueUtils::getLongValue,
                defaultValue, BOOLEAN))
        .put(new FunctionSignature(functionName, Arrays.asList(FLOAT, FLOAT)),
            equalPredicate(functionName, table, floatFunc, ExprValueUtils::getFloatValue,
                defaultValue, BOOLEAN))
        .put(new FunctionSignature(functionName, Arrays.asList(DOUBLE, DOUBLE)),
            equalPredicate(functionName, table, doubleFunc, ExprValueUtils::getDoubleValue,
                defaultValue, BOOLEAN))
        .put(new FunctionSignature(functionName, Arrays.asList(STRING, STRING)),
            equalPredicate(functionName, table, stringFunc, ExprValueUtils::getStringValue,
                defaultValue, BOOLEAN))
        .put(new FunctionSignature(functionName, Arrays.asList(BOOLEAN, BOOLEAN)),
            equalPredicate(functionName, table, booleanFunc, ExprValueUtils::getBooleanValue,
                defaultValue, BOOLEAN))
        .put(new FunctionSignature(functionName, Arrays.asList(ARRAY, ARRAY)),
            equalPredicate(functionName, table, listFunc, ExprValueUtils::getCollectionValue,
                defaultValue, BOOLEAN))
        .put(new FunctionSignature(functionName, Arrays.asList(STRUCT, STRUCT)),
            equalPredicate(functionName, table, mapFunc, ExprValueUtils::getTupleValue,
                defaultValue, BOOLEAN))
        .build();
  }

  private static Map<FunctionSignature, FunctionBuilder> predicate(
      FunctionName functionName,
      BiFunction<Integer, Integer, Boolean> integerFunc,
      BiFunction<Long, Long, Boolean> longFunc,
      BiFunction<Float, Float, Boolean> floatFunc,
      BiFunction<Double, Double, Boolean> doubleFunc,
      BiFunction<String, String, Boolean> stringFunc) {
    ImmutableMap.Builder<FunctionSignature, FunctionBuilder> builder = new ImmutableMap.Builder<>();
    return builder
        .put(new FunctionSignature(functionName, Arrays.asList(INTEGER, INTEGER)),
            compareValue(functionName, integerFunc, ExprValueUtils::getIntegerValue,
                BOOLEAN))
        .put(new FunctionSignature(functionName, Arrays.asList(LONG, LONG)),
            compareValue(functionName, longFunc, ExprValueUtils::getLongValue,
                BOOLEAN))
        .put(new FunctionSignature(functionName, Arrays.asList(FLOAT, FLOAT)),
            compareValue(functionName, floatFunc, ExprValueUtils::getFloatValue,
                BOOLEAN))
        .put(new FunctionSignature(functionName, Arrays.asList(DOUBLE, DOUBLE)),
            compareValue(functionName, doubleFunc, ExprValueUtils::getDoubleValue,
                BOOLEAN))
        .put(new FunctionSignature(functionName, Arrays.asList(STRING, STRING)),
            compareValue(functionName, stringFunc, ExprValueUtils::getStringValue,
                BOOLEAN))
        .build();
  }

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

  /**
   * Building method for equalTo and notEqualTo operators.
   *
   * @param defaultValue the return value when expr value is missing/null
   */
  private static <T, R> FunctionBuilder equalPredicate(FunctionName functionName,
                                                       Table<ExprValue, ExprValue, ExprValue> table,
                                                       BiFunction<T, T, R> function,
                                                       Function<ExprValue, T> observer,
                                                       ExprValue defaultValue,
                                                       ExprCoreType returnType) {
    return arguments -> new FunctionExpression(functionName, arguments) {
      @Override
      public ExprValue valueOf(Environment<Expression, ExprValue> env) {
        ExprValue arg1 = arguments.get(0).valueOf(env);
        ExprValue arg2 = arguments.get(1).valueOf(env);
        if (table.contains(arg1, arg2)) {
          return table.get(arg1, arg2);
        } else if (arg1.isMissing() || arg1.isNull() || arg2.isMissing() || arg2.isNull()) {
          return defaultValue;
        } else {
          return ExprValueUtils.fromObjectValue(
              function.apply(observer.apply(arg1), observer.apply(arg2)));
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

  /**
   * Building method for operators including.
   * less than (<) operator
   * less than or equal to (<=) operator
   * greater than (>) operator
   * greater than or equal to (>=) operator
   */
  private static <T, R> FunctionBuilder compareValue(FunctionName functionName,
                                                     BiFunction<T, T, R> function,
                                                     Function<ExprValue, T> observer,
                                                     ExprCoreType returnType) {
    return arguments -> new FunctionExpression(functionName, arguments) {
      @Override
      public ExprValue valueOf(Environment<Expression, ExprValue> env) {
        ExprValue arg1 = arguments.get(0).valueOf(env);
        ExprValue arg2 = arguments.get(1).valueOf(env);
        return ExprValueUtils.fromObjectValue(
            function.apply(observer.apply(arg1), observer.apply(arg2)));
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
