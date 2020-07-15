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

package com.amazon.opendistroforelasticsearch.sql.expression.operator;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionBuilder;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionName;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;

@UtilityClass
public class OperatorUtils {
  /**
   * Construct {@link FunctionBuilder} which call function with three arguments produced by
   * observers.In general, if any operand evaluates to a MISSING value, the enclosing operator
   * will return MISSING; if none of operands evaluates to a MISSING value but there is an
   * operand evaluates to a NULL value, the enclosing operator will return NULL.
   *
   * @param functionName function name
   * @param function {@link BiFunction}
   * @param observer1 extract the value of type T from the first argument
   * @param observer2 extract the value of type U from the first argument
   * @param observer3 extract the value of type V from the first argument
   * @param returnType return type
   * @param <T> the type of the first argument to the function
   * @param <U> the type of the second argument to the function
   * @param <V> the type of the third argument to the function
   * @param <R> the type of the result of the function
   * @return {@link FunctionBuilder}
   */
  public static <T, U, V, R> FunctionBuilder tripleArgFunc(
      FunctionName functionName,
      TriFunction<T, U, V, R> function,
      Function<ExprValue, T> observer1,
      Function<ExprValue, U> observer2,
      Function<ExprValue, V> observer3,
      ExprCoreType returnType) {
    return arguments -> new FunctionExpression(functionName, arguments) {
      @Override
      public ExprValue valueOf(Environment<Expression, ExprValue> valueEnv) {
        ExprValue arg1 = arguments.get(0).valueOf(valueEnv);
        ExprValue arg2 = arguments.get(1).valueOf(valueEnv);
        ExprValue arg3 = arguments.get(2).valueOf(valueEnv);
        if (arg1.isMissing() || arg2.isMissing() || arg3.isMissing()) {
          return ExprValueUtils.missingValue();
        } else if (arg1.isNull() || arg2.isNull() || arg3.isNull()) {
          return ExprValueUtils.nullValue();
        } else {
          return ExprValueUtils.fromObjectValue(
              function.apply(observer1.apply(arg1), observer2.apply(arg2), observer3.apply(arg3)));
        }
      }

      @Override
      public ExprType type() {
        return returnType;
      }

      @Override
      public String toString() {
        return String.format("%s(%s, %s, %s)", functionName, arguments.get(0).toString(), arguments
            .get(1).toString(), arguments.get(2).toString());
      }
    };
  }

  /**
   * Construct {@link FunctionBuilder} which call function with arguments produced by observer.
   *
   * @param functionName function name
   * @param function     {@link BiFunction}
   * @param observer     extract the value of type T from the first argument
   * @param returnType   return type
   * @param <T>          the type of the first and second argument to the function
   * @param <R>          the type of the result of the function
   * @return {@link FunctionBuilder}
   */
  public static <T, R> FunctionBuilder binaryOperator(
      FunctionName functionName,
      BiFunction<T, T, R> function,
      Function<ExprValue, T> observer,
      ExprCoreType returnType) {
    return binaryOperator(functionName, function, observer, observer, returnType);
  }

