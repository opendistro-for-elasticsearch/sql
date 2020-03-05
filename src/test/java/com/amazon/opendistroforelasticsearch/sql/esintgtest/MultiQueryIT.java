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

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;

public class MultiQueryIT extends SQLIntegTestCase {

    private static String MINUS_SCROLL_DEFAULT_HINT = " /*! MINUS_SCROLL_FETCH_AND_RESULT_LIMITS(1000, 50, 100) */ ";
    private static String MINUS_TERMS_OPTIMIZATION_HINT = " /*! MINUS_USE_TERMS_OPTIMIZATION(true) */ ";

    @Override
    protected void init() throws Exception {
        loadIndex(Index.ACCOUNT);
        loadIndex(Index.DOG);
        loadIndex(Index.GAME_OF_THRONES);
        loadIndex(Index.SYSTEM);
    }

    @Test
    public void unionAllSameRequestOnlyOneRecordTwice() throws IOException {
        String query = String.format("SELECT firstname " +
                                     "FROM %s " +
                                     "WHERE firstname = 'Amber' " +
                                     "LIMIT 1 " +
                                     "UNION ALL " +
                                     "SELECT firstname " +
                                     "FROM %s " +
                                     "WHERE firstname = 'Amber'",
                TestsConstants.TEST_INDEX_ACCOUNT, TestsConstants.TEST_INDEX_ACCOUNT);

        JSONObject response = executeQuery(query);
        JSONArray hits = getHits(response);

        assertThat(getHits(response).length(), equalTo(2));
        for (int i = 0; i < hits.length(); i++) {
            JSONObject hit = hits.getJSONObject(i);
            JSONObject source = getSource(hit);

            assertThat(source.getString("firstname"), equalTo("Amber"));
        }
    }

    @Test
    public void unionAllOnlyOneRecordEachWithAlias() throws IOException {
        String query = String.format("SELECT firstname FROM %s WHERE firstname = 'Amber' " +
                                     "UNION ALL " +
                                     "SELECT dog_name as firstname FROM %s WHERE dog_name = 'rex'",
                TestsConstants.TEST_INDEX_ACCOUNT, TestsConstants.TEST_INDEX_DOG);

        JSONObject response = executeQuery(query);
        assertThat(getHits(response).length(), equalTo(2));

        Set<String> names = new HashSet<>();
        JSONArray hits = getHits(response);
        for (int i = 0; i < hits.length(); i++) {
            JSONObject hit = hits.getJSONObject(i);
            JSONObject source = getSource(hit);

            names.add(source.getString("firstname"));
        }

        assertThat(names, hasItems("Amber", "rex"));
    }

    @Test
    public void unionAllOnlyOneRecordEachWithComplexAlias() throws IOException {
        String query = String.format("SELECT firstname FROM %s WHERE firstname = 'Amber' " +
                                     "UNION ALL " +
                                     "SELECT name.firstname as firstname " +
                                     "FROM %s " +
                                     "WHERE name.firstname = 'daenerys'",
                TestsConstants.TEST_INDEX_ACCOUNT, TestsConstants.TEST_INDEX_GAME_OF_THRONES);

        JSONObject response = executeQuery(query);
        assertThat(getHits(response).length(), equalTo(2));

        Set<String> names = new HashSet<>();
        JSONArray hits = getHits(response);
        for (int i = 0; i < hits.length(); i++) {
            JSONObject hit = hits.getJSONObject(i);
            JSONObject source = getSource(hit);

            names.add(source.getString("firstname"));
        }

        assertThat(names, hasItems("Amber", "Daenerys"));
    }

    @Test
    public void minusAMinusANoAlias() throws IOException {
        innerMinusAMinusANoAlias("");
    }

    @Test
    public void minusAMinusANoAliasWithScrolling() throws IOException {
        innerMinusAMinusANoAlias(MINUS_SCROLL_DEFAULT_HINT);
    }

    @Test
    public void minusAMinusANoAliasWithScrollingAndTerms() throws IOException {
        innerMinusAMinusANoAlias(MINUS_SCROLL_DEFAULT_HINT + MINUS_TERMS_OPTIMIZATION_HINT);
    }

    @Test
    public void minusAMinusBNoAlias() throws IOException {
        innerMinusAMinusBNoAlias("");
    }

    @Test
    public void minusAMinusBNoAliasWithScrolling() throws IOException {
        innerMinusAMinusBNoAlias(MINUS_SCROLL_DEFAULT_HINT);
    }

