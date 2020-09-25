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

public class CalenderInfo {

  private Map<Integer, Calendar> map = new HashMap<>();

  /**
   * Set Calender in map for all modes.
   * @param date ExprValue of Date/Datetime/Timestamp type.
   */
  public CalenderInfo(ExprValue date) {
    map.put(0, getCalender(Calendar.SUNDAY, 7, date));
    map.put(1, getCalender(Calendar.MONDAY, 5, date));
    map.put(2, getCalender(Calendar.SUNDAY, 7, date));
    map.put(3, getCalender(Calendar.MONDAY, 5, date));
    map.put(4, getCalender(Calendar.SUNDAY, 4, date));
    map.put(5, getCalender(Calendar.MONDAY, 7, date));
    map.put(6, getCalender(Calendar.SUNDAY, 4, date));
    map.put(7, getCalender(Calendar.MONDAY, 7, date));
  }

  /**
   * Set first day of week, minimal days in first week and date in calendar.
   * @param firstDayOfWeek the given first day of the week.
   * @param minimalDaysInWeek the given minimal days required in the first week of the year.
   * @param date the ExprValue of Date/Datetime/Timestamp type.
   */
  private Calendar getCalender(int firstDayOfWeek, int minimalDaysInWeek, ExprValue date) {
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
  public int getWeekNumber(int mode) {
    if (map.containsKey(mode)) {
      int weekNumber = map.get(mode).get(Calendar.WEEK_OF_YEAR);
      if ((weekNumber == 52 || weekNumber == 53)
          && map.get(mode).get(Calendar.DAY_OF_MONTH) < 7
          && Arrays.asList(0, 1, 4, 5).stream().anyMatch(n -> mode == n)) {
        weekNumber = 0;
      }
      return weekNumber;
    }
    throw new SemanticCheckException(
        String.format("mode:%s is invalid, please use mode value between 0-7", mode));
  }
}
