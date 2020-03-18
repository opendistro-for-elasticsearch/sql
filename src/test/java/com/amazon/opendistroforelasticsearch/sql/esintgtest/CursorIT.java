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
import org.elasticsearch.client.Response;
import org.elasticsearch.client.ResponseException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;

import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_ACCOUNT;
import static org.hamcrest.Matchers.equalTo;

public class CursorIT extends SQLIntegTestCase {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    private static final String JDBC = "jdbc";

    @Override
    protected void init() throws Exception {
        loadIndex(Index.ACCOUNT);
    }

    @Test
    public void invalidFetchSize() throws IOException {
        // invalid fetch_size --> negative(-2), non-numeric("hello")
        // acceptable fetch_size --> positive numbers, even in string form "532.4"
        exception.expect(ResponseException.class);
        exception.expectMessage("Fetch_size must be greater or equal to 0");
        String selectQuery = StringUtils.format("SELECT firstname, state FROM %s", TEST_INDEX_ACCOUNT);
        String response = executeFetchQuery(selectQuery, -2, JDBC);
    }

    @Test
    public void noPaginationWhenFetchSizeZero() throws IOException {
        // fetch_size = 0 , default to non-pagination behaviour for simple queries
        // this can be checked by checking that cursor is not present
        String selectQuery = StringUtils.format("SELECT firstname, state FROM %s", TEST_INDEX_ACCOUNT);
        JSONObject response = new JSONObject(executeFetchQuery(selectQuery, 0, JDBC));
        Assert.assertFalse(response.has("cursor"));
    }

