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
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionResolver;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
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
    repository.register(adddate());
    repository.register(date());
    repository.register(date_add());
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
    repository.register(week());
    repository.register(year());
  }

  /**
   * Specify a start date and add a temporal amount to the date.
   * The return type depends on the date type and the interval unit. Detailed supported signatures:
   * (STRING/DATE/DATETIME/TIMESTAMP, INTERVAL) -> DATETIME
   * (DATE, LONG) -> DATE
   * (STRING/DATETIME/TIMESTAMP, LONG) -> DATETIME
   */

  private FunctionResolver add_date(FunctionName functionName) {
    return define(functionName,
        impl(nullMissingHandling(DateTimeFunction::exprAddDateInterval),
            DATETIME, STRING, INTERVAL),
        impl(nullMissingHandling(DateTimeFunction::exprAddDateInterval), DATETIME, DATE, INTERVAL),
        impl(nullMissingHandling(DateTimeFunction::exprAddDateInterval),
            DATETIME, DATETIME, INTERVAL),
        impl(nullMissingHandling(DateTimeFunction::exprAddDateInterval),
            DATETIME, TIMESTAMP, INTERVAL),
        impl(nullMissingHandling(DateTimeFunction::exprAddDateDays), DATE, DATE, LONG),
        impl(nullMissingHandling(DateTimeFunction::exprAddDateDays), DATETIME, DATETIME, LONG),
        impl(nullMissingHandling(DateTimeFunction::exprAddDateDays), DATETIME, TIMESTAMP, LONG),
        impl(nullMissingHandling(DateTimeFunction::exprAddDateDays), DATETIME, STRING, LONG)
    );
  }

  private FunctionResolver adddate() {
    return add_date(BuiltinFunctionName.ADDDATE.getName());
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

  private FunctionResolver date_add() {
    return add_date(BuiltinFunctionName.DATE_ADD.getName());
  }

  /**
   * Specify a start date and subtract a temporal amount to the date.
   * The return type depends on the date type and the interval unit. Detailed supported signatures:
   * (STRING/DATE/DATETIME/TIMESTAMP, INTERVAL) -> DATETIME
   * (DATE, LONG) -> DATE
   * (STRING/DATETIME/TIMESTAMP, LONG) -> DATETIME
   */
  private FunctionResolver sub_date(FunctionName functionName) {
    return define(functionName,
        impl(nullMissingHandling(DateTimeFunction::exprSubDateInterval),
            DATETIME, STRING, INTERVAL),
        impl(nullMissingHandling(DateTimeFunction::exprSubDateInterval), DATETIME, DATE, INTERVAL),
        impl(nullMissingHandling(DateTimeFunction::exprSubDateInterval),
            DATETIME, DATETIME, INTERVAL),
        impl(nullMissingHandling(DateTimeFunction::exprSubDateInterval),
            DATETIME, TIMESTAMP, INTERVAL),
        impl(nullMissingHandling(DateTimeFunction::exprSubDateDays), DATE, DATE, LONG),
        impl(nullMissingHandling(DateTimeFunction::exprSubDateDays), DATETIME, DATETIME, LONG),
        impl(nullMissingHandling(DateTimeFunction::exprSubDateDays), DATETIME, TIMESTAMP, LONG),
        impl(nullMissingHandling(DateTimeFunction::exprSubDateDays), DATETIME, STRING, LONG)
    );
  }

  private FunctionResolver date_sub() {
    return sub_date(BuiltinFunctionName.DATE_SUB.getName());
  }

  /**
   * DAY(STRING/DATE/DATETIME/TIMESTAMP). return the day of the month (1-31).
   */
  private FunctionResolver day() {
    return define(BuiltinFunctionName.DAY.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprDayOfMonth), INTEGER, DATE),
        impl(nullMissingHandling(DateTimeFunction::exprDayOfMonth), INTEGER, DATETIME),
        impl(nullMissingHandling(DateTimeFunction::exprDayOfMonth), INTEGER, TIMESTAMP),
        impl(nullMissingHandling(DateTimeFunction::exprDayOfMonth), INTEGER, STRING)
    );
  }

  /**
   * DAYNAME(STRING/DATE/DATETIME/TIMESTAMP).
   * return the name of the weekday for date, including Monday, Tuesday, Wednesday,
   * Thursday, Friday, Saturday and Sunday.
   */
  private FunctionResolver dayName() {
    return define(BuiltinFunctionName.DAYNAME.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprDayName), STRING, DATE),
        impl(nullMissingHandling(DateTimeFunction::exprDayName), STRING, DATETIME),
        impl(nullMissingHandling(DateTimeFunction::exprDayName), STRING, TIMESTAMP),
        impl(nullMissingHandling(DateTimeFunction::exprDayName), STRING, STRING)
    );
  }

  /**
   * DAYOFMONTH(STRING/DATE/DATETIME/TIMESTAMP). return the day of the month (1-31).
   */
  private FunctionResolver dayOfMonth() {
    return define(BuiltinFunctionName.DAYOFMONTH.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprDayOfMonth), INTEGER, DATE),
        impl(nullMissingHandling(DateTimeFunction::exprDayOfMonth), INTEGER, DATETIME),
        impl(nullMissingHandling(DateTimeFunction::exprDayOfMonth), INTEGER, TIMESTAMP),
        impl(nullMissingHandling(DateTimeFunction::exprDayOfMonth), INTEGER, STRING)
    );
  }

  /**
   * DAYOFWEEK(STRING/DATE/DATETIME/TIMESTAMP).
   * return the weekday index for date (1 = Sunday, 2 = Monday, …, 7 = Saturday).
   */
  private FunctionResolver dayOfWeek() {
    return define(BuiltinFunctionName.DAYOFWEEK.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprDayOfWeek), INTEGER, DATE),
        impl(nullMissingHandling(DateTimeFunction::exprDayOfWeek), INTEGER, DATETIME),
        impl(nullMissingHandling(DateTimeFunction::exprDayOfWeek), INTEGER, TIMESTAMP),
        impl(nullMissingHandling(DateTimeFunction::exprDayOfWeek), INTEGER, STRING)
    );
  }

  /**
   * DAYOFYEAR(STRING/DATE/DATETIME/TIMESTAMP).
   * return the day of the year for date (1-366).
   */
  private FunctionResolver dayOfYear() {
    return define(BuiltinFunctionName.DAYOFYEAR.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprDayOfYear), INTEGER, DATE),
        impl(nullMissingHandling(DateTimeFunction::exprDayOfYear), INTEGER, DATETIME),
        impl(nullMissingHandling(DateTimeFunction::exprDayOfYear), INTEGER, TIMESTAMP),
        impl(nullMissingHandling(DateTimeFunction::exprDayOfYear), INTEGER, STRING)
    );
  }

  /**
   * FROM_DAYS(LONG). return the date value given the day number N.
   */
  private FunctionResolver from_days() {
    return define(BuiltinFunctionName.FROM_DAYS.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprFromDays), DATE, LONG));
  }

  /**
   * HOUR(STRING/TIME/DATETIME/TIMESTAMP). return the hour value for time.
   */
  private FunctionResolver hour() {
    return define(BuiltinFunctionName.HOUR.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprHour), INTEGER, STRING),
        impl(nullMissingHandling(DateTimeFunction::exprHour), INTEGER, TIME),
        impl(nullMissingHandling(DateTimeFunction::exprHour), INTEGER, DATETIME),
        impl(nullMissingHandling(DateTimeFunction::exprHour), INTEGER, TIMESTAMP)
    );
  }

  /**
   * MICROSECOND(STRING/TIME/DATETIME/TIMESTAMP). return the microsecond value for time.
   */
  private FunctionResolver microsecond() {
    return define(BuiltinFunctionName.MICROSECOND.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprMicrosecond), INTEGER, STRING),
        impl(nullMissingHandling(DateTimeFunction::exprMicrosecond), INTEGER, TIME),
        impl(nullMissingHandling(DateTimeFunction::exprMicrosecond), INTEGER, DATETIME),
        impl(nullMissingHandling(DateTimeFunction::exprMicrosecond), INTEGER, TIMESTAMP)
    );
  }

  /**
   * MINUTE(STRING/TIME/DATETIME/TIMESTAMP). return the minute value for time.
   */
  private FunctionResolver minute() {
    return define(BuiltinFunctionName.MINUTE.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprMinute), INTEGER, STRING),
        impl(nullMissingHandling(DateTimeFunction::exprMinute), INTEGER, TIME),
        impl(nullMissingHandling(DateTimeFunction::exprMinute), INTEGER, DATETIME),
        impl(nullMissingHandling(DateTimeFunction::exprMinute), INTEGER, TIMESTAMP)
    );
  }

  /**
   * MONTH(STRING/DATE/DATETIME/TIMESTAMP). return the month for date (1-12).
   */
  private FunctionResolver month() {
    return define(BuiltinFunctionName.MONTH.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprMonth), INTEGER, DATE),
        impl(nullMissingHandling(DateTimeFunction::exprMonth), INTEGER, DATETIME),
        impl(nullMissingHandling(DateTimeFunction::exprMonth), INTEGER, TIMESTAMP),
        impl(nullMissingHandling(DateTimeFunction::exprMonth), INTEGER, STRING)
    );
  }

  /**
   * MONTHNAME(STRING/DATE/DATETIME/TIMESTAMP). return the full name of the month for date.
   */
  private FunctionResolver monthName() {
    return define(BuiltinFunctionName.MONTHNAME.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprMonthName), STRING, DATE),
        impl(nullMissingHandling(DateTimeFunction::exprMonthName), STRING, DATETIME),
        impl(nullMissingHandling(DateTimeFunction::exprMonthName), STRING, TIMESTAMP),
        impl(nullMissingHandling(DateTimeFunction::exprMonthName), STRING, STRING)
    );
  }

  /**
   * QUARTER(STRING/DATE/DATETIME/TIMESTAMP). return the month for date (1-4).
   */
  private FunctionResolver quarter() {
    return define(BuiltinFunctionName.QUARTER.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprQuarter), INTEGER, DATE),
        impl(nullMissingHandling(DateTimeFunction::exprQuarter), INTEGER, DATETIME),
        impl(nullMissingHandling(DateTimeFunction::exprQuarter), INTEGER, TIMESTAMP),
        impl(nullMissingHandling(DateTimeFunction::exprQuarter), INTEGER, STRING)
    );
  }

  /**
   * SECOND(STRING/TIME/DATETIME/TIMESTAMP). return the second value for time.
   */
  private FunctionResolver second() {
    return define(BuiltinFunctionName.SECOND.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprSecond), INTEGER, STRING),
        impl(nullMissingHandling(DateTimeFunction::exprSecond), INTEGER, TIME),
        impl(nullMissingHandling(DateTimeFunction::exprSecond), INTEGER, DATETIME),
        impl(nullMissingHandling(DateTimeFunction::exprSecond), INTEGER, TIMESTAMP)
    );
  }

  private FunctionResolver subdate() {
    return sub_date(BuiltinFunctionName.SUBDATE.getName());
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
   * TIME_TO_SEC(STRING/TIME/DATETIME/TIMESTAMP). return the time argument, converted to seconds.
   */
  private FunctionResolver time_to_sec() {
    return define(BuiltinFunctionName.TIME_TO_SEC.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprTimeToSec), LONG, STRING),
        impl(nullMissingHandling(DateTimeFunction::exprTimeToSec), LONG, TIME),
        impl(nullMissingHandling(DateTimeFunction::exprTimeToSec), LONG, TIMESTAMP),
        impl(nullMissingHandling(DateTimeFunction::exprTimeToSec), LONG, DATETIME)
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
   * TO_DAYS(STRING/DATE/DATETIME/TIMESTAMP). return the day number of the given date.
   */
  private FunctionResolver to_days() {
    return define(BuiltinFunctionName.TO_DAYS.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprToDays), LONG, STRING),
        impl(nullMissingHandling(DateTimeFunction::exprToDays), LONG, TIMESTAMP),
        impl(nullMissingHandling(DateTimeFunction::exprToDays), LONG, DATE),
        impl(nullMissingHandling(DateTimeFunction::exprToDays), LONG, DATETIME));
  }

  /**
   * WEEK(DATE[,mode]). return the week number for date.
   */
  private FunctionResolver week() {
    return define(BuiltinFunctionName.WEEK.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprWeekWithoutMode), INTEGER, DATE),
        impl(nullMissingHandling(DateTimeFunction::exprWeekWithoutMode), INTEGER, DATETIME),
        impl(nullMissingHandling(DateTimeFunction::exprWeekWithoutMode), INTEGER, TIMESTAMP),
        impl(nullMissingHandling(DateTimeFunction::exprWeekWithoutMode), INTEGER, STRING),
        impl(nullMissingHandling(DateTimeFunction::exprWeek), INTEGER, DATE, INTEGER),
        impl(nullMissingHandling(DateTimeFunction::exprWeek), INTEGER, DATETIME, INTEGER),
        impl(nullMissingHandling(DateTimeFunction::exprWeek), INTEGER, TIMESTAMP, INTEGER),
        impl(nullMissingHandling(DateTimeFunction::exprWeek), INTEGER, STRING, INTEGER)
    );
  }

  /**
   * YEAR(STRING/DATE/DATETIME/TIMESTAMP). return the year for date (1000-9999).
   */
  private FunctionResolver year() {
    return define(BuiltinFunctionName.YEAR.getName(),
        impl(nullMissingHandling(DateTimeFunction::exprYear), INTEGER, DATE),
        impl(nullMissingHandling(DateTimeFunction::exprYear), INTEGER, DATETIME),
        impl(nullMissingHandling(DateTimeFunction::exprYear), INTEGER, TIMESTAMP),
        impl(nullMissingHandling(DateTimeFunction::exprYear), INTEGER, STRING)
    );
  }

  /**
   * ADDDATE function implementation for ExprValue.
   *
   * @param date ExprValue of String/Date/Datetime/Timestamp type.
   * @param expr ExprValue of Interval type, the temporal amount to add.
   * @return Datetime resulted from expr added to date.
   */
  private ExprValue exprAddDateInterval(ExprValue date, ExprValue expr) {
    ExprValue exprValue = new ExprDatetimeValue(date.datetimeValue().plus(expr.intervalValue()));
    return (exprValue.timeValue().toSecondOfDay() == 0 ? new ExprDateValue(exprValue.dateValue())
        : exprValue);
  }

  /**
   * ADDDATE function implementation for ExprValue.
   *
   * @param date ExprValue of String/Date/Datetime/Timestamp type.
   * @param days ExprValue of Long type, representing the number of days to add.
   * @return Date/Datetime resulted from days added to date.
   */
  private ExprValue exprAddDateDays(ExprValue date, ExprValue days) {
    ExprValue exprValue = new ExprDatetimeValue(date.datetimeValue().plusDays(days.longValue()));
    return (exprValue.timeValue().toSecondOfDay() == 0 ? new ExprDateValue(exprValue.dateValue())
        : exprValue);
  }

  /**
   * Date implementation for ExprValue.
   *
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
   *
   * @param date ExprValue of Date/String type.
   * @return ExprValue.
   */
  private ExprValue exprDayName(ExprValue date) {
    return new ExprStringValue(
        date.dateValue().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.getDefault()));
  }

  /**
   * Day of Month implementation for ExprValue.
   *
   * @param date ExprValue of Date/String type.
   * @return ExprValue.
   */
  private ExprValue exprDayOfMonth(ExprValue date) {
    return new ExprIntegerValue(date.dateValue().getDayOfMonth());
  }

  /**
   * Day of Week implementation for ExprValue.
   *
   * @param date ExprValue of Date/String type.
   * @return ExprValue.
   */
  private ExprValue exprDayOfWeek(ExprValue date) {
    return new ExprIntegerValue((date.dateValue().getDayOfWeek().getValue() % 7) + 1);
  }

  /**
   * Day of Year implementation for ExprValue.
   *
   * @param date ExprValue of Date/String type.
   * @return ExprValue.
   */
  private ExprValue exprDayOfYear(ExprValue date) {
    return new ExprIntegerValue(date.dateValue().getDayOfYear());
  }

  /**
   * From_days implementation for ExprValue.
   *
   * @param exprValue Day number N.
   * @return ExprValue.
   */
  private ExprValue exprFromDays(ExprValue exprValue) {
    return new ExprDateValue(LocalDate.ofEpochDay(exprValue.longValue() - DAYS_0000_TO_1970));
  }

  /**
   * Hour implementation for ExprValue.
   *
   * @param time ExprValue of Time/String type.
   * @return ExprValue.
   */
  private ExprValue exprHour(ExprValue time) {
    return new ExprIntegerValue(time.timeValue().getHour());
  }

  /**
   * Microsecond implementation for ExprValue.
   *
   * @param time ExprValue of Time/String type.
   * @return ExprValue.
   */
  private ExprValue exprMicrosecond(ExprValue time) {
    return new ExprIntegerValue(
        TimeUnit.MICROSECONDS.convert(time.timeValue().getNano(), TimeUnit.NANOSECONDS));
  }

  /**
   * Minute implementation for ExprValue.
   *
   * @param time ExprValue of Time/String type.
   * @return ExprValue.
   */
  private ExprValue exprMinute(ExprValue time) {
    return new ExprIntegerValue(time.timeValue().getMinute());
  }

  /**
   * Month for date implementation for ExprValue.
   *
   * @param date ExprValue of Date/String type.
   * @return ExprValue.
   */
  private ExprValue exprMonth(ExprValue date) {
    return new ExprIntegerValue(date.dateValue().getMonthValue());
  }

  /**
   * Name of the Month implementation for ExprValue.
   *
   * @param date ExprValue of Date/String type.
   * @return ExprValue.
   */
  private ExprValue exprMonthName(ExprValue date) {
    return new ExprStringValue(
        date.dateValue().getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault()));
  }

  /**
   * Quarter for date implementation for ExprValue.
   *
   * @param date ExprValue of Date/String type.
   * @return ExprValue.
   */
  private ExprValue exprQuarter(ExprValue date) {
    int month = date.dateValue().getMonthValue();
    return new ExprIntegerValue((month / 3) + ((month % 3) == 0 ? 0 : 1));
  }

  /**
   * Second implementation for ExprValue.
   *
   * @param time ExprValue of Time/String type.
   * @return ExprValue.
   */
  private ExprValue exprSecond(ExprValue time) {
    return new ExprIntegerValue(time.timeValue().getSecond());
  }

  /**
   * SUBDATE function implementation for ExprValue.
   *
   * @param date ExprValue of String/Date/Datetime/Timestamp type.
   * @param days ExprValue of Long type, representing the number of days to subtract.
   * @return Date/Datetime resulted from days subtracted to date.
   */
  private ExprValue exprSubDateDays(ExprValue date, ExprValue days) {
    ExprValue exprValue = new ExprDatetimeValue(date.datetimeValue().minusDays(days.longValue()));
    return (exprValue.timeValue().toSecondOfDay() == 0 ? new ExprDateValue(exprValue.dateValue())
        : exprValue);
  }

  /**
   * SUBDATE function implementation for ExprValue.
   *
   * @param date ExprValue of String/Date/Datetime/Timestamp type.
   * @param expr ExprValue of Interval type, the temporal amount to subtract.
   * @return Datetime resulted from expr subtracted to date.
   */
  private ExprValue exprSubDateInterval(ExprValue date, ExprValue expr) {
    ExprValue exprValue = new ExprDatetimeValue(date.datetimeValue().minus(expr.intervalValue()));
    return (exprValue.timeValue().toSecondOfDay() == 0 ? new ExprDateValue(exprValue.dateValue())
        : exprValue);
  }

  /**
   * Time implementation for ExprValue.
   *
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
   *
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
   *
   * @param time ExprValue of Time/String type.
   * @return ExprValue.
   */
  private ExprValue exprTimeToSec(ExprValue time) {
    return new ExprLongValue(time.timeValue().toSecondOfDay());
  }

  /**
   * To_days implementation for ExprValue.
   *
   * @param date ExprValue of Date/String type.
   * @return ExprValue.
   */
  private ExprValue exprToDays(ExprValue date) {
    return new ExprLongValue(date.dateValue().toEpochDay() + DAYS_0000_TO_1970);
  }

  /**
   * Week for date implementation for ExprValue.
   * @param date ExprValue of Date/Datetime/Timestamp/String type.
   * @param mode ExprValue of Integer type.
   */
  private ExprValue exprWeek(ExprValue date, ExprValue mode) {
    return new ExprIntegerValue(
        CalendarLookup.getWeekNumber(mode.integerValue(), date.dateValue()));
  }

  /**
   * Week for date implementation for ExprValue.
   * When mode is not specified default value mode 0 is used for default_week_format.
   * @param date ExprValue of Date/Datetime/Timestamp/String type.
   * @return ExprValue.
   */
  private ExprValue exprWeekWithoutMode(ExprValue date) {
    return exprWeek(date, new ExprIntegerValue(0));
  }

  /**
   * Year for date implementation for ExprValue.
   *
   * @param date ExprValue of Date/String type.
   * @return ExprValue.
   */
  private ExprValue exprYear(ExprValue date) {
    return new ExprIntegerValue(date.dateValue().getYear());
  }

}
