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

package com.amazon.opendistroforelasticsearch.sql.expression.operator.arthmetic;

import static com.amazon.opendistroforelasticsearch.sql.expression.operator.OperatorUtils.doubleArgFunc;
import static com.amazon.opendistroforelasticsearch.sql.expression.operator.OperatorUtils.noArgFunction;
import static com.amazon.opendistroforelasticsearch.sql.expression.operator.OperatorUtils.unaryOperator;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionBuilder;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionResolver;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionSignature;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MathematicalFunction {
  /**
   * Register Mathematical Functions.
   *
   * @param repository {@link BuiltinFunctionRepository}.
   */
  public static void register(BuiltinFunctionRepository repository) {
    repository.register(abs());
    repository.register(ceil());
    repository.register(ceiling());
    repository.register(euler());
    repository.register(exp());
    repository.register(floor());
    repository.register(ln());
    repository.register(log());
    repository.register(log10());
    repository.register(log2());
    repository.register(pi());
    repository.register(rand());
  }

  /**
   * Definition of abs() function. The supported signature of abs() function are INT -> INT LONG ->
   * LONG FLOAT -> FLOAT DOUBLE -> DOUBLE
   */
  private static FunctionResolver abs() {
    return new FunctionResolver(
        BuiltinFunctionName.ABS.getName(),
        singleArgumentFunction(
            BuiltinFunctionName.ABS.getName(), Math::abs, Math::abs, Math::abs, Math::abs));
  }

  /**
   * Definition of ceil(x)/ceiling(x) function. Calculate the next highest integer that x rounds up
   * to The supported signature of ceil/ceiling function is DOUBLE -> INTEGER
   */
  private static FunctionResolver ceil() {
    FunctionName functionName = BuiltinFunctionName.CEIL.getName();
    return new FunctionResolver(
        functionName,
        new ImmutableMap.Builder<FunctionSignature, FunctionBuilder>()
            .put(
                new FunctionSignature(functionName, Arrays.asList(ExprCoreType.DOUBLE)),
                unaryOperator(
                    functionName,
                    v -> ((int) Math.ceil(v)),
                    ExprValueUtils::getDoubleValue,
                    ExprCoreType.INTEGER))
            .build());
  }

  private static FunctionResolver ceiling() {
    FunctionName functionName = BuiltinFunctionName.CEILING.getName();
    return new FunctionResolver(
        functionName,
        new ImmutableMap.Builder<FunctionSignature, FunctionBuilder>()
            .put(
                new FunctionSignature(functionName, Arrays.asList(ExprCoreType.DOUBLE)),
                unaryOperator(
                    functionName,
                    v -> ((int) Math.ceil(v)),
                    ExprValueUtils::getDoubleValue,
                    ExprCoreType.INTEGER))
            .build());
  }

  /**
   * Definition of e() function.
   * Get the Euler's number.
   * () -> DOUBLE
   */
  private static FunctionResolver euler() {
    FunctionName functionName = BuiltinFunctionName.E.getName();
    return new FunctionResolver(functionName,
        new ImmutableMap.Builder<FunctionSignature, FunctionBuilder>()
            .put(new FunctionSignature(functionName, Collections.emptyList()),
                noArgFunction(functionName, () -> Math.E, ExprCoreType.DOUBLE))
            .build());
  }

  /**
   * Definition of exp(x) function. Calculate exponent function e to the x The supported signature
   * of exp function is INTEGER/LONG/FLOAT/DOUBLE -> DOUBLE
   */
  private static FunctionResolver exp() {
    return new FunctionResolver(
        BuiltinFunctionName.EXP.getName(),
        singleArgumentFunction(BuiltinFunctionName.EXP.getName(), Math::exp));
  }

  /**
   * Definition of floor(x) function. Calculate the next nearest whole integer that x rounds down to
   * The supported signature of floor function is DOUBLE -> INTEGER
   */
  private static FunctionResolver floor() {
    FunctionName functionName = BuiltinFunctionName.FLOOR.getName();
    return new FunctionResolver(
        functionName,
        new ImmutableMap.Builder<FunctionSignature, FunctionBuilder>()
            .put(
                new FunctionSignature(functionName, Arrays.asList(ExprCoreType.DOUBLE)),
                unaryOperator(
                    functionName,
                    v -> ((int) Math.floor(v)),
                    ExprValueUtils::getDoubleValue,
                    ExprCoreType.INTEGER))
            .build());
  }

  /**
   * Definition of ln(x) function. Calculate the natural logarithm of x The supported signature of
   * ln function is INTEGER/LONG/FLOAT/DOUBLE -> DOUBLE
   */
  private static FunctionResolver ln() {
    return new FunctionResolver(
        BuiltinFunctionName.LN.getName(),
        singleArgumentFunction(BuiltinFunctionName.LN.getName(), Math::log));
  }

  /**
   * Definition of log(b, x) function. Calculate the logarithm of x using b as the base The
   * supported signature of log function is (b: INTEGER/LONG/FLOAT/DOUBLE, x:
   * INTEGER/LONG/FLOAT/DOUBLE]) -> DOUBLE
   */
  private static FunctionResolver log() {
    FunctionName functionName = BuiltinFunctionName.LOG.getName();
    return new FunctionResolver(
        functionName,
        new ImmutableMap.Builder<FunctionSignature, FunctionBuilder>()
            .put(
                new FunctionSignature(functionName, Arrays.asList(ExprCoreType.DOUBLE)),
                unaryOperator(
                    functionName, Math::log, ExprValueUtils::getDoubleValue, ExprCoreType.DOUBLE))
            .put(
                new FunctionSignature(
                    functionName, Arrays.asList(ExprCoreType.DOUBLE, ExprCoreType.DOUBLE)),
                doubleArgFunc(
                    functionName,
                    (b, v) -> Math.log(v) / Math.log(b),
                    ExprValueUtils::getDoubleValue,
                    ExprValueUtils::getDoubleValue,
                    ExprCoreType.DOUBLE))
            .build());
  }

  /**
   * Definition of log10(x) function. Calculate base-10 logarithm of x The supported signature of
   * log function is INTEGER/LONG/FLOAT/DOUBLE -> DOUBLE
   */
  private static FunctionResolver log10() {
    return new FunctionResolver(
        BuiltinFunctionName.LOG10.getName(),
        singleArgumentFunction(BuiltinFunctionName.LOG10.getName(), Math::log10));
  }

  /**
   * Definition of log2(x) function. Calculate base-2 logarithm of x The supported signature of log
   * function is INTEGER/LONG/FLOAT/DOUBLE -> DOUBLE
   */
  private static FunctionResolver log2() {
    return new FunctionResolver(
        BuiltinFunctionName.LOG2.getName(),
        singleArgumentFunction(BuiltinFunctionName.LOG2.getName(), v -> Math.log(v) / Math.log(2)));
  }

  /**
   * Definition of pi() function.
   * Get the value of pi.
   * () -> DOUBLE
   */
  private static FunctionResolver pi() {
    FunctionName functionName = BuiltinFunctionName.PI.getName();
    return new FunctionResolver(functionName,
        new ImmutableMap.Builder<FunctionSignature, FunctionBuilder>()
            .put(new FunctionSignature(functionName, Collections.emptyList()),
                noArgFunction(functionName, () -> Math.PI, ExprCoreType.DOUBLE))
            .build());
  }

  /**
   * Definition of rand() and rand(N) function.
   * rand() returns a random floating-point value in the range 0 <= value < 1.0
   * If integer N is specified, the seed is initialized prior to execution.
   * One implication of this behavior is with identical argument N,rand(N) returns the same value
   * each time, and thus produces a repeatable sequence of column values.
   * The supported signature of rand function is
   * ([INTEGER]) -> FLOAT
   */
  private static FunctionResolver rand() {
    FunctionName functionName = BuiltinFunctionName.RAND.getName();
    return new FunctionResolver(functionName,
        new ImmutableMap.Builder<FunctionSignature, FunctionBuilder>()
            .put(
                new FunctionSignature(functionName, Collections.emptyList()),
                noArgFunction(functionName, () -> new Random().nextFloat(), ExprCoreType.FLOAT))
            .put(
                new FunctionSignature(functionName, Arrays.asList(ExprCoreType.INTEGER)),
                unaryOperator(
                    functionName, n -> new Random(n).nextFloat(), ExprValueUtils::getIntegerValue,
                    ExprCoreType.FLOAT))
            .build());
  }

  /**
   * Util method to generate single argument function bundles. Applicable for INTEGER -> INTEGER
   * LONG -> LONG FLOAT -> FLOAT DOUBLE -> DOUBLE
   */
  private static Map<FunctionSignature, FunctionBuilder> singleArgumentFunction(
      FunctionName functionName,
      Function<Integer, Integer> integerFunc,
      Function<Long, Long> longFunc,
      Function<Float, Float> floatFunc,
      Function<Double, Double> doubleFunc) {
    ImmutableMap.Builder<FunctionSignature, FunctionBuilder> builder = new ImmutableMap.Builder<>();
    builder.put(
        new FunctionSignature(functionName, Arrays.asList(ExprCoreType.INTEGER)),
        unaryOperator(
            functionName, integerFunc, ExprValueUtils::getIntegerValue, ExprCoreType.INTEGER));
    builder.put(
        new FunctionSignature(functionName, Arrays.asList(ExprCoreType.LONG)),
        unaryOperator(functionName, longFunc, ExprValueUtils::getLongValue, ExprCoreType.LONG));
    builder.put(
        new FunctionSignature(functionName, Arrays.asList(ExprCoreType.FLOAT)),
        unaryOperator(functionName, floatFunc, ExprValueUtils::getFloatValue, ExprCoreType.FLOAT));
    builder.put(
        new FunctionSignature(functionName, Arrays.asList(ExprCoreType.DOUBLE)),
        unaryOperator(
            functionName, doubleFunc, ExprValueUtils::getDoubleValue, ExprCoreType.DOUBLE));
    return builder.build();
  }

  /** Util method to generate single argument function bundles. Applicable for DOUBLE -> DOUBLE */
  private static Map<FunctionSignature, FunctionBuilder> singleArgumentFunction(
      FunctionName functionName, Function<Double, Double> doubleFunc) {
    ImmutableMap.Builder<FunctionSignature, FunctionBuilder> builder = new ImmutableMap.Builder<>();
    return builder
        .put(
            new FunctionSignature(functionName, Arrays.asList(ExprCoreType.DOUBLE)),
            unaryOperator(
                functionName, doubleFunc, ExprValueUtils::getDoubleValue, ExprCoreType.DOUBLE))
        .build();
  }
}
