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

package com.amazon.opendistroforelasticsearch.sql.expression;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprShortValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.Aggregator;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.NamedAggregator;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DSL {
  private final BuiltinFunctionRepository repository;

  public static LiteralExpression literal(Short value) {
    return new LiteralExpression(new ExprShortValue(value));
  }

  public static LiteralExpression literal(Integer value) {
    return new LiteralExpression(ExprValueUtils.integerValue(value));
  }

  public static LiteralExpression literal(Long value) {
    return new LiteralExpression(ExprValueUtils.longValue(value));
  }

  public static LiteralExpression literal(Float value) {
    return new LiteralExpression(ExprValueUtils.floatValue(value));
  }

  public static LiteralExpression literal(Double value) {
    return new LiteralExpression(ExprValueUtils.doubleValue(value));
  }

  public static LiteralExpression literal(String value) {
    return new LiteralExpression(ExprValueUtils.stringValue(value));
  }

  public static LiteralExpression literal(Boolean value) {
    return new LiteralExpression(ExprValueUtils.booleanValue(value));
  }

  public static LiteralExpression literal(ExprValue value) {
    return new LiteralExpression(value);
  }

  /**
   * Wrap a number to {@link LiteralExpression}.
   */
  public static LiteralExpression literal(Number value) {
    if (value instanceof Integer) {
      return new LiteralExpression(ExprValueUtils.integerValue(value.intValue()));
    } else if (value instanceof Long) {
      return new LiteralExpression(ExprValueUtils.longValue(value.longValue()));
    } else if (value instanceof Float) {
      return new LiteralExpression(ExprValueUtils.floatValue(value.floatValue()));
    } else {
      return new LiteralExpression(ExprValueUtils.doubleValue(value.doubleValue()));
    }
  }

  public static ReferenceExpression ref(String ref, ExprType type) {
    return new ReferenceExpression(ref, type);
  }

  /**
   * Wrap a named expression if not yet. The intent is that different languages may use
   * Alias or not when building AST. This caused either named or unnamed expression
   * is resolved by analyzer. To make unnamed expression acceptable for logical project,
   * it is required to wrap it by named expression here before passing to logical project.
   *
   * @param expression  expression
   * @return            expression if named already or expression wrapped by named expression.
   */
  public static NamedExpression named(Expression expression) {
    if (expression instanceof NamedExpression) {
      return (NamedExpression) expression;
    }
    return named(expression.toString(), expression);
  }

  public static NamedExpression named(String name, Expression expression) {
    return new NamedExpression(name, expression);
  }

  public static NamedExpression named(String name, Expression expression, String alias) {
    return new NamedExpression(name, expression, alias);
  }

  public static NamedAggregator named(String name, Aggregator aggregator) {
    return new NamedAggregator(name, aggregator);
  }

  public FunctionExpression abs(Expression... expressions) {
    return function(BuiltinFunctionName.ABS, expressions);
  }

  public FunctionExpression ceil(Expression... expressions) {
    return function(BuiltinFunctionName.CEIL, expressions);
  }

  public FunctionExpression ceiling(Expression... expressions) {
    return function(BuiltinFunctionName.CEILING, expressions);
  }

  public FunctionExpression conv(Expression... expressions) {
    return function(BuiltinFunctionName.CONV, expressions);
  }

  public FunctionExpression crc32(Expression... expressions) {
    return function(BuiltinFunctionName.CRC32, expressions);
  }

  public FunctionExpression euler(Expression... expressions) {
    return function(BuiltinFunctionName.E, expressions);
  }

  public FunctionExpression exp(Expression... expressions) {
    return function(BuiltinFunctionName.EXP, expressions);
  }

  public FunctionExpression floor(Expression... expressions) {
    return function(BuiltinFunctionName.FLOOR, expressions);
  }

  public FunctionExpression ln(Expression... expressions) {
    return function(BuiltinFunctionName.LN, expressions);
  }

  public FunctionExpression log(Expression... expressions) {
    return function(BuiltinFunctionName.LOG, expressions);
  }

  public FunctionExpression log10(Expression... expressions) {
    return function(BuiltinFunctionName.LOG10, expressions);
  }

  public FunctionExpression log2(Expression... expressions) {
    return function(BuiltinFunctionName.LOG2, expressions);
  }

  public FunctionExpression mod(Expression... expressions) {
    return function(BuiltinFunctionName.MOD, expressions);
  }

  public FunctionExpression pi(Expression... expressions) {
    return function(BuiltinFunctionName.PI, expressions);
  }

  public FunctionExpression pow(Expression... expressions) {
    return function(BuiltinFunctionName.POW, expressions);
  }

  public FunctionExpression power(Expression... expressions) {
    return function(BuiltinFunctionName.POWER, expressions);
  }

  public FunctionExpression rand(Expression... expressions) {
    return function(BuiltinFunctionName.RAND, expressions);
  }

  public FunctionExpression round(Expression... expressions) {
    return function(BuiltinFunctionName.ROUND, expressions);
  }

  public FunctionExpression sign(Expression... expressions) {
    return function(BuiltinFunctionName.SIGN, expressions);
  }

  public FunctionExpression sqrt(Expression... expressions) {
    return function(BuiltinFunctionName.SQRT, expressions);
  }

  public FunctionExpression truncate(Expression... expressions) {
    return function(BuiltinFunctionName.TRUNCATE, expressions);
  }

  public FunctionExpression acos(Expression... expressions) {
    return function(BuiltinFunctionName.ACOS, expressions);
  }

  public FunctionExpression asin(Expression... expressions) {
    return function(BuiltinFunctionName.ASIN, expressions);
  }

  public FunctionExpression atan(Expression... expressions) {
    return function(BuiltinFunctionName.ATAN, expressions);
  }

  public FunctionExpression atan2(Expression... expressions) {
    return function(BuiltinFunctionName.ATAN2, expressions);
  }

  public FunctionExpression cos(Expression... expressions) {
    return function(BuiltinFunctionName.COS, expressions);
  }

  public FunctionExpression cot(Expression... expressions) {
    return function(BuiltinFunctionName.COT, expressions);
  }

  public FunctionExpression degrees(Expression... expressions) {
    return function(BuiltinFunctionName.DEGREES, expressions);
  }

  public FunctionExpression radians(Expression... expressions) {
    return function(BuiltinFunctionName.RADIANS, expressions);
  }

  public FunctionExpression sin(Expression... expressions) {
    return function(BuiltinFunctionName.SIN, expressions);
  }

  public FunctionExpression tan(Expression... expressions) {
    return function(BuiltinFunctionName.TAN, expressions);
  }

  public FunctionExpression add(Expression... expressions) {
    return function(BuiltinFunctionName.ADD, expressions);
  }

  public FunctionExpression subtract(Expression... expressions) {
    return function(BuiltinFunctionName.SUBTRACT, expressions);
  }

  public FunctionExpression multiply(Expression... expressions) {
    return function(BuiltinFunctionName.MULTIPLY, expressions);
  }
  
  public FunctionExpression adddate(Expression... expressions) {
    return function(BuiltinFunctionName.ADDDATE, expressions);
  }

  public FunctionExpression date(Expression... expressions) {
    return function(BuiltinFunctionName.DATE, expressions);
  }

  public FunctionExpression date_add(Expression... expressions) {
    return function(BuiltinFunctionName.DATE_ADD, expressions);
  }

  public FunctionExpression date_sub(Expression... expressions) {
    return function(BuiltinFunctionName.DATE_SUB, expressions);
  }

  public FunctionExpression day(Expression... expressions) {
    return function(BuiltinFunctionName.DAY, expressions);
  }

  public FunctionExpression dayname(Expression... expressions) {
    return function(BuiltinFunctionName.DAYNAME, expressions);
  }

  public FunctionExpression dayofmonth(Expression... expressions) {
    return function(BuiltinFunctionName.DAYOFMONTH, expressions);
  }

  public FunctionExpression dayofweek(Expression... expressions) {
    return function(BuiltinFunctionName.DAYOFWEEK, expressions);
  }

  public FunctionExpression dayofyear(Expression... expressions) {
    return function(BuiltinFunctionName.DAYOFYEAR, expressions);
  }

  public FunctionExpression from_days(Expression... expressions) {
    return function(BuiltinFunctionName.FROM_DAYS, expressions);
  }

  public FunctionExpression hour(Expression... expressions) {
    return function(BuiltinFunctionName.HOUR, expressions);
  }

  public FunctionExpression microsecond(Expression... expressions) {
    return function(BuiltinFunctionName.MICROSECOND, expressions);
  }

  public FunctionExpression minute(Expression... expressions) {
    return function(BuiltinFunctionName.MINUTE, expressions);
  }

  public FunctionExpression month(Expression... expressions) {
    return function(BuiltinFunctionName.MONTH, expressions);
  }

  public FunctionExpression monthname(Expression... expressions) {
    return function(BuiltinFunctionName.MONTHNAME, expressions);
  }

  public FunctionExpression quarter(Expression... expressions) {
    return function(BuiltinFunctionName.QUARTER, expressions);
  }

  public FunctionExpression second(Expression... expressions) {
    return function(BuiltinFunctionName.SECOND, expressions);
  }

  public FunctionExpression subdate(Expression... expressions) {
    return function(BuiltinFunctionName.SUBDATE, expressions);
  }

  public FunctionExpression time(Expression... expressions) {
    return function(BuiltinFunctionName.TIME, expressions);
  }

  public FunctionExpression time_to_sec(Expression... expressions) {
    return function(BuiltinFunctionName.TIME_TO_SEC, expressions);
  }

  public FunctionExpression timestamp(Expression... expressions) {
    return function(BuiltinFunctionName.TIMESTAMP, expressions);
  }

  public FunctionExpression to_days(Expression... expressions) {
    return function(BuiltinFunctionName.TO_DAYS, expressions);
  }

  public FunctionExpression year(Expression... expressions) {
    return function(BuiltinFunctionName.YEAR, expressions);
  }

  public FunctionExpression divide(Expression... expressions) {
    return function(BuiltinFunctionName.DIVIDE, expressions);
  }

  public FunctionExpression module(Expression... expressions) {
    return function(BuiltinFunctionName.MODULES, expressions);
  }

  public FunctionExpression substr(Expression... expressions) {
    return function(BuiltinFunctionName.SUBSTR, expressions);
  }
  
  public FunctionExpression substring(Expression... expressions) {
    return function(BuiltinFunctionName.SUBSTR, expressions);
  }

  public FunctionExpression ltrim(Expression... expressions) {
    return function(BuiltinFunctionName.LTRIM, expressions);
  }

  public FunctionExpression rtrim(Expression... expressions) {
    return function(BuiltinFunctionName.RTRIM, expressions);
  }

  public FunctionExpression trim(Expression... expressions) {
    return function(BuiltinFunctionName.TRIM, expressions);
  }

  public FunctionExpression upper(Expression... expressions) {
    return function(BuiltinFunctionName.UPPER, expressions);
  }

  public FunctionExpression lower(Expression... expressions) {
    return function(BuiltinFunctionName.LOWER, expressions);
  }

  public FunctionExpression regexp(Expression... expressions) {
    return function(BuiltinFunctionName.REGEXP, expressions);
  }

  public FunctionExpression concat(Expression... expressions) {
    return function(BuiltinFunctionName.CONCAT, expressions);
  }

  public FunctionExpression concat_ws(Expression... expressions) {
    return function(BuiltinFunctionName.CONCAT_WS, expressions);
  }

  public FunctionExpression length(Expression... expressions) {
    return function(BuiltinFunctionName.LENGTH, expressions);
  }

  public FunctionExpression strcmp(Expression... expressions) {
    return function(BuiltinFunctionName.STRCMP, expressions);
  }

  public FunctionExpression and(Expression... expressions) {
    return function(BuiltinFunctionName.AND, expressions);
  }

  public FunctionExpression or(Expression... expressions) {
    return function(BuiltinFunctionName.OR, expressions);
  }

  public FunctionExpression xor(Expression... expressions) {
    return function(BuiltinFunctionName.XOR, expressions);
  }

  public FunctionExpression not(Expression... expressions) {
    return function(BuiltinFunctionName.NOT, expressions);
  }

  public FunctionExpression equal(Expression... expressions) {
    return function(BuiltinFunctionName.EQUAL, expressions);
  }

  public FunctionExpression notequal(Expression... expressions) {
    return function(BuiltinFunctionName.NOTEQUAL, expressions);
  }

  public FunctionExpression less(Expression... expressions) {
    return function(BuiltinFunctionName.LESS, expressions);
  }

  public FunctionExpression lte(Expression... expressions) {
    return function(BuiltinFunctionName.LTE, expressions);
  }

  public FunctionExpression greater(Expression... expressions) {
    return function(BuiltinFunctionName.GREATER, expressions);
  }

  public FunctionExpression gte(Expression... expressions) {
    return function(BuiltinFunctionName.GTE, expressions);
  }

  public FunctionExpression like(Expression... expressions) {
    return function(BuiltinFunctionName.LIKE, expressions);
  }

  public FunctionExpression notLike(Expression... expressions) {
    return function(BuiltinFunctionName.NOT_LIKE, expressions);
  }

  public Aggregator avg(Expression... expressions) {
    return aggregate(BuiltinFunctionName.AVG, expressions);
  }

  public Aggregator sum(Expression... expressions) {
    return aggregate(BuiltinFunctionName.SUM, expressions);
  }

  public Aggregator count(Expression... expressions) {
    return aggregate(BuiltinFunctionName.COUNT, expressions);
  }

  public Aggregator min(Expression... expressions) {
    return aggregate(BuiltinFunctionName.MIN, expressions);
  }

  public Aggregator max(Expression... expressions) {
    return aggregate(BuiltinFunctionName.MAX, expressions);
  }

  private FunctionExpression function(BuiltinFunctionName functionName, Expression... expressions) {
    return (FunctionExpression) repository.compile(
        functionName.getName(), Arrays.asList(expressions));
  }

  private Aggregator aggregate(BuiltinFunctionName functionName, Expression... expressions) {
    return (Aggregator) repository.compile(
        functionName.getName(), Arrays.asList(expressions));
  }

  public FunctionExpression isnull(Expression... expressions) {
    return function(BuiltinFunctionName.IS_NULL, expressions);
  }

  public FunctionExpression isnotnull(Expression... expressions) {
    return function(BuiltinFunctionName.IS_NOT_NULL, expressions);
  }

  public FunctionExpression interval(Expression value, Expression unit) {
    return (FunctionExpression) repository.compile(
        BuiltinFunctionName.INTERVAL.getName(), Arrays.asList(value, unit));
  }
}
