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

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.FLOAT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.LONG;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.SHORT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprDoubleValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprFloatValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprIntegerValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprLongValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprNullValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprShortValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprStringValue;
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
import com.google.common.collect.ImmutableList;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;

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
    return FunctionDSL.define(BuiltinFunctionName.ABS.getName(),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(v -> new ExprShortValue(Math.abs(v.shortValue()))),
            SHORT, SHORT),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(v -> new ExprIntegerValue(Math.abs(v.integerValue()))),
            INTEGER, INTEGER),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(v -> new ExprLongValue(Math.abs(v.longValue()))),
            LONG, LONG),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(v -> new ExprFloatValue(Math.abs(v.floatValue()))),
            FLOAT, FLOAT),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(v -> new ExprDoubleValue(Math.abs(v.doubleValue()))),
            DOUBLE, DOUBLE)
    );
  }

  /**
   * Definition of ceil(x)/ceiling(x) function. Calculate the next highest integer that x rounds up
   * to The supported signature of ceil/ceiling function is DOUBLE -> INTEGER
   */
  private static FunctionResolver ceil() {
    return FunctionDSL.define(BuiltinFunctionName.CEIL.getName(),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(v -> new ExprIntegerValue(Math.ceil(v.doubleValue()))),
            INTEGER, DOUBLE)
    );
  }

  private static FunctionResolver ceiling() {
    return FunctionDSL.define(BuiltinFunctionName.CEILING.getName(),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(v -> new ExprIntegerValue(Math.ceil(v.doubleValue()))),
            INTEGER, DOUBLE)
    );
  }

  /**
   * Definition of conv(x, a, b) function.
   * Convert number x from base a to base b
   * The supported signature of floor function is
   * (STRING, INTEGER, INTEGER) -> STRING
   * (INTEGER, INTEGER, INTEGER) -> STRING
   */
  private static FunctionResolver conv() {
    return FunctionDSL.define(BuiltinFunctionName.CONV.getName(),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling((x, a, b) -> new ExprStringValue(
                Integer.toString(Integer.parseInt(x.stringValue(), a.integerValue()),
                    b.integerValue())
            )),
            STRING, STRING, INTEGER, INTEGER),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling((x, a, b) -> new ExprStringValue(
                Integer.toString(Integer.parseInt(x.integerValue().toString(), a.integerValue()),
                    b.integerValue())
            )),
            STRING, INTEGER, INTEGER, INTEGER)
    );
  }

  /**
   * Definition of crc32(x) function.
   * Calculate a cyclic redundancy check value and returns a 32-bit unsigned value
   * The supported signature of crc32 function is
   * STRING -> LONG
   */
  private static FunctionResolver crc32() {
    return FunctionDSL.define(BuiltinFunctionName.CRC32.getName(),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(v -> {
              CRC32 crc = new CRC32();
              crc.update(v.stringValue().getBytes());
              return new ExprLongValue(crc.getValue());
            }),
            LONG, STRING)
    );
  }

  /**
   * Definition of e() function.
   * Get the Euler's number.
   * () -> DOUBLE
   */
  private static FunctionResolver euler() {
    return FunctionDSL.define(BuiltinFunctionName.E.getName(),
        FunctionDSL.impl(() -> new ExprDoubleValue(Math.E), DOUBLE)
    );
  }

  /**
   * Definition of exp(x) function. Calculate exponent function e to the x The supported signature
   * of exp function is INTEGER/LONG/FLOAT/DOUBLE -> DOUBLE
   */
  private static FunctionResolver exp() {
    return FunctionDSL.define(BuiltinFunctionName.EXP.getName(),
        ExprCoreType.numberTypes().stream()
            .map(type -> FunctionDSL.impl(FunctionDSL.nullMissingHandling(
                v -> new ExprDoubleValue(Math.exp(v.doubleValue()))),
                type, DOUBLE)).collect(Collectors.toList()));
  }

  /**
   * Definition of floor(x) function. Calculate the next nearest whole integer that x rounds down to
   * The supported signature of floor function is DOUBLE -> INTEGER
   */
  private static FunctionResolver floor() {
    return FunctionDSL.define(BuiltinFunctionName.FLOOR.getName(),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(v -> new ExprIntegerValue(Math.floor(v.doubleValue()))),
            INTEGER, DOUBLE)
    );
  }

  /**
   * Definition of ln(x) function. Calculate the natural logarithm of x The supported signature of
   * ln function is INTEGER/LONG/FLOAT/DOUBLE -> DOUBLE
   */
  private static FunctionResolver ln() {
    return FunctionDSL.define(BuiltinFunctionName.LN.getName(),
        ExprCoreType.numberTypes().stream()
            .map(type -> FunctionDSL.impl(FunctionDSL.nullMissingHandling(
                v -> new ExprDoubleValue(Math.log(v.doubleValue()))),
                type, DOUBLE)).collect(Collectors.toList()));
  }

  /**
   * Definition of log(b, x) function. Calculate the logarithm of x using b as the base The
   * supported signature of log function is (b: INTEGER/LONG/FLOAT/DOUBLE, x:
   * INTEGER/LONG/FLOAT/DOUBLE]) -> DOUBLE
   */
  private static FunctionResolver log() {
    ImmutableList.Builder<SerializableFunction<FunctionName, Pair<FunctionSignature,
        FunctionBuilder>>> builder = new ImmutableList.Builder<>();

    // build unary log(x), SHORT/INTEGER/LONG/FLOAT/DOUBLE -> DOUBLE
    for (ExprType type : ExprCoreType.numberTypes()) {
      builder.add(FunctionDSL.impl(FunctionDSL
              .nullMissingHandling(v -> new ExprDoubleValue(Math.log(v.doubleValue()))),
          DOUBLE, type));
    }

    // build binary function log(b, x)
    for (ExprType baseType : ExprCoreType.numberTypes()) {
      for (ExprType numberType : ExprCoreType.numberTypes()) {
        builder.add(FunctionDSL.impl(FunctionDSL
                .nullMissingHandling((b, x) -> new ExprDoubleValue(
                    Math.log(x.doubleValue()) / Math.log(b.doubleValue()))),
            DOUBLE, baseType, numberType));
      }
    }
    return FunctionDSL.define(BuiltinFunctionName.LOG.getName(), builder.build());
  }


  /**
   * Definition of log10(x) function. Calculate base-10 logarithm of x The supported signature of
   * log function is SHORT/INTEGER/LONG/FLOAT/DOUBLE -> DOUBLE
   */
  private static FunctionResolver log10() {
    return FunctionDSL.define(BuiltinFunctionName.LOG10.getName(),
        ExprCoreType.numberTypes().stream()
            .map(type -> FunctionDSL.impl(FunctionDSL.nullMissingHandling(
                v -> new ExprDoubleValue(Math.log10(v.doubleValue()))),
                type, DOUBLE)).collect(Collectors.toList()));
  }

  /**
   * Definition of log2(x) function. Calculate base-2 logarithm of x The supported signature of log
   * function is SHORT/INTEGER/LONG/FLOAT/DOUBLE -> DOUBLE
   */
  private static FunctionResolver log2() {
    return FunctionDSL.define(BuiltinFunctionName.LOG2.getName(),
        ExprCoreType.numberTypes().stream()
            .map(type -> FunctionDSL.impl(FunctionDSL.nullMissingHandling(
                v -> new ExprDoubleValue(Math.log(v.doubleValue()) / Math.log(2))), DOUBLE, type))
            .collect(Collectors.toList()));
  }

  /**
   * Definition of mod(x, y) function.
   * Calculate the remainder of x divided by y
   * The supported signature of mod function is
   * (x: INTEGER/LONG/FLOAT/DOUBLE, y: INTEGER/LONG/FLOAT/DOUBLE)
   * -> wider type between types of x and y
   */
  private static FunctionResolver mod() {
    return FunctionDSL.define(BuiltinFunctionName.MOD.getName(),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> v2.shortValue() == 0 ? ExprNullValue.of() :
                    new ExprShortValue(v1.shortValue() % v2.shortValue())),
            SHORT, SHORT, SHORT),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> v2.shortValue() == 0 ? ExprNullValue.of() :
                    new ExprIntegerValue(Math.floorMod(v1.integerValue(),
                        v2.integerValue()))),
            INTEGER, INTEGER, INTEGER),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> v2.shortValue() == 0 ? ExprNullValue.of() :
                    new ExprLongValue(Math.floorMod(v1.longValue(), v2.longValue()))),
            LONG, LONG, LONG),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> v2.shortValue() == 0 ? ExprNullValue.of() :
                    new ExprFloatValue(v1.floatValue() % v2.floatValue())),
            FLOAT, FLOAT, FLOAT),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> v2.shortValue() == 0 ? ExprNullValue.of() :
                    new ExprDoubleValue(v1.doubleValue() % v2.doubleValue())),
            DOUBLE, DOUBLE, DOUBLE)
    );
  }

  /**
   * Definition of pi() function.
   * Get the value of pi.
   * () -> DOUBLE
   */
  private static FunctionResolver pi() {
    return FunctionDSL.define(BuiltinFunctionName.PI.getName(),
        FunctionDSL.impl(() -> new ExprDoubleValue(Math.PI), DOUBLE)
    );
  }

  /**
   * Definition of pow(x, y)/power(x, y) function.
   * Calculate the value of x raised to the power of y
   * The supported signature of pow/power function is
   * (INTEGER, INTEGER) -> DOUBLE
   * (LONG, LONG) -> DOUBLE
   * (FLOAT, FLOAT) -> DOUBLE
   * (DOUBLE, DOUBLE) -> DOUBLE
   */
  private static FunctionResolver pow() {
    return FunctionDSL.define(BuiltinFunctionName.POW.getName(), powerFunctionImpl());
  }

  private static FunctionResolver power() {
    return FunctionDSL.define(BuiltinFunctionName.POWER.getName(), powerFunctionImpl());
  }

  private List<SerializableFunction<FunctionName, Pair<FunctionSignature,
      FunctionBuilder>>> powerFunctionImpl() {
    return Arrays.asList(FunctionDSL.impl(
        FunctionDSL.nullMissingHandling(
            (v1, v2) -> new ExprDoubleValue(Math.pow(v1.shortValue(), v2.shortValue()))),
        DOUBLE, SHORT, SHORT),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> new ExprDoubleValue(Math.pow(v1.integerValue(),
                    v2.integerValue()))),
            DOUBLE, INTEGER, INTEGER),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> new ExprDoubleValue(Math.pow(v1.longValue(), v2.longValue()))),
            DOUBLE, LONG, LONG),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> new ExprDoubleValue(Math.pow(v1.floatValue(), v2.floatValue()))),
            DOUBLE, FLOAT, FLOAT),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> new ExprDoubleValue(Math.pow(v1.doubleValue(), v2.doubleValue()))),
            DOUBLE, DOUBLE, DOUBLE));
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
    return FunctionDSL.define(BuiltinFunctionName.RAND.getName(),
        FunctionDSL.impl(() -> new ExprFloatValue(new Random().nextFloat()), FLOAT),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                v -> new ExprFloatValue(new Random(v.integerValue()).nextFloat())), FLOAT, INTEGER)
    );
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
    return FunctionDSL.define(BuiltinFunctionName.ROUND.getName(),
        // rand(x)
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                v -> new ExprLongValue((long) Math.round(v.integerValue()))),
            LONG, INTEGER),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                v -> new ExprLongValue((long) Math.round(v.longValue()))),
            LONG, LONG),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                v -> new ExprDoubleValue((double) Math.round(v.floatValue()))),
            DOUBLE, FLOAT),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                v -> new ExprDoubleValue((double) Math.round(v.doubleValue()))),
            DOUBLE, DOUBLE),

        // rand(x, d)
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (x, d) -> new ExprLongValue(
                    new BigDecimal(x.integerValue()).setScale(d.integerValue(),
                        RoundingMode.HALF_UP).longValue())),
            LONG, INTEGER, INTEGER),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (x, d) -> new ExprLongValue(new BigDecimal(x.longValue()).setScale(d.integerValue(),
                    RoundingMode.HALF_UP).longValue())),
            LONG, LONG, INTEGER),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (x, d) -> new ExprDoubleValue(new BigDecimal(x.floatValue())
                    .setScale(d.integerValue(), RoundingMode.HALF_UP).doubleValue())),
            DOUBLE, FLOAT, INTEGER),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (x, d) -> new ExprDoubleValue(new BigDecimal(x.doubleValue())
                    .setScale(d.integerValue(), RoundingMode.HALF_UP).doubleValue())),
            DOUBLE, DOUBLE, INTEGER));
  }

  /**
   * Definition of sign(x) function.
   * Returns the sign of the argument as -1, 0, or 1
   * depending on whether x is negative, zero, or positive
   * The supported signature is
   * SHORT/INTEGER/LONG/FLOAT/DOUBLE -> INTEGER
   */
  private static FunctionResolver sign() {
    return FunctionDSL.define(BuiltinFunctionName.SIGN.getName(),
        ExprCoreType.numberTypes().stream()
            .map(type -> FunctionDSL.impl(FunctionDSL.nullMissingHandling(
                v -> new ExprIntegerValue(Math.signum(v.doubleValue()))),
                INTEGER, type)).collect(Collectors.toList()));
  }

  /**
   * Definition of sqrt(x) function.
   * Calculate the square root of a non-negative number x
   * The supported signature is
   * INTEGER/LONG/FLOAT/DOUBLE -> DOUBLE
   */
  private static FunctionResolver sqrt() {
    return FunctionDSL.define(BuiltinFunctionName.SQRT.getName(),
        ExprCoreType.numberTypes().stream()
            .map(type -> FunctionDSL.impl(FunctionDSL.nullMissingHandling(
                v -> v.doubleValue() < 0 ? ExprNullValue.of() :
                    new ExprDoubleValue(Math.sqrt(v.doubleValue()))),
                DOUBLE, type)).collect(Collectors.toList()));
  }

  /**
   * Definition of truncate(x, d) function.
   * Returns the number x, truncated to d decimal places
   * The supported signature of round function is
   * (x: INTEGER, y: INTEGER) -> LONG
   * (x: LONG, y: INTEGER) -> LONG
   * (x: FLOAT, y: INTEGER) -> DOUBLE
   * (x: DOUBLE, y: INTEGER) -> DOUBLE
   */
  private static FunctionResolver truncate() {
    return FunctionDSL.define(BuiltinFunctionName.TRUNCATE.getName(),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (x, y) -> new ExprLongValue(
                    new BigDecimal(x.integerValue()).setScale(y.integerValue(),
                        RoundingMode.DOWN).longValue())),
            LONG, INTEGER, INTEGER),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (x, y) -> new ExprLongValue(
                    new BigDecimal(x.integerValue()).setScale(y.integerValue(),
                        RoundingMode.DOWN).longValue())),
            LONG, LONG, INTEGER),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (x, y) -> new ExprDoubleValue(
                    new BigDecimal(x.floatValue()).setScale(y.integerValue(),
                        RoundingMode.DOWN).doubleValue())),
            DOUBLE, FLOAT, INTEGER),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (x, y) -> new ExprDoubleValue(
                    new BigDecimal(x.doubleValue()).setScale(y.integerValue(),
                        RoundingMode.DOWN).doubleValue())),
            DOUBLE, DOUBLE, INTEGER));
  }

  /**
   * Definition of acos(x) function.
   * Calculates the arc cosine of x, that is, the value whose cosine is x.
   * Returns NULL if x is not in the range -1 to 1.
   * The supported signature of acos function is
   * INTEGER/LONG/FLOAT/DOUBLE -> DOUBLE
   */
  private static FunctionResolver acos() {
    return FunctionDSL.define(BuiltinFunctionName.ACOS.getName(),
        ExprCoreType.numberTypes().stream()
            .map(type -> FunctionDSL.impl(FunctionDSL.nullMissingHandling(
                v -> v.doubleValue() < -1 || v.doubleValue() > 1 ? ExprNullValue.of() :
                    new ExprDoubleValue(Math.acos(v.doubleValue()))),
                DOUBLE, type)).collect(Collectors.toList()));
  }

  /**
   * Definition of asin(x) function.
   * Calculates the arc sine of x, that is, the value whose sine is x.
   * Returns NULL if x is not in the range -1 to 1.
   * The supported signature of asin function is
   * INTEGER/LONG/FLOAT/DOUBLE -> DOUBLE
   */
  private static FunctionResolver asin() {
    return FunctionDSL.define(BuiltinFunctionName.ASIN.getName(),
        ExprCoreType.numberTypes().stream()
            .map(type -> FunctionDSL.impl(FunctionDSL.nullMissingHandling(
                v -> v.doubleValue() < -1 || v.doubleValue() > 1 ? ExprNullValue.of() :
                    new ExprDoubleValue(Math.asin(v.doubleValue()))),
                DOUBLE, type)).collect(Collectors.toList()));
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
    ImmutableList.Builder<SerializableFunction<FunctionName, Pair<FunctionSignature,
        FunctionBuilder>>> builder = new ImmutableList.Builder<>();

    for (ExprType type : ExprCoreType.numberTypes()) {
      builder.add(FunctionDSL.impl(FunctionDSL
              .nullMissingHandling(x -> new ExprDoubleValue(Math.atan(x.doubleValue()))), type,
          DOUBLE));
      builder.add(FunctionDSL.impl(FunctionDSL
          .nullMissingHandling((y, x) -> new ExprDoubleValue(Math.atan2(y.doubleValue(),
              x.doubleValue()))), DOUBLE, type, type));
    }

    return FunctionDSL.define(BuiltinFunctionName.ATAN.getName(), builder.build());
  }

  /**
   * Definition of atan2(y, x) function.
   * Calculates the arc tangent of y / x, except that the signs of both arguments
   * are used to determine the quadrant of the result.
   * The supported signature of atan2 function is
   * (x: INTEGER/LONG/FLOAT/DOUBLE, y: INTEGER/LONG/FLOAT/DOUBLE) -> DOUBLE
   */
  private static FunctionResolver atan2() {
    ImmutableList.Builder<SerializableFunction<FunctionName, Pair<FunctionSignature,
        FunctionBuilder>>> builder = new ImmutableList.Builder<>();

    for (ExprType type : ExprCoreType.numberTypes()) {
      builder.add(FunctionDSL.impl(FunctionDSL
          .nullMissingHandling((y, x) -> new ExprDoubleValue(Math.atan2(y.doubleValue(),
              x.doubleValue()))), DOUBLE, type, type));
    }

    return FunctionDSL.define(BuiltinFunctionName.ATAN2.getName(), builder.build());
  }

  /**
   * Definition of cos(x) function.
   * Calculates the cosine of X, where X is given in radians
   * The supported signature of cos function is
   * INTEGER/LONG/FLOAT/DOUBLE -> DOUBLE
   */
  private static FunctionResolver cos() {
    return FunctionDSL.define(BuiltinFunctionName.COS.getName(),
        ExprCoreType.numberTypes().stream()
            .map(type -> FunctionDSL.impl(FunctionDSL.nullMissingHandling(
                v -> new ExprDoubleValue(Math.cos(v.doubleValue()))),
                DOUBLE, type)).collect(Collectors.toList()));
  }

  /**
   * Definition of cot(x) function.
   * Calculates the cotangent of x
   * The supported signature of cot function is
   * INTEGER/LONG/FLOAT/DOUBLE -> DOUBLE
   */
  private static FunctionResolver cot() {
    return FunctionDSL.define(BuiltinFunctionName.COT.getName(),
        ExprCoreType.numberTypes().stream()
            .map(type -> FunctionDSL.impl(FunctionDSL.nullMissingHandling(
                v -> {
                  Double value = v.doubleValue();
                  if (value == 0) {
                    throw new ArithmeticException(
                        String.format("Out of range value for cot(%s)", value));
                  }
                  return new ExprDoubleValue(1 / Math.tan(value));
                }),
                DOUBLE, type)).collect(Collectors.toList()));
  }

  /**
   * Definition of degrees(x) function.
   * Converts x from radians to degrees
   * The supported signature of degrees function is
   * INTEGER/LONG/FLOAT/DOUBLE -> DOUBLE
   */
  private static FunctionResolver degrees() {
    return FunctionDSL.define(BuiltinFunctionName.DEGREES.getName(),
        ExprCoreType.numberTypes().stream()
            .map(type -> FunctionDSL.impl(FunctionDSL.nullMissingHandling(
                v -> new ExprDoubleValue(Math.toDegrees(v.doubleValue()))),
                type, DOUBLE)).collect(Collectors.toList()));
  }

  /**
   * Definition of radians(x) function.
   * Converts x from degrees to radians
   * The supported signature of radians function is
   * INTEGER/LONG/FLOAT/DOUBLE -> DOUBLE
   */
  private static FunctionResolver radians() {
    return FunctionDSL.define(BuiltinFunctionName.RADIANS.getName(),
        ExprCoreType.numberTypes().stream()
            .map(type -> FunctionDSL.impl(FunctionDSL.nullMissingHandling(
                v -> new ExprDoubleValue(Math.toRadians(v.doubleValue()))),
                DOUBLE, type)).collect(Collectors.toList()));
  }

  /**
   * Definition of sin(x) function.
   * Calculates the sine of x, where x is given in radians
   * The supported signature of sin function is
   * INTEGER/LONG/FLOAT/DOUBLE -> DOUBLE
   */
  private static FunctionResolver sin() {
    return FunctionDSL.define(BuiltinFunctionName.SIN.getName(),
        ExprCoreType.numberTypes().stream()
            .map(type -> FunctionDSL.impl(FunctionDSL.nullMissingHandling(
                v -> new ExprDoubleValue(Math.sin(v.doubleValue()))),
                DOUBLE, type)).collect(Collectors.toList()));
  }

  /**
   * Definition of tan(x) function.
   * Calculates the tangent of x, where x is given in radians
   * The supported signature of tan function is
   * INTEGER/LONG/FLOAT/DOUBLE -> DOUBLE
   */
  private static FunctionResolver tan() {
    return FunctionDSL.define(BuiltinFunctionName.TAN.getName(),
        ExprCoreType.numberTypes().stream()
            .map(type -> FunctionDSL.impl(FunctionDSL.nullMissingHandling(
                v -> new ExprDoubleValue(Math.tan(v.doubleValue()))),
                DOUBLE, type)).collect(Collectors.toList()));
  }
}
