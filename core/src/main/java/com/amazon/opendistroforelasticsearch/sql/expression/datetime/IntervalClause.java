/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.expression.datetime;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.getIntegerValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.getLongValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.getStringValue;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.FLOAT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.LONG;
import static com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionDSL.define;

import com.amazon.opendistroforelasticsearch.sql.common.utils.StringUtils;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprIntervalValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionBuilder;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionResolver;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionSignature;
import com.amazon.opendistroforelasticsearch.sql.expression.function.SerializableBiFunction;
import com.amazon.opendistroforelasticsearch.sql.expression.function.SerializableFunction;
import java.time.Duration;
import java.time.Period;
import java.util.Arrays;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;

@UtilityClass
public class IntervalClause {

  private static final String MICRO_SECOND = "microsecond";
  private static final String SECOND = "second";
  private static final String MINUTE = "minute";
  private static final String HOUR = "hour";
  private static final String DAY = "day";
  private static final String WEEK = "week";
  private static final String MONTH = "month";
  private static final String QUARTER = "quarter";
  private static final String YEAR = "year";

  public void register(BuiltinFunctionRepository repository) {
    repository.register(interval());
  }

  private FunctionResolver interval() {
    return define(BuiltinFunctionName.INTERVAL.getName(),
        intervalImp(IntervalClause::interval, INTEGER),
        intervalImp(IntervalClause::interval, LONG),
        intervalImp(IntervalClause::interval, FLOAT),
        intervalImp(IntervalClause::interval, DOUBLE));
  }

  private ExprValue interval(ExprValue value, ExprValue unit) {
    switch (getStringValue(unit).toLowerCase()) {
      case MICRO_SECOND:
        return microsecond(value);
      case SECOND:
        return second(value);
      case MINUTE:
        return minute(value);
      case HOUR:
        return hour(value);
      case DAY:
        return day(value);
      case WEEK:
        return week(value);
      case MONTH:
        return month(value);
      case QUARTER:
        return quarter(value);
      case YEAR:
        return year(value);
      default:
        throw new ExpressionEvaluationException(
            String.format("interval unit %s is not supported", getStringValue(unit)));
    }
  }

  private ExprValue microsecond(ExprValue value) {
    return new ExprIntervalValue(Duration.ofNanos(getLongValue(value) * 1000));
  }

  private ExprValue second(ExprValue value) {
    return new ExprIntervalValue(Duration.ofSeconds(getLongValue(value)));
  }

  private ExprValue minute(ExprValue value) {
    return new ExprIntervalValue(Duration.ofMinutes(getLongValue(value)));
  }

  private ExprValue hour(ExprValue value) {
    return new ExprIntervalValue(Duration.ofHours(getLongValue(value)));
  }

  private ExprValue day(ExprValue value) {
    return new ExprIntervalValue(Duration.ofDays(getIntegerValue(value)));
  }

  private ExprValue week(ExprValue value) {
    return new ExprIntervalValue(Period.ofWeeks(getIntegerValue(value)));
  }

  private ExprValue month(ExprValue value) {
    return new ExprIntervalValue(Period.ofMonths(getIntegerValue(value)));
  }

  private ExprValue quarter(ExprValue value) {
    return new ExprIntervalValue(Period.ofMonths(getIntegerValue(value) * 3));
  }

  private ExprValue year(ExprValue value) {
    return new ExprIntervalValue(Period.ofYears(getIntegerValue(value)));
  }

  /**
   * Interval argument implementation as function.
   */
  private static SerializableFunction
      <FunctionName, Pair<FunctionSignature, FunctionBuilder>> intervalImp(
      SerializableBiFunction<ExprValue, ExprValue, ExprValue> function,
      ExprType argType) {

    return functionName -> {
      FunctionSignature functionSignature =
          new FunctionSignature(functionName, Arrays.asList(argType, ExprCoreType.STRING));
      FunctionBuilder functionBuilder =
          arguments -> new FunctionExpression(functionName, arguments) {
            @Override
            public ExprValue valueOf(Environment<Expression, ExprValue> valueEnv) {
              ExprValue value = arguments.get(0).valueOf(valueEnv);
              ExprValue unit = arguments.get(1).valueOf(valueEnv);
              return function.apply(value, unit);
            }

            @Override
            public ExprType type() {
              return ExprCoreType.INTERVAL;
            }

            @Override
            public String toString() {
              return String.format("INTERVAL %s %s", arguments.get(0).toString(),
                  StringUtils.unquoteText(arguments.get(1).toString()));
            }
          };
      return Pair.of(functionSignature, functionBuilder);
    };
  }

}
