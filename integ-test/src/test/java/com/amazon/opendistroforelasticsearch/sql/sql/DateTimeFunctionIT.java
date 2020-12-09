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

package com.amazon.opendistroforelasticsearch.sql.sql;

import static com.amazon.opendistroforelasticsearch.sql.legacy.plugin.RestSqlAction.QUERY_API_ENDPOINT;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.rows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.schema;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifyDataRows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifySchema;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_BANK;
import static com.amazon.opendistroforelasticsearch.sql.util.TestUtils.getResponseBody;

import com.amazon.opendistroforelasticsearch.sql.ast.expression.In;
import com.amazon.opendistroforelasticsearch.sql.common.utils.StringUtils;
import com.amazon.opendistroforelasticsearch.sql.legacy.SQLIntegTestCase;
import com.amazon.opendistroforelasticsearch.sql.util.TestUtils;
import java.io.IOException;
import java.util.Locale;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class DateTimeFunctionIT extends SQLIntegTestCase {

  @Override
  public void init() throws Exception {
    super.init();
    TestUtils.enableNewQueryEngine(client());
    loadIndex(Index.BANK);
  }

  @Test
  public void testDateInGroupBy() throws IOException{
    JSONObject result =
            executeQuery(String.format("SELECT DATE(birthdate) FROM %s GROUP BY DATE(birthdate)",TEST_INDEX_BANK) );
    verifySchema(result,
            schema("DATE(birthdate)", null, "date"));
    verifyDataRows(result,
            rows("2017-10-23"),
            rows("2017-11-20"),
            rows("2018-06-23"),
            rows("2018-11-13"),
            rows("2018-06-27"),
            rows("2018-08-19"),
            rows("2018-08-11"));
  }

  @Test
  public void testAddDate() throws IOException {
    JSONObject result =
        executeQuery("select adddate(timestamp('2020-09-16 17:30:00'), interval 1 day)");
    verifySchema(result,
        schema("adddate(timestamp('2020-09-16 17:30:00'), interval 1 day)", null, "datetime"));
    verifyDataRows(result, rows("2020-09-17 17:30:00"));

    result = executeQuery("select adddate(date('2020-09-16'), 1)");
    verifySchema(result, schema("adddate(date('2020-09-16'), 1)", null, "date"));
    verifyDataRows(result, rows("2020-09-17"));

    result = executeQuery("select adddate('2020-09-16', 1)");
    verifySchema(result, schema("adddate('2020-09-16', 1)", null, "datetime"));
    verifyDataRows(result, rows("2020-09-17"));

    result = executeQuery("select adddate('2020-09-16 17:30:00', interval 1 day)");
    verifySchema(result,
        schema("adddate('2020-09-16 17:30:00', interval 1 day)", null, "datetime"));
    verifyDataRows(result, rows("2020-09-17 17:30:00"));

    result = executeQuery("select adddate('2020-09-16', interval 1 day)");
    verifySchema(result,
        schema("adddate('2020-09-16', interval 1 day)", null, "datetime"));
    verifyDataRows(result, rows("2020-09-17"));
  }

  @Test
  public void testDateAdd() throws IOException {
    JSONObject result =
        executeQuery("select date_add(timestamp('2020-09-16 17:30:00'), interval 1 day)");
    verifySchema(result,
        schema("date_add(timestamp('2020-09-16 17:30:00'), interval 1 day)", null, "datetime"));
    verifyDataRows(result, rows("2020-09-17 17:30:00"));

    result = executeQuery("select date_add(date('2020-09-16'), 1)");
    verifySchema(result, schema("date_add(date('2020-09-16'), 1)", null, "date"));
    verifyDataRows(result, rows("2020-09-17"));

    result = executeQuery("select date_add('2020-09-16', 1)");
    verifySchema(result, schema("date_add('2020-09-16', 1)", null, "datetime"));
    verifyDataRows(result, rows("2020-09-17"));

    result = executeQuery("select date_add('2020-09-16 17:30:00', interval 1 day)");
    verifySchema(result,
        schema("date_add('2020-09-16 17:30:00', interval 1 day)", null, "datetime"));
    verifyDataRows(result, rows("2020-09-17 17:30:00"));

    result = executeQuery("select date_add('2020-09-16', interval 1 day)");
    verifySchema(result,
        schema("date_add('2020-09-16', interval 1 day)", null, "datetime"));
    verifyDataRows(result, rows("2020-09-17"));

    result =
            executeQuery(String.format("SELECT DATE_ADD(birthdate, INTERVAL 1 YEAR) FROM %s GROUP BY 1",TEST_INDEX_BANK) );
    verifySchema(result,
            schema("DATE_ADD(birthdate, INTERVAL 1 YEAR)", null, "datetime"));
    verifyDataRows(result,
            rows("2018-10-23 00:00:00"),
            rows("2018-11-20 00:00:00"),
            rows("2019-06-23 00:00:00"),
            rows("2019-11-13 23:33:20"),
            rows("2019-06-27 00:00:00"),
            rows("2019-08-19 00:00:00"),
            rows("2019-08-11 00:00:00"));
  }

  @Test
  public void testDateSub() throws IOException {
    JSONObject result =
        executeQuery("select date_sub(timestamp('2020-09-16 17:30:00'), interval 1 day)");
    verifySchema(result,
        schema("date_sub(timestamp('2020-09-16 17:30:00'), interval 1 day)", null, "datetime"));
    verifyDataRows(result, rows("2020-09-15 17:30:00"));

    result = executeQuery("select date_sub(date('2020-09-16'), 1)");
    verifySchema(result, schema("date_sub(date('2020-09-16'), 1)", null, "date"));
    verifyDataRows(result, rows("2020-09-15"));

    result = executeQuery("select date_sub('2020-09-16', 1)");
    verifySchema(result, schema("date_sub('2020-09-16', 1)", null, "datetime"));
    verifyDataRows(result, rows("2020-09-15"));

    result = executeQuery("select date_sub('2020-09-16 17:30:00', interval 1 day)");
    verifySchema(result,
        schema("date_sub('2020-09-16 17:30:00', interval 1 day)", null, "datetime"));
    verifyDataRows(result, rows("2020-09-15 17:30:00"));

    result = executeQuery("select date_sub('2020-09-16', interval 1 day)");
    verifySchema(result,
        schema("date_sub('2020-09-16', interval 1 day)", null, "datetime"));
    verifyDataRows(result, rows("2020-09-15"));
  }

  @Test
  public void testDay() throws IOException {
    JSONObject result = executeQuery("select day(date('2020-09-16'))");
    verifySchema(result, schema("day(date('2020-09-16'))", null, "integer"));
    verifyDataRows(result, rows(16));

    result = executeQuery("select day('2020-09-16')");
    verifySchema(result, schema("day('2020-09-16')", null, "integer"));
    verifyDataRows(result, rows(16));
  }

  @Test
  public void testDayName() throws IOException {
    JSONObject result = executeQuery("select dayname(date('2020-09-16'))");
    verifySchema(result, schema("dayname(date('2020-09-16'))", null, "keyword"));
    verifyDataRows(result, rows("Wednesday"));

    result = executeQuery("select dayname('2020-09-16')");
    verifySchema(result, schema("dayname('2020-09-16')", null, "keyword"));
    verifyDataRows(result, rows("Wednesday"));
  }

  @Test
  public void testDayOfMonth() throws IOException {
    JSONObject result = executeQuery("select dayofmonth(date('2020-09-16'))");
    verifySchema(result, schema("dayofmonth(date('2020-09-16'))", null, "integer"));
    verifyDataRows(result, rows(16));

    result = executeQuery("select dayofmonth('2020-09-16')");
    verifySchema(result, schema("dayofmonth('2020-09-16')", null, "integer"));
    verifyDataRows(result, rows(16));
  }

  @Test
  public void testDayOfWeek() throws IOException {
    JSONObject result = executeQuery("select dayofweek(date('2020-09-16'))");
    verifySchema(result, schema("dayofweek(date('2020-09-16'))", null, "integer"));
    verifyDataRows(result, rows(4));

    result = executeQuery("select dayofweek('2020-09-16')");
    verifySchema(result, schema("dayofweek('2020-09-16')", null, "integer"));
    verifyDataRows(result, rows(4));
  }

  @Test
  public void testDayOfYear() throws IOException {
    JSONObject result = executeQuery("select dayofyear(date('2020-09-16'))");
    verifySchema(result, schema("dayofyear(date('2020-09-16'))", null, "integer"));
    verifyDataRows(result, rows(260));

    result = executeQuery("select dayofyear('2020-09-16')");
    verifySchema(result, schema("dayofyear('2020-09-16')", null, "integer"));
    verifyDataRows(result, rows(260));
  }

  @Test
  public void testFromDays() throws IOException {
    JSONObject result = executeQuery("select from_days(738049)");
    verifySchema(result, schema("from_days(738049)", null, "date"));
    verifyDataRows(result, rows("2020-09-16"));
  }

  @Test
  public void testHour() throws IOException {
    JSONObject result = executeQuery("select hour(timestamp('2020-09-16 17:30:00'))");
    verifySchema(result, schema("hour(timestamp('2020-09-16 17:30:00'))", null, "integer"));
    verifyDataRows(result, rows(17));

    result = executeQuery("select hour(time('17:30:00'))");
    verifySchema(result, schema("hour(time('17:30:00'))", null, "integer"));
    verifyDataRows(result, rows(17));

    result = executeQuery("select hour('2020-09-16 17:30:00')");
    verifySchema(result, schema("hour('2020-09-16 17:30:00')", null, "integer"));
    verifyDataRows(result, rows(17));

    result = executeQuery("select hour('17:30:00')");
    verifySchema(result, schema("hour('17:30:00')", null, "integer"));
    verifyDataRows(result, rows(17));
  }

  @Test
  public void testMicrosecond() throws IOException {
    JSONObject result = executeQuery("select microsecond(timestamp('2020-09-16 17:30:00.123456'))");
    verifySchema(result,
        schema("microsecond(timestamp('2020-09-16 17:30:00.123456'))", null, "integer"));
    verifyDataRows(result, rows(123456));

    result = executeQuery("select microsecond(time('17:30:00.000010'))");
    verifySchema(result, schema("microsecond(time('17:30:00.000010'))", null, "integer"));
    verifyDataRows(result, rows(10));

    result = executeQuery("select microsecond('2020-09-16 17:30:00.123456')");
    verifySchema(result, schema("microsecond('2020-09-16 17:30:00.123456')", null, "integer"));
    verifyDataRows(result, rows(123456));

    result = executeQuery("select microsecond('17:30:00.000010')");
    verifySchema(result, schema("microsecond('17:30:00.000010')", null, "integer"));
    verifyDataRows(result, rows(10));
  }

  @Test
  public void testMinute() throws IOException {
    JSONObject result = executeQuery("select minute(timestamp('2020-09-16 17:30:00'))");
    verifySchema(result, schema("minute(timestamp('2020-09-16 17:30:00'))", null, "integer"));
    verifyDataRows(result, rows(30));

    result = executeQuery("select minute(time('17:30:00'))");
    verifySchema(result, schema("minute(time('17:30:00'))", null, "integer"));
    verifyDataRows(result, rows(30));

    result = executeQuery("select minute('2020-09-16 17:30:00')");
    verifySchema(result, schema("minute('2020-09-16 17:30:00')", null, "integer"));
    verifyDataRows(result, rows(30));

    result = executeQuery("select minute('17:30:00')");
    verifySchema(result, schema("minute('17:30:00')", null, "integer"));
    verifyDataRows(result, rows(30));
  }

  @Test
  public void testMonth() throws IOException {
    JSONObject result = executeQuery("select month(date('2020-09-16'))");
    verifySchema(result, schema("month(date('2020-09-16'))", null, "integer"));
    verifyDataRows(result, rows(9));

    result = executeQuery("select month('2020-09-16')");
    verifySchema(result, schema("month('2020-09-16')", null, "integer"));
    verifyDataRows(result, rows(9));
  }

  @Test
  public void testMonthName() throws IOException {
    JSONObject result = executeQuery("select monthname(date('2020-09-16'))");
    verifySchema(result, schema("monthname(date('2020-09-16'))", null, "keyword"));
    verifyDataRows(result, rows("September"));

    result = executeQuery("select monthname('2020-09-16')");
    verifySchema(result, schema("monthname('2020-09-16')", null, "keyword"));
    verifyDataRows(result, rows("September"));
  }

  @Test
  public void testQuarter() throws IOException {
    JSONObject result = executeQuery("select quarter(date('2020-09-16'))");
    verifySchema(result, schema("quarter(date('2020-09-16'))", null, "integer"));
    verifyDataRows(result, rows(3));

    result = executeQuery("select quarter('2020-09-16')");
    verifySchema(result, schema("quarter('2020-09-16')", null, "integer"));
    verifyDataRows(result, rows(3));
  }

  @Test
  public void testSecond() throws IOException {
    JSONObject result = executeQuery("select second(timestamp('2020-09-16 17:30:00'))");
    verifySchema(result, schema("second(timestamp('2020-09-16 17:30:00'))", null, "integer"));
    verifyDataRows(result, rows(0));

    result = executeQuery("select second(time('17:30:00'))");
    verifySchema(result, schema("second(time('17:30:00'))", null, "integer"));
    verifyDataRows(result, rows(0));

    result = executeQuery("select second('2020-09-16 17:30:00')");
    verifySchema(result, schema("second('2020-09-16 17:30:00')", null, "integer"));
    verifyDataRows(result, rows(0));

    result = executeQuery("select second('17:30:00')");
    verifySchema(result, schema("second('17:30:00')", null, "integer"));
    verifyDataRows(result, rows(0));
  }

  @Test
  public void testSubDate() throws IOException {
    JSONObject result =
        executeQuery("select subdate(timestamp('2020-09-16 17:30:00'), interval 1 day)");
    verifySchema(result,
        schema("subdate(timestamp('2020-09-16 17:30:00'), interval 1 day)", null, "datetime"));
    verifyDataRows(result, rows("2020-09-15 17:30:00"));

    result = executeQuery("select subdate(date('2020-09-16'), 1)");
    verifySchema(result, schema("subdate(date('2020-09-16'), 1)", null, "date"));
    verifyDataRows(result, rows("2020-09-15"));

    result = executeQuery("select subdate('2020-09-16 17:30:00', interval 1 day)");
    verifySchema(result,
        schema("subdate('2020-09-16 17:30:00', interval 1 day)", null, "datetime"));
    verifyDataRows(result, rows("2020-09-15 17:30:00"));

    result = executeQuery("select subdate('2020-09-16', 1)");
    verifySchema(result, schema("subdate('2020-09-16', 1)", null, "datetime"));
    verifyDataRows(result, rows("2020-09-15"));

    result = executeQuery("select subdate('2020-09-16', interval 1 day)");
    verifySchema(result,
        schema("subdate('2020-09-16', interval 1 day)", null, "datetime"));
    verifyDataRows(result, rows("2020-09-15"));
  }

  @Test
  public void testTimeToSec() throws IOException {
    JSONObject result = executeQuery("select time_to_sec(time('17:30:00'))");
    verifySchema(result, schema("time_to_sec(time('17:30:00'))", null, "long"));
    verifyDataRows(result, rows(63000));

    result = executeQuery("select time_to_sec('17:30:00')");
    verifySchema(result, schema("time_to_sec('17:30:00')", null, "long"));
    verifyDataRows(result, rows(63000));
  }

  @Test
  public void testToDays() throws IOException {
    JSONObject result = executeQuery("select to_days(date('2020-09-16'))");
    verifySchema(result, schema("to_days(date('2020-09-16'))", null, "long"));
    verifyDataRows(result, rows(738049));

    result = executeQuery("select to_days('2020-09-16')");
    verifySchema(result, schema("to_days('2020-09-16')", null, "long"));
    verifyDataRows(result, rows(738049));
  }

  @Test
  public void testYear() throws IOException {
    JSONObject result = executeQuery("select year(date('2020-09-16'))");
    verifySchema(result, schema("year(date('2020-09-16'))", null, "integer"));
    verifyDataRows(result, rows(2020));

    result = executeQuery("select year('2020-09-16')");
    verifySchema(result, schema("year('2020-09-16')", null, "integer"));
    verifyDataRows(result, rows(2020));
  }

  private void week(String date, int mode, int expectedResult) throws IOException {
    JSONObject result = executeQuery(StringUtils.format("select week(date('%s'), %d)", date,
        mode));
    verifySchema(result,
        schema(StringUtils.format("week(date('%s'), %d)", date, mode), null, "integer"));
    verifyDataRows(result, rows(expectedResult));
  }

  @Test
  public void testWeek() throws IOException {
    JSONObject result = executeQuery("select week(date('2008-02-20'))");
    verifySchema(result, schema("week(date('2008-02-20'))", null, "integer"));
    verifyDataRows(result, rows(7));

    week("2008-02-20", 0, 7);
    week("2008-02-20", 1, 8);
    week("2008-12-31", 1, 53);
    week("2000-01-01", 0, 0);
    week("2000-01-01", 2, 52);
  }

  void verifyDateFormat(String date, String type, String format, String formatted) throws IOException {
    String query = String.format("date_format(%s('%s'), '%s')", type, date, format);
    JSONObject result = executeQuery("select " + query);
    verifySchema(result, schema(query, null, "keyword"));
    verifyDataRows(result, rows(formatted));

    query = String.format("date_format('%s', '%s')", date, format);
    result = executeQuery("select " + query);
    verifySchema(result, schema(query, null, "keyword"));
    verifyDataRows(result, rows(formatted));
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

  protected JSONObject executeQuery(String query) throws IOException {
    Request request = new Request("POST", QUERY_API_ENDPOINT);
    request.setJsonEntity(String.format(Locale.ROOT, "{\n" + "  \"query\": \"%s\"\n" + "}", query));

    RequestOptions.Builder restOptionsBuilder = RequestOptions.DEFAULT.toBuilder();
    restOptionsBuilder.addHeader("Content-Type", "application/json");
    request.setOptions(restOptionsBuilder);

    Response response = client().performRequest(request);
    return new JSONObject(getResponseBody(response));
  }
}
