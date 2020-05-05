/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.esintgtest;

import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestUtils.getResponseBody;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_ACCOUNT;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_DATE_TIME;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_NESTED_SIMPLE;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.containsString;

public class CursorIT extends SQLIntegTestCase {

    private static final String CURSOR = "cursor";
    private static final String DATAROWS = "datarows";
    private static final String SCHEMA = "schema";
    private static final String JDBC = "jdbc";
    private static final String NEW_LINE = "\n";

    @Override
    protected void init() throws Exception {
        loadIndex(Index.ACCOUNT);
        enableCursorClusterSetting();
    }

    /**
     * Acceptable fetch_size are positive numbers.
     * For example 0, 24, 53.0, "110" (parsable string) , "786.23"
     * Negative values should throw 400
     */
    @Test
    public void invalidNegativeFetchSize() throws IOException {
        String query = StringUtils.format("SELECT firstname, state FROM %s", TestsConstants.TEST_INDEX_ACCOUNT);
        Response response = null;
        try {
            String queryResult = executeFetchQuery(query, -2, JDBC);
        } catch (ResponseException ex) {
            response = ex.getResponse();
        }

        JSONObject resp = new JSONObject(TestUtils.getResponseBody(response));
        assertThat(resp.getInt("status"), equalTo(400));
        assertThat(resp.query("/error/reason"), equalTo("Invalid SQL query"));
        assertThat(resp.query("/error/details"), equalTo("Fetch_size must be greater or equal to 0"));
        assertThat(resp.query("/error/type"), equalTo("IllegalArgumentException"));
    }

    /**
     * Non-numeric fetch_size value should throw 400
     */
    @Test
    public void invalidNonNumericFetchSize() throws IOException {
        String query = StringUtils.format("SELECT firstname, state FROM %s", TestsConstants.TEST_INDEX_ACCOUNT);
        Response response = null;
        try {
            String queryResult = executeFetchAsStringQuery(query, "hello world", JDBC);
        } catch (ResponseException ex) {
            response = ex.getResponse();
        }

        JSONObject resp = new JSONObject(TestUtils.getResponseBody(response));
        assertThat(resp.getInt("status"), equalTo(400));
        assertThat(resp.query("/error/reason"), equalTo("Invalid SQL query"));
        assertThat(resp.query("/error/details"), equalTo("Failed to parse field [fetch_size]"));
        assertThat(resp.query("/error/type"), equalTo("IllegalArgumentException"));
    }

    @Test
    public void testExceptionOnCursorExplain() throws IOException {
        String cursorRequest = "{\"cursor\":\"d:eyJhIjp7fSwicyI6IkRYRjFaWEo1\"}";
        Request sqlRequest = getSqlRequest(cursorRequest, true);
        Response response = null;
        try {
            String queryResult = executeRequest(sqlRequest);
        } catch (ResponseException ex) {
            response = ex.getResponse();
        }

        JSONObject resp = new JSONObject(TestUtils.getResponseBody(response));
        assertThat(resp.getInt("status"), equalTo(400));
        assertThat(resp.query("/error/reason"), equalTo("Invalid SQL query"));
        assertThat(resp.query("/error/details"), equalTo("Invalid request. Cannot explain cursor"));
        assertThat(resp.query("/error/type"), equalTo("IllegalArgumentException"));
    }

    /**
     * For fetch_size = 0, default to non-pagination behaviour for simple queries
     * This can be verified by checking that cursor is not present, and old default limit applies
     */
    @Test
    public void noPaginationWhenFetchSizeZero() throws IOException {
        String selectQuery = StringUtils.format("SELECT firstname, state FROM %s", TEST_INDEX_ACCOUNT);
        JSONObject response = new JSONObject(executeFetchQuery(selectQuery, 0, JDBC));
        assertFalse(response.has(CURSOR));
        assertThat(response.getJSONArray(DATAROWS).length(), equalTo(200));
    }

