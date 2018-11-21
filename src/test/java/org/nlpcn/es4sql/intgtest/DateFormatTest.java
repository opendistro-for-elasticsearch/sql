package org.nlpcn.es4sql.intgtest;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Test;
import org.nlpcn.es4sql.MainTestSuite;
import org.nlpcn.es4sql.SearchDao;
import org.nlpcn.es4sql.exception.SqlParseException;
import org.nlpcn.es4sql.query.SqlElasticSearchRequestBuilder;

import java.sql.SQLFeatureNotSupportedException;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.nlpcn.es4sql.TestsConstants.TEST_INDEX_ONLINE;
import static org.nlpcn.es4sql.TestsConstants.SIMPLE_DATE_FORMAT;

public class DateFormatTest {

    private static final String SELECT_FROM =
            "SELECT insert_time " +
            "FROM " + TEST_INDEX_ONLINE + "/online ";

    /**
     * All of the following tests use UTC as their date_format timezone as this is the same timezone of the data
     * being queried. This is to prevent discrepancies in the Elasticsearch query that is eventually made and the
     * actual field data that is being checked for the integration tests.
     */

    @Test
    public void equalTo() {
        assertThat(
            query(SELECT_FROM + "WHERE date_format(insert_time, 'YYYY-MM-dd', 'UTC') = '2014-08-17'"),
            containsInAnyOrder("2014-08-17")
        );
    }

    @Test
    public void lessThan() {
        assertThat(
            query(SELECT_FROM + "WHERE date_format(insert_time, 'YYYY-MM-dd', 'UTC') < '2014-08-18'"),
            containsInAnyOrder("2014-08-17")
        );
    }

    @Test
    public void lessThanOrEqualTo() {
        assertThat(
            query(SELECT_FROM + "WHERE date_format(insert_time, 'YYYY-MM-dd', 'UTC') <= '2014-08-18'"),
            containsInAnyOrder("2014-08-17", "2014-08-18")
        );
    }

    @Test
    public void greaterThan() {
        assertThat(
            query(SELECT_FROM + "WHERE date_format(insert_time, 'YYYY-MM-dd', 'UTC') > '2014-08-23'"),
            containsInAnyOrder("2014-08-24")
        );
    }

    /**
     * Large LIMIT values were given for some of these queries since the default result size of the query is 200 and
     * this ends up excluding some of the expected values causing the assertion to fail. LIMIT overrides this.
     */
    @Test
    public void greaterThanOrEqualTo() {
        assertThat(
            query(SELECT_FROM + "WHERE date_format(insert_time, 'YYYY-MM-dd', 'UTC') >= '2014-08-23' LIMIT 1000"),
            containsInAnyOrder("2014-08-23", "2014-08-24")
        );
    }

    @Test
    public void and() {
        assertThat(
            query(SELECT_FROM +
                  "WHERE date_format(insert_time, 'YYYY-MM-dd', 'UTC') >= '2014-08-21' " +
                  "AND date_format(insert_time, 'YYYY-MM-dd', 'UTC') <= '2014-08-23' LIMIT 1000"),
            containsInAnyOrder("2014-08-21", "2014-08-22", "2014-08-23")
        );
    }

    @Test
    public void or() {
        assertThat(
            query(SELECT_FROM +
                  "WHERE date_format(insert_time, 'YYYY-MM-dd', 'UTC') < '2014-08-18' " +
                  "OR date_format(insert_time, 'YYYY-MM-dd', 'UTC') > '2014-08-23'"),
            containsInAnyOrder("2014-08-17", "2014-08-24")
        );
    }


    private Set<Object> query(String query) {
        try {
            SearchDao searchDao = MainTestSuite.getSearchDao();
            SqlElasticSearchRequestBuilder select = (SqlElasticSearchRequestBuilder) searchDao.explain(query).explain();
            return getResult(((SearchResponse) select.get()).getHits(), "insert_time");
        } catch (SQLFeatureNotSupportedException | SqlParseException e) {
            throw new RuntimeException(e);
        }
    }

    private Set<Object> getResult(SearchHits response, String fieldName) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(SIMPLE_DATE_FORMAT);

        SearchHit[] hits = response.getHits();
        Set<Object> result = new HashSet<>();
        for (SearchHit hit : hits) {
            Map<String, Object> source = hit.getSourceAsMap();
            DateTime date = new DateTime(source.get(fieldName), DateTimeZone.UTC);
            String formattedDate = formatter.print(date);

            result.add(formattedDate);
        }
        return result;
    }
}
