package com.amazon.opendistroforelasticsearch.sql.executor.format;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class DateFieldFormatterTest
{

  @Test
  public void testApplyJDBCDateFormat_kibana_sample_data_ecommerce_order_date()
  {
    String columnName = "order_date";
    DateFormat dateFormat = DateFormat.DATE_OPTIONAL_TIME;
    String originalDateValue = "2020-02-24T09:28:48+00:00";
    String expectedDateValue = "2020-02-24 01:28:48.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_kibana_sample_data_flights_timestamp()
  {
    String columnName = "timestamp";
    DateFormat dateFormat = DateFormat.DATE_OPTIONAL_TIME;
    String originalDateValue = "2020-02-03T00:00:00";
    String expectedDateValue = "2020-02-03 00:00:00.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_kibana_sample_data_logs_utc_date()
  {
    String columnName = "utc_date";
    DateFormat dateFormat = DateFormat.DATE_OPTIONAL_TIME;
    String originalDateValue = "2020-02-02T00:39:02.912Z";
    String expectedDateValue = "2020-02-02 00:39:02.912";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_epochMillis()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.EPOCH_MILLIS;
    String originalDateValue = "727430805000";
    String expectedDateValue = "1993-01-19 00:06:45.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_epochSecond()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.EPOCH_SECOND;
    String originalDateValue = "727430805";
    String expectedDateValue = "1993-01-19 00:06:45.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_dateOptionalTime_date()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.DATE_OPTIONAL_TIME;
    String originalDateValue = "1993-01-19";
    String expectedDateValue = "1993-01-19 00:00:00.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_dateOptionalTime_dateAndTime()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.DATE_OPTIONAL_TIME;
    String originalDateValue = "1993-01-19T00:06:45.123-0800";
    String expectedDateValue = "1993-01-19 00:06:45.123";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_basicDate()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.BASIC_DATE;
    String originalDateValue = "19930119";
    String expectedDateValue = "1993-01-19 00:00:00.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_basicDateTime()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.BASIC_DATE_TIME;
    String originalDateValue = "19930119T120645.123-0800";
    String expectedDateValue = "1993-01-19 12:06:45.123";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_basicDateTimeNoMillis()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.BASIC_DATE_TIME_NO_MILLIS;
    String originalDateValue = "19930119T120645-0800";
    String expectedDateValue = "1993-01-19 12:06:45.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_basicOrdinalDate()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.BASIC_ORDINAL_DATE;
    String originalDateValue = "1993019";
    String expectedDateValue = "1993-01-19 00:00:00.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_basicOrdinalDateTime()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.BASIC_ORDINAL_DATE_TIME;
    String originalDateValue = "1993019T120645.123-0800";
    String expectedDateValue = "1993-01-19 12:06:45.123";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_basicOrdinalDateTimeNoMillis()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.BASIC_ORDINAL_DATE_TIME_NO_MILLIS;
    String originalDateValue = "1993019T120645-0800";
    String expectedDateValue = "1993-01-19 12:06:45.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_basicTime()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.BASIC_TIME;
    String originalDateValue = "120645.123-0800";
    String expectedDateValue = "1970-01-01 12:06:45.123";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_basicTimeNoMillis()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.BASIC_TIME_NO_MILLIS;
    String originalDateValue = "120645-0800";
    String expectedDateValue = "1970-01-01 12:06:45.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_basicTTime()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.BASIC_T_TIME;
    String originalDateValue = "T120645.123-0800";
    String expectedDateValue = "1970-01-01 12:06:45.123";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_basicTTimeNoMillis()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.BASIC_T_TIME_NO_MILLIS;
    String originalDateValue = "T120645-0800";
    String expectedDateValue = "1970-01-01 12:06:45.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_basicWeekDate()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.BASIC_WEEK_DATE;
    String originalDateValue = "1993W042";
    String expectedDateValue = "1993-01-19 00:00:00.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_basicWeekDateTime()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.BASIC_WEEK_DATE_TIME;
    String originalDateValue = "1993W042T120645.123-0800";
    String expectedDateValue = "1993-01-19 12:06:45.123";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_basicWeekDateTimeNoMillis()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.BASIC_WEEK_DATE_TIME_NO_MILLIS;
    String originalDateValue = "1993W042T120645-0800";
    String expectedDateValue = "1993-01-19 12:06:45.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_date()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.DATE;
    String originalDateValue = "1993-01-19";
    String expectedDateValue = "1993-01-19 00:00:00.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_dateHour()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.DATE_HOUR;
    String originalDateValue = "1993-01-19T12";
    String expectedDateValue = "1993-01-19 12:00:00.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_dateHourMinute()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.DATE_HOUR_MINUTE;
    String originalDateValue = "1993-01-19T12:06";
    String expectedDateValue = "1993-01-19 12:06:00.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_dateHourMinuteSecond()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.DATE_HOUR_MINUTE_SECOND;
    String originalDateValue = "1993-01-19T12:06:45";
    String expectedDateValue = "1993-01-19 12:06:45.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_dateHourMinuteSecondFraction()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.DATE_HOUR_MINUTE_SECOND_FRACTION;
    String originalDateValue = "1993-01-19T12:06:45.123";
    String expectedDateValue = "1993-01-19 12:06:45.123";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_dateHourMinuteSecondMillis()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.DATE_HOUR_MINUTE_SECOND_MILLIS;
    String originalDateValue = "1993-01-19T12:06:45.123";
    String expectedDateValue = "1993-01-19 12:06:45.123";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_dateTime()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.DATE_TIME;
    String originalDateValue = "1993-01-19T12:06:45.123-0800";
    String expectedDateValue = "1993-01-19 12:06:45.123";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_dateTimeNoMillis()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.DATE_TIME_NO_MILLIS;
    String originalDateValue = "1993-01-19T12:06:45-0800";
    String expectedDateValue = "1993-01-19 12:06:45.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_hour()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.HOUR;
    String originalDateValue = "12";
    String expectedDateValue = "1970-01-01 12:00:00.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_hourMinute()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.HOUR_MINUTE;
    String originalDateValue = "12:06";
    String expectedDateValue = "1970-01-01 12:06:00.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_hourMinuteSecond()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.HOUR_MINUTE_SECOND;
    String originalDateValue = "12:06:45";
    String expectedDateValue = "1970-01-01 12:06:45.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_hourMinuteSecondFraction()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.HOUR_MINUTE_SECOND_FRACTION;
    String originalDateValue = "12:06:45.123";
    String expectedDateValue = "1970-01-01 12:06:45.123";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_hourMinuteSecondMillis()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.HOUR_MINUTE_SECOND_MILLIS;
    String originalDateValue = "12:06:45.123";
    String expectedDateValue = "1970-01-01 12:06:45.123";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_ordinalDate()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.ORDINAL_DATE;
    String originalDateValue = "1993-019";
    String expectedDateValue = "1993-01-19 00:00:00.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_ordinalDateTime()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.ORDINAL_DATE_TIME;
    String originalDateValue = "1993-019T12:06:45.123-0800";
    String expectedDateValue = "1993-01-19 12:06:45.123";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_ordinalDateTimeNoMillis()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.ORDINAL_DATE_TIME_NO_MILLIS;
    String originalDateValue = "1993-019T12:06:45-0800";
    String expectedDateValue = "1993-01-19 12:06:45.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_time()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.TIME;
    String originalDateValue = "12:06:45.123-0800";
    String expectedDateValue = "1970-01-01 12:06:45.123";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_timeNoMillis()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.TIME_NO_MILLIS;
    String originalDateValue = "12:06:45-0800";
    String expectedDateValue = "1970-01-01 12:06:45.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_tTime()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.T_TIME;
    String originalDateValue = "T12:06:45.123-0800";
    String expectedDateValue = "1970-01-01 12:06:45.123";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_tTimeNoMillis()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.T_TIME_NO_MILLIS;
    String originalDateValue = "T12:06:45-0800";
    String expectedDateValue = "1970-01-01 12:06:45.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_weekDate()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.WEEK_DATE;
    String originalDateValue = "1993-W04-2";
    String expectedDateValue = "1993-01-19 00:00:00.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_weekDateTime()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.WEEK_DATE_TIME;
    String originalDateValue = "1993-W04-2T12:06:45.123-0800";
    String expectedDateValue = "1993-01-19 12:06:45.123";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_weekDateTimeNoMillis()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.WEEK_DATE_TIME_NO_MILLIS;
    String originalDateValue = "1993-W04-2T12:06:45-0800";
    String expectedDateValue = "1993-01-19 12:06:45.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_weekyear()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.WEEK_YEAR;
    String originalDateValue = "1993";
    String expectedDateValue = "1993-01-01 00:00:00.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_weekyearWeek()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.WEEKYEAR_WEEK;
    String originalDateValue = "1993-W04";
    String expectedDateValue = "1993-01-17 00:00:00.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_weekyearWeekDay()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.WEEKYEAR_WEEK_DAY;
    String originalDateValue = "1993-W04-2";
    String expectedDateValue = "1993-01-19 00:00:00.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_year()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.YEAR;
    String originalDateValue = "1993";
    String expectedDateValue = "1993-01-01 00:00:00.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_yearMonth()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.YEAR_MONTH;
    String originalDateValue = "1993-01";
    String expectedDateValue = "1993-01-01 00:00:00.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_yearMonthDay()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.YEAR_MONTH_DAY;
    String originalDateValue = "1993-01-19";
    String expectedDateValue = "1993-01-19 00:00:00.000";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_incorrectFormat()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.DATE_OPTIONAL_TIME;
    String originalDateValue = "1581724085";
    // Invalid format for date value; should return original value
    String expectedDateValue = "1581724085";

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  @Test
  public void testApplyJDBCDateFormat_nullDateData()
  {
    String columnName = "date_field";
    DateFormat dateFormat = DateFormat.DATE_OPTIONAL_TIME;
    String originalDateValue = null;
    // Nulls should be preserved
    String expectedDateValue = null;

    verifyFormatting(columnName, dateFormat, originalDateValue, expectedDateValue);
  }

  private void verifyFormatting(String columnName, DateFormat dateFormat, String originalDateValue, String expectedDateValue)
  {
    List<Schema.Column> columns = buildColumnList(columnName);
    Map<String, String> dateFieldFormatMap = buildDateFieldFormatMap(columnName, dateFormat);

    Map<String, Object> rowSource = new HashMap<>();
    rowSource.put(columnName, originalDateValue);

    DateFieldFormatter dateFieldFormatter = new DateFieldFormatter(dateFieldFormatMap, columns);
    executeFormattingAndCompare(dateFieldFormatter, rowSource, columnName, expectedDateValue);
  }

  private void executeFormattingAndCompare(
      DateFieldFormatter formatter,
      Map<String, Object> rowSource,
      String columnToCheck,
      String expectedDateValue) {
    formatter.applyJDBCDateFormat(rowSource);
    assertEquals(expectedDateValue, rowSource.get(columnToCheck));
  }

  private List<Schema.Column> buildColumnList(String columnName) {
    return ImmutableList.<Schema.Column>builder()
        .add(new Schema.Column(columnName, null, Schema.Type.DATE))
        .build();
  }

  private Map<String, String> buildDateFieldFormatMap(String columnName, DateFormat dateFormat) {
    return ImmutableMap.<String, String>builder()
        .put(columnName, dateFormat.nameLowerCase())
        .build();
  }

}