    /**
     * The index has 1000 records, with fetch size of 50 we should get 20 pages with no cursor on last page
     */
    @Test
    public void validNumberOfPages() throws IOException {
        String selectQuery = StringUtils.format("SELECT firstname, state FROM %s", TEST_INDEX_ACCOUNT);
        JSONObject response = new JSONObject(executeFetchQuery(selectQuery, 50, JDBC));
        String cursor = response.getString(CURSOR);
        int pageCount = 1;

        while (!cursor.isEmpty()) { //this condition also checks that there is no cursor on last page
            response = executeCursorQuery(cursor);
            cursor = response.optString(CURSOR);
            pageCount++;
        }

        assertThat(pageCount, equalTo(20));

        // using random value here, with fetch size of 28 we should get 36 pages (ceil of 1000/28)
        response = new JSONObject(executeFetchQuery(selectQuery, 28, JDBC));
        cursor = response.getString(CURSOR);
        System.out.println(response);
        pageCount = 1;

        while (!cursor.isEmpty()) {
            response = executeCursorQuery(cursor);
            cursor = response.optString(CURSOR);
            pageCount++;
        }
        assertThat(pageCount, equalTo(36));
    }


    @Test
    public void validTotalResultWithAndWithoutPagination() throws IOException {
        // simple query - accounts index has 1000 docs, using higher limit to get all docs
        String selectQuery = StringUtils.format("SELECT firstname, state FROM %s ", TEST_INDEX_ACCOUNT );
        verifyWithAndWithoutPaginationResponse(selectQuery + " LIMIT 2000" , selectQuery , 80);
    }

    @Test
    public void validTotalResultWithAndWithoutPaginationWhereClause() throws IOException {
        String selectQuery = StringUtils.format(
                "SELECT firstname, state FROM %s WHERE balance < 25000 AND age > 32", TEST_INDEX_ACCOUNT
        );
        verifyWithAndWithoutPaginationResponse(selectQuery + " LIMIT 2000" , selectQuery , 17);
    }

    @Test
    public void validTotalResultWithAndWithoutPaginationOrderBy() throws IOException {
        String selectQuery = StringUtils.format(
                "SELECT firstname, state FROM %s ORDER BY balance DESC ", TEST_INDEX_ACCOUNT
        );
        verifyWithAndWithoutPaginationResponse(selectQuery + " LIMIT 2000" , selectQuery , 26);
    }

    @Test
    public void validTotalResultWithAndWithoutPaginationWhereAndOrderBy() throws IOException {
        String selectQuery = StringUtils.format(
                "SELECT firstname, state FROM %s WHERE balance < 25000 ORDER BY balance ASC ", TEST_INDEX_ACCOUNT
        );
        verifyWithAndWithoutPaginationResponse(selectQuery + " LIMIT 2000" , selectQuery , 80);

    }

    @Test
    public void validTotalResultWithAndWithoutPaginationNested() throws IOException {
        loadIndex(Index.NESTED_SIMPLE);
        String selectQuery = StringUtils.format(
                "SELECT name, a.city, a.state FROM %s m , m.address as a ", TEST_INDEX_NESTED_SIMPLE
        );
        verifyWithAndWithoutPaginationResponse(selectQuery + " LIMIT 2000" , selectQuery , 1);
    }

    @Test
    public void noCursorWhenResultsLessThanFetchSize() throws IOException {
        // fetch_size is 100, but actual number of rows returned from ElasticSearch is 97
        // a scroll context will be opened but will be closed after first page as all records are fetched
        String selectQuery = StringUtils.format(
                "SELECT * FROM %s WHERE balance < 25000 AND age > 36 LIMIT 2000", TEST_INDEX_ACCOUNT
        );
        JSONObject response = new JSONObject(executeFetchQuery(selectQuery, 100, JDBC));
        assertFalse(response.has(CURSOR));
    }

    @Test
    public void testCursorWithPreparedStatement() throws IOException {
        JSONObject response = executeJDBCRequest(String.format("{" +
                "  \"fetch_size\": 200," +
                "  \"query\": \" SELECT age, state FROM %s WHERE age > ? OR state IN (?, ?)\"," +
                "  \"parameters\": [" +
                "    {" +
                "      \"type\": \"integer\"," +
                "      \"value\": 25" +
                "    }," +
                "        {" +
                "      \"type\": \"string\"," +
                "      \"value\": \"WA\"" +
                "    }," +
                "            {" +
                "      \"type\": \"string\"," +
                "      \"value\": \"UT\"" +
                "    }" +
                "  ]" +
                "}", TestsConstants.TEST_INDEX_ACCOUNT));

        assertTrue(response.has(CURSOR));
    }

