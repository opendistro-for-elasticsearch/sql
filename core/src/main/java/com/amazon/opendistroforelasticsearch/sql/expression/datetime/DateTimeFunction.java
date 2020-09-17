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

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DATE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DATETIME;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTERVAL;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.LONG;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.TIME;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.TIMESTAMP;
import static com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionDSL.define;
import static com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionDSL.impl;
import static com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionDSL.nullMissingHandling;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprDateValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprDatetimeValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprIntegerValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprLongValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprStringValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTimeValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTimestampValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionResolver;
import com.google.common.base.CharMatcher;
import java.time.LocalDate;
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

  // The number of days from year zero to year 1970.
  private static final Long DAYS_0000_TO_1970 = (146097 * 5L) - (30L * 365L + 7L);

  /**
   * Register Date and Time Functions.
   *
   * @param repository {@link BuiltinFunctionRepository}.
   */
  public void register(BuiltinFunctionRepository repository) {
    repository.register(date());
    repository.register(date_sub());
    repository.register(day());
    repository.register(dayName());
    repository.register(dayOfMonth());
    repository.register(dayOfWeek());
    repository.register(dayOfYear());
    repository.register(from_days());
    repository.register(hour());
    repository.register(microsecond());
    repository.register(minute());
    repository.register(month());
    repository.register(monthName());
    repository.register(quarter());
    repository.register(second());
    repository.register(subdate());
    repository.register(time());
    repository.register(time_to_sec());
    repository.register(timestamp());
    repository.register(to_days());
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
   * Extracts the date part of a date and time value.
   * Also to construct a date type. The supported signatures:
   * STRING/DATE/DATETIME/TIMESTAMP -> DATE
   */
  private FunctionResolver date_sub() {
    return define(BuiltinFunctionName.DATE_SUB.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprSubDateInterval), DATE, DATE, INTERVAL),
        impl(nullMissingHandling(DateTimeFunction::exprSubDateInterval), DATETIME, DATE, INTERVAL),
        impl(nullMissingHandling(DateTimeFunction::exprSubDateInterval),
            DATETIME, DATETIME, INTERVAL),
        impl(nullMissingHandling(DateTimeFunction::exprSubDateInterval),
            DATETIME, TIMESTAMP, INTERVAL),
        impl(nullMissingHandling(DateTimeFunction::exprSubDateDays), DATE, DATE, LONG),
        impl(nullMissingHandling(DateTimeFunction::exprSubDateDays), DATETIME, DATETIME, LONG),
        impl(nullMissingHandling(DateTimeFunction::exprSubDateDays), DATETIME, TIMESTAMP, LONG)
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
   * DAYOFMONTH(DATE). return the day of the month (1-31).
   */
  private FunctionResolver dayOfMonth() {
    return define(BuiltinFunctionName.DAYOFMONTH.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprDayOfMonth),
            INTEGER, DATE),
        impl(nullMissingHandling(DateTimeFunction::exprDayOfMonth), INTEGER, STRING)
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
   * FROM_DAYS(DATE). return the date value given the day number N.
   */
  private FunctionResolver from_days() {
    return define(BuiltinFunctionName.FROM_DAYS.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprFromDays), DATE, LONG));
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
   * MICROSECOND(TIME). return the microsecond value for time.
   */
  private FunctionResolver microsecond() {
    return define(BuiltinFunctionName.MICROSECOND.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprMicrosecond),
            INTEGER, TIME),
        impl(nullMissingHandling(DateTimeFunction::exprMicrosecond),
            INTEGER, DATETIME),
        impl(nullMissingHandling(DateTimeFunction::exprMicrosecond),
            INTEGER, TIMESTAMP)
    );
  }

  /**
   * MINUTE(TIME). return the minute value for time.
   */
  private FunctionResolver minute() {
    return define(BuiltinFunctionName.MINUTE.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprMinute),
            INTEGER, TIME),
        impl(nullMissingHandling(DateTimeFunction::exprMinute),
            INTEGER, DATETIME),
        impl(nullMissingHandling(DateTimeFunction::exprMinute),
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
   * MONTHNAME(DATE). return the full name of the month for date.
   */
  private FunctionResolver monthName() {
    return define(BuiltinFunctionName.MONTHNAME.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprMonthName),
            STRING, DATE)
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
   * SECOND(TIME). return the second value for time.
   */
  private FunctionResolver second() {
    return define(BuiltinFunctionName.SECOND.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprSecond),
            INTEGER, TIME),
        impl(nullMissingHandling(DateTimeFunction::exprSecond),
            INTEGER, DATETIME),
        impl(nullMissingHandling(DateTimeFunction::exprSecond),
            INTEGER, TIMESTAMP)
    );
  }

  /**
   * Extracts the date part of a date and time value.
   * Also to construct a date type. The supported signatures:
   * STRING/DATE/DATETIME/TIMESTAMP -> DATE
   */
  private FunctionResolver subdate() {
    return define(BuiltinFunctionName.SUBDATE.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprSubDateInterval), DATE, DATE, INTERVAL),
        impl(nullMissingHandling(DateTimeFunction::exprSubDateInterval), DATETIME, DATE, INTERVAL),
        impl(nullMissingHandling(DateTimeFunction::exprSubDateInterval),
            DATETIME, DATETIME, INTERVAL),
        impl(nullMissingHandling(DateTimeFunction::exprSubDateInterval),
            DATETIME, TIMESTAMP, INTERVAL),
        impl(nullMissingHandling(DateTimeFunction::exprSubDateDays), DATE, DATE, LONG),
        impl(nullMissingHandling(DateTimeFunction::exprSubDateDays), DATETIME, DATETIME, LONG),
        impl(nullMissingHandling(DateTimeFunction::exprSubDateDays), DATETIME, TIMESTAMP, LONG)
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
   * TIME_TO_SEC(TIME). return the time argument, converted to seconds.
   */
  private FunctionResolver time_to_sec() {
    return define(BuiltinFunctionName.TIME_TO_SEC.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprTimeToSec),
            LONG, TIME)
    );
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
   * TO_DAYS(DATE). return the day number of the given date.
   */
  private FunctionResolver to_days() {
    return define(BuiltinFunctionName.TO_DAYS.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprToDays), LONG, DATE),
        impl(nullMissingHandling(DateTimeFunction::exprToDays), LONG, DATETIME));
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
   * Name of the Weekday implementation for ExprValue.
   * @param date ExprValue of Date type.
   * @return ExprValue.
   */
  private ExprValue exprDayName(ExprValue date) {
    return new ExprStringValue(
        date.dateValue().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()));
  }

  /**
   * Day of Month implementation for ExprValue.
   * @param date ExprValue of Date type.
   * @return ExprValue.
   */
  private ExprValue exprDayOfMonth(ExprValue date) {
    if (date instanceof ExprStringValue) {
      return new ExprIntegerValue(
          new ExprDateValue(date.stringValue()).dateValue().getDayOfMonth());
    }
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

  /** From_days implementation for ExprValue.
   * @param exprValue Day number N.
   * @return ExprValue.
   */
  private ExprValue exprFromDays(ExprValue exprValue) {
    return new ExprDateValue(LocalDate.ofEpochDay(exprValue.longValue() - DAYS_0000_TO_1970));
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
   * Microsecond implementation for ExprValue.
   * @param exprValue ExprValue of Time type.
   * @return ExprValue.
   */
  private ExprValue exprMicrosecond(ExprValue exprValue) {
    return new ExprIntegerValue((exprValue.timeValue().getNano() == 0) ? 0
        : Integer.parseInt(CharMatcher.is('0')
            .trimTrailingFrom(Integer.toString(exprValue.timeValue().getNano()))));
  }

  /**
   * Minute implementation for ExprValue.
   * @param exprValue ExprValue of Time type.
   * @return ExprValue.
   */
  private ExprValue exprMinute(ExprValue exprValue) {
    return new ExprIntegerValue(exprValue.timeValue().getMinute());
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
   * Name of the Month implementation for ExprValue.
   * @param date ExprValue of Date type.
   * @return ExprValue.
   */
  private ExprValue exprMonthName(ExprValue date) {
    return new ExprStringValue(
        date.dateValue().getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()));
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
   * Second implementation for ExprValue.
   * @param exprValue ExprValue of Time type.
   * @return ExprValue.
   */
  private ExprValue exprSecond(ExprValue exprValue) {
    return new ExprIntegerValue(exprValue.timeValue().getSecond());
  }

  /**
   * SUBDATE function implementation for ExprValue.
   *
   * @param date ExprValue of Date/Datetime/Timestamp type.
   * @param days ExprValue of Long type, representing the number of days to subtract.
   * @return Date/Datetime resulted from days subtracted to date.
   */
  private ExprValue exprSubDateDays(ExprValue date, ExprValue days) {
    if (date instanceof ExprDateValue) {
      return new ExprDateValue(date.dateValue().minusDays(days.longValue()));
    } else {
      return new ExprDatetimeValue(date.datetimeValue().minusDays(days.longValue()));
    }
  }

  /**
   * SUBDATE function implementation for ExprValue.
   *
   * @param date ExprValue of Date/Datetime/Timestamp type.
   * @param expr ExprValue of Interval type, the temporal amount to subtract.
   * @return Date/Datetime resulted from expr subtracted to date.
   */
  private ExprValue exprSubDateInterval(ExprValue date, ExprValue expr) {
    return new ExprDatetimeValue(date.datetimeValue().minus(expr.intervalValue()));
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

  /**
   * Time To Sec implementation for ExprValue.
   * @param exprValue ExprValue of Time type.
   * @return ExprValue.
   */
  private ExprValue exprTimeToSec(ExprValue exprValue) {
    return new ExprLongValue(exprValue.timeValue().toSecondOfDay());
  }

  /**
   * To_days implementation for ExprValue.
   * @param exprValue ExprValue of Date type.
   * @return ExprValue.
   */
  private ExprValue exprToDays(ExprValue exprValue) {
    return new ExprLongValue(exprValue.dateValue().toEpochDay() + DAYS_0000_TO_1970);
  }

  /**
   * Year for date implementation for ExprValue.
   * @param date ExprValue of Date type.
   * @return ExprValue.
   */
  private ExprValue exprYear(ExprValue date) {
    return new ExprIntegerValue(date.dateValue().getYear());
  }
}
