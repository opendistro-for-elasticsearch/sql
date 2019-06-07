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

package com.amazon.opendistroforelasticsearch.sql.esintgtest;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.xcontent.LoggingDeprecationHandler;
import org.elasticsearch.common.xcontent.NamedXContentRegistry;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.SearchHit;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;

public class DateFunctionsIT extends SQLIntegTestCase {

    private static final String FROM = "FROM " + TestsConstants.TEST_INDEX_ONLINE + "/online";

    /**
     * Some of the first few SQL functions are tested in both SELECT and WHERE cases for flexibility and the remainder
     * are merely tested in SELECT for simplicity.
     *
     * There is a limitation in all date SQL functions in that they expect a date field as input. In the future this
     * can be expanded on by supporting CAST and casting dates given as Strings to TIMESTAMP (SQL's date type).
     */

    @Override
    protected void init() throws Exception {
        loadIndex(Index.ONLINE);
    }

    @Test
    public void year() throws IOException {
        SearchHit[] hits = query(
            "SELECT YEAR(insert_time) as year"
        );
        for (SearchHit hit : hits) {
            int year = (int) getField(hit, "year");
            DateTime insertTime = getDateFromSource(hit, "insert_time");
            assertThat(year, equalTo(insertTime.year().get()));
        }
    }

    @Test
    public void monthOfYear() throws IOException {
        SearchHit[] hits = query(
            "SELECT MONTH_OF_YEAR(insert_time) as month_of_year"
        );
        for (SearchHit hit : hits) {
            int monthOfYear = (int) getField(hit, "month_of_year");
            DateTime insertTime = getDateFromSource(hit, "insert_time");
            assertThat(monthOfYear, equalTo(insertTime.monthOfYear().get()));
        }
    }

    @Test
    public void weekOfYearInSelect() throws IOException {
        SearchHit[] hits = query(
            "SELECT WEEK_OF_YEAR(insert_time) as week_of_year"
        );
        for (SearchHit hit : hits) {
            int weekOfYear = (int) getField(hit, "week_of_year");
            DateTime insertTime = getDateFromSource(hit, "insert_time");
            assertThat(weekOfYear, equalTo(insertTime.weekOfWeekyear().get()));
        }
    }

    @Test
    public void weekOfYearInWhere() throws IOException {
        SearchHit[] hits = query(
            "SELECT insert_time",
            "WHERE DATE_FORMAT(insert_time, 'YYYY-MM-dd') < '2014-08-19' AND " +
            "WEEK_OF_YEAR(insert_time) > 33",
            "LIMIT 2000"
        );
        for (SearchHit hit : hits) {
            DateTime insertTime = getDateFromSource(hit, "insert_time");
            assertThat(insertTime.weekOfWeekyear().get(), greaterThan(33));
        }
    }

    @Test
    public void dayOfYearInSelect() throws IOException {
        SearchHit[] hits = query(
            "SELECT DAY_OF_YEAR(insert_time) as day_of_year", "LIMIT 2000"
        );
        for (SearchHit hit : hits) {
            int dayOfYear = (int) getField(hit, "day_of_year");
            DateTime insertTime = getDateFromSource(hit, "insert_time");
            assertThat(dayOfYear, equalTo(insertTime.dayOfYear().get()));
        }
    }

    @Test
    public void dayOfYearInWhere() throws IOException {
        SearchHit[] hits = query(
            "SELECT insert_time", "WHERE DAY_OF_YEAR(insert_time) < 233", "LIMIT 10000"
        );
        for (SearchHit hit : hits) {
            DateTime insertTime = getDateFromSource(hit, "insert_time");
            assertThat(insertTime.dayOfYear().get(), lessThan(233));
        }
    }

    @Test
    public void dayOfMonthInSelect() throws IOException {
        SearchHit[] hits = query(
            "SELECT DAY_OF_MONTH(insert_time) as day_of_month", "LIMIT 2000"
        );
        for (SearchHit hit : hits) {
            int dayOfMonth = (int) getField(hit, "day_of_month");
            DateTime insertTime = getDateFromSource(hit, "insert_time");
            assertThat(dayOfMonth, equalTo(insertTime.dayOfMonth().get()));
        }
    }