    @Test
    public void testRegressionOnDateFormatChange() throws IOException {
        loadIndex(Index.DATETIME);
        /**
         * With pagination, the field should be date formatted to MySQL format as in
         * @see <a href="https://github.com/opendistro-for-elasticsearch/sql/pull/367">PR #367</a
         *
         * TEST_INDEX_DATE_TIME has three docs with login_time as date field with following values
         * 1.2015-01-01
         * 2.2015-01-01T12:10:30Z
         * 3.1585882955
         * 4.2020-04-08T11:10:30+05:00
         */

        List<String> actualDateList = new ArrayList<>();
        String selectQuery = StringUtils.format("SELECT login_time FROM %s LIMIT 500", TEST_INDEX_DATE_TIME);
        JSONObject response = new JSONObject(executeFetchQuery(selectQuery, 1, JDBC));
        String cursor = response.getString(CURSOR);
        actualDateList.add(response.getJSONArray(DATAROWS).getJSONArray(0).getString(0));

        while (!cursor.isEmpty()) {
            response = executeCursorQuery(cursor);
            cursor = response.optString(CURSOR);
            actualDateList.add(response.getJSONArray(DATAROWS).getJSONArray(0).getString(0));
        }

        List<String> expectedDateList = Arrays.asList(
                "2015-01-01 00:00:00.000",
                "2015-01-01 12:10:30.000",
                "1585882955", // by existing design, this is not formatted in MySQL standard format
                "2020-04-08 06:10:30.000");

        assertThat(actualDateList, equalTo(expectedDateList));
    }


    @Test
    public void defaultBehaviorWhenCursorSettingIsDisabled() throws IOException {
        updateClusterSettings(new ClusterSetting(PERSISTENT, "opendistro.sql.cursor.enabled", "false"));
        String query = StringUtils.format("SELECT firstname, email, state FROM %s", TEST_INDEX_ACCOUNT);
        JSONObject response = new JSONObject(executeFetchQuery(query, 100, JDBC));
        assertFalse(response.has(CURSOR));

        updateClusterSettings(new ClusterSetting(PERSISTENT, "opendistro.sql.cursor.enabled", "true"));
        query = StringUtils.format("SELECT firstname, email, state FROM %s", TEST_INDEX_ACCOUNT);
        response = new JSONObject(executeFetchQuery(query, 100, JDBC));
        assertTrue(response.has(CURSOR));

        wipeAllClusterSettings();
    }


    @Test
    public void testCursorSettings() throws IOException {
        // reverting enableCursorClusterSetting() in init() method before checking defaults
        updateClusterSettings(new ClusterSetting(PERSISTENT, "opendistro.sql.cursor.enabled", null));

        // Assert default cursor settings
        JSONObject clusterSettings = getAllClusterSettings();
        assertThat(clusterSettings.query("/defaults/opendistro.sql.cursor.enabled"), equalTo("false"));
        assertThat(clusterSettings.query("/defaults/opendistro.sql.cursor.fetch_size"), equalTo("1000"));
        assertThat(clusterSettings.query("/defaults/opendistro.sql.cursor.keep_alive"), equalTo("1m"));

        updateClusterSettings(new ClusterSetting(PERSISTENT, "opendistro.sql.cursor.enabled", "true"));
        updateClusterSettings(new ClusterSetting(TRANSIENT, "opendistro.sql.cursor.fetch_size", "400"));
        updateClusterSettings(new ClusterSetting(PERSISTENT, "opendistro.sql.cursor.keep_alive", "200s"));

        clusterSettings = getAllClusterSettings();
        assertThat(clusterSettings.query("/persistent/opendistro.sql.cursor.enabled"), equalTo("true"));
        assertThat(clusterSettings.query("/transient/opendistro.sql.cursor.fetch_size"), equalTo("400"));
        assertThat(clusterSettings.query("/persistent/opendistro.sql.cursor.keep_alive"), equalTo("200s"));

        wipeAllClusterSettings();
    }


