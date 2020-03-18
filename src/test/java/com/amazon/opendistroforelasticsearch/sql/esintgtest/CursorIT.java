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
import org.elasticsearch.client.ResponseException;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;


import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.everyItem;
import static org.junit.Assert.assertThat;


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
        // invalid fetch_size --> negative(-2), non-numeric("hello)
        // acceptable fetch_size --> positive numbers, even in string form "532.4"
//        exception.expect(ResponseException.class);
//        String selectQuery = StringUtils.format("SELECT firstname, state FROM %s", TestsConstants.TEST_INDEX_ACCOUNT);
//        String response = executeFetchQuery(selectQuery, -2, JDBC);
//        assertThat(response.query("/status"), equalTo(400));
//        assertThat(response.query("/error/details"), equalTo("Fetch_size must be greater or equal to 0"));
    }

    @Test
    public void noPaginationWhenFetchSizeZero() throws IOException {
        // fetch_size = 0 , default to non-pagination behaviour for simple queries
        // this can be checked by checking that cursor is not present
        String selectQuery = StringUtils.format("SELECT firstname, state FROM %s", TestsConstants.TEST_INDEX_ACCOUNT);
        JSONObject response = new JSONObject(executeFetchQuery(selectQuery, 0, JDBC));
        Assert.assertFalse(response.has("cursor"));
    }

    @Test
    public void validNumberOfPages() throws IOException {
        // the index has 1000 records, with fetch size of 50 we should get 20 pages with no cursor on last page
        String selectQuery = StringUtils.format("SELECT firstname, state FROM %s", TestsConstants.TEST_INDEX_ACCOUNT);
        JSONObject response = new JSONObject(executeFetchQuery(selectQuery, 50, JDBC));
        String cursor = response.getString("cursor");
        int pageCount = 1;

        while (!cursor.isEmpty()) { //this condition also checks that there is no cursor on last page
            response = executeCursorQuery(cursor);
            cursor = response.optString("cursor");
            pageCount++;
        }

        Assert.assertThat(pageCount, equalTo(20));

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
        Assert.assertThat(pageCount, equalTo(36));

        // verify the scroll context was cleared
    }


    @Test
    public void validTotalResultWithAndWithoutPagination() throws IOException {
        // simple query - accounts index has 1000 docs, using higher limit to get all docs
        String selectQuery = StringUtils.format(
                "SELECT firstname, state FROM %s ",
                TestsConstants.TEST_INDEX_ACCOUNT
        );
        verifyWithAndWithoutPaginationResponse(selectQuery + " LIMIT 2000" , selectQuery , 80);
    }

    @Test
    public void validTotalResultWithAndWithoutPaginationWhereClause() throws IOException {
        String selectQuery = StringUtils.format(
                "SELECT firstname, state FROM %s WHERE balance < 25000 AND age > 32",
                TestsConstants.TEST_INDEX_ACCOUNT
        );
        verifyWithAndWithoutPaginationResponse(selectQuery + " LIMIT 2000" , selectQuery , 17);

    }

    @Test
    public void validTotalResultWithAndWithoutPaginationOrderBy() throws IOException {
        String selectQuery = StringUtils.format(
                "SELECT firstname, state FROM %s ORDER BY balance DESC ",
                TestsConstants.TEST_INDEX_ACCOUNT
        );
        verifyWithAndWithoutPaginationResponse(selectQuery + " LIMIT 2000" , selectQuery , 26);

    }

    @Test
    public void validTotalResultWithAndWithoutPaginationWhereAndOrderBy() throws IOException {
        String selectQuery = StringUtils.format(
                "SELECT firstname, state FROM %s WHERE balance < 25000 ORDER BY balance ASC ",
                TestsConstants.TEST_INDEX_ACCOUNT
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

        Assert.assertThat(1, equalTo(1));



    }

    @Test
    public void noCursorWhenResultsLessThanFetchSize() throws IOException {
        // fetch_size is 100, but actual number of rows returned from ElasticSearch is 97
        String selectQuery = StringUtils.format(
                "SELECT * FROM %s WHERE balance < 25000 AND age > 36 LIMIT 2000",
                TestsConstants.TEST_INDEX_ACCOUNT);
        JSONObject response = new JSONObject(executeFetchQuery(selectQuery, 100, JDBC));
        Assert.assertFalse(response.has("cursor"));
    }



    @Test
    public void defaultBehaviorWhenCursorSettingIsDisabled() throws IOException {
        // for example a index has 13000 rows but
        // query : "SELECT * from accounts where  balance < 100" --> total results being 250
        // for any fetch size greater than (but less than index.max_result_window) total results
        // there should be no cursor and close the context in the backend

        Assert.assertThat(1, equalTo(1));

    }


    @Test
    public void testCursorSettings() throws IOException {
        // default fetch size 1000 , the max effective fetch_size is limited by max_result_window
        // default scroll context time : should it be 1m? , only where we opening scroll contexts
        // enable/disable cursor for all query


        Assert.assertThat(1, equalTo(1));

    }


    @Test
    public void testDefaultFetchSize() throws IOException {
        // the default fetch size
        Assert.assertThat(1, equalTo(1));

    }

    @Test
    public void testCursorCloseAPI() throws IOException {
        // multiple invocation of closing cursor should return success
        // fetch page using old cursor should throw error

        Assert.assertThat(1, equalTo(1));

    }


    @Test
    public void invalidCursorId() throws IOException {
        // could be either not decodable or scroll context already closed (I guess this is already taken above)

        Assert.assertThat(1, equalTo(1));

    }

    @Test
    public void respectLimitPassedInSelectClause() throws IOException {
        // fetch_size = 1000
        // the index has 13059 records, with fetch size of 1000 we should get 14 pages with no cursor on last page
        // query : "SELECT * from accounts LIMIT 9999" --> total results being 250
        // there should be only

        Assert.assertThat(1, equalTo(1));

    }


    // ----- Regression test ---------
    // other queries like aggregation and joins should not be effected

    @Test
    public void aggregationJoinQueriesNotAffected() throws IOException {
        // TODO: change test case name
        // aggregation and joins queries are not affected

        Assert.assertThat(1, equalTo(1));

    }

    @Test
    public void noPaginationWithNonJDBCFormat() throws IOException {
        // original ES Json, CSV, RAW, etc..

        Assert.assertThat(1, equalTo(1));

    }

    // query coverage
    // tests to see what all queries are covered, especially check if subqueries and nested work well with this
    @Test
    public void queryCoverage() throws IOException {
        // original ES Json, CSV, RAW, etc..

        Assert.assertThat(1, equalTo(1));

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