    @Test
    public void dayOfMonthInWhere() throws IOException {
        SearchHit[] hits = query(
            "SELECT insert_time", "WHERE DAY_OF_MONTH(insert_time) < 21", "LIMIT 10000"
        );
        for (SearchHit hit : hits) {
            DateTime insertTime = getDateFromSource(hit, "insert_time");
            assertThat(insertTime.dayOfMonth().get(), lessThan(21));
        }
    }

    @Test
    public void dayOfWeek() throws IOException {
        SearchHit[] hits = query(
            "SELECT DAY_OF_WEEK(insert_time) as day_of_week", "LIMIT 2000"
        );
        for (SearchHit hit : hits) {
            int dayOfWeek = (int) getField(hit, "day_of_week");
            DateTime insertTime = getDateFromSource(hit, "insert_time");
            assertThat(dayOfWeek, equalTo(insertTime.dayOfWeek().get()));
        }
    }

    @Test
    public void hourOfDay() throws IOException {
        SearchHit[] hits = query(
            "SELECT HOUR_OF_DAY(insert_time) as hour_of_day", "LIMIT 1000"
        );
        for (SearchHit hit : hits) {
            int hourOfDay = (int) getField(hit, "hour_of_day");
            DateTime insertTime = getDateFromSource(hit, "insert_time");
            assertThat(hourOfDay, equalTo(insertTime.hourOfDay().get()));
        }
    }

    @Test
    public void minuteOfDay() throws IOException {
        SearchHit[] hits = query(
            "SELECT MINUTE_OF_DAY(insert_time) as minute_of_day", "LIMIT 500"
        );
        for (SearchHit hit : hits) {
            int minuteOfDay = (int) getField(hit, "minute_of_day");
            DateTime insertTime = getDateFromSource(hit, "insert_time");
            assertThat(minuteOfDay, equalTo(insertTime.minuteOfDay().get()));
        }
    }

    @Test
    public void minuteOfHour() throws IOException {
        SearchHit[] hits = query(
            "SELECT MINUTE_OF_HOUR(insert_time) as minute_of_hour", "LIMIT 500"
        );
        for (SearchHit hit : hits) {
            int minuteOfHour = (int) getField(hit, "minute_of_hour");
            DateTime insertTime = getDateFromSource(hit, "insert_time");
            assertThat(minuteOfHour, equalTo(insertTime.minuteOfHour().get()));
        }
    }

    @Test
    public void secondOfMinute() throws IOException {
        SearchHit[] hits = query(
            "SELECT SECOND_OF_MINUTE(insert_time) as second_of_minute", "LIMIT 500"
        );
        for (SearchHit hit : hits) {
            int secondOfMinute = (int) getField(hit, "second_of_minute");
            DateTime insertTime = getDateFromSource(hit, "insert_time");
            assertThat(secondOfMinute, equalTo(insertTime.secondOfMinute().get()));
        }
    }

    private SearchHit[] query(String select, String... statements) throws IOException {
        return execute(select + " " + FROM + " " + String.join(" ", statements));
    }

    // TODO: I think this code is now re-used in multiple classes, would be good to move to the base class.
    private SearchHit[] execute(String sqlRequest) throws IOException {
        final JSONObject jsonObject = executeRequest(makeRequest(sqlRequest));

        final XContentParser parser = XContentFactory.xContent(XContentType.JSON).createParser(
            NamedXContentRegistry.EMPTY,
            LoggingDeprecationHandler.INSTANCE,
            jsonObject.toString());
        return SearchResponse.fromXContent(parser).getHits().getHits();
    }

    private Object getField(SearchHit hit, String fieldName) {
        return hit.field(fieldName).getValue();
    }

    private Object getFieldFromSource(SearchHit hit, String fieldName) {
        return hit.getSourceAsMap().get(fieldName);
    }

    private DateTime getDateTime(String date) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        return formatter.parseDateTime(date);
    }

    private DateTime getDateFromSource(SearchHit hit, String dateField) {
        return getDateTime((String) getFieldFromSource(hit, dateField));
    }
}