    @Test
    public void testDefaultFetchSizeFromClusterSettings() throws IOException {
        // the default fetch size is 1000
        // using non-nested query here as page will have more rows on flattening
        String query = StringUtils.format("SELECT firstname, email, state FROM %s", TEST_INDEX_ACCOUNT);
        JSONObject response = new JSONObject(executeFetchLessQuery(query, JDBC));
        JSONArray datawRows = response.optJSONArray(DATAROWS);
        assertThat(datawRows.length(), equalTo(1000));

        updateClusterSettings(new ClusterSetting(TRANSIENT, "opendistro.sql.cursor.fetch_size", "786"));
        response = new JSONObject(executeFetchLessQuery(query, JDBC));
        datawRows = response.optJSONArray(DATAROWS);
        assertThat(datawRows.length(), equalTo(786));
        assertTrue(response.has(CURSOR));

        wipeAllClusterSettings();
    }

    @Test
    public void testCursorCloseAPI() throws IOException {
        // multiple invocation of closing cursor should return success
        // fetch page using old cursor should throw error
        String selectQuery = StringUtils.format(
                "SELECT firstname, state FROM %s WHERE balance > 100 and age < 40", TEST_INDEX_ACCOUNT);
        JSONObject result = new JSONObject(executeFetchQuery(selectQuery, 50, JDBC));
        String cursor = result.getString(CURSOR);

        // Retrieving next 10 pages out of remaining 19 pages
        for(int i =0 ; i < 10 ; i++) {
            result = executeCursorQuery(cursor);
            cursor = result.optString(CURSOR);
        }
        //Closing the cursor
        JSONObject closeResp = executeCursorCloseQuery(cursor);
        assertThat(closeResp.getBoolean("succeeded"), equalTo(true));

        //Closing the cursor multiple times is idempotent
        for(int i =0 ; i < 5 ; i++) {
            closeResp = executeCursorCloseQuery(cursor);
            assertThat(closeResp.getBoolean("succeeded"), equalTo(true));
        }

        // using the cursor after its cleared, will throw exception
        Response response = null;
        try {
            JSONObject queryResult = executeCursorQuery(cursor);
        } catch (ResponseException ex) {
            response = ex.getResponse();
        }

        JSONObject resp = new JSONObject(TestUtils.getResponseBody(response));
        assertThat(resp.getInt("status"), equalTo(404));
        assertThat(resp.query("/error/reason"), equalTo("all shards failed"));
        assertThat(resp.query("/error/caused_by/reason").toString(), containsString("No search context found"));
        assertThat(resp.query("/error/type"), equalTo("search_phase_execution_exception"));
    }


    @Test
    public void invalidCursorIdNotDecodable() throws IOException {
        // could be either not decode-able
        String randomCursor = "d:eyJzY2hlbWEiOlt7Im5hbWUiOiJmaXJzdG5hbWUiLCJ0eXBlIjoidGV4dCJ9LHsibmFtZSI6InN0Y";

        Response response = null;
        try {
            JSONObject resp = executeCursorQuery(randomCursor);
        } catch (ResponseException ex) {
            response = ex.getResponse();
        }

        JSONObject resp = new JSONObject(TestUtils.getResponseBody(response));
        assertThat(resp.getInt("status"), equalTo(400));
        assertThat(resp.query("/error/type"), equalTo("illegal_argument_exception"));
    }

