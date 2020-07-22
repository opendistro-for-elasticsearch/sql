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

  /**
   * Date and Time Functions.
   */
  DAYOFMONTH(FunctionName.of("dayofmonth")),

  /**
   * Aggregation Function.
   */
  AVG(FunctionName.of("avg")),
  SUM(FunctionName.of("sum")),
  COUNT(FunctionName.of("count"));

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
