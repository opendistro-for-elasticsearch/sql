package com.amazon.opendistroforelasticsearch.sql.expression.function;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Builtin Function Name.
 */
@Getter
@RequiredArgsConstructor
public enum BuiltinFunctionName {
  /**
   * Mathematical Functions.
   */
  ABS(FunctionName.of("abs")),
  CEIL(FunctionName.of("ceil")),
  CEILING(FunctionName.of("ceiling")),
  CONV(FunctionName.of("conv")),
  CRC32(FunctionName.of("crc32")),
  E(FunctionName.of("e")),
  EXP(FunctionName.of("exp")),
  FLOOR(FunctionName.of("floor")),
  LN(FunctionName.of("ln")),
  LOG(FunctionName.of("log")),
  LOG10(FunctionName.of("log10")),
  LOG2(FunctionName.of("log2")),
  MOD(FunctionName.of("mod")),
  PI(FunctionName.of("pi")),
  POW(FunctionName.of("pow")),
  POWER(FunctionName.of("power")),
  RAND(FunctionName.of("rand")),
  ROUND(FunctionName.of("round")),
  SIGN(FunctionName.of("sign")),
  SQRT(FunctionName.of("sqrt")),
  TRUNCATE(FunctionName.of("truncate")),

  ACOS(FunctionName.of("acos")),
  ASIN(FunctionName.of("asin")),
  ATAN(FunctionName.of("atan")),
  ATAN2(FunctionName.of("atan2")),
  COS(FunctionName.of("cos")),
  COT(FunctionName.of("cot")),
  DEGREES(FunctionName.of("degrees")),
  RADIANS(FunctionName.of("radians")),
  SIN(FunctionName.of("sin")),
  TAN(FunctionName.of("tan")),

  /**
   * Date and Time Functions.
   */
  ADDDATE(FunctionName.of("adddate")),
  DATE(FunctionName.of("date")),
  DATE_ADD(FunctionName.of("date_add")),
  DATE_SUB(FunctionName.of("date_sub")),
  DAY(FunctionName.of("day")),
  DAYNAME(FunctionName.of("dayname")),
  DAYOFMONTH(FunctionName.of("dayofmonth")),
  DAYOFWEEK(FunctionName.of("dayofweek")),
  DAYOFYEAR(FunctionName.of("dayofyear")),
  FROM_DAYS(FunctionName.of("from_days")),
  HOUR(FunctionName.of("hour")),
  MICROSECOND(FunctionName.of("microsecond")),
  MINUTE(FunctionName.of("minute")),
  MONTH(FunctionName.of("month")),
  MONTHNAME(FunctionName.of("monthname")),
  QUARTER(FunctionName.of("quarter")),
  SECOND(FunctionName.of("second")),
  SUBDATE(FunctionName.of("subdate")),
  TIME(FunctionName.of("time")),
  TIME_TO_SEC(FunctionName.of("time_to_sec")),
  TIMESTAMP(FunctionName.of("timestamp")),
  TO_DAYS(FunctionName.of("to_days")),
  WEEK(FunctionName.of("week")),
  YEAR(FunctionName.of("year")),

  /**
   * Text Functions.
   */
  TOSTRING(FunctionName.of("tostring")),

  /**
   * Arithmetic Operators.
   */
  ADD(FunctionName.of("+")),
  SUBTRACT(FunctionName.of("-")),
  MULTIPLY(FunctionName.of("*")),
  DIVIDE(FunctionName.of("/")),
  MODULES(FunctionName.of("%")),

  /**
   * Boolean Operators.
   */
  AND(FunctionName.of("and")),
  OR(FunctionName.of("or")),
  XOR(FunctionName.of("xor")),
  NOT(FunctionName.of("not")),
  EQUAL(FunctionName.of("=")),
  NOTEQUAL(FunctionName.of("!=")),
  LESS(FunctionName.of("<")),
  LTE(FunctionName.of("<=")),
  GREATER(FunctionName.of(">")),
  GTE(FunctionName.of(">=")),
  LIKE(FunctionName.of("like")),
  NOT_LIKE(FunctionName.of("not like")),

  /**
   * Aggregation Function.
   */
  AVG(FunctionName.of("avg")),
  SUM(FunctionName.of("sum")),
  COUNT(FunctionName.of("count")),
  MIN(FunctionName.of("min")),
  MAX(FunctionName.of("max")),

  /**
   * Text Functions.
   */
  SUBSTR(FunctionName.of("substr")),
  SUBSTRING(FunctionName.of("substring")),
  RTRIM(FunctionName.of("rtrim")),
  LTRIM(FunctionName.of("ltrim")),
  TRIM(FunctionName.of("trim")),
  UPPER(FunctionName.of("upper")),
  LOWER(FunctionName.of("lower")),
  REGEXP(FunctionName.of("regexp")),
  CONCAT(FunctionName.of("concat")),
  CONCAT_WS(FunctionName.of("concat_ws")),
  LENGTH(FunctionName.of("length")),
  STRCMP(FunctionName.of("strcmp")),

  /**
   * NULL Test.
   */
  IS_NULL(FunctionName.of("is null")),
  IS_NOT_NULL(FunctionName.of("is not null")),

  INTERVAL(FunctionName.of("interval"));

  private final FunctionName name;

  private static final Map<FunctionName, BuiltinFunctionName> ALL_NATIVE_FUNCTIONS;

  static {
    ImmutableMap.Builder<FunctionName, BuiltinFunctionName> builder = new ImmutableMap.Builder<>();
    for (BuiltinFunctionName func : BuiltinFunctionName.values()) {
      builder.put(func.getName(), func);
    }
    ALL_NATIVE_FUNCTIONS = builder.build();
  }

  public static Optional<BuiltinFunctionName> of(String str) {
    return Optional.ofNullable(ALL_NATIVE_FUNCTIONS.getOrDefault(FunctionName.of(str), null));
  }
}
