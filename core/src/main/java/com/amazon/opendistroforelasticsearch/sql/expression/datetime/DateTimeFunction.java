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

package com.amazon.opendistroforelasticsearch.sql.expression.datetime;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.getStringValue;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DATE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DATETIME;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.TIME;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.TIMESTAMP;
import static com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionDSL.define;
import static com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionDSL.impl;
import static com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionDSL.nullMissingHandling;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprDateValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprIntegerValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprStringValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTimeValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTimestampValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionResolver;
import java.time.format.TextStyle;
import java.util.Locale;
import lombok.experimental.UtilityClass;

/**
 * The definition of date and time functions.
 * 1) have the clear interface for function define.
 * 2) the implementation should rely on ExprValue.
 */
@UtilityClass
public class DateTimeFunction {
  /**
   * Register Date and Time Functions.
   *
   * @param repository {@link BuiltinFunctionRepository}.
   */
  public void register(BuiltinFunctionRepository repository) {
    repository.register(date());
    repository.register(dayName());
    repository.register(monthName());
    repository.register(dayOfMonth());
    repository.register(dayOfWeek());
    repository.register(dayOfYear());
    repository.register(hour());
    repository.register(time());
    repository.register(timestamp());
    repository.register(day());
    repository.register(month());
    repository.register(quarter());
    repository.register(year());
  }