    @Test
    public void minusAMinusBNoAliasWithScrollingAndTerms() throws IOException {
        innerMinusAMinusBNoAlias(MINUS_SCROLL_DEFAULT_HINT + MINUS_TERMS_OPTIMIZATION_HINT);
    }

    @Test
    public void minusCMinusDTwoFieldsNoAlias() throws IOException {
        innerMinusCMinusDTwoFieldsNoAlias("");
    }

    @Test
    public void minusCMinusDTwoFieldsNoAliasWithScrolling() throws IOException {
        innerMinusCMinusDTwoFieldsNoAlias(MINUS_SCROLL_DEFAULT_HINT);
    }

    @Test
    public void minusCMinusDTwoFieldsAliasOnBothSecondTableFields() throws IOException {
        String query = String.format("SELECT pk, letter FROM %s WHERE system_name = 'C' " +
                                     "MINUS " +
                                     "SELECT myId as pk, myLetter as letter FROM %s WHERE system_name = 'E'",
                TestsConstants.TEST_INDEX_SYSTEM, TestsConstants.TEST_INDEX_SYSTEM);

        JSONObject response = executeQuery(query);
        assertThat(getHits(response).length(), equalTo(1));

        JSONObject hit = getHits(response).getJSONObject(0);
        JSONObject source = getSource(hit);

        assertThat(source.length(), equalTo(2));
        Assert.assertTrue("Source expected to contain 'pk' field", source.has("pk"));
        Assert.assertTrue("Source expected to contain 'letter' field", source.has("letter"));
        assertThat(source.getInt("pk"), equalTo(1));
        assertThat(source.getString("letter"), equalTo("e"));
    }

    @Test
    public void minusCMinusDTwoFieldsAliasOnBothTables() throws IOException {
        innerMinusCMinusDTwoFieldsAliasOnBothTables("");
    }

    @Test
    public void minusCMinusDTwoFieldsAliasOnBothTablesWithScrolling() throws IOException {
        innerMinusCMinusDTwoFieldsAliasOnBothTables(MINUS_SCROLL_DEFAULT_HINT);
    }

    @Test
    public void minusCMinusCTwoFieldsOneAlias() throws IOException {
        String query = String.format("SELECT pk as myId, letter FROM %s WHERE system_name = 'C' " +
                                     "MINUS " +
                                     "SELECT pk as myId, letter FROM %s WHERE system_name = 'C'",
                TestsConstants.TEST_INDEX_SYSTEM, TestsConstants.TEST_INDEX_SYSTEM);

        JSONObject response = executeQuery(query);
        assertThat(getHits(response).length(), equalTo(0));
    }

    @Test
    public void minusCMinusTNonExistentFieldTwoFields() throws IOException {
        String query = String.format("SELECT pk, letter FROM %s WHERE system_name = 'C' " +
                                     "MINUS " +
                                     "SELECT pk, letter FROM %s WHERE system_name = 'T' ",
                TestsConstants.TEST_INDEX_SYSTEM, TestsConstants.TEST_INDEX_SYSTEM);

        JSONObject response = executeQuery(query);
        assertThat(getHits(response).length(), equalTo(3));
    }

    @Test
    public void minusCMinusTNonExistentFieldOneField() throws IOException {
        innerMinusCMinusTNonExistentFieldOneField("");
    }

    @Test
    public void minusCMinusTNonExistentOneFieldWithScrolling() throws IOException {
        innerMinusCMinusTNonExistentFieldOneField(MINUS_SCROLL_DEFAULT_HINT);
    }

    @Test
    public void minusCMinusTNonExistentFieldOneFieldWithScrollingAndOptimization() throws IOException {
        innerMinusCMinusTNonExistentFieldOneField(MINUS_SCROLL_DEFAULT_HINT + MINUS_TERMS_OPTIMIZATION_HINT);
    }

    @Test
    public void minusTMinusCNonExistentFieldFirstQuery() throws IOException {
        innerMinusTMinusCNonExistentFieldFirstQuery("");
    }

    @Test
    public void minusTMinusCNonExistentFieldFirstQueryWithScrolling() throws IOException {
        innerMinusTMinusCNonExistentFieldFirstQuery(MINUS_SCROLL_DEFAULT_HINT);
    }

    @Test
    public void minusTMinusCNonExistentFieldFirstQueryWithScrollingAndOptimization() throws IOException {
        innerMinusTMinusCNonExistentFieldFirstQuery(MINUS_SCROLL_DEFAULT_HINT + MINUS_TERMS_OPTIMIZATION_HINT);
    }

