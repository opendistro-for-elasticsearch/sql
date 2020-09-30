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

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.exception.SemanticCheckException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

class CalendarLookup {

  private Map<Integer, Calendar> map = new HashMap<>();

  /**
   * Set Calendar in map for all modes.
   * @param date ExprValue of Date/Datetime/Timestamp/String type.
   */
  CalendarLookup(ExprValue date) {
    map.put(0, getCalendar(Calendar.SUNDAY, 7, date));
    map.put(1, getCalendar(Calendar.MONDAY, 5, date));
    map.put(2, getCalendar(Calendar.SUNDAY, 7, date));
    map.put(3, getCalendar(Calendar.MONDAY, 5, date));
    map.put(4, getCalendar(Calendar.SUNDAY, 4, date));
    map.put(5, getCalendar(Calendar.MONDAY, 7, date));
    map.put(6, getCalendar(Calendar.SUNDAY, 4, date));
    map.put(7, getCalendar(Calendar.MONDAY, 7, date));
  }

  /**
   * Set first day of week, minimal days in first week and date in calendar.
   * @param firstDayOfWeek the given first day of the week.
   * @param minimalDaysInWeek the given minimal days required in the first week of the year.
   * @param date the ExprValue of Date/Datetime/Timestamp/String type.
   */
  private Calendar getCalendar(int firstDayOfWeek, int minimalDaysInWeek, ExprValue date) {
    Calendar calendar = Calendar.getInstance();
    calendar.setFirstDayOfWeek(firstDayOfWeek);
    calendar.setMinimalDaysInFirstWeek(minimalDaysInWeek);
    calendar.set(date.dateValue().getYear(), date.dateValue().getMonthValue() - 1,
        date.dateValue().getDayOfMonth());
    return calendar;
  }

  /**
   * Returns week number for date according to mode.
   * @param mode Integer for mode. Valid mode values are 0 to 7.
   */
  int getWeekNumber(int mode) {
    if (map.containsKey(mode)) {
      int weekNumber = map.get(mode).get(Calendar.WEEK_OF_YEAR);
      if ((weekNumber > 51)
          && (map.get(mode).get(Calendar.DAY_OF_MONTH) < 7)
          && Arrays.asList(0, 1, 4, 5).contains(mode)) {
        weekNumber = 0;
      }
      return weekNumber;
    }
    throw new SemanticCheckException(
        String.format("mode:%s is invalid, please use mode value between 0-7", mode));
  }

  /**
   * Returns year for date according to mode.
   *
   * @param mode Integer for mode. Valid mode values are 0 to 7.
   */
  int getYearNumber(int mode) {
    int weekNumber = getWeekNumber(mode);
    int yearNumber = map.get(mode).get(Calendar.YEAR);
    if ((weekNumber > 51) && (map.get(mode).get(Calendar.DAY_OF_MONTH) < 7)) {
      yearNumber--;
    }
    return yearNumber;
  }
}
