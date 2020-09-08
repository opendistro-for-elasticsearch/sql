/*
 *
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.expression.function;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Function Define Utility.
 */
@UtilityClass
public class FunctionDSL {
  /**
   * Define overloaded function with implementation.
   *
   * @param functionName function name.
   * @param functions    a list of function implementation.
   * @return FunctionResolver.
   */
  public static FunctionResolver define(FunctionName functionName,
                                 SerializableFunction<FunctionName, Pair<FunctionSignature,
                                     FunctionBuilder>>... functions) {
    return define(functionName, Arrays.asList(functions));
  }

  /**
   * Define overloaded function with implementation.
   *
   * @param functionName function name.
   * @param functions    a list of function implementation.
   * @return FunctionResolver.
   */
  public static FunctionResolver define(FunctionName functionName,
                                 List<SerializableFunction<FunctionName, Pair<FunctionSignature,
                                     FunctionBuilder>>> functions) {

    FunctionResolver.FunctionResolverBuilder builder = FunctionResolver.builder();
    builder.functionName(functionName);
    for (Function<FunctionName, Pair<FunctionSignature, FunctionBuilder>> func : functions) {
      Pair<FunctionSignature, FunctionBuilder> functionBuilder = func.apply(functionName);
      builder.functionBundle(functionBuilder.getKey(), functionBuilder.getValue());
    }
    return builder.build();
  }

  /**
   * No Arg Function Implementation.
   *
   * @param function   {@link ExprValue} based unary function.
   * @param returnType return type.
   * @return Unary Function Implementation.
   */
  public static SerializableFunction<FunctionName, Pair<FunctionSignature, FunctionBuilder>> impl(
      SerializableNoArgFunction<ExprValue> function,
      ExprType returnType) {

    return functionName -> {
      FunctionSignature functionSignature =
          new FunctionSignature(functionName, Collections.emptyList());
      FunctionBuilder functionBuilder =
          arguments -> new FunctionExpression(functionName, Collections.emptyList()) {
            @Override
            public ExprValue valueOf(Environment<Expression, ExprValue> valueEnv) {
              return function.get();
            }

            @Override
            public ExprType type() {
              return returnType;
            }

            @Override
            public String toString() {
              return String.format("%s()", functionName);
            }
          };
      return Pair.of(functionSignature, functionBuilder);
    };
  }