  /**
   * Extracts the date part of a date and time value.
   * Also to construct a date type. The supported signatures:
   * STRING/DATE/DATETIME/TIMESTAMP -> DATE
   */
  private FunctionResolver date() {
    return define(BuiltinFunctionName.DATE.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprDate), DATE, STRING),
        impl(nullMissingHandling(DateTimeFunction::exprDate), DATE, DATE),
        impl(nullMissingHandling(DateTimeFunction::exprDate), DATE, DATETIME),
        impl(nullMissingHandling(DateTimeFunction::exprDate), DATE, TIMESTAMP));
  }

  /**
   * DAYNAME(DATE). return the name of the weekday for date, including Monday, Tuesday, Wednesday,
   * Thursday, Friday, Saturday and Sunday.
   */
  private FunctionResolver dayName() {
    return define(BuiltinFunctionName.DAYNAME.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprDayName),
            STRING, DATE)
    );
  }

  /**
   * MONTHNAME(DATE). return the full name of the month for date.
   */
  private FunctionResolver monthName() {
    return define(BuiltinFunctionName.MONTHNAME.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprMonthName),
            STRING, DATE)
    );
  }

  /**
   * DAYOFMONTH(DATE). return the day of the month (1-31).
   */
  private FunctionResolver dayOfMonth() {
    return define(BuiltinFunctionName.DAYOFMONTH.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprDayOfMonth),
            INTEGER, DATE)
    );
  }

  /**
   * DAYOFWEEK(DATE). return the weekday index for date (1 = Sunday, 2 = Monday, …, 7 = Saturday).
   */
  private FunctionResolver dayOfWeek() {
    return define(BuiltinFunctionName.DAYOFWEEK.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprDayOfWeek),
            INTEGER, DATE)
    );
  }

  /**
   * DAYOFYEAR(DATE). return the day of the year for date (1-366).
   */
  private FunctionResolver dayOfYear() {
    return define(BuiltinFunctionName.DAYOFYEAR.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprDayOfYear),
            INTEGER, DATE)
    );
  }

  /**
   * DAY(DATE). return the day of the month (1-31).
   */
  private FunctionResolver day() {
    return define(BuiltinFunctionName.DAY.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprDayOfMonth),
            INTEGER, DATE)
    );
  }

  /**
   * HOUR(TIME). return the hour value for time.
   */
  private FunctionResolver hour() {
    return define(BuiltinFunctionName.HOUR.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprHour),
            INTEGER, TIME),
        impl(nullMissingHandling(DateTimeFunction::exprHour),
            INTEGER, DATETIME),
        impl(nullMissingHandling(DateTimeFunction::exprHour),
            INTEGER, TIMESTAMP)
    );
  }

  /**
   * MONTH(DATE). return the month for date (1-12).
   */
  private FunctionResolver month() {
    return define(BuiltinFunctionName.MONTH.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprMonth),
            INTEGER, DATE)
    );
  }

  /**
   * QUARTER(DATE). return the month for date (1-4).
   */
  private FunctionResolver quarter() {
    return define(BuiltinFunctionName.QUARTER.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprQuarter),
            INTEGER, DATE)
    );
  }

  /**
   * YEAR(DATE). return the year for date (1000-9999).
   */
  private FunctionResolver year() {
    return define(BuiltinFunctionName.YEAR.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprYear),
            INTEGER, DATE)
    );
  }

  /**
   * Extracts the time part of a date and time value.
   * Also to construct a time type. The supported signatures:
   * STRING/DATE/DATETIME/TIME/TIMESTAMP -> TIME
   */
  private FunctionResolver time() {
    return define(BuiltinFunctionName.TIME.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprTime), TIME, STRING),
        impl(nullMissingHandling(DateTimeFunction::exprTime), TIME, DATE),
        impl(nullMissingHandling(DateTimeFunction::exprTime), TIME, DATETIME),
        impl(nullMissingHandling(DateTimeFunction::exprTime), TIME, TIME),
        impl(nullMissingHandling(DateTimeFunction::exprTime), TIME, TIMESTAMP));
  }

  /**
   * Extracts the timestamp of a date and time value.
   * Also to construct a date type. The supported signatures:
   * STRING/DATE/DATETIME/TIMESTAMP -> DATE
   */
  private FunctionResolver timestamp() {
    return define(BuiltinFunctionName.TIMESTAMP.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprTimestamp), TIMESTAMP, STRING),
        impl(nullMissingHandling(DateTimeFunction::exprTimestamp), TIMESTAMP, DATE),
        impl(nullMissingHandling(DateTimeFunction::exprTimestamp), TIMESTAMP, DATETIME),
        impl(nullMissingHandling(DateTimeFunction::exprTimestamp), TIMESTAMP, TIMESTAMP));
  }

  /**
   * Date implementation for ExprValue.
   * @param exprValue ExprValue of Date type or String type.
   * @return ExprValue.
   */
  private ExprValue exprDate(ExprValue exprValue) {
    if (exprValue instanceof ExprStringValue) {
      return new ExprDateValue(exprValue.stringValue());
    } else {
      return new ExprDateValue(exprValue.dateValue());
    }
  }

  /**
   * Month for date implementation for ExprValue.
   * @param date ExprValue of Date type.
   * @return ExprValue.
   */
  private ExprValue exprMonth(ExprValue date) {
    return new ExprIntegerValue(date.dateValue().getMonthValue());
  }

  /**
   * Quarter for date implementation for ExprValue.
   *
   * @param date ExprValue of Date type.
   * @return ExprValue.
   */
  private ExprValue exprQuarter(ExprValue date) {
    return new ExprIntegerValue((date.dateValue().getMonthValue() % 3) == 0
        ? (date.dateValue().getMonthValue() / 3)
        : ((date.dateValue().getMonthValue() / 3) + 1));
  }

  /**
   * Year for date implementation for ExprValue.
   * @param date ExprValue of Date type.
   * @return ExprValue.
   */
  private ExprValue exprYear(ExprValue date) {
    return new ExprIntegerValue(date.dateValue().getYear());
  }

  /**
   * Name of the Weekday implementation for ExprValue.
   * @param date ExprValue of Date type.
   * @return ExprValue.
   */
  private ExprValue exprDayName(ExprValue date) {
    return new ExprStringValue(
        date.dateValue().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()));
  }

  /**
   * Name of the Month implementation for ExprValue.
   * @param date ExprValue of Date type.
   * @return ExprValue.
   */
  private ExprValue exprMonthName(ExprValue date) {
    return new ExprStringValue(
        date.dateValue().getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()));
  }

  /**
   * Day of Month implementation for ExprValue.
   * @param date ExprValue of Date type.
   * @return ExprValue.
   */
  private ExprValue exprDayOfMonth(ExprValue date) {
    return new ExprIntegerValue(date.dateValue().getDayOfMonth());
  }

  /**
   * Day of Week implementation for ExprValue.
   * @param date ExprValue of Date type.
   * @return ExprValue.
   */
  private ExprValue exprDayOfWeek(ExprValue date) {
    return new ExprIntegerValue(date.dateValue().getDayOfWeek().getValue() == 7 ? 1
        : date.dateValue().getDayOfWeek().getValue() + 1);
  }

  /**
   * Day of Year implementation for ExprValue.
   * @param date ExprValue of Date type.
   * @return ExprValue.
   */
  private ExprValue exprDayOfYear(ExprValue date) {
    return new ExprIntegerValue(date.dateValue().getDayOfYear());
  }

  /**
   * Hour implementation for ExprValue.
   * @param exprValue ExprValue of Time type.
   * @return ExprValue.
   */
  private ExprValue exprHour(ExprValue exprValue) {
    return new ExprIntegerValue(exprValue.timeValue().getHour());
  }

  /**
   * Time implementation for ExprValue.
   * @param exprValue ExprValue of Time type or String.
   * @return ExprValue.
   */
  private ExprValue exprTime(ExprValue exprValue) {
    if (exprValue instanceof ExprStringValue) {
      return new ExprTimeValue(exprValue.stringValue());
    } else {
      return new ExprTimeValue(exprValue.timeValue());
    }
  }

  /**
   * Timestamp implementation for ExprValue.
   * @param exprValue ExprValue of Timestamp type or String type.
   * @return ExprValue.
   */
  private ExprValue exprTimestamp(ExprValue exprValue) {
    if (exprValue instanceof ExprStringValue) {
      return new ExprTimestampValue(exprValue.stringValue());
    } else {
      return new ExprTimestampValue(exprValue.timestampValue());
    }
  }
}