    private void innerMinusAMinusANoAlias(String hint) throws IOException {
        String query = String.format("SELECT %s pk FROM %s WHERE system_name = 'A' " +
                                     "MINUS " +
                                     "SELECT pk FROM %s WHERE system_name = 'A'",
                hint, TestsConstants.TEST_INDEX_SYSTEM, TestsConstants.TEST_INDEX_SYSTEM);

        JSONObject response = executeQuery(query);
        assertThat(getHits(response).length(), equalTo(0));
    }

    private void innerMinusAMinusBNoAlias(String hint) throws IOException {
        String query = String.format("SELECT %s pk FROM %s WHERE system_name = 'A' " +
                                     "MINUS " +
                                     "SELECT pk FROM %s WHERE system_name = 'B'",
                hint, TestsConstants.TEST_INDEX_SYSTEM, TestsConstants.TEST_INDEX_SYSTEM);

        JSONObject response = executeQuery(query);
        assertThat(getHits(response).length(), equalTo(1));

        JSONObject hit = getHits(response).getJSONObject(0);
        JSONObject source = getSource(hit);
        assertThat(source.length(), equalTo(1));
        Assert.assertTrue("Source expected to contain 'pk' field", source.has("pk"));
        assertThat(source.getInt("pk"), equalTo(3));
    }

    private void innerMinusCMinusDTwoFieldsNoAlias(String hint) throws IOException {
        String query = String.format("SELECT %s pk, letter FROM %s WHERE system_name = 'C' " +
                                     "MINUS " +
                                     "SELECT pk, letter FROM %s WHERE system_name = 'D'",
                hint, TestsConstants.TEST_INDEX_SYSTEM, TestsConstants.TEST_INDEX_SYSTEM);

        JSONObject response = executeQuery(query);
        assertThat(getHits(response).length(), equalTo(1));

        JSONObject hit = getHits(response).getJSONObject(0);
        JSONObject source = getSource(hit);

        assertThat(source.length(), equalTo(2));
        Assert.assertTrue("Source expected to contain 'pk' field", source.has("pk"));
        Assert.assertTrue("Source expected to contain 'letter' field", source.has("letter"));
        assertThat(source.getInt("pk"), equalTo(1));
        assertThat(source.getString("letter"), equalTo("e"));
    }

    private void innerMinusCMinusDTwoFieldsAliasOnBothTables(String hint) throws IOException  {
        String query = String.format("SELECT %s pk as myId, letter FROM %s WHERE system_name = 'C' " +
                                     "MINUS " +
                                     "SELECT myId, myLetter as letter FROM %s WHERE system_name = 'E'",
                hint, TestsConstants.TEST_INDEX_SYSTEM, TestsConstants.TEST_INDEX_SYSTEM);

        JSONObject response = executeQuery(query);
        assertThat(getHits(response).length(), equalTo(1));

        JSONObject hit = getHits(response).getJSONObject(0);
        JSONObject source = getSource(hit);

        assertThat(source.length(), equalTo(2));
        Assert.assertTrue("Source expected to contain 'pk' field as 'myId'", source.has("myId"));
        Assert.assertTrue("Source expected to contain 'letter' field", source.has("letter"));
        assertThat(source.getInt("myId"), equalTo(1));
        assertThat(source.getString("letter"), equalTo("e"));
    }

    private void innerMinusCMinusTNonExistentFieldOneField(String hint) throws IOException {
        String query = String.format("SELECT %s letter FROM %s WHERE system_name = 'C' " +
                                     "MINUS " +
                                     "SELECT letter FROM %s WHERE system_name = 'T'",
                hint, TestsConstants.TEST_INDEX_SYSTEM, TestsConstants.TEST_INDEX_SYSTEM);

        JSONObject response = executeQuery(query);
        assertThat(getHits(response).length(), equalTo(3));
    }

    private void innerMinusTMinusCNonExistentFieldFirstQuery(String hint) throws IOException {
        String query = String.format("SELECT %s letter FROM %s WHERE system_name = 'T' " +
                                     "MINUS " +
                                     "SELECT letter FROM %s WHERE system_name = 'C'",
                hint, TestsConstants.TEST_INDEX_SYSTEM, TestsConstants.TEST_INDEX_SYSTEM);

        JSONObject response = executeQuery(query);
        assertThat(getHits(response).length(), equalTo(0));
    }
}
