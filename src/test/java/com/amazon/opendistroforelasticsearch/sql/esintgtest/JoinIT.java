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

import com.google.common.collect.ImmutableMap;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_ACCOUNT;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_DOG;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_DOG2;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_GAME_OF_THRONES;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_LOCATION;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_LOCATION2;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_PEOPLE;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_PEOPLE2;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class JoinIT extends SQLIntegTestCase {

    private static final String USE_NL_HINT = " /*! USE_NL*/";

    @Override
    protected void init() throws Exception {

        loadIndex(Index.DOG);
        loadIndex(Index.DOGS2);
        loadIndex(Index.PEOPLE);
        loadIndex(Index.PEOPLE2);
        loadIndex(Index.GAME_OF_THRONES);
        loadIndex(Index.LOCATION);
        loadIndex(Index.LOCATION_TWO);
        loadIndex(Index.ACCOUNT);
    }

    @Test
    public void joinParseCheckSelectedFieldsSplitHASH() throws IOException {
        joinParseCheckSelectedFieldsSplit(false);
    }

    @Test
    public void joinParseCheckSelectedFieldsSplitNL() throws IOException {
        joinParseCheckSelectedFieldsSplit(true);
    }

    @Test
    public void joinParseWithHintsCheckSelectedFieldsSplitHASH() throws IOException {

        String query = String.format(Locale.ROOT, "SELECT /*! HASH_WITH_TERMS_FILTER*/ " +
                "a.firstname ,a.lastname, a.gender ,d.dog_name FROM %s a JOIN %s d " +
                "ON d.holdersName = a.firstname WHERE (a.age > 10 OR a.balance > 2000) AND d.age > 1",
                TEST_INDEX_PEOPLE, TEST_INDEX_DOG);

        JSONObject result = executeQuery(query);
        verifyJoinParseCheckSelectedFieldsSplitResult(result, false);

        String explanation = explainQuery(query);
        Assert.assertThat(explanation, containsString("holdersName"));

        // TODO: figure out why explain does not show results from first query in term filter and
        //       fix either the test or the code.
        //Arrays.asList("daenerys","nanette","virginia","aurelia","mcgee","hattie","elinor","burton").forEach(name -> {
        //    Assert.assertThat(explanation, containsString(name));
        //});
    }

    @Test
    public void joinWithNoWhereButWithConditionHash() throws IOException {

        joinWithNoWhereButWithCondition(false);
    }

    @Test
    public void joinWithNoWhereButWithConditionNL() throws IOException {

        joinWithNoWhereButWithCondition(true);
    }

    @Test
    public void joinWithStarHASH() throws IOException {

        String query = String.format(Locale.ROOT, "SELECT * FROM %1$s c " +
                "JOIN %1$s h ON h.hname = c.house ", TEST_INDEX_GAME_OF_THRONES);

        JSONObject result = executeQuery(query);
        JSONArray hits = getHits(result);

        Assert.assertEquals(4, hits.length());
        String house = (String)hits.query("/0/_source/c.house");
        Assert.assertThat(house, anyOf(equalTo("Targaryen"), equalTo("Stark"), equalTo("Lannister")));
        String houseName = (String)hits.query("/0/_source/h.hname");
        Assert.assertEquals(house, houseName);
    }

    @Test
    public void joinNoConditionButWithWhereHASH() throws IOException {

        joinNoConditionButWithWhere(false);
    }

    @Test
    public void joinNoConditionButWithWhereNL() throws IOException {

        joinNoConditionButWithWhere(true);
    }

    @Test
    public void joinNoConditionAndNoWhereHASH() throws IOException {

        joinNoConditionAndNoWhere(false);
    }

    @Test
    public void joinNoConditionAndNoWhereNL() throws IOException {

        joinNoConditionAndNoWhere(true);
    }

    @Test
    public void joinNoConditionAndNoWhereWithTotalLimitHASH() throws IOException {

        joinNoConditionAndNoWhereWithTotalLimit(false);
    }

    @Test
    public void joinNoConditionAndNoWhereWithTotalLimitNL() throws IOException {

        joinNoConditionAndNoWhereWithTotalLimit(true);
    }

    @Test
    public void joinWithNestedFieldsOnReturnHASH() throws IOException {

        joinWithNestedFieldsOnReturn(false);
    }

    @Test
    public void joinWithNestedFieldsOnReturnNL() throws IOException {

        joinWithNestedFieldsOnReturn(true);
    }

    @Test
    public void joinWithAllAliasOnReturnHASH() throws IOException {

        joinWithAllAliasOnReturn(false);
    }
    @Test
    public void joinWithAllAliasOnReturnNL() throws IOException {

        joinWithAllAliasOnReturn(true);
    }

    @Test
    public void joinWithSomeAliasOnReturnHASH() throws IOException {

        joinWithSomeAliasOnReturn(false);
    }

    @Test
    public void joinWithSomeAliasOnReturnNL() throws IOException {

        joinWithSomeAliasOnReturn(true);
    }

    @Test
    public void joinWithNestedFieldsOnComparisonAndOnReturnHASH() throws IOException {

        joinWithNestedFieldsOnComparisonAndOnReturn(false);
    }

    @Test
    public void joinWithNestedFieldsOnComparisonAndOnReturnNL() throws IOException {

        joinWithNestedFieldsOnComparisonAndOnReturn(true);
    }

    @Test
    public void testLeftJoinHASH() throws IOException {

        testLeftJoin(false);
    }

    @Test
    public void testLeftJoinNL() throws IOException {

        testLeftJoin(true);
    }

    @Test
    public void hintLimits_firstLimitSecondNullHASH() throws IOException {

        hintLimits_firstLimitSecondNull(false);
    }

    @Test
    public void hintLimits_firstLimitSecondNullNL() throws IOException {

        hintLimits_firstLimitSecondNull(true);
    }

    @Test
    public void hintLimits_firstLimitSecondLimitHASH() throws IOException {

        hintLimits_firstLimitSecondLimit(false);
    }

    @Test
    public void hintLimits_firstLimitSecondLimitNL() throws IOException {

        hintLimits_firstLimitSecondLimit(true);
    }

    @Ignore("Join limit hint is deprecated and easily broken due to limit on unsorted records")
    @Test
    public void hintLimits_firstLimitSecondLimitOnlyOneNL() throws IOException {

        hintLimits_firstLimitSecondLimitOnlyOne(true);
    }

    @Ignore("Join limit hint is deprecated and easily broken due to limit on unsorted records")
    @Test
    public void hintLimits_firstLimitSecondLimitOnlyOneHASH() throws IOException {

        hintLimits_firstLimitSecondLimitOnlyOne(false);
    }

    @Test
    public void hintLimits_firstNullSecondLimitHASH() throws IOException {

        hintLimits_firstNullSecondLimit(false);
    }

    @Test
    public void hintLimits_firstNullSecondLimitNL() throws IOException {

        hintLimits_firstNullSecondLimit(true);
    }

    @Test
    public void testLeftJoinWithLimitHASH() throws IOException {

        testLeftJoinWithLimit(false);
    }

    @Test
    public void testLeftJoinWithLimitNL() throws IOException {

        testLeftJoinWithLimit(true);
    }

    @Test
    public void hintMultiSearchCanRunFewTimesNL() throws IOException {

        String query = String.format(Locale.ROOT, "SELECT /*! USE_NL*/ /*! NL_MULTISEARCH_SIZE(2)*/ " +
                "c.name.firstname,c.parents.father,h.hname,h.words FROM %1$s c " +
                "JOIN %1$s h", TEST_INDEX_GAME_OF_THRONES);

        JSONObject result = executeQuery(query);
        JSONArray hits = getHits(result);
        Assert.assertThat(hits.length(), equalTo(42));
    }

    @Test
    public void joinWithGeoIntersectNL() throws IOException {

        String query = String.format(Locale.ROOT, "SELECT p1.description,p2.description " +
                "FROM %s p1 JOIN %s p2 ON GEO_INTERSECTS(p2.place,p1.place)",
                TEST_INDEX_LOCATION, TEST_INDEX_LOCATION2);

        JSONObject result = executeQuery(query);
        JSONArray hits = getHits(result);
        Assert.assertThat(hits.length(), equalTo(2));

        Assert.assertThat(hits.query("/0/_source/p2.description"), equalTo("squareRelated"));
        Assert.assertThat(hits.query("/1/_source/p2.description"), equalTo("squareRelated"));
    }

    // TODO: resolve issue #25 in github and update the test if needed
    // TODO: this test causes the in-memory node to fail/crash
    @Ignore
    @Test
    public void joinWithInQuery() throws IOException {

        //TODO: Either change the ON condition field to keyword or create a different subquery
        String query = String.format(Locale.ROOT, "SELECT c.gender,c.name.firstname,h.hname,h.words " +
                "FROM %1$s c JOIN %1$s h ON h.hname = c.house " +
                "WHERE c.name.firstname IN (SELECT holdersName FROM %2$s)",
                TEST_INDEX_GAME_OF_THRONES, TEST_INDEX_DOG);

        JSONObject result = executeQuery(query);
        JSONArray hits = getHits(result);
        Assert.assertThat(hits.length(), equalTo(1));
        Assert.assertThat(hits.query("/0/_source/c.name.firstname"), equalTo("Daenerys"));
    }

    @Test
    public void joinWithOrHASH() throws IOException {

        joinWithOr(false);
    }

    @Test
    public void joinWithOrNL() throws IOException {

        joinWithOr(true);
    }

    @Ignore // TODO: explanation does not have the terms section
    @Test
    public void joinWithOrWithTermsFilterOpt() throws IOException {

        String query = String.format(Locale.ROOT, "SELECT /*! HASH_WITH_TERMS_FILTER*/ " +
                        "d.dog_name,c.name.firstname FROM %s c " +
                        "JOIN %s d ON d.holdersName = c.name.firstname OR d.age = c.name.ofHisName",
                TEST_INDEX_GAME_OF_THRONES, TEST_INDEX_DOG);

        executeQuery(query);
        String explanation = explainQuery(query);

        Assert.assertTrue(containsTerm(explanation, "holdersName"));
        Assert.assertTrue(containsTerm(explanation, "age"));

        Arrays.asList("daenerys", "brandon", "eddard", "jaime").forEach(
            name -> Assert.assertTrue(explanation.contains(name))
        );
    }

    @Test
    public void joinWithOrderbyFirstTableHASH() throws IOException {

        joinWithOrderFirstTable(false);
    }

    @Test
    public void joinWithOrderbyFirstTableNL() throws IOException {

        joinWithOrderFirstTable(true);
    }

    @Test
    public void joinWithAllFromSecondTableHASH() throws IOException {

        joinWithAllFromSecondTable(false);
    }

    @Test
    public void joinWithAllFromSecondTableNL() throws IOException {

        joinWithAllFromSecondTable(true);
    }

    @Test
    public void joinWithAllFromFirstTableHASH() throws IOException {

        joinWithAllFromFirstTable(false);
    }

    @Test
    public void joinWithAllFromFirstTableNL() throws IOException {

        joinWithAllFromFirstTable(true);
    }

    @Test
    public void leftJoinWithAllFromSecondTableHASH() throws IOException {

        leftJoinWithAllFromSecondTable(false);
    }

    @Test
    public void leftJoinWithAllFromSecondTableNL() throws IOException {

        leftJoinWithAllFromSecondTable(true);
    }

    @Test
    public void joinParseCheckSelectedFieldsSplitNLConditionOrderEQ() throws IOException {

        final String query = String.format(Locale.ROOT, "SELECT /*! USE_NL*/ " +
                "a.firstname, a.lastname, a.gender, d.dog_name FROM %s a JOIN %s d " +
                "ON a.firstname = d.holdersName WHERE (a.age > 10 OR a.balance > 2000) AND d.age > 1",
                TEST_INDEX_PEOPLE2, TEST_INDEX_DOG2);

        JSONObject result = executeQuery(query);
        JSONArray hits = getHits(result);

        Assert.assertThat(hits.length(), equalTo(2));

        Map<String, String> match1 = ImmutableMap.of(
                "a.firstname", "Daenerys",
                "a.lastname", "Targaryen",
                "a.gender", "M",
                "d.dog_name", "rex");
        Map<String, String> match2 = ImmutableMap.of(
                "a.firstname", "Hattie",
                "a.lastname", "Bond",
                "a.gender", "M",
                "d.dog_name", "snoopy");

        Assert.assertTrue(hitsInclude(hits, match1));
        Assert.assertTrue(hitsInclude(hits, match2));
    }

    @Test
    public void joinParseCheckSelectedFieldsSplitNLConditionOrderGT() throws IOException {

        final String query = String.format(Locale.ROOT, "SELECT /*! USE_NL*/ " +
                "a.firstname, a.lastname, a.gender, d.firstname, d.age  FROM " +
                "%s a JOIN %s d on a.age < d.age " +
                "WHERE (d.firstname = 'Lynn' OR d.firstname = 'Obrien') AND a.firstname = 'Mcgee'",
                TEST_INDEX_PEOPLE, TEST_INDEX_ACCOUNT);

        JSONObject result = executeQuery(query);
        JSONArray hits = getHits(result);

        Assert.assertThat(hits.length(), equalTo(2));

        Map<String, ?> oneMatch = ImmutableMap.of("a.firstname", "Mcgee", "a.lastname", "Mooney",
                "a.gender", "M", "d.firstname", "Obrien", "d.age", 40);
        Map<String, ?> secondMatch = ImmutableMap.of("a.firstname", "Mcgee", "a.lastname", "Mooney",
                "a.gender", "M", "d.firstname", "Lynn", "d.age", 40);

        Assert.assertTrue(hitsInclude(hits, oneMatch));
        Assert.assertTrue(hitsInclude(hits, secondMatch));
    }

    @Test
    public void joinParseCheckSelectedFieldsSplitNLConditionOrderLT() throws IOException {

        final String query = String.format(Locale.ROOT, "SELECT /*! USE_NL*/ " +
                "a.firstname, a.lastname, a.gender, d.firstname, d.age  FROM " +
                "%s a JOIN %s d on a.age > d.age " +
                "WHERE (d.firstname = 'Sandoval' OR d.firstname = 'Hewitt') AND a.firstname = 'Fulton'",
                TEST_INDEX_PEOPLE, TEST_INDEX_ACCOUNT);

        JSONObject result = executeQuery(query);
        JSONArray hits = getHits(result);

        Assert.assertThat(hits.length(), equalTo(2));

        Map<String, ?> oneMatch = ImmutableMap.of("a.firstname", "Fulton", "a.lastname", "Holt",
                "a.gender", "F", "d.firstname", "Sandoval", "d.age", 22);
        Map<String, ?> secondMatch = ImmutableMap.of("a.firstname", "Fulton", "a.lastname", "Holt",
                "a.gender", "F", "d.firstname", "Hewitt", "d.age", 22);

        Assert.assertTrue(hitsInclude(hits, oneMatch));
        Assert.assertTrue(hitsInclude(hits, secondMatch));
    }

    @Test
    public void leftJoinNLWithNullInCondition() throws IOException {

        joinWithNullInCondition(true, "LEFT", "OR", "OR", 7);
    }

    @Test
    public void leftJoinNLWithNullInCondition1() throws IOException {

        joinWithNullInCondition(true, "LEFT", "OR", "AND", 7);
    }

    @Test
    public void leftJoinNLWithNullInCondition2() throws IOException {

        joinWithNullInCondition(true, "LEFT", "AND", "AND", 7);
    }

    @Test
    public void leftJoinNLWithNullInCondition3() throws IOException {

        joinWithNullInCondition(true, "LEFT", "AND", "OR", 7);
    }

    @Test
    public void innerJoinNLWithNullInCondition() throws IOException {

        joinWithNullInCondition(true, "", "OR", "OR", 0 );
    }

    @Test
    public void innerJoinNLWithNullInCondition1() throws IOException {

        joinWithNullInCondition(true, "", "OR", "AND", 0);
    }

    @Test
    public void innerJoinNLWithNullInCondition2() throws IOException {

        joinWithNullInCondition(true, "", "AND", "AND", 0);
    }

    @Test
    public void innerJoinNLWithNullInCondition3() throws IOException {

        joinWithNullInCondition(true, "", "AND", "OR", 0);
    }

    private void joinWithAllFromSecondTable(boolean useNestedLoops) throws IOException {

        final String hint = useNestedLoops ? USE_NL_HINT : "";
        final String query = String.format(Locale.ROOT, "SELECT%1$s c.name.firstname, d.* " +
                        "FROM %2$s c JOIN %2$s d ON d.hname = c.house",
                hint, TEST_INDEX_GAME_OF_THRONES);

        JSONObject result = executeQuery(query);
        JSONArray hits = getHits(result);

        if (useNestedLoops) {
            Assert.assertThat(hits.length(), equalTo(0));
        } else {
            Assert.assertThat(hits.length(), equalTo(4));
            Assert.assertThat(hits.getJSONObject(0).getJSONObject("_source").length(), equalTo(5));
        }
    }

    private void joinWithAllFromFirstTable(boolean useNestedLoops) throws IOException {

        final String hint = useNestedLoops ? USE_NL_HINT : "";
        final String query = String.format(Locale.ROOT, "SELECT%1$s c.name.firstname " +
                        "FROM %2$s d JOIN %2$s c ON c.house = d.hname",
                hint, TEST_INDEX_GAME_OF_THRONES);

        JSONObject result = executeQuery(query);
        JSONArray hits = getHits(result);

        if (useNestedLoops) {
            Assert.assertThat(hits.length(), equalTo(0));
        } else {
            Assert.assertThat(hits.length(), equalTo(4));
            Assert.assertThat(hits.getJSONObject(0).getJSONObject("_source").length(), equalTo(1));
        }
    }

    private void leftJoinWithAllFromSecondTable(boolean useNestedLoops) throws IOException {

        final String hint = useNestedLoops ? USE_NL_HINT : "";
        final String query = String.format(Locale.ROOT, "SELECT%1$s c.name.firstname, d.* " +
                        "FROM %2$s c LEFT JOIN %2$s d ON d.hname = c.house",
                hint, TEST_INDEX_GAME_OF_THRONES);

        JSONObject result = executeQuery(query);
        JSONArray hits = getHits(result);

        Assert.assertThat(hits.length(), equalTo(7));

        hits.forEach(hitObj -> {
            JSONObject hit = (JSONObject)hitObj;

            Assert.assertThat(hit.getJSONObject("_source").length(),
                    equalTo(hit.getString("_id").endsWith("0") ? 1 : 5));
        });
    }

    private void joinParseCheckSelectedFieldsSplit(boolean useNestedLoops) throws IOException {

        final String hint = useNestedLoops ? USE_NL_HINT : "";
        String query = String.format(Locale.ROOT, "SELECT%s a.firstname ,a.lastname,a.gender,d.dog_name " +
                "FROM %s a JOIN %s d ON d.holdersName = a.firstname " +
                "WHERE (a.age > 10 OR a.balance > 2000) AND d.age > 1", hint, TEST_INDEX_PEOPLE, TEST_INDEX_DOG);

        JSONObject result = executeQuery(query);
        verifyJoinParseCheckSelectedFieldsSplitResult(result, useNestedLoops);
    }

    private void joinNoConditionButWithWhere(boolean useNestedLoops) throws IOException {

        final String hint = useNestedLoops ? USE_NL_HINT : "";
        String query = String.format(Locale.ROOT, "SELECT%s c.gender,h.hname,h.words FROM %2$s c " +
                        "JOIN %2$s h WHERE match_phrase(c.name.firstname, 'Daenerys')",
                hint, TEST_INDEX_GAME_OF_THRONES);

        JSONObject result = executeQuery(query);
        JSONArray hits = getHits(result);
        Assert.assertThat(hits.length(), equalTo(7));
    }

    private void joinNoConditionAndNoWhere(boolean useNestedLoops) throws IOException {

        final String hint = useNestedLoops ? USE_NL_HINT : "";
        String query = String.format(Locale.ROOT, "SELECT%s c.name.firstname,c.parents.father,h.hname,h.words " +
                        "FROM %2$s c JOIN %2$s h",
                hint, TEST_INDEX_GAME_OF_THRONES);

        JSONObject result = executeQuery(query);
        JSONArray hits = getHits(result);
        Assert.assertThat(hits.length(), equalTo(49));
    }

    private void joinWithNoWhereButWithCondition(boolean useNestedLoops) throws IOException {

        final String hint = useNestedLoops ? USE_NL_HINT : "";
        String query = String.format(Locale.ROOT, "SELECT%s c.gender,h.hname,h.words " +
                        "FROM %2$s c JOIN %2$s h ON h.hname = c.house",
                hint, TEST_INDEX_GAME_OF_THRONES);

        JSONObject result = executeQuery(query);
        JSONArray hits = getHits(result);

        Map<String,Object> someMatch =  ImmutableMap.of(
                "c.gender", "F",
                "h.hname","Targaryen",
                "h.words", "fireAndBlood");

        if (useNestedLoops) {
            // TODO: should the NL result be different?
            Assert.assertThat(hits.length(), equalTo(0));
        } else {
            Assert.assertThat(hits.length(), equalTo(4));
            Assert.assertTrue(hitsInclude(hits, someMatch));
        }
    }

    private void verifyJoinParseCheckSelectedFieldsSplitResult(JSONObject result, boolean useNestedLoops) {

        Map<String, String> match1 = ImmutableMap.of(
                "a.firstname", "Daenerys",
                "a.lastname", "Targaryen",
                "a.gender", "M",
                "d.dog_name", "rex");
        Map<String, String> match2 = ImmutableMap.of(
                "a.firstname", "Hattie",
                "a.lastname", "Bond",
                "a.gender", "M",
                "d.dog_name", "snoopy");

        JSONArray hits = getHits(result);

        if (useNestedLoops) {
            //TODO: change field mapping in ON condition to keyword or change query to get result
            // TODO: why does NL query return no results?
            Assert.assertThat(hits.length(), equalTo(0));
        } else {
            Assert.assertThat(hits.length(), equalTo(2));
            Assert.assertTrue(hitsInclude(hits, match1));
            Assert.assertTrue(hitsInclude(hits, match2));
        }
    }

    private void joinNoConditionAndNoWhereWithTotalLimit(boolean useNestedLoops) throws  IOException {

        final String hint = useNestedLoops ? USE_NL_HINT : "";
        String query = String.format(Locale.ROOT, "SELECT%s c.name.firstname,c.parents.father,h.hname,h.words" +
                        " FROM %2$s c JOIN %2$s h LIMIT 9",
                hint, TEST_INDEX_GAME_OF_THRONES);

        JSONObject result = executeQuery(query);
        JSONArray hits = getHits(result);
        Assert.assertThat(hits.length(), equalTo(9));
    }

    private void joinWithNestedFieldsOnReturn(boolean useNestedLoops) throws IOException {

        final String hint = useNestedLoops ? USE_NL_HINT : "";
        String query = String.format(Locale.ROOT, "SELECT%s c.name.firstname,c.parents.father,h.hname,h.words " +
                        "FROM %2$s c JOIN %2$s h ON h.hname = c.house " +
                        "WHERE match_phrase(c.name.firstname, 'Daenerys')",
                hint, TEST_INDEX_GAME_OF_THRONES);

        JSONObject result = executeQuery(query);
        JSONArray hits = getHits(result);
        final Map<String, String> expectedMatch =  ImmutableMap.of(
                "c.name.firstname", "Daenerys",
                "c.parents.father", "Aerys",
                "h.hname", "Targaryen",
                "h.words", "fireAndBlood");
        if (useNestedLoops) {
            Assert.assertThat(hits.length(), equalTo(0));
        } else {
            Assert.assertThat(hits.length(), equalTo(1));
            assertHitMatches(hits.getJSONObject(0), expectedMatch);
        }
    }

    private void joinWithAllAliasOnReturn(boolean useNestedLoops) throws IOException {

        final String hint = useNestedLoops ? USE_NL_HINT : "";
        String query = String.format(Locale.ROOT, "SELECT%s c.name.firstname name,c.parents.father father," +
                        "h.hname house FROM %2$s c JOIN %2$s h ON h.hname = c.house " +
                        "WHERE match_phrase(c.name.firstname, 'Daenerys')",
                hint, TEST_INDEX_GAME_OF_THRONES);

        JSONObject result = executeQuery(query);
        JSONArray hits = getHits(result);
        final Map<String, String> expectedMatch =  ImmutableMap.of(
                "name", "Daenerys",
                "father", "Aerys",
                "house", "Targaryen");

        if (useNestedLoops) {
            Assert.assertThat(hits.length(), equalTo(0));
        } else {
            Assert.assertThat(hits.length(), equalTo(1));
            assertHitMatches(hits.getJSONObject(0), expectedMatch);
        }
    }

    private void joinWithSomeAliasOnReturn(boolean useNestedLoops)  throws IOException {

        final String hint = useNestedLoops ? USE_NL_HINT : "";
        String query = String.format(Locale.ROOT, "SELECT%s c.name.firstname ,c.parents.father father, " +
                        "h.hname house FROM %2$s c JOIN %2$s h ON h.hname = c.house " +
                        "WHERE match_phrase(c.name.firstname, 'Daenerys')",
                hint, TEST_INDEX_GAME_OF_THRONES);

        JSONObject result = executeQuery(query);
        JSONArray hits = getHits(result);
        final Map<String, String> expectedMatch = ImmutableMap.of(
                "c.name.firstname", "Daenerys",
                "father", "Aerys",
                "house", "Targaryen");

        if (useNestedLoops) {
            //TODO: Either change the ON condition field to keyword or create a different subquery
            Assert.assertThat(hits.length(), equalTo(0));
        } else {
            Assert.assertThat(hits.length(), equalTo(1));
            assertHitMatches(hits.getJSONObject(0), expectedMatch);
        }
    }

    private void joinWithNestedFieldsOnComparisonAndOnReturn(boolean useNestedLoops) throws IOException {

        final String hint = useNestedLoops ? USE_NL_HINT : "";
        String query = String.format(Locale.ROOT, "SELECT%s c.name.firstname,c.parents.father, h.hname,h.words " +
                        " FROM %2$s c JOIN %2$s h ON h.hname = c.name.lastname " +
                        "WHERE match_phrase(c.name.firstname, 'Daenerys')",
                hint, TEST_INDEX_GAME_OF_THRONES);

        JSONObject result = executeQuery(query);
        JSONArray hits = getHits(result);
        final Map<String,String> expectedMatch =  ImmutableMap.of(
                "c.name.firstname", "Daenerys",
                "c.parents.father", "Aerys",
                "h.hname", "Targaryen",
                "h.words", "fireAndBlood");

        if (useNestedLoops) {
            Assert.assertThat(hits.length(), equalTo(0));
        } else {
            Assert.assertThat(hits.length(), equalTo(1));
            assertHitMatches(hits.getJSONObject(0), expectedMatch);
        }
    }

    private void testLeftJoin(boolean useNestedLoops) throws IOException {

        final String hint = useNestedLoops ? USE_NL_HINT : "";
        String query = String.format("SELECT%s c.name.firstname, f.name.firstname,f.name.lastname " +
                        "FROM %2$s c LEFT JOIN %2$s f " +
                        "ON f.name.firstname = c.parents.father",
                hint, TEST_INDEX_GAME_OF_THRONES);

        JSONObject result = executeQuery(query);
        JSONArray hits = getHits(result);
        Assert.assertThat(hits.length(), equalTo(7));

        final Map<String, Object> firstMatch = new HashMap<>();
        firstMatch.put("c.name.firstname", "Daenerys");
        firstMatch.put("f.name.firstname", null);
        firstMatch.put("f.name.lastname", null);

        final Map<String, Object> secondMatch = new HashMap<>();
        secondMatch.put("c.name.firstname", "Brandon");

        if (useNestedLoops) {
            secondMatch.put("f.name.firstname", null);
            secondMatch.put("f.name.lastname", null);
        } else {
            secondMatch.put("f.name.firstname", "Eddard");
            secondMatch.put("f.name.lastname", "Stark");
        }

        Assert.assertTrue(hitsInclude(hits, firstMatch));
        Assert.assertTrue(hitsInclude(hits, secondMatch));
    }

    private void hintLimits_firstLimitSecondNull(boolean useNestedLoops) throws IOException {

        final String hint = useNestedLoops ? USE_NL_HINT : "";
        String query = String.format(Locale.ROOT, "SELECT%s /*! JOIN_TABLES_LIMIT(2,null) */ " +
                        "c.name.firstname,c.parents.father, h.hname,h.words " +
                        "FROM %2$s c JOIN %2$s h",
                hint, TEST_INDEX_GAME_OF_THRONES);

        JSONObject result = executeQuery(query);
        JSONArray hits = getHits(result);
        Assert.assertThat(hits.length(), equalTo(14));
    }

    private void hintLimits_firstLimitSecondLimit(boolean useNestedLoops) throws IOException {

        final String hint = useNestedLoops ? USE_NL_HINT : "";
        String query = String.format(Locale.ROOT, "SELECT%s /*! JOIN_TABLES_LIMIT(2,2) */ " +
                "c.name.firstname,c.parents.father, h.hname,h.words FROM %2$s c " +
                "JOIN %2$s h", hint, TEST_INDEX_GAME_OF_THRONES);

        JSONObject result = executeQuery(query);
        JSONArray hits = getHits(result);
        Assert.assertThat(hits.length(), equalTo(4));
    }

    private void hintLimits_firstLimitSecondLimitOnlyOne(boolean useNestedLoops) throws IOException {

        final String hint = useNestedLoops ? USE_NL_HINT : "";
        String query = String.format(Locale.ROOT, "SELECT%s /*! JOIN_TABLES_LIMIT(3,1) */ " +
                        "c.name.firstname,c.parents.father , h.hname,h.words FROM %2$s h " +
                        "JOIN  %2$s c  ON c.name.lastname = h.hname",
                hint, TEST_INDEX_GAME_OF_THRONES);

        JSONObject result = executeQuery(query);
        JSONArray hits = getHits(result);
        Assert.assertThat(hits.length(), equalTo(0));
    }

    private void hintLimits_firstNullSecondLimit(boolean useNestedLoops) throws IOException {

        final String hint = useNestedLoops ? USE_NL_HINT : "";
        String query = String.format(Locale.ROOT, "SELECT%s /*! JOIN_TABLES_LIMIT(null,2) */ " +
                "c.name.firstname,c.parents.father , h.hname,h.words FROM %2$s c " +
                "JOIN %2$s h", hint, TEST_INDEX_GAME_OF_THRONES);

        JSONObject result = executeQuery(query);
        JSONArray hits = getHits(result);
        Assert.assertThat(hits.length(), equalTo(14));
    }

    private void testLeftJoinWithLimit(boolean useNestedLoops) throws IOException {

        final String hint = useNestedLoops ? USE_NL_HINT : "";
        String query = String.format(Locale.ROOT, "SELECT%s /*! JOIN_TABLES_LIMIT(3,null) */ " +
                        "c.name.firstname, f.name.firstname,f.name.lastname FROM %2$s c " +
                        "LEFT JOIN %2$s f ON f.name.firstname = c.parents.father",
                hint, TEST_INDEX_GAME_OF_THRONES);

        JSONObject result = executeQuery(query);
        JSONArray hits = getHits(result);
        Assert.assertThat(hits.length(), equalTo(3));
    }

    private void joinWithOr(boolean useNestedLoops) throws IOException {

        final String hint = useNestedLoops ? USE_NL_HINT : "";
        String query = String.format(Locale.ROOT, "SELECT%s d.dog_name,c.name.firstname " +
                        "FROM %s c JOIN %s d " +
                        "ON d.holdersName = c.name.firstname OR d.age = c.name.ofHisName",
                hint, TEST_INDEX_GAME_OF_THRONES, TEST_INDEX_DOG);

        JSONObject result = executeQuery(query);
        JSONArray hits = getHits(result);

        final Map<String, String> firstMatch =  ImmutableMap.of(
                "c.name.firstname", "Daenerys",
                "d.dog_name", "rex");
        final Map<String, String> secondMatch =  ImmutableMap.of(
                "c.name.firstname", "Brandon",
                "d.dog_name", "snoopy");

        if (useNestedLoops) {
            Assert.assertThat(hits.length(), equalTo(1));
            Assert.assertTrue("hits contains brandon", hitsInclude(hits, secondMatch));
        } else {
            Assert.assertThat(hits.length(), equalTo(2));
            Assert.assertTrue("hits contains daenerys", hitsInclude(hits, firstMatch));
            Assert.assertTrue("hits contains brandon", hitsInclude(hits, secondMatch));
        }
    }

    private void joinWithOrderFirstTable(boolean useNestedLoops) throws IOException {

        final String hint = useNestedLoops ? USE_NL_HINT : "";
        String query = String.format(Locale.ROOT, "SELECT%s c.name.firstname,d.words " +
                        "FROM %2$s c JOIN %2$s d ON d.hname = c.house " +
                        "ORDER BY c.name.firstname",
                hint, TEST_INDEX_GAME_OF_THRONES);

        JSONObject result = executeQuery(query);
        JSONArray hits = getHits(result);

        if (useNestedLoops) {
            Assert.assertThat(hits.length(), equalTo(0));
        } else {

            Assert.assertThat(hits.length(), equalTo(4));

            String[] expectedNames = { "Brandon", "Daenerys", "Eddard", "Jaime" };

            IntStream.rangeClosed(0, 3).forEach(i -> {
                String firstnamePath = String.format(Locale.ROOT, "/%d/_source/c.name.firstname", i);
                Assert.assertThat(hits.query(firstnamePath), equalTo(expectedNames[i]));
            });
        }
    }

    private boolean containsTerm(final String explainedQuery, final String termName) {

        return Pattern.compile(
                Pattern.quote("\"terms\":{")
                        + ".*"
                        + Pattern.quote("\"" + termName + "\":[")
        )
                .matcher(explainedQuery.replaceAll("\\s+",""))
                .find();
    }

    private void joinWithNullInCondition(boolean useNestedLoops, String left,
                                         String oper1, String oper2, int expectedNum) throws IOException {

        final String hint = useNestedLoops ? USE_NL_HINT : "";
        String query = String.format(Locale.ROOT, "SELECT%s c.name.firstname,c.parents.father,c.hname," +
                        "f.name.firstname,f.house,f.hname FROM %s c " +
                        "%s JOIN %s f ON f.name.firstname = c.parents.father " +
                        "%s f.house = c.hname %s f.house = c.name.firstname",
                hint, TEST_INDEX_GAME_OF_THRONES, left, TEST_INDEX_GAME_OF_THRONES, oper1, oper2);

        JSONObject result = executeQuery(query);
        JSONArray hits = getHits(result);

        if (useNestedLoops) {
            Assert.assertThat(hits.length(), equalTo(expectedNum));
        } else {
            // This branch is reserved for hash join, no test currently enters it.
            // Making it fail, so that whenever tests are added for hash join,
            // this method gets properly updated.
            Assert.fail();
        }
    }

    private boolean hitsInclude(final JSONArray actualHits, Map<String, ?> expectedSourceValues) {

        for (final Object hitObj : actualHits.toList()) {

            final Map<String, ?> hit = uncheckedGetMap(hitObj);
            if (hitMatches(hit, expectedSourceValues)) {
                return true;
            }
        }

        return false;
    }

    private void assertHitMatches(final JSONObject actualHit, final Map<String, ?> expectedSourceValues) {

        final JSONObject src = actualHit.getJSONObject("_source");
        Assert.assertThat(src.length(), equalTo(expectedSourceValues.size()));
        src.keySet().forEach(key -> {
            Assert.assertTrue(expectedSourceValues.containsKey(key));
            Object value = src.get(key);
            Assert.assertThat(value, equalTo(expectedSourceValues.get(key)));
        });
    }

    private boolean hitMatches(final Map<String, ?> actualHit, final Map<String, ?> expectedSourceValues) {

        final Map<String, ?> src = uncheckedGetMap(actualHit.get("_source"));

        if (src.size() != expectedSourceValues.size()) {
            return false;
        }

        for (final String key: src.keySet()) {

            if (!expectedSourceValues.containsKey(key)) {
                return false;
            }

            Object actualValue = src.get(key);
            Object expectedValue = expectedSourceValues.get(key);
            if ((actualValue == null && expectedValue != null) ||
                    (actualValue != null && expectedValue == null)) {
                return false;
            } else if (actualValue != null && !actualValue.equals(expectedValue)) {
                return false;
            }
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    private Map<String, ?> uncheckedGetMap(final Object mapObject) {
        return (Map<String, Object>)mapObject;
    }
}
