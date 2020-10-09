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

package com.amazon.opendistroforelasticsearch.sql.ppl;

import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_DATE;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.rows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.schema;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifySchema;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifySome;

import com.amazon.opendistroforelasticsearch.sql.common.utils.StringUtils;
import java.io.IOException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class DateTimeFunctionIT extends PPLIntegTestCase {

  @Override
  public void init() throws IOException {
    loadIndex(Index.DATE);
  }

  @Test
  public void testAddDate() throws IOException {
    JSONObject result =
        executeQuery(String.format(
            "source=%s | eval f = adddate(timestamp('2020-09-16 17:30:00'), interval 1 day) | fields f", TEST_INDEX_DATE));
    verifySchema(result,
        schema("f", null, "datetime"));
    verifySome(result.getJSONArray("datarows"), rows("2020-09-17 17:30:00"));

    result = executeQuery(String.format(
        "source=%s | eval f = adddate(date('2020-09-16'), 1) | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "date"));
    verifySome(result.getJSONArray("datarows"), rows("2020-09-17"));

    result = executeQuery(String.format(
        "source=%s | eval f = adddate('2020-09-16', 1) | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "datetime"));
    verifySome(result.getJSONArray("datarows"), rows("2020-09-17"));

    result =
        executeQuery(String.format(
            "source=%s | eval f = adddate('2020-09-16 17:30:00', interval 1 day) | fields f", TEST_INDEX_DATE));
    verifySchema(result,
        schema("f", null, "datetime"));
    verifySome(result.getJSONArray("datarows"), rows("2020-09-17 17:30:00"));
  }

  @Test
  public void testDateAdd() throws IOException {
    JSONObject result =
        executeQuery(String.format(
            "source=%s | eval f = date_add(timestamp('2020-09-16 17:30:00'), interval 1 day) | fields f", TEST_INDEX_DATE));
    verifySchema(result,
        schema("f", null, "datetime"));
    verifySome(result.getJSONArray("datarows"), rows("2020-09-17 17:30:00"));

    result = executeQuery(String.format(
        "source=%s | eval f = date_add(date('2020-09-16'), 1) | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "date"));
    verifySome(result.getJSONArray("datarows"), rows("2020-09-17"));

    result = executeQuery(String.format(
        "source=%s | eval f = date_add('2020-09-16', 1) | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "datetime"));
    verifySome(result.getJSONArray("datarows"), rows("2020-09-17"));

    result =
        executeQuery(String.format(
            "source=%s | eval f = date_add('2020-09-16 17:30:00', interval 1 day) | fields f", TEST_INDEX_DATE));
    verifySchema(result,
        schema("f", null, "datetime"));
    verifySome(result.getJSONArray("datarows"), rows("2020-09-17 17:30:00"));
  }

  @Test
  public void testDateSub() throws IOException {
    JSONObject result =
        executeQuery(String.format(
            "source=%s | eval f =  date_sub(timestamp('2020-09-16 17:30:00'), interval 1 day) | fields f", TEST_INDEX_DATE));
    verifySchema(result,
        schema("f", null, "datetime"));
    verifySome(result.getJSONArray("datarows"), rows("2020-09-15 17:30:00"));

    result = executeQuery(String.format(
            "source=%s | eval f =  date_sub(date('2020-09-16'), 1) | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "date"));
    verifySome(result.getJSONArray("datarows"), rows("2020-09-15"));

    result = executeQuery(String.format(
            "source=%s | eval f =  date_sub('2020-09-16', 1) | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "datetime"));
    verifySome(result.getJSONArray("datarows"), rows("2020-09-15"));

    result =
        executeQuery(String.format(
            "source=%s | eval f =  date_sub('2020-09-16 17:30:00', interval 1 day) | fields f", TEST_INDEX_DATE));
    verifySchema(result,
        schema("f", null, "datetime"));
    verifySome(result.getJSONArray("datarows"), rows("2020-09-15 17:30:00"));
  }

  @Test
  public void testDay() throws IOException {
    JSONObject result = executeQuery(String.format(
            "source=%s | eval f =  day(date('2020-09-16')) | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(16));

    result = executeQuery(String.format(
            "source=%s | eval f =  day('2020-09-16') | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(16));
  }

  @Test
  public void testDayName() throws IOException {
    JSONObject result = executeQuery(String.format(
            "source=%s | eval f =  dayname(date('2020-09-16')) | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "string"));
    verifySome(result.getJSONArray("datarows"), rows("Wednesday"));

    result = executeQuery(String.format(
            "source=%s | eval f =  dayname('2020-09-16') | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "string"));
    verifySome(result.getJSONArray("datarows"), rows("Wednesday"));
  }

  @Test
  public void testDayOfMonth() throws IOException {
    JSONObject result = executeQuery(String.format(
            "source=%s | eval f =  dayofmonth(date('2020-09-16')) | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(16));

    result = executeQuery(String.format(
            "source=%s | eval f =  dayofmonth('2020-09-16') | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(16));
  }

  @Test
  public void testDayOfWeek() throws IOException {
    JSONObject result = executeQuery(String.format(
            "source=%s | eval f =  dayofweek(date('2020-09-16')) | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(4));

    result = executeQuery(String.format(
            "source=%s | eval f =  dayofweek('2020-09-16') | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(4));
  }

  @Test
  public void testDayOfYear() throws IOException {
    JSONObject result = executeQuery(String.format(
            "source=%s | eval f =  dayofyear(date('2020-09-16')) | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(260));

    result = executeQuery(String.format(
            "source=%s | eval f =  dayofyear('2020-09-16') | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(260));
  }

  @Test
  public void testFromDays() throws IOException {
    JSONObject result = executeQuery(String.format(
            "source=%s | eval f =  from_days(738049) | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "date"));
    verifySome(result.getJSONArray("datarows"), rows("2020-09-16"));
  }

  @Test
  public void testHour() throws IOException {
    JSONObject result = executeQuery(String.format(
            "source=%s | eval f =  hour(timestamp('2020-09-16 17:30:00')) | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(17));

    result = executeQuery(String.format(
            "source=%s | eval f =  hour(time('17:30:00')) | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(17));

    result = executeQuery(String.format(
            "source=%s | eval f =  hour('2020-09-16 17:30:00') | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(17));

    result = executeQuery(String.format(
            "source=%s | eval f =  hour('17:30:00') | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(17));
  }

  @Test
  public void testMicrosecond() throws IOException {
    JSONObject result = executeQuery(String.format(
            "source=%s | eval f =  microsecond(timestamp('2020-09-16 17:30:00.123456')) | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(123456));

    result = executeQuery(String.format(
            "source=%s | eval f =  microsecond(time('17:30:00.000010')) | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(10));

    result = executeQuery(String.format(
            "source=%s | eval f =  microsecond('2020-09-16 17:30:00.123456') | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(123456));

    result = executeQuery(String.format(
            "source=%s | eval f =  microsecond('17:30:00.000010') | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(10));
  }

  @Test
  public void testMinute() throws IOException {
    JSONObject result = executeQuery(String.format(
            "source=%s | eval f =  minute(timestamp('2020-09-16 17:30:00')) | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(30));

    result = executeQuery(String.format(
            "source=%s | eval f =  minute(time('17:30:00')) | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(30));

    result = executeQuery(String.format(
            "source=%s | eval f =  minute('2020-09-16 17:30:00') | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(30));

    result = executeQuery(String.format(
            "source=%s | eval f =  minute('17:30:00') | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(30));
  }

  @Test
  public void testMonth() throws IOException {
    JSONObject result = executeQuery(String.format(
            "source=%s | eval f =  month(date('2020-09-16')) | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(9));

    result = executeQuery(String.format(
            "source=%s | eval f =  month('2020-09-16') | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(9));
  }

  @Test
  public void testMonthName() throws IOException {
    JSONObject result = executeQuery(String.format(
            "source=%s | eval f =  monthname(date('2020-09-16')) | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "string"));
    verifySome(result.getJSONArray("datarows"), rows("September"));

    result = executeQuery(String.format(
            "source=%s | eval f =  monthname('2020-09-16') | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "string"));
    verifySome(result.getJSONArray("datarows"), rows("September"));
  }

  @Test
  public void testQuarter() throws IOException {
    JSONObject result = executeQuery(String.format(
            "source=%s | eval f =  quarter(date('2020-09-16')) | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(3));

    result = executeQuery(String.format(
            "source=%s | eval f =  quarter('2020-09-16') | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(3));
  }

  @Test
  public void testSecond() throws IOException {
    JSONObject result = executeQuery(String.format(
            "source=%s | eval f =  second(timestamp('2020-09-16 17:30:00')) | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(0));

    result = executeQuery(String.format(
            "source=%s | eval f =  second(time('17:30:00')) | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(0));

    result = executeQuery(String.format(
            "source=%s | eval f =  second('2020-09-16 17:30:00') | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(0));

    result = executeQuery(String.format(
            "source=%s | eval f =  second('17:30:00') | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(0));
  }

  @Test
  public void testSubDate() throws IOException {
    JSONObject result =
        executeQuery(String.format(
            "source=%s | eval f =  subdate(timestamp('2020-09-16 17:30:00'), interval 1 day) | fields f", TEST_INDEX_DATE));
    verifySchema(result,
        schema("f", null, "datetime"));
    verifySome(result.getJSONArray("datarows"), rows("2020-09-15 17:30:00"));

    result = executeQuery(String.format(
            "source=%s | eval f =  subdate(date('2020-09-16'), 1) | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "date"));
    verifySome(result.getJSONArray("datarows"), rows("2020-09-15"));

    result =
        executeQuery(String.format(
            "source=%s | eval f =  subdate('2020-09-16 17:30:00', interval 1 day) | fields f", TEST_INDEX_DATE));
    verifySchema(result,
        schema("f", null, "datetime"));
    verifySome(result.getJSONArray("datarows"), rows("2020-09-15 17:30:00"));

    result = executeQuery(String.format(
            "source=%s | eval f =  subdate('2020-09-16', 1) | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "datetime"));
    verifySome(result.getJSONArray("datarows"), rows("2020-09-15"));
  }

  @Test
  public void testTimeToSec() throws IOException {
    JSONObject result = executeQuery(String.format(
            "source=%s | eval f =  time_to_sec(time('17:30:00')) | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "long"));
    verifySome(result.getJSONArray("datarows"), rows(63000));

    result = executeQuery(String.format(
            "source=%s | eval f =  time_to_sec('17:30:00') | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "long"));
    verifySome(result.getJSONArray("datarows"), rows(63000));
  }

  @Test
  public void testToDays() throws IOException {
    JSONObject result = executeQuery(String.format(
            "source=%s | eval f =  to_days(date('2020-09-16')) | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "long"));
    verifySome(result.getJSONArray("datarows"), rows(738049));

    result = executeQuery(String.format(
            "source=%s | eval f =  to_days('2020-09-16') | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "long"));
    verifySome(result.getJSONArray("datarows"), rows(738049));
  }

  private void week(String date, int mode, int expectedResult) throws IOException {
    JSONObject result = executeQuery(StringUtils.format(
        "source=%s | eval f = week(date('%s'), %d) | fields f", TEST_INDEX_DATE, date, mode));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(expectedResult));
  }

  @Test
  public void testWeek() throws IOException {
    JSONObject result = executeQuery(String.format(
        "source=%s | eval f = week(date('2008-02-20')) | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(7));

    week("2008-02-20", 0, 7);
    week("2008-02-20", 1, 8);
    week("2008-12-31", 1, 53);
    week("2000-01-01", 0, 0);
    week("2000-01-01", 2, 52);
  }

  @Test
  public void testYear() throws IOException {
    JSONObject result = executeQuery(String.format(
            "source=%s | eval f =  year(date('2020-09-16')) | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(2020));

    result = executeQuery(String.format(
            "source=%s | eval f =  year('2020-09-16') | fields f", TEST_INDEX_DATE));
    verifySchema(result, schema("f", null, "integer"));
    verifySome(result.getJSONArray("datarows"), rows(2020));
  }

  void verifyDateFormat(String date, String type, String format, String formatted) throws IOException {
    JSONObject result = executeQuery(String.format(
            "source=%s | eval f =  date_format(%s('%s'), '%s') | fields f",
            TEST_INDEX_DATE, type, date, format));
    verifySchema(result, schema("f", null, "string"));
    verifySome(result.getJSONArray("datarows"), rows(formatted));

    result = executeQuery(String.format(
            "source=%s | eval f =  date_format('%s', '%s') | fields f",
            TEST_INDEX_DATE, date, format));
    verifySchema(result, schema("f", null, "string"));
    verifySome(result.getJSONArray("datarows"), rows(formatted));
  }

  @Test
  public void testDateFormat() throws IOException {
    String timestamp = "1998-01-31 13:14:15.012345";
    String timestampFormat = "%a %b %c %D %d %e %f %H %h %I %i %j %k %l %M "
        + "%m %p %r %S %s %T %% %P";
    String timestampFormatted = "Sat Jan 01 31st 31 31 12345 13 01 01 14 031 13 1 "
        + "January 01 PM 01:14:15 PM 15 15 13:14:15 % P";
    verifyDateFormat(timestamp, "timestamp", timestampFormat, timestampFormatted);

    String date = "1998-01-31";
    String dateFormat = "%U %u %V %v %W %w %X %x %Y %y";
    String dateFormatted = "4 4 4 4 Saturday 6 1998 1998 1998 98";
    verifyDateFormat(date, "date", dateFormat, dateFormatted);
  }
}
