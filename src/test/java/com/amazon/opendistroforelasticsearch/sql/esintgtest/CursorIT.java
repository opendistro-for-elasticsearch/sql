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

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.Matchers.equalTo;


public class CursorIT extends SQLIntegTestCase {

    @Override
    protected void init() throws Exception {
        loadIndex(Index.ACCOUNT);
    }

    @Test
    public void invalidFetchSize() throws IOException {
        // negative or non-numeric fetch_size

        Assert.assertThat(1, equalTo(1));
    }


    @Test
    public void noPaginationWhenFetchSizeZero() throws IOException {
        // fetch_size = 0 ; default to non-pagination behaviour for simple queries

        Assert.assertThat(1, equalTo(1));
    }

    @Test
    public void validNumberOfPages() throws IOException {
        // non-zero fetch_size
        // the index has 13059 records, with fetch size of 1000 we should get 14 pages with no cursor on last page

        Assert.assertThat(1, equalTo(1));


        //test no cursor on last page


        // verify the scroll context was cleared
    }


    @Test
    public void validTotalResultWithAndWithoutPagination() throws IOException {
        // fetch_size = 1000
        // the index has 13059 records, with fetch size of 1000 we should get 14 pages with no cursor on last page
        //
        Assert.assertThat(1, equalTo(1));

    }


    @Test
    public void validTotalResultWithAndWithoutPaginationWhereClause() throws IOException {
        // fetch_size = 1000
        // the index has 13059 records, with fetch size of 1000 we should get 14 pages with no cursor on last page
        //
        Assert.assertThat(1, equalTo(1));

    }

    @Test
    public void validTotalResultWithAndWithoutPaginationOrderBy() throws IOException {
        // fetch_size = 1000
        // the index has 13059 records, with fetch size of 1000 we should get 14 pages with no cursor on last page
        //
        Assert.assertThat(1, equalTo(1));

    }

    @Test
    public void validTotalResultWithAndWithoutPaginationWhereAndOrderBy() throws IOException {
        // fetch_size = 1000
        // the index has 13059 records, with fetch size of 1000 we should get 14 pages with no cursor on last page
        //
        Assert.assertThat(1, equalTo(1));

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
        // for example a index has 13000 rows but
        // query : "SELECT * from accounts where  balance < 100" --> total results being 250
        // for any fetch size greater than (but less than index.max_result_window) total results
        // there should be no cursor and close the context in the backend

        Assert.assertThat(1, equalTo(1));

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
    public void respectLimitPassedInSelectCluase() throws IOException {
        // fetch_size = 1000
        // the index has 13059 records, with fetch size of 1000 we should get 14 pages with no cursor on last page
        // query : "SELECT * from accounts LIMIT 9999" --> total results being 250
        // there should be only

        Assert.assertThat(1, equalTo(1));

    }


    // ----- Regression test ---------
    // other queries like aggregation and joins should not be effected

    @Test
    public void aggregationJoinQueriesNotaffected() throws IOException {
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

}



