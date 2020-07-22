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
import static com.amazon.opendistroforelasticsearch.sql.expression.operator.OperatorUtils.tripleArgFunc;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.zip.CRC32;
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
    repository.register(conv());
    repository.register(crc32());
    repository.register(euler());
    repository.register(exp());
    repository.register(floor());
    repository.register(ln());
    repository.register(log());
    repository.register(log10());
    repository.register(log2());
    repository.register(mod());
    repository.register(pow());
    repository.register(power());
    repository.register(round());
    repository.register(sign());
    repository.register(sqrt());
    repository.register(truncate());
    repository.register(pi());
    repository.register(rand());
    repository.register(acos());
    repository.register(asin());
    repository.register(atan());
    repository.register(atan2());
    repository.register(cos());
    repository.register(cot());
    repository.register(degrees());
    repository.register(radians());
    repository.register(sin());
    repository.register(tan());
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
   * Definition of conv(x, a, b) function.
   * Convert number x from base a to base b
   * The supported signature of floor function is
   * (STRING, INTEGER, INTEGER) -> STRING
   */
  private static FunctionResolver conv() {
    FunctionName functionName = BuiltinFunctionName.CONV.getName();
    return new FunctionResolver(
        functionName,
        new ImmutableMap.Builder<FunctionSignature, FunctionBuilder>()
            .put(
                new FunctionSignature(functionName,
                    Arrays.asList(ExprCoreType.STRING, ExprCoreType.INTEGER, ExprCoreType.INTEGER)),
                tripleArgFunc(functionName,
                    (num, fromBase, toBase) -> Integer.toString(
                        Integer.parseInt(num, fromBase), toBase),
                    ExprValueUtils::getStringValue, ExprValueUtils::getIntegerValue,
                    ExprValueUtils::getIntegerValue, ExprCoreType.STRING))
            .put(
                new FunctionSignature(functionName,
                    Arrays.asList(
                        ExprCoreType.INTEGER, ExprCoreType.INTEGER, ExprCoreType.INTEGER)),
                tripleArgFunc(functionName,
                    (num, fromBase, toBase) -> Integer.toString(
                        Integer.parseInt(num.toString(), fromBase), toBase),
                    ExprValueUtils::getIntegerValue, ExprValueUtils::getIntegerValue,
                    ExprValueUtils::getIntegerValue, ExprCoreType.STRING))
            .build());
  }

  /**
   * Definition of crc32(x) function.
   * Calculate a cyclic redundancy check value and returns a 32-bit unsigned value
   * The supported signature of crc32 function is
   * STRING -> LONG
   */
  private static FunctionResolver crc32() {
    FunctionName functionName = BuiltinFunctionName.CRC32.getName();
    return new FunctionResolver(functionName,
        new ImmutableMap.Builder<FunctionSignature, FunctionBuilder>()
            .put(
                new FunctionSignature(functionName, Arrays.asList(ExprCoreType.STRING)),
                unaryOperator(
                    functionName,
                    v -> {
                      CRC32 crc = new CRC32();
                      crc.update(v.getBytes());
                      return crc.getValue();
                    },
                    ExprValueUtils::getStringValue, ExprCoreType.LONG))
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
   * Definition of mod(x, y) function.
   * Calculate the remainder of x divided by y
   * The supported signature of mod function is
   * (x: INTEGER/LONG/FLOAT/DOUBLE, y: INTEGER/LONG/FLOAT/DOUBLE)
   * -> wider type between types of x and y
   */
  private static FunctionResolver mod() {
    return new FunctionResolver(
        BuiltinFunctionName.MOD.getName(),
        doubleArgumentsFunction(BuiltinFunctionName.MOD.getName(),
            (v1, v2) -> v2 == 0 ? null : v1 % v2,
            (v1, v2) -> v2 == 0 ? null : v1 % v2,
            (v1, v2) -> v2 == 0 ? null : v1 % v2,
            (v1, v2) -> v2 == 0 ? null : v1 % v2));
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
   * Definition of pow(x, y)/power(x, y) function.
   * Calculate the value of x raised to the power of y
   * The supported signature of pow/power function is
   * (INTEGER, INTEGER) -> INTEGER
   * (LONG, LONG) -> LONG
   * (FLOAT, FLOAT) -> FLOAT
   * (DOUBLE, DOUBLE) -> DOUBLE
   */
  private static FunctionResolver pow() {
    FunctionName functionName = BuiltinFunctionName.POW.getName();
    return new FunctionResolver(functionName, doubleArgumentsFunction(functionName, Math::pow));
  }

  private static FunctionResolver power() {
    FunctionName functionName = BuiltinFunctionName.POWER.getName();
    return new FunctionResolver(functionName, doubleArgumentsFunction(functionName, Math::pow));
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
   * Definition of round(x)/round(x, d) function.
   * Rounds the argument x to d decimal places, d defaults to 0 if not specified.
   * The supported signature of round function is
   * (x: INTEGER [, y: INTEGER]) -> INTEGER
   * (x: LONG [, y: INTEGER]) -> LONG
   * (x: FLOAT [, y: INTEGER]) -> FLOAT
   * (x: DOUBLE [, y: INTEGER]) -> DOUBLE
   */
  private static FunctionResolver round() {
    FunctionName functionName = BuiltinFunctionName.ROUND.getName();
    return new FunctionResolver(functionName,
        new ImmutableMap.Builder<FunctionSignature, FunctionBuilder>()
            .put(
                new FunctionSignature(functionName, Arrays.asList(ExprCoreType.INTEGER)),
                unaryOperator(
                    functionName, v -> (long) Math.round(v), ExprValueUtils::getIntegerValue,
                    ExprCoreType.LONG))
            .put(
                new FunctionSignature(functionName, Arrays.asList(ExprCoreType.LONG)),
                unaryOperator(
                    functionName, v -> (long) Math.round(v), ExprValueUtils::getLongValue,
                    ExprCoreType.LONG))
            .put(
                new FunctionSignature(functionName, Arrays.asList(ExprCoreType.FLOAT)),
                unaryOperator(
                    functionName, v -> (double) Math.round(v), ExprValueUtils::getFloatValue,
                    ExprCoreType.DOUBLE))
            .put(
                new FunctionSignature(functionName, Arrays.asList(ExprCoreType.DOUBLE)),
                unaryOperator(
                    functionName, v -> (double) Math.round(v), ExprValueUtils::getDoubleValue,
                    ExprCoreType.DOUBLE))
            .put(
                new FunctionSignature(
                    functionName, Arrays.asList(ExprCoreType.INTEGER, ExprCoreType.INTEGER)),
                doubleArgFunc(functionName,
                    (v1, v2) -> new BigDecimal(v1).setScale(v2, RoundingMode.HALF_UP).longValue(),
                    ExprValueUtils::getIntegerValue, ExprValueUtils::getIntegerValue,
                    ExprCoreType.LONG))
            .put(
                new FunctionSignature(
                    functionName, Arrays.asList(ExprCoreType.LONG, ExprCoreType.INTEGER)),
                doubleArgFunc(functionName,
                    (v1, v2) -> new BigDecimal(v1).setScale(v2, RoundingMode.HALF_UP).longValue(),
                    ExprValueUtils::getLongValue, ExprValueUtils::getIntegerValue,
                    ExprCoreType.LONG))
            .put(
                new FunctionSignature(
                    functionName, Arrays.asList(ExprCoreType.FLOAT, ExprCoreType.INTEGER)),
                doubleArgFunc(functionName,
                    (v1, v2) -> new BigDecimal(v1).setScale(v2, RoundingMode.HALF_UP).doubleValue(),
                    ExprValueUtils::getFloatValue, ExprValueUtils::getIntegerValue,
                    ExprCoreType.DOUBLE))
            .put(
                new FunctionSignature(
                    functionName, Arrays.asList(ExprCoreType.DOUBLE, ExprCoreType.INTEGER)),
                doubleArgFunc(functionName,
                    (v1, v2) -> new BigDecimal(v1).setScale(v2, RoundingMode.HALF_UP).doubleValue(),
                    ExprValueUtils::getDoubleValue, ExprValueUtils::getIntegerValue,
                    ExprCoreType.DOUBLE))
            .build());
  }

  /**
   * Definition of sign(x) function.
   * Returns the sign of the argument as -1, 0, or 1
   * depending on whether x is negative, zero, or positive
   * The supported signature is
   * INTEGER/LONG/FLOAT/DOUBLE -> INTEGER
   */
  private static FunctionResolver sign() {
    FunctionName functionName = BuiltinFunctionName.SIGN.getName();
    return new FunctionResolver(
        functionName,
        new ImmutableMap.Builder<FunctionSignature, FunctionBuilder>()
            .put(
                new FunctionSignature(
                    functionName, Arrays.asList(ExprCoreType.DOUBLE)),
                unaryOperator(
                    functionName, v -> (int) Math.signum(v), ExprValueUtils::getDoubleValue,
                    ExprCoreType.INTEGER))
            .build());
  }

  /**
   * Definition of sqrt(x) function.
   * Calculate the square root of a non-negative number x
   * The supported signature is
   * INTEGER/LONG/FLOAT/DOUBLE -> DOUBLE
   */
  private static FunctionResolver sqrt() {
    FunctionName functionName = BuiltinFunctionName.SQRT.getName();
    return new FunctionResolver(
        functionName,
        singleArgumentFunction(
            functionName,
            v -> v < 0 ? null : Math.sqrt(v)));
  }

  /**
   * Definition of truncate(x, d) function.
   * Returns the number x, truncated to d decimal places
   * The supported signature of round function is
   * (x: INTEGER, y: INTEGER) -> INTEGER
   * (x: LONG, y: INTEGER) -> LONG
   * (x: FLOAT, y: INTEGER) -> FLOAT
   * (x: DOUBLE, y: INTEGER) -> DOUBLE
   */
  private static FunctionResolver truncate() {
    FunctionName functionName = BuiltinFunctionName.TRUNCATE.getName();
    return new FunctionResolver(functionName,
        new ImmutableMap.Builder<FunctionSignature, FunctionBuilder>()
            .put(
                new FunctionSignature(
                    functionName, Arrays.asList(ExprCoreType.INTEGER, ExprCoreType.INTEGER)),
                doubleArgFunc(functionName,
                    (v1, v2) -> new BigDecimal(v1).setScale(v2, RoundingMode.DOWN).longValue(),
                    ExprValueUtils::getIntegerValue, ExprValueUtils::getIntegerValue,
                    ExprCoreType.LONG))
            .put(
                new FunctionSignature(
                    functionName, Arrays.asList(ExprCoreType.LONG, ExprCoreType.INTEGER)),
                doubleArgFunc(functionName,
                    (v1, v2) -> new BigDecimal(v1).setScale(v2, RoundingMode.DOWN).longValue(),
                    ExprValueUtils::getLongValue, ExprValueUtils::getIntegerValue,
                    ExprCoreType.LONG))
            .put(
                new FunctionSignature(
                    functionName, Arrays.asList(ExprCoreType.FLOAT, ExprCoreType.INTEGER)),
                doubleArgFunc(functionName,
                    (v1, v2) -> new BigDecimal(v1).setScale(v2, RoundingMode.DOWN).doubleValue(),
                    ExprValueUtils::getFloatValue, ExprValueUtils::getIntegerValue,
                    ExprCoreType.DOUBLE))
            .put(
                new FunctionSignature(
                    functionName, Arrays.asList(ExprCoreType.DOUBLE, ExprCoreType.INTEGER)),
                doubleArgFunc(functionName,
                    (v1, v2) -> new BigDecimal(v1).setScale(v2, RoundingMode.DOWN).doubleValue(),
                    ExprValueUtils::getDoubleValue, ExprValueUtils::getIntegerValue,
                    ExprCoreType.DOUBLE))
            .build());
  }

  /**
   * Definition of acos(x) function.
   * Calculates the arc cosine of x, that is, the value whose cosine is x.
   * Returns NULL if x is not in the range -1 to 1.
   * The supported signature of acos function is
   * INTEGER/LONG/FLOAT/DOUBLE -> DOUBLE
   */
  private static FunctionResolver acos() {
    FunctionName functionName = BuiltinFunctionName.ACOS.getName();
    return new FunctionResolver(
        functionName,
        singleArgumentFunction(functionName, v -> v < -1 || v > 1 ? null : Math.acos(v)));
  }

  /**
   * Definition of asin(x) function.
   * Calculates the arc sine of x, that is, the value whose sine is x.
   * Returns NULL if x is not in the range -1 to 1.
   * The supported signature of asin function is
   * INTEGER/LONG/FLOAT/DOUBLE -> DOUBLE
   */
  private static FunctionResolver asin() {
    FunctionName functionName = BuiltinFunctionName.ASIN.getName();
    return new FunctionResolver(
        functionName,
        singleArgumentFunction(functionName, v -> v < -1 || v > 1 ? null : Math.asin(v)));
  }

  /**
   * Definition of atan(x) and atan(y, x) function.
   * atan(x) calculates the arc tangent of x, that is, the value whose tangent is x.
   * atan(y, x) calculates the arc tangent of y / x, except that the signs of both arguments
   * are used to determine the quadrant of the result.
   * The supported signature of atan function is
   * (x: INTEGER/LONG/FLOAT/DOUBLE, y: INTEGER/LONG/FLOAT/DOUBLE) -> DOUBLE
   */
  private static FunctionResolver atan() {
    FunctionName functionName = BuiltinFunctionName.ATAN.getName();
    return new FunctionResolver(functionName,
        new ImmutableMap.Builder<FunctionSignature, FunctionBuilder>()
            .put(
                new FunctionSignature(functionName, Arrays.asList(ExprCoreType.DOUBLE)),
                unaryOperator(
                    functionName, Math::atan, ExprValueUtils::getDoubleValue, ExprCoreType.DOUBLE))
            .put(
                new FunctionSignature(
                    functionName, Arrays.asList(ExprCoreType.DOUBLE, ExprCoreType.DOUBLE)),
                doubleArgFunc(functionName,
                    Math::atan2, ExprValueUtils::getDoubleValue, ExprValueUtils::getDoubleValue,
                    ExprCoreType.DOUBLE))
            .build());
  }

  /**
   * Definition of atan2(y, x) function.
   * Calculates the arc tangent of y / x, except that the signs of both arguments
   * are used to determine the quadrant of the result.
   * The supported signature of atan2 function is
   * (x: INTEGER/LONG/FLOAT/DOUBLE, y: INTEGER/LONG/FLOAT/DOUBLE) -> DOUBLE
   */
  private static FunctionResolver atan2() {
    FunctionName functionName = BuiltinFunctionName.ATAN2.getName();
    return new FunctionResolver(functionName,
        new ImmutableMap.Builder<FunctionSignature, FunctionBuilder>()
            .put(
                new FunctionSignature(
                    functionName, Arrays.asList(ExprCoreType.DOUBLE, ExprCoreType.DOUBLE)),
                doubleArgFunc(functionName,
                    Math::atan2, ExprValueUtils::getDoubleValue, ExprValueUtils::getDoubleValue,
                    ExprCoreType.DOUBLE))
            .build());
  }

  /**
   * Definition of cos(x) function.
   * Calculates the cosine of X, where X is given in radians
   * The supported signature of cos function is
   * INTEGER/LONG/FLOAT/DOUBLE -> DOUBLE
   */
  private static FunctionResolver cos() {
    FunctionName functionName = BuiltinFunctionName.COS.getName();
    return new FunctionResolver(functionName, singleArgumentFunction(functionName, Math::cos));
  }

  /**
   * Definition of cot(x) function.
   * Calculates the cotangent of x
   * The supported signature of cot function is
   * INTEGER/LONG/FLOAT/DOUBLE -> DOUBLE
   */
  private static FunctionResolver cot() {
    FunctionName functionName = BuiltinFunctionName.COT.getName();
    return new FunctionResolver(
        functionName,
        singleArgumentFunction(functionName, v -> {
          if (v == 0) {
            throw new ArithmeticException(String.format("Out of range value for cot(%s)", v));
          }
          return 1 / Math.tan(v);
        }));
  }

  /**
   * Definition of degrees(x) function.
   * Converts x from radians to degrees
   * The supported signature of degrees function is
   * INTEGER/LONG/FLOAT/DOUBLE -> DOUBLE
   */
  private static FunctionResolver degrees() {
    FunctionName functionName = BuiltinFunctionName.DEGREES.getName();
    return new FunctionResolver(
        functionName, singleArgumentFunction(functionName, Math::toDegrees));
  }

  /**
   * Definition of radians(x) function.
   * Converts x from degrees to radians
   * The supported signature of radians function is
   * INTEGER/LONG/FLOAT/DOUBLE -> DOUBLE
   */
  private static FunctionResolver radians() {
    FunctionName functionName = BuiltinFunctionName.RADIANS.getName();
    return new FunctionResolver(
        functionName, singleArgumentFunction(functionName, Math::toRadians));
  }

  /**
   * Definition of sin(x) function.
   * Calculates the sine of x, where x is given in radians
   * The supported signature of sin function is
   * INTEGER/LONG/FLOAT/DOUBLE -> DOUBLE
   */
  private static FunctionResolver sin() {
    FunctionName functionName = BuiltinFunctionName.SIN.getName();
    return new FunctionResolver(functionName, singleArgumentFunction(functionName, Math::sin));
  }

  /**
   * Definition of tan(x) function.
   * Calculates the tangent of x, where x is given in radians
   * The supported signature of tan function is
   * INTEGER/LONG/FLOAT/DOUBLE -> DOUBLE
   */
  private static FunctionResolver tan() {
    FunctionName functionName = BuiltinFunctionName.TAN.getName();
    return new FunctionResolver(functionName, singleArgumentFunction(functionName, Math::tan));
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

  private static Map<FunctionSignature, FunctionBuilder> doubleArgumentsFunction(
      FunctionName functionName,
      BiFunction<Integer, Integer, Integer> intFunc,
      BiFunction<Long, Long, Long> longFunc,
      BiFunction<Float, Float, Float> floatFunc,
      BiFunction<Double, Double, Double> doubleFunc) {
    return new ImmutableMap.Builder<FunctionSignature, FunctionBuilder>()
        .put(
            new FunctionSignature(
                functionName, Arrays.asList(ExprCoreType.INTEGER, ExprCoreType.INTEGER)),
            doubleArgFunc(
                functionName, intFunc, ExprValueUtils::getIntegerValue,
                ExprValueUtils::getIntegerValue, ExprCoreType.INTEGER))
        .put(
            new FunctionSignature(
                functionName, Arrays.asList(ExprCoreType.LONG, ExprCoreType.LONG)),
            doubleArgFunc(
                functionName, longFunc, ExprValueUtils::getLongValue,
                ExprValueUtils::getLongValue, ExprCoreType.LONG))
        .put(
            new FunctionSignature(
                functionName, Arrays.asList(ExprCoreType.FLOAT, ExprCoreType.FLOAT)),
            doubleArgFunc(
                functionName, floatFunc, ExprValueUtils::getFloatValue,
                ExprValueUtils::getFloatValue, ExprCoreType.FLOAT))
        .put(
            new FunctionSignature(
                functionName, Arrays.asList(ExprCoreType.DOUBLE, ExprCoreType.DOUBLE)),
            doubleArgFunc(
                functionName, doubleFunc, ExprValueUtils::getDoubleValue,
                ExprValueUtils::getDoubleValue, ExprCoreType.DOUBLE))
        .build();
  }

  private static Map<FunctionSignature, FunctionBuilder> doubleArgumentsFunction(
      FunctionName functionName,
      BiFunction<Double, Double, Double> doubleFunc) {
    return new ImmutableMap.Builder<FunctionSignature, FunctionBuilder>()
        .put(
            new FunctionSignature(
                functionName, Arrays.asList(ExprCoreType.DOUBLE, ExprCoreType.DOUBLE)),
            doubleArgFunc(
                functionName, doubleFunc, ExprValueUtils::getDoubleValue,
                ExprValueUtils::getDoubleValue, ExprCoreType.DOUBLE)).build();
  }
}
