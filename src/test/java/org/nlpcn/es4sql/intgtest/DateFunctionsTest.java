package org.nlpcn.es4sql.intgtest;

import com.alibaba.druid.sql.parser.ParserException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;
import org.nlpcn.es4sql.SearchDao;
import org.nlpcn.es4sql.exception.SqlParseException;
import org.nlpcn.es4sql.query.SqlElasticSearchRequestBuilder;

import java.sql.SQLFeatureNotSupportedException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.nlpcn.es4sql.intgtest.TestsConstants.TEST_INDEX_ONLINE;

public class DateFunctionsTest {

    private static final String FROM = "FROM " + TEST_INDEX_ONLINE + "/online";

    /**
     * Some of the first few SQL functions are tested in both SELECT and WHERE cases for flexibility and the remainder
     * are merely tested in SELECT for simplicity.
     *
     * There is a limitation in all date SQL functions in that they expect a date field as input. In the future this
     * can be expanded on by supporting CAST and casting dates given as Strings to TIMESTAMP (SQL's date type).
     */

    @Test
    public void year() {
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
    public void monthOfYear() {
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
    public void weekOfYearInSelect() {
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
    public void weekOfYearInWhere() {
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
    public void dayOfYearInSelect() {
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
    public void dayOfYearInWhere() {
        SearchHit[] hits = query(
                "SELECT insert_time", "WHERE DAY_OF_YEAR(insert_time) < 233", "LIMIT 10000"
        );
        for (SearchHit hit : hits) {
            DateTime insertTime = getDateFromSource(hit, "insert_time");
            assertThat(insertTime.dayOfYear().get(), lessThan(233));
        }
    }

    @Test
    public void dayOfMonthInSelect() {
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
    public void dayOfMonthInWhere() {
        SearchHit[] hits = query(
                "SELECT insert_time", "WHERE DAY_OF_MONTH(insert_time) < 21", "LIMIT 10000"
        );
        for (SearchHit hit : hits) {
            DateTime insertTime = getDateFromSource(hit, "insert_time");
            assertThat(insertTime.dayOfMonth().get(), lessThan(21));
        }
    }

    @Test
    public void dayOfWeek() {
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
    public void hourOfDay() {
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
    public void minuteOfDay() {
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
    public void minuteOfHour() {
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
    public void secondOfMinute() {
        SearchHit[] hits = query(
                "SELECT SECOND_OF_MINUTE(insert_time) as second_of_minute", "LIMIT 500"
        );
        for (SearchHit hit : hits) {
            int secondOfMinute = (int) getField(hit, "second_of_minute");
            DateTime insertTime = getDateFromSource(hit, "insert_time");
            assertThat(secondOfMinute, equalTo(insertTime.secondOfMinute().get()));
        }
    }

    private SearchHit[] query(String select, String... statements) {
        return execute(select + " " + FROM + " " + String.join(" ", statements));
    }

    private SearchHit[] execute(String sql) {
        try {
            SearchDao searchDao = MainTestSuite.getSearchDao();
            SqlElasticSearchRequestBuilder select = (SqlElasticSearchRequestBuilder) searchDao.explain(sql).explain();
            return ((SearchResponse) select.get()).getHits().getHits();
        } catch (SQLFeatureNotSupportedException | SqlParseException e) {
            throw new ParserException("Unable to parse query: " + sql);
        }

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
