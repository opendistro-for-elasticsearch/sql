package com.amazon.opendistroforelasticsearch.sql.executor.format;

import java.text.SimpleDateFormat;

public enum DateFormat
{
  // Special cases that are parsed separately
  DATE_OPTIONAL_TIME(""),
  EPOCH_MILLIS(""),
  EPOCH_SECOND(""),

  BASIC_DATE(Date.BASIC_DATE),
  BASIC_DATE_TIME(Date.BASIC_DATE + Time.T + Time.BASIC_TIME + Time.MILLIS + Time.TZ),
  BASIC_DATE_TIME_NO_MILLIS(Date.BASIC_DATE + Time.T + Time.BASIC_TIME + Time.TZ),

  BASIC_ORDINAL_DATE(Date.BASIC_ORDINAL_DATE),
  BASIC_ORDINAL_DATE_TIME(Date.BASIC_ORDINAL_DATE + Time.T + Time.BASIC_TIME + Time.MILLIS + Time.TZ),
  BASIC_ORDINAL_DATE_TIME_NO_MILLIS(Date.BASIC_ORDINAL_DATE+ Time.T + Time.BASIC_TIME + Time.TZ),

  BASIC_TIME(Time.BASIC_TIME + Time.MILLIS + Time.TZ),
  BASIC_TIME_NO_MILLIS(Time.BASIC_TIME + Time.TZ),

  BASIC_T_TIME(Time.T + Time.BASIC_TIME + Time.MILLIS + Time.TZ),
  BASIC_T_TIME_NO_MILLIS(Time.T + Time.BASIC_TIME + Time.TZ),

  BASIC_WEEK_DATE(Date.BASIC_WEEK_DATE),
  BASIC_WEEK_DATE_TIME(Date.BASIC_WEEK_DATE + Time.T + Time.BASIC_TIME + Time.MILLIS + Time.TZ),
  BASIC_WEEK_DATE_TIME_NO_MILLIS(Date.BASIC_WEEK_DATE + Time.T + Time.BASIC_TIME + Time.TZ),

  DATE(Date.DATE),
  DATE_HOUR(Date.DATE + Time.T + Time.HOUR),
  DATE_HOUR_MINUTE(Date.DATE + Time.T + Time.HOUR_MINUTE),
  DATE_HOUR_MINUTE_SECOND(Date.DATE + Time.T + Time.TIME),
  DATE_HOUR_MINUTE_SECOND_FRACTION(Date.DATE + Time.T + Time.TIME + Time.MILLIS),
  DATE_HOUR_MINUTE_SECOND_MILLIS(Date.DATE + Time.T + Time.TIME + Time.MILLIS),
  DATE_TIME(Date.DATE + Time.T + Time.TIME + Time.MILLIS + Time.TZZ),
  DATE_TIME_NO_MILLIS(Date.DATE + Time.T + Time.TIME + Time.TZZ),

  HOUR(Time.HOUR),
  HOUR_MINUTE(Time.HOUR_MINUTE),
  HOUR_MINUTE_SECOND(Time.TIME),
  HOUR_MINUTE_SECOND_FRACTION(Time.TIME + Time.MILLIS),
  HOUR_MINUTE_SECOND_MILLIS(Time.TIME + Time.MILLIS),

  ORDINAL_DATE(Date.ORDINAL_DATE),
  ORDINAL_DATE_TIME(Date.ORDINAL_DATE + Time.T + Time.TIME + Time.MILLIS + Time.TZZ),
  ORDINAL_DATE_TIME_NO_MILLIS(Date.ORDINAL_DATE + Time.T + Time.TIME + Time.TZZ),

  TIME(Time.TIME + Time.MILLIS + Time.TZZ),
  TIME_NO_MILLIS(Time.TIME + Time.TZZ),

  T_TIME(Time.T + Time.TIME + Time.MILLIS + Time.TZZ),
  T_TIME_NO_MILLIS(Time.T + Time.TIME + Time.TZZ),

  WEEK_DATE(Date.WEEK_DATE),
  WEEK_DATE_TIME(Date.WEEK_DATE + Time.T + Time.TIME + Time.MILLIS + Time.TZZ),
  WEEK_DATE_TIME_NO_MILLIS(Date.WEEK_DATE + Time.T + Time.TIME + Time.TZZ),

  // Note: input mapping is "weekyear", but output value is "week_year"
  WEEK_YEAR(Date.WEEKYEAR),
  WEEKYEAR_WEEK(Date.WEEKYEAR_WEEK),
  WEEKYEAR_WEEK_DAY(Date.WEEK_DATE),

  YEAR(Date.YEAR),
  YEAR_MONTH(Date.YEAR_MONTH),
  YEAR_MONTH_DAY(Date.DATE);

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

  private String formatString;

  DateFormat(String formatString)
  {
    this.formatString = formatString;
  }

  public String getFormatString() {
    return formatString;
  }

  public String nameLowerCase()
  {
    return name().toLowerCase();
  }

  public static String getFormattedDate(java.util.Date date, String dateFormat) {
    return new SimpleDateFormat(dateFormat).format(date);
  }
}
