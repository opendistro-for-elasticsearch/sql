/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.opendistroforelasticsearch.sql.executor.format;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class DateFormat {

    private static Map<String, String> formatMap = new HashMap<>();

    static {
        // Special cases that are parsed separately
        formatMap.put("date_optional_time", "");
        formatMap.put("strict_date_optional_time", "");
        formatMap.put("epoch_millis", "");
        formatMap.put("epoch_second", "");

        formatMap.put("basic_date", Date.BASIC_DATE);
        formatMap.put("basic_date_time", Date.BASIC_DATE + Time.T + Time.BASIC_TIME + Time.MILLIS + Time.TZ);
        formatMap.put("basic_date_time_no_millis", Date.BASIC_DATE + Time.T + Time.BASIC_TIME + Time.TZ);

        formatMap.put("basic_ordinal_date", Date.BASIC_ORDINAL_DATE);
        formatMap.put("basic_ordinal_date_time",
                      Date.BASIC_ORDINAL_DATE + Time.T + Time.BASIC_TIME + Time.MILLIS + Time.TZ);
        formatMap.put("basic_ordinal_date_time_no_millis", Date.BASIC_ORDINAL_DATE+ Time.T + Time.BASIC_TIME + Time.TZ);

        formatMap.put("basic_time", Time.BASIC_TIME + Time.MILLIS + Time.TZ);
        formatMap.put("basic_time_no_millis", Time.BASIC_TIME + Time.TZ);

        formatMap.put("basic_t_time", Time.T + Time.BASIC_TIME + Time.MILLIS + Time.TZ);
        formatMap.put("basic_t_time_no_millis", Time.T + Time.BASIC_TIME + Time.TZ);

        formatMap.put("basic_week_date", Date.BASIC_WEEK_DATE);
        formatMap.put("basic_week_date_time", Date.BASIC_WEEK_DATE + Time.T + Time.BASIC_TIME + Time.MILLIS + Time.TZ);
        formatMap.put("basic_week_date_time_no_millis", Date.BASIC_WEEK_DATE + Time.T + Time.BASIC_TIME + Time.TZ);

        formatMap.put("date", Date.DATE);
        formatMap.put("date_hour", Date.DATE + Time.T + Time.HOUR);
        formatMap.put("date_hour_minute", Date.DATE + Time.T + Time.HOUR_MINUTE);
        formatMap.put("date_hour_minute_second", Date.DATE + Time.T + Time.TIME);
        formatMap.put("date_hour_minute_second_fraction", Date.DATE + Time.T + Time.TIME + Time.MILLIS);
        formatMap.put("date_hour_minute_second_millis", Date.DATE + Time.T + Time.TIME + Time.MILLIS);
        formatMap.put("date_time", Date.DATE + Time.T + Time.TIME + Time.MILLIS + Time.TZZ);
        formatMap.put("date_time_no_millis", Date.DATE + Time.T + Time.TIME + Time.TZZ);

        formatMap.put("hour", Time.HOUR);
        formatMap.put("hour_minute", Time.HOUR_MINUTE);
        formatMap.put("hour_minute_second", Time.TIME);
        formatMap.put("hour_minute_second_fraction", Time.TIME + Time.MILLIS);
        formatMap.put("hour_minute_second_millis", Time.TIME + Time.MILLIS);

        formatMap.put("ordinal_date", Date.ORDINAL_DATE);
        formatMap.put("ordinal_date_time", Date.ORDINAL_DATE + Time.T + Time.TIME + Time.MILLIS + Time.TZZ);
        formatMap.put("ordinal_date_time_no_millis", Date.ORDINAL_DATE + Time.T + Time.TIME + Time.TZZ);

        formatMap.put("time", Time.TIME + Time.MILLIS + Time.TZZ);
        formatMap.put("time_no_millis", Time.TIME + Time.TZZ);

        formatMap.put("t_time", Time.T + Time.TIME + Time.MILLIS + Time.TZZ);
        formatMap.put("t_time_no_millis", Time.T + Time.TIME + Time.TZZ);

        formatMap.put("week_date", Date.WEEK_DATE);
        formatMap.put("week_date_time", Date.WEEK_DATE + Time.T + Time.TIME + Time.MILLIS + Time.TZZ);
        formatMap.put("week_date_time_no_millis", Date.WEEK_DATE + Time.T + Time.TIME + Time.TZZ);

        // Note: input mapping is "weekyear", but output value is "week_year"
        formatMap.put("week_year", Date.WEEKYEAR);
        formatMap.put("weekyear_week", Date.WEEKYEAR_WEEK);
        formatMap.put("weekyear_week_day", Date.WEEK_DATE);

        formatMap.put("year", Date.YEAR);
        formatMap.put("year_month", Date.YEAR_MONTH);
        formatMap.put("year_month_day", Date.DATE);
    }

    private DateFormat() {
    }

    public static String getFormatString(String formatName) {
        return formatMap.get(formatName);
    }

    public static String getFormattedDate(java.util.Date date, String dateFormat) {
        Instant instant = date.toInstant();
        ZonedDateTime zdt = ZonedDateTime.ofInstant(instant, ZoneId.of("Etc/UTC"));
        return zdt.format(DateTimeFormatter.ofPattern(dateFormat));
    }

    private static class Date {
        static String BASIC_DATE = "yyyyMMdd";
        static String BASIC_ORDINAL_DATE = "yyyyDDD";
        static String BASIC_WEEK_DATE = "YYYY'W'wwu";

        static String DATE = "yyyy-MM-dd";
        static String ORDINAL_DATE = "yyyy-DDD";

        static String YEAR = "yyyy";
        static String YEAR_MONTH = "yyyy-MM";

        static String WEEK_DATE = "YYYY-'W'ww-u";
        static String WEEKYEAR = "YYYY";
        static String WEEKYEAR_WEEK = "YYYY-'W'ww";
    }

    private static class Time {
        static String T = "'T'";
        static String BASIC_TIME = "HHmmss";
        static String TIME = "HH:mm:ss";

        static String HOUR = "HH";
        static String HOUR_MINUTE = "HH:mm";

        static String MILLIS = ".SSS";
        static String TZ = "Z";
        static String TZZ = "XX";
    }
}