  /**
   * Unary Function Implementation.
   *
   * @param function   {@link ExprValue} based unary function.
   * @param returnType return type.
   * @param argsType   argument type.
   * @return Unary Function Implementation.
   */
  public static SerializableFunction<FunctionName, Pair<FunctionSignature, FunctionBuilder>> impl(
      SerializableFunction<ExprValue, ExprValue> function,
      ExprType returnType,
      ExprType argsType) {

    return functionName -> {
      FunctionSignature functionSignature =
          new FunctionSignature(functionName, Collections.singletonList(argsType));
      FunctionBuilder functionBuilder =
          arguments -> new FunctionExpression(functionName, arguments) {
            @Override
            public ExprValue valueOf(Environment<Expression, ExprValue> valueEnv) {
              ExprValue value = arguments.get(0).valueOf(valueEnv);
              return function.apply(value);
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
      return Pair.of(functionSignature, functionBuilder);
    };
  }

  /**
   * Binary Function Implementation.
   *
   * @param function   {@link ExprValue} based unary function.
   * @param returnType return type.
   * @param args1Type   argument type.
   * @param args2Type   argument type.
   * @return Binary Function Implementation.
   */
  public static SerializableFunction<FunctionName, Pair<FunctionSignature, FunctionBuilder>> impl(
      SerializableBiFunction<ExprValue, ExprValue, ExprValue> function,
      ExprType returnType,
      ExprType args1Type,
      ExprType args2Type) {

    return functionName -> {
      FunctionSignature functionSignature =
          new FunctionSignature(functionName, Arrays.asList(args1Type, args2Type));
      FunctionBuilder functionBuilder =
          arguments -> new FunctionExpression(functionName, arguments) {
            @Override
            public ExprValue valueOf(Environment<Expression, ExprValue> valueEnv) {
              ExprValue arg1 = arguments.get(0).valueOf(valueEnv);
              ExprValue arg2 = arguments.get(1).valueOf(valueEnv);
              return function.apply(arg1, arg2);
            }

            @Override
            public ExprType type() {
              return returnType;
            }

            @Override
            public String toString() {
              return String.format("%s(%s, %s)", functionName, arguments.get(0).toString(),
                  arguments.get(1).toString());
            }
          };
      return Pair.of(functionSignature, functionBuilder);
    };
  }

  /**
   * Triple Function Implementation.
   *
   * @param function   {@link ExprValue} based unary function.
   * @param returnType return type.
   * @param args1Type  argument type.
   * @param args2Type  argument type.
   * @return Binary Function Implementation.
   */
  public static SerializableFunction<FunctionName, Pair<FunctionSignature, FunctionBuilder>> impl(
      SerializableTriFunction<ExprValue, ExprValue, ExprValue, ExprValue> function,
      ExprType returnType,
      ExprType args1Type,
      ExprType args2Type,
      ExprType args3Type) {

    return functionName -> {
      FunctionSignature functionSignature =
          new FunctionSignature(functionName, Arrays.asList(args1Type, args2Type, args3Type));
      FunctionBuilder functionBuilder =
          arguments -> new FunctionExpression(functionName, arguments) {
            @Override
            public ExprValue valueOf(Environment<Expression, ExprValue> valueEnv) {
              ExprValue arg1 = arguments.get(0).valueOf(valueEnv);
              ExprValue arg2 = arguments.get(1).valueOf(valueEnv);
              ExprValue arg3 = arguments.get(2).valueOf(valueEnv);
              return function.apply(arg1, arg2, arg3);
            }

            @Override
            public ExprType type() {
              return returnType;
            }

            @Override
            public String toString() {
              return String.format("%s(%s, %s, %s)", functionName, arguments.get(0).toString(),
                  arguments.get(1).toString(), arguments.get(2).toString());
            }
          };
      return Pair.of(functionSignature, functionBuilder);
    };
  }

  /**
   * Wrapper the unary ExprValue function with default NULL and MISSING handling.
   */
  public static SerializableFunction<ExprValue, ExprValue> nullMissingHandling(
      SerializableFunction<ExprValue, ExprValue> function) {
    return value -> {
      if (value.isMissing()) {
        return ExprValueUtils.missingValue();
      } else if (value.isNull()) {
        return ExprValueUtils.nullValue();
      } else {
        return function.apply(value);
      }
    };
  }

  /**
   * Wrapper the binary ExprValue function with default NULL and MISSING handling.
   */
  public static SerializableBiFunction<ExprValue, ExprValue, ExprValue> nullMissingHandling(
      SerializableBiFunction<ExprValue, ExprValue, ExprValue> function) {
    return (v1, v2) -> {
      if (v1.isMissing() || v2.isMissing()) {
        return ExprValueUtils.missingValue();
      } else if (v1.isNull() || v2.isNull()) {
        return ExprValueUtils.nullValue();
      } else {
        return function.apply(v1, v2);
      }
    };
  }

  /**
   * Wrapper the triple ExprValue function with default NULL and MISSING handling.
   */
  public SerializableTriFunction<ExprValue, ExprValue, ExprValue, ExprValue> nullMissingHandling(
      SerializableTriFunction<ExprValue, ExprValue, ExprValue, ExprValue> function) {
    return (v1, v2, v3) -> {
      if (v1.isMissing() || v2.isMissing() || v3.isMissing()) {
        return ExprValueUtils.missingValue();
      } else if (v1.isNull() || v2.isNull() || v3.isNull()) {
        return ExprValueUtils.nullValue();
      } else {
        return function.apply(v1, v2, v3);
      }
    };
  }
}
