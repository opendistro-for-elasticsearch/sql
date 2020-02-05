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

import org.json.JSONObject;
import org.junit.Test;

import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.rows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.schema;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifyDataRows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifySchema;

public class TypeInformationIT extends SQLIntegTestCase {

    @Override
    protected void init() throws Exception {
        loadIndex(Index.ACCOUNT);
        loadIndex(Index.ONLINE);
        loadIndex(Index.DATE);
    }

    /*
    numberOperators
     */
    @Test
    public void testAbsWithIntFieldReturnsInt() {
        JSONObject response = executeJdbcRequest("SELECT ABS(age) FROM " + TestsConstants.TEST_INDEX_ACCOUNT +
                " ORDER BY age LIMIT 5");

        verifySchema(response, schema("ABS(age)", null, "long"));
        verifyDataRows(response,
                rows(20),
                rows(20),
                rows(20),
                rows(20),
                rows(20));
    }

    @Test
    public void testCeilWithLongFieldReturnsLong() {
        JSONObject response = executeJdbcRequest("SELECT CEIL(balance) FROM " + TestsConstants.TEST_INDEX_ACCOUNT +
                " ORDER BY balance LIMIT 5");

        verifySchema(response, schema("CEIL(balance)", null, "long"));
        verifyDataRows(response,
                rows(1011),
                rows(1031),
                rows(1110),
                rows(1133),
                rows(1172));
    }

    /*
    mathConstants
     */
    @Test
    public void testPiReturnsDouble() {
        JSONObject response = executeJdbcRequest("SELECT PI() FROM " + TestsConstants.TEST_INDEX_ACCOUNT
                + " LIMIT 1");

        verifySchema(response, schema("PI()", null, "double"));
        verifyDataRows(response,
                rows(3.141592653589793));
    }

    /*
    stringOperators
     */
    @Test
    public void testUpperWithStringFieldReturnsString() {
        JSONObject response = executeJdbcRequest("SELECT UPPER(firstname) AS firstname_alias FROM " +
                TestsConstants.TEST_INDEX_ACCOUNT + " ORDER BY firstname_alias LIMIT 2");

        verifySchema(response, schema("firstname_alias", null, "text"));
        verifyDataRows(response,
                rows("ABBOTT"),
                rows("ABIGAIL"));
    }

    @Test
    public void testLowerWithTextFieldReturnsText() {
        JSONObject response = executeJdbcRequest("SELECT LOWER(firstname) FROM " +
                TestsConstants.TEST_INDEX_ACCOUNT + " ORDER BY firstname LIMIT 2");

        verifySchema(response, schema("LOWER(firstname)", null, "text"));
        verifyDataRows(response,
                rows("abbott"),
                rows("abigail"));
    }

    /*
    stringFunctions
     */
    @Test
    public void testLengthWithTextFieldReturnsInt() {
        JSONObject response = executeJdbcRequest("SELECT length(firstname) FROM " +
                TestsConstants.TEST_INDEX_ACCOUNT + " ORDER BY firstname LIMIT 2");

        verifySchema(response, schema("length(firstname)", null, "integer"));
        verifyDataRows(response,
                rows(6),
                rows(7));
    }

    /*
    trigFunctions
     */
    @Test
    public void testSinWithLongFieldReturnsLong() {
        JSONObject response = executeJdbcRequest("SELECT sin(balance) FROM " +
                TestsConstants.TEST_INDEX_ACCOUNT + " ORDER BY firstname LIMIT 2");

        verifySchema(response, schema("sin(balance)", null, "long"));
        verifyDataRows(response,
                rows(0.544804964572613),
                rows(0.5375391881734781));
    }

    @Test
    public void testRadiansWithLongFieldReturnsLong() {
        JSONObject response = executeJdbcRequest("SELECT radians(balance) FROM " +
                TestsConstants.TEST_INDEX_ACCOUNT + " ORDER BY firstname LIMIT 2");

        verifySchema(response, schema("radians(balance)", null, "long"));
        verifyDataRows(response,
                rows(192.2480171071754),
                rows(235.23547658379573));
    }

    /*
    binaryOperators
     */

    @Test
    public void testAddWithIntReturnsInt() {
        JSONObject response = executeJdbcRequest("SELECT (balance + 5) AS balance_add_five FROM " +
                TestsConstants.TEST_INDEX_ACCOUNT + " ORDER BY firstname LIMIT 2");

        verifySchema(response, schema("balance_add_five", null, "integer"));
        verifyDataRows(response,
                rows(11020),
                rows(13483));
    }

    @Test
    public void testSubtractLongWithLongReturnsLong() {
        JSONObject response = executeJdbcRequest("SELECT (balance - balance) FROM " +
                TestsConstants.TEST_INDEX_ACCOUNT + " ORDER BY firstname LIMIT 2");

        verifySchema(response, schema("subtract(balance, balance)", null, "long"));
        verifyDataRows(response,
                rows(0),
                rows(0));
    }

    /*
    dateFunctions
     */
    @Test
    public void testDayOfWeekWithKeywordReturnsText() {
        JSONObject response = executeJdbcRequest("SELECT DAY_OF_WEEK(insert_time) FROM "
                + TestsConstants.TEST_INDEX_ONLINE + " LIMIT 2");

        verifySchema(response,
                schema("DAY_OF_WEEK(insert_time)", null, "text"));

        verifyDataRows(response,
                rows(7),
                rows(7));
    }

    @Test
    public void testYearWithKeywordReturnsText() {
        JSONObject response = executeJdbcRequest("SELECT YEAR(insert_time) FROM "
                + TestsConstants.TEST_INDEX_ONLINE + " LIMIT 2");

        verifySchema(response, schema("YEAR(insert_time)", null, "text"));

        verifyDataRows(response,
                rows(2014),
                rows(2014));
    }

    private JSONObject executeJdbcRequest(String query) {
        return new JSONObject(executeQuery(query, "jdbc"));
    }

}