    /**
     * The index has 1000 records, with fetch size of 50 and LIMIT in place
     * we should get Math.ceil(limit/fetchSize) pages and LIMIT number of rows.
     * Basically it should not retrieve all records in presence of a smaller LIMIT value.
     */
    @Test
    public void respectLimitPassedInSelectClause() throws IOException {
        int limit = 234;
        String selectQuery = StringUtils.format("SELECT age, balance FROM %s LIMIT %s", TEST_INDEX_ACCOUNT, limit);
        JSONObject response = new JSONObject(executeFetchQuery(selectQuery, 50, JDBC));
        String cursor = response.getString(CURSOR);
        int actualDataRowCount = response.getJSONArray(DATAROWS).length();
        int pageCount = 1;

        while (!cursor.isEmpty()) {
            response = executeCursorQuery(cursor);
            cursor = response.optString(CURSOR);
            actualDataRowCount += response.getJSONArray(DATAROWS).length();
            pageCount++;
        }

        assertThat(pageCount, equalTo(5));
        assertThat(actualDataRowCount, equalTo(limit));
    }


    @Test
    public void noPaginationWithNonJDBCFormat() throws IOException {
        // checking for CSV, RAW format
        String query = StringUtils.format("SELECT firstname, email, state FROM %s LIMIT 2000", TEST_INDEX_ACCOUNT);
        String csvResult = executeFetchQuery(query, 100, "csv");
        String[] rows = csvResult.split(NEW_LINE);
        // all the 1001 records (+1 for header) are retrieved instead of fetch_size number of records
        assertThat(rows.length, equalTo(1001));

        String rawResult = executeFetchQuery(query, 100, "raw");
        rows = rawResult.split(NEW_LINE);
        // all the 1000 records (NO headers) are retrieved instead of fetch_size number of records
        assertThat(rows.length, equalTo(1000));
    }


    public void verifyWithAndWithoutPaginationResponse(String sqlQuery, String cursorQuery, int fetch_size) throws IOException {
        // we are only checking here for schema and datarows
        JSONObject withoutCursorResponse = new JSONObject(executeFetchQuery(sqlQuery, 0, JDBC));

        JSONObject withCursorResponse = new JSONObject("{\"schema\":[],\"datarows\":[]}");
        JSONArray schema = withCursorResponse.getJSONArray(SCHEMA);
        JSONArray dataRows = withCursorResponse.getJSONArray(DATAROWS);

        JSONObject response = new JSONObject(executeFetchQuery(cursorQuery, fetch_size, JDBC));
        response.optJSONArray(SCHEMA).forEach(schema::put);
        response.optJSONArray(DATAROWS).forEach(dataRows::put);

        String cursor = response.getString(CURSOR);
        while (!cursor.isEmpty()) {
            response = executeCursorQuery(cursor);
            response.optJSONArray(DATAROWS).forEach(dataRows::put);
            cursor = response.optString(CURSOR);
        }

        verifySchema(withoutCursorResponse.optJSONArray(SCHEMA), withCursorResponse.optJSONArray(SCHEMA));
        verifyDataRows(withoutCursorResponse.optJSONArray(DATAROWS), withCursorResponse.optJSONArray(DATAROWS));
    }

    public void verifySchema(JSONArray schemaOne, JSONArray schemaTwo) {
        assertTrue(schemaOne.similar(schemaTwo));
    }

    public void verifyDataRows(JSONArray dataRowsOne, JSONArray dataRowsTwo) {
        assertTrue(dataRowsOne.similar(dataRowsTwo));
    }

    private void enableCursorClusterSetting() throws IOException{
        updateClusterSettings(new ClusterSetting("persistent", "opendistro.sql.cursor.enabled", "true"));
    }

    public String executeFetchAsStringQuery(String query, String fetchSize, String requestType) throws IOException {
        String endpoint = "/_opendistro/_sql?format=" + requestType;
        String requestBody = makeRequest(query, fetchSize);

        Request sqlRequest = new Request("POST", endpoint);
        sqlRequest.setJsonEntity(requestBody);

        Response response = client().performRequest(sqlRequest);
        String responseString = getResponseBody(response, true);
        return responseString;
    }

    private String makeRequest(String query, String fetch_size) {
        return String.format("{" +
                "  \"fetch_size\": \"%s\"," +
                "  \"query\": \"%s\"" +
                "}", fetch_size, query);
    }

    private JSONObject executeJDBCRequest(String requestBody) throws IOException {
        Request sqlRequest = getSqlRequest(requestBody, false, JDBC);
        return new JSONObject(executeRequest(sqlRequest));
    }
}