  /**
   * Construct {@link FunctionBuilder} which call function with arguments produced by observer1 and
   * observer2 In general, if any operand evaluates to a MISSING value, the enclosing operator will
   * return MISSING; if none of operands evaluates to a MISSING value but there is an operand
   * evaluates to a NULL value, the enclosing operator will return NULL.
   *
   * @param functionName function name
   * @param function     {@link BiFunction}
   * @param observer1    extract the value of type T from the first argument
   * @param observer2    extract the value of type U from the second argument
   * @param returnType   return type
   * @param <T>          the type of the first argument to the function
   * @param <U>          the type of the second argument to the function
   * @param <R>          the type of the result of the function
   * @return {@link FunctionBuilder}
   */
  public static <T, U, R> FunctionBuilder binaryOperator(
      FunctionName functionName,
      BiFunction<T, U, R> function,
      Function<ExprValue, T> observer1,
      Function<ExprValue, U> observer2,
      ExprCoreType returnType) {
    return arguments ->
        new FunctionExpression(functionName, arguments) {
          @Override
          public ExprValue valueOf(Environment<Expression, ExprValue> env) {
            ExprValue arg1 = arguments.get(0).valueOf(env);
            ExprValue arg2 = arguments.get(1).valueOf(env);
            if (arg1.isMissing() || arg2.isMissing()) {
              return ExprValueUtils.missingValue();
            } else if (arg1.isNull() || arg2.isNull()) {
              return ExprValueUtils.nullValue();
            } else {
              return ExprValueUtils.fromObjectValue(
                  function.apply(observer1.apply(arg1), observer2.apply(arg2)));
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
   * Construct {@link FunctionBuilder} which call function with arguments produced by observer1 and
   * observer2 In general, if any operand evaluates to a MISSING value, the enclosing operator will
   * return MISSING; if none of operands evaluates to a MISSING value but there is an operand
   * evaluates to a NULL value, the enclosing operator will return NULL.
   *
   * @param functionName function name
   * @param function     {@link BiFunction}
   * @param observer1    extract the value of type T from the first argument
   * @param observer2    extract the value of type U from the second argument
   * @param returnType   return type
   * @param <T>          the type of the first argument to the function
   * @param <U>          the type of the second argument to the function
   * @param <R>          the type of the result of the function
   * @return {@link FunctionBuilder}
   */
  public static <T, U, R> FunctionBuilder doubleArgFunc(
      FunctionName functionName,
      BiFunction<T, U, R> function,
      Function<ExprValue, T> observer1,
      Function<ExprValue, U> observer2,
      ExprType returnType) {
    return arguments ->
        new FunctionExpression(functionName, arguments) {
          @Override
          public ExprValue valueOf(Environment<Expression, ExprValue> env) {
            ExprValue arg1 = arguments.get(0).valueOf(env);
            ExprValue arg2 = arguments.get(1).valueOf(env);
            if (arg1.isMissing() || arg2.isMissing()) {
              return ExprValueUtils.missingValue();
            } else if (arg1.isNull() || arg2.isNull()) {
              return ExprValueUtils.nullValue();
            } else {
              return ExprValueUtils.fromObjectValue(
                  function.apply(observer1.apply(arg1), observer2.apply(arg2)));
            }
          }

          @Override
          public ExprType type() {
            return returnType;
          }

          @Override
          public String toString() {
            return String.format("%s(%s, %s)", functionName, arguments.get(0).toString(), arguments
                .get(1).toString());
          }
        };
  }

  /**
   * Construct {@link FunctionBuilder} which call function with arguments produced by observer In
   * general, if any operand evaluates to a MISSING value, the enclosing operator will return
   * MISSING; if none of operands evaluates to a MISSING value but there is an operand evaluates to
   * a NULL value, the enclosing operator will return NULL.
   *
   * @param functionName function name
   * @param function     {@link Function}
   * @param observer     extract the value of type T from the first argument
   * @param returnType   return type
   * @param <T>          the type of the first argument to the function
   * @param <R>          the type of the result of the function
   * @return {@link FunctionBuilder}
   */
  public static <T, R> FunctionBuilder unaryOperator(
      FunctionName functionName,
      Function<T, R> function,
      Function<ExprValue, T> observer,
      ExprCoreType returnType) {
    return arguments ->
        new FunctionExpression(functionName, arguments) {
          @Override
          public ExprValue valueOf(Environment<Expression, ExprValue> env) {
            ExprValue arg1 = arguments.get(0).valueOf(env);
            if (arg1.isMissing()) {
              return ExprValueUtils.missingValue();
            } else if (arg1.isNull()) {
              return ExprValueUtils.nullValue();
            } else {
              return ExprValueUtils.fromObjectValue(function.apply(observer.apply(arg1)));
            }
          }

          @Override
          public ExprType type() {
            return returnType;
          }

          @Override
          public String toString() {
            return String.format("%s(%s)", functionName,
                arguments.stream()
                    .map(Object::toString)
                    .collect(Collectors.joining(", ")));
          }
        };
  }

  /**
   * String comparator.
   */
  public static final BiFunction<String, String, Integer> STRING_COMPARATOR = String::compareTo;
  /**
   * List comparator.
   */
  public static final BiFunction<List, List, Integer> LIST_COMPARATOR =
      (left, right) -> Integer.compare(left.size(), right.size());
  /**
   * Map comparator.
   */
  public static final BiFunction<Map, Map, Integer> MAP_COMPARATOR =
      (left, right) -> Integer.compare(left.size(), right.size());
  /**
   * Predicate NULL or MISSING.
   */
  public static final BiPredicate<ExprValue, ExprValue> COMPARE_WITH_NULL_OR_MISSING =
      (left, right) -> left.isMissing() || right.isMissing() || left.isNull() || right.isNull();

  public interface TriFunction<T, U, V, R> {
    R apply(T t, U u, V v);
  }
}