    @Test
    public void validNumberOfPages() throws IOException {
        // the index has 1000 records, with fetch size of 50 we should get 20 pages with no cursor on last page
        String selectQuery = StringUtils.format("SELECT firstname, state FROM %s", TEST_INDEX_ACCOUNT);
        JSONObject response = new JSONObject(executeFetchQuery(selectQuery, 50, JDBC));
        String cursor = response.getString("cursor");
        int pageCount = 1;

        while (!cursor.isEmpty()) { //this condition also checks that there is no cursor on last page
            response = executeCursorQuery(cursor);
            cursor = response.optString("cursor");
            pageCount++;
        }

        assertThat(pageCount, equalTo(20));

        // using random value here, with fetch size of 28 we should get 36 pages (ceil of 1000/28)
        response = new JSONObject(executeFetchQuery(selectQuery, 28, JDBC));
        cursor = response.getString("cursor");
        System.out.println(response);
        pageCount = 1;

        while (!cursor.isEmpty()) {
            response = executeCursorQuery(cursor);
            cursor = response.optString("cursor");
            pageCount++;
        }
        assertThat(pageCount, equalTo(36));

        // verify the scroll context was cleared
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

    //TODOD: add test cases for nested and subqueries after checking both works as part of query coverage test


    @Test
    public void exceptionIfRetrievingResultFromClosedCursor() throws IOException {
        // fetch_size = 1000
        // the index has 13059 records, with fetch size of 1000 we should get 14 pages with no cursor on last page
        // retrieve first few pages, then close the context, and try to re-use the last cursorID
        // check x-pack behaviour and replicate it here

        assertThat(1, equalTo(1));

    }

    @Test
    public void noCursorWhenResultsLessThanFetchSize() throws IOException {
        // fetch_size is 100, but actual number of rows returned from ElasticSearch is 97
        // a scroll context will be opened but will be closed after first page as all records are fetched
        String selectQuery = StringUtils.format(
                "SELECT * FROM %s WHERE balance < 25000 AND age > 36 LIMIT 2000", TEST_INDEX_ACCOUNT
        );
        JSONObject response = new JSONObject(executeFetchQuery(selectQuery, 100, JDBC));
        Assert.assertFalse(response.has("cursor"));
    }



    @Test
    public void defaultBehaviorWhenCursorSettingIsDisabled() throws IOException {
        updateClusterSettings(new ClusterSetting(PERSISTENT, "opendistro.sql.cursor.enabled", "false"));
        String query = StringUtils.format("SELECT firstname, email, state FROM %s", TEST_INDEX_ACCOUNT);
        JSONObject response = new JSONObject(executeFetchQuery(query, 100, JDBC));
        Assert.assertFalse(response.has("cursor"));

        updateClusterSettings(new ClusterSetting(PERSISTENT, "opendistro.sql.cursor.enabled", null));
        query = StringUtils.format("SELECT firstname, email, state FROM %s", TEST_INDEX_ACCOUNT);
        response = new JSONObject(executeFetchQuery(query, 100, JDBC));
        Assert.assertTrue(response.has("cursor"));

        wipeAllClusterSettings();
    }


    @Test
    public void testCursorSettings() throws IOException {
        // Assert default cursor setings
        JSONObject clusterSettings = getAllClusterSettings();
        assertThat(clusterSettings.query("/defaults/opendistro.sql.cursor.enabled"), equalTo("true"));
        assertThat(clusterSettings.query("/defaults/opendistro.sql.cursor.fetch_size"), equalTo("1000"));
        assertThat(clusterSettings.query("/defaults/opendistro.sql.cursor.keep_alive"), equalTo("1m"));

        updateClusterSettings(new ClusterSetting(PERSISTENT, "opendistro.sql.cursor.enabled", "false"));
        updateClusterSettings(new ClusterSetting(TRANSIENT, "opendistro.sql.cursor.fetch_size", "400"));
        updateClusterSettings(new ClusterSetting(PERSISTENT, "opendistro.sql.cursor.keep_alive", "200s"));

        clusterSettings = getAllClusterSettings();
        assertThat(clusterSettings.query("/persistent/opendistro.sql.cursor.enabled"), equalTo("false"));
        assertThat(clusterSettings.query("/transient/opendistro.sql.cursor.fetch_size"), equalTo("400"));
        assertThat(clusterSettings.query("/persistent/opendistro.sql.cursor.keep_alive"), equalTo("200s"));

        wipeAllClusterSettings();
    }


    @Test
    public void testDefaultFetchSize() throws IOException {
        // the default fetch size is 1000
        // using non-nested query here as page will have more rows on flattening
        String query = StringUtils.format("SELECT firstname, email, state FROM %s", TEST_INDEX_ACCOUNT);
        JSONObject response = new JSONObject(executeFetchLessQuery(query, JDBC));
        JSONArray datawRows = response.optJSONArray("datarows");
        assertThat(datawRows.length(), equalTo(1000));

        updateClusterSettings(new ClusterSetting(TRANSIENT, "opendistro.sql.cursor.fetch_size", "786"));
        response = new JSONObject(executeFetchLessQuery(query, JDBC));
        datawRows = response.optJSONArray("datarows");
        assertThat(datawRows.length(), equalTo(786));

        wipeAllClusterSettings();
    }

    @Test
    public void testCursorCloseAPI() throws IOException {
        // multiple invocation of closing cursor should return success
        // fetch page using old cursor should throw error

        assertThat(1, equalTo(1));

    }


    @Test
    public void invalidCursorId() throws IOException {
        // could be either not decodable or scroll context already closed (I guess this is already taken above)

        assertThat(1, equalTo(1));

    }

    @Test
    public void respectLimitPassedInSelectClause() throws IOException {
        // fetch_size = 1000
        // the index has 13059 records, with fetch size of 1000 we should get 14 pages with no cursor on last page
        // query : "SELECT * from accounts LIMIT 9999" --> total results being 250
        // there should be only

        assertThat(1, equalTo(1));

    }


    // ----- Regression test ---------
    // other queries like aggregation and joins should not be effected

    @Test
    public void aggregationJoinQueriesNotAffected() throws IOException {
        // TODO: change test case name
        // aggregation and joins queries are not affected

        assertThat(1, equalTo(1));

    }

    @Test
    public void noPaginationWithNonJDBCFormat() throws IOException {
        // original ES Json, CSV, RAW, etc..

        assertThat(1, equalTo(1));

    }

    // query coverage
    // tests to see what all queries are covered, especially check if subqueries and nested work well with this
    @Test
    public void queryCoverage() throws IOException {
        // original ES Json, CSV, RAW, etc..

        assertThat(1, equalTo(1));

    }

    public void verifyWithAndWithoutPaginationResponse(String sqlQuery, String cursorQuery, int fetch_size) throws IOException {
        // we are only checking here for schema and aatarows
        JSONObject withoutCursorResponse = new JSONObject(executeFetchQuery(sqlQuery, 0, JDBC));

        JSONObject withCursorResponse = new JSONObject("{\"schema\":[],\"datarows\":[]}");
        JSONArray schema = withCursorResponse.getJSONArray("schema");
        JSONArray dataRows = withCursorResponse.getJSONArray("datarows");

        JSONObject tempResponse = new JSONObject(executeFetchQuery(cursorQuery, fetch_size, JDBC));
        tempResponse.optJSONArray("schema").forEach(item -> schema.put(item));
        tempResponse.optJSONArray("datarows").forEach(item -> dataRows.put(item));

        String cursor = tempResponse.getString("cursor");
        while (!cursor.isEmpty()) {
            tempResponse = executeCursorQuery(cursor);
            tempResponse.optJSONArray("datarows").forEach(item -> dataRows.put(item));
            cursor = tempResponse.optString("cursor");
        }

        verifySchema(withoutCursorResponse.optJSONArray("schema"), withCursorResponse.optJSONArray("schema"));
        verifyDataRows(withoutCursorResponse.optJSONArray("datarows"), withCursorResponse.optJSONArray("datarows"));
    }

    public void verifySchema(JSONArray schemaOne, JSONArray schemaTwo) {
        assertTrue(schemaOne.similar(schemaTwo));
    }

    public void verifyDataRows(JSONArray dataRowsOne, JSONArray dataRowsTwo) {
        assertTrue(dataRowsOne.similar(dataRowsTwo));
    }

}



