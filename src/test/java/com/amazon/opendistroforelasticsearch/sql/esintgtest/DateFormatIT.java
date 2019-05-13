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

import com.amazon.opendistroforelasticsearch.sql.exception.SqlParseException;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import static org.hamcrest.Matchers.contains;

public class DateFormatIT extends SQLIntegTestCase {

    private static final String SELECT_FROM =
            "SELECT insert_time " +
            "FROM " + TestsConstants.TEST_INDEX_ONLINE + "/online ";

    @Override
    protected void init() throws Exception {
        loadIndex(Index.ONLINE);
    }

    /**
     * All of the following tests use UTC as their date_format timezone as this is the same timezone of the data
     * being queried. This is to prevent discrepancies in the Elasticsearch query and the actual field data that is
     * being checked for the integration tests.
     *
     * Large LIMIT values were given for some of these queries since the default result size of the query is 200 and
     * this ends up excluding some of the expected values causing the assertion to fail. LIMIT overrides this.
     */

    @Test
    public void equalTo() throws SqlParseException {
        assertThat(
            dateQuery(SELECT_FROM + "WHERE date_format(insert_time, 'yyyy-MM-dd', 'UTC') = '2014-08-17'"),
            contains("2014-08-17")
        );
    }

    @Test
    public void lessThan() throws SqlParseException {
        assertThat(
            dateQuery(SELECT_FROM + "WHERE date_format(insert_time, 'yyyy-MM-dd', 'UTC') < '2014-08-18'"),
            contains("2014-08-17")
        );
    }

    @Test
    public void lessThanOrEqualTo() throws SqlParseException {
        assertThat(
            dateQuery(SELECT_FROM + "WHERE date_format(insert_time, 'yyyy-MM-dd', 'UTC') <= '2014-08-18' " +
                      "ORDER BY insert_time " +
                      "LIMIT 1000"),
            contains("2014-08-17", "2014-08-18")
        );
    }

    @Test
    public void greaterThan() throws SqlParseException {
        assertThat(
            dateQuery(SELECT_FROM + "WHERE date_format(insert_time, 'yyyy-MM-dd', 'UTC') > '2014-08-23'"),
            contains("2014-08-24")
        );
    }

    @Test
    public void greaterThanOrEqualTo() throws SqlParseException {
        assertThat(
            dateQuery(SELECT_FROM + "WHERE date_format(insert_time, 'yyyy-MM-dd', 'UTC') >= '2014-08-23' " +
                      "ORDER BY insert_time " +
                      "LIMIT 2000"),
            contains("2014-08-23", "2014-08-24")
        );
    }

    @Test
    public void and() throws SqlParseException{
        assertThat(
            dateQuery(SELECT_FROM +
                      "WHERE date_format(insert_time, 'yyyy-MM-dd', 'UTC') >= '2014-08-21' " +
                      "AND date_format(insert_time, 'yyyy-MM-dd', 'UTC') <= '2014-08-23' " +
                      "ORDER BY insert_time " +
                      "LIMIT 3000"),
            contains("2014-08-21", "2014-08-22", "2014-08-23")
        );
    }

    @Test
    public void or() throws SqlParseException {
        assertThat(
            dateQuery(SELECT_FROM +
                      "WHERE date_format(insert_time, 'yyyy-MM-dd', 'UTC') < '2014-08-18' " +
                      "OR date_format(insert_time, 'yyyy-MM-dd', 'UTC') > '2014-08-23' " +
                      "ORDER BY insert_time " +
                      "LIMIT 1000"),
            contains("2014-08-17", "2014-08-24")
        );
    }

    private Set<Object> dateQuery(String sql) throws SqlParseException {
        try {
            JSONObject response = executeQuery(sql);
            return getResult(response, "insert_time");
        } catch (IOException e) {
            throw new SqlParseException(String.format("Unable to process query '%s'", sql));
        }
    }

    private Set<Object> getResult(JSONObject response, String fieldName) {
        DateTimeFormatter formatter = DateTimeFormat.forPattern(TestsConstants.SIMPLE_DATE_FORMAT);

        JSONArray hits = getHits(response);
        Set<Object> result = new TreeSet<>(); // Using TreeSet so order is maintained
        for (int i = 0; i < hits.length(); i++) {
            JSONObject hit = hits.getJSONObject(i);
            JSONObject source = getSource(hit);
            DateTime date = new DateTime(source.get(fieldName), DateTimeZone.UTC);
            String formattedDate = formatter.print(date);

            result.add(formattedDate);
        }

        return result;
    }
}
