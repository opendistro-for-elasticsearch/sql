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
import org.elasticsearch.client.AdminClient;
import org.elasticsearch.client.Client;
import org.elasticsearch.test.ESIntegTestCase;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import static com.amazon.opendistroforelasticsearch.sql.intgtest.TestsConstants.TEST_INDEX_DOG;
import static com.amazon.opendistroforelasticsearch.sql.intgtest.TestsConstants.TEST_INDEX_GAME_OF_THRONES;
import static com.amazon.opendistroforelasticsearch.sql.intgtest.TestsConstants.TEST_INDEX_PEOPLE;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class JoinIT extends SQLIntegTestCase {

    private static final String USE_NL_HINT = " /*! USE_NL*/";

    @Override
    protected void setupSuiteScopeCluster() throws Exception {
        AdminClient adminClient = this.admin();
        Client esClient = ESIntegTestCase.client();

        loadDogIndex(adminClient, esClient);
        loadPeopleIndex(adminClient, esClient);
        loadGameOfThronesIndex(adminClient, esClient);
    }

    @Test
    public void joinParseCheckSelectedFieldsSplitHASH() throws IOException {
        joinParseCheckSelectedFieldsSplit(false);
    }

    @Test
    public void joinParseCheckSelectedFieldsSplitNL() throws IOException {
        joinParseCheckSelectedFieldsSplit(true);
    }

    private void joinParseCheckSelectedFieldsSplit(boolean useNestedLoops) throws IOException {

        String query = "SELECT a.firstname ,a.lastname , a.gender ,d.dog_name  FROM " +
                TEST_INDEX_PEOPLE +
                "/people a " +
                " JOIN " +
                TEST_INDEX_DOG +
                "/dog d on d.holdersName = a.firstname " +
                " WHERE " +
                " (a.age > 10 OR a.balance > 2000)" +
                " AND d.age > 1";

        if (useNestedLoops) {
            query = query.replace("SELECT","SELECT /*! USE_NL*/ ");
        }

        JSONObject result = executeQuery(query);

        verifyJoinParseCheckSelectedFieldsSplitResult(result, useNestedLoops);
    }

    @Test
    public void joinParseWithHintsCheckSelectedFieldsSplitHASH() throws IOException {

        String query = "SELECT /*! HASH_WITH_TERMS_FILTER*/ a.firstname ,a.lastname , a.gender ,d.dog_name  FROM " +
                TEST_INDEX_PEOPLE + "/people a " +
                " JOIN " + TEST_INDEX_DOG + "/dog d ON d.holdersName = a.firstname " +
                " WHERE (a.age > 10 OR a.balance > 2000) AND d.age > 1";
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

        String query = String.format("SELECT * FROM %s/gotCharacters c JOIN %s/gotCharacters h " +
                "ON h.hname = c.house ",TEST_INDEX_GAME_OF_THRONES, TEST_INDEX_GAME_OF_THRONES);
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

    private void joinNoConditionButWithWhere(boolean useNestedLoops) throws IOException {

        final String hint = useNestedLoops ? USE_NL_HINT : "";
        String query = String.format(Locale.ROOT, "SELECT%s c.gender,h.hname,h.words FROM %s/gotCharacters c " +
                        "JOIN %s/gotCharacters h WHERE match_phrase(c.name.firstname, 'Daenerys')",
                hint, TEST_INDEX_GAME_OF_THRONES, TEST_INDEX_GAME_OF_THRONES);

        JSONObject result = executeQuery(query);
        JSONArray hits = getHits(result);
        Assert.assertThat(hits.length(), equalTo(7));
    }

    private void joinNoConditionAndNoWhere(boolean useNestedLoops) throws IOException {

        final String hint = useNestedLoops ? USE_NL_HINT : "";
        String query = String.format(Locale.ROOT, "SELECT%s c.name.firstname,c.parents.father,h.hname,h.words " +
                        "FROM %s/gotCharacters c JOIN %s/gotCharacters h",
                hint, TEST_INDEX_GAME_OF_THRONES, TEST_INDEX_GAME_OF_THRONES);

        JSONObject result = executeQuery(query);
        JSONArray hits = getHits(result);
        Assert.assertThat(hits.length(), equalTo(49));
    }

    private void joinWithNoWhereButWithCondition(boolean useNestedLoops) throws IOException {

        final String hint = useNestedLoops ? USE_NL_HINT : "";
        String query = String.format(Locale.ROOT, "SELECT%s c.gender,h.hname,h.words FROM %s/gotCharacters c " +
                "JOIN %s/gotCharacters h ON h.hname = c.house",
                hint, TEST_INDEX_GAME_OF_THRONES, TEST_INDEX_GAME_OF_THRONES);

        JSONObject result = executeQuery(query);
        JSONArray hits = getHits(result);

        Map<String,Object> someMatch =  ImmutableMap.of("c.gender", "F", "h.hname", "Targaryen",
                "h.words", "fireAndBlood");

        if (useNestedLoops) {
            // TODO: should the NL result be different?
            Assert.assertEquals(0, hits.length());
        } else {
            Assert.assertEquals(4, hits.length());
            boolean foundMatch = false;
            for (int i = 0; i < hits.length(); ++i) {
                JSONObject sourceFields = hits.getJSONObject(i).getJSONObject("_source");
                if (sourceFields.getString("h.hname").equals(someMatch.get("h.hname"))) {
                    foundMatch = true;
                    for (String key : someMatch.keySet()) {
                        Assert.assertThat(sourceFields.get(key), equalTo(someMatch.get(key)));
                    }
                }
            }
            Assert.assertThat(foundMatch, equalTo(true));
        }
    }

    private void verifyJoinParseCheckSelectedFieldsSplitResult(JSONObject result, boolean useNestedLoops) {

        Map<String, String> match1 = ImmutableMap.of("a.firstname", "Daenerys", "a.lastname", "Targaryen",
                "a.gender", "M", "d.dog_name", "rex");
        Map<String, String> match2 = ImmutableMap.of("a.firstname", "Hattie", "a.lastname", "Bond",
                "a.gender", "M", "d.dog_name", "snoopy");

        JSONArray hits = getHits(result);

        if (useNestedLoops) {
            //TODO: change field mapping in ON condition to keyword or change query to get result
            // TODO: why does NL query return no results?
            Assert.assertThat(hits.length(), equalTo(0));
        } else {
            Assert.assertThat(hits.length(), equalTo(2));

            int id1 = 0;
            int id2 = 1;

            if (hits.query("/0/_source/d.dog_name").equals("snoopy")) {
                id1 = 1;
                id2 = 0;
            }

            assertHitMatches(hits.getJSONObject(id1), match1);
            assertHitMatches(hits.getJSONObject(id2), match2);
        }
    }

    private void assertHitMatches(JSONObject actualHit, Map<String, ?> expectedFieldValues) {

        JSONObject src = actualHit.getJSONObject("_source");
        Assert.assertThat(src.length(), equalTo(expectedFieldValues.size()));
        src.keySet().forEach(key -> {
            Assert.assertTrue(expectedFieldValues.containsKey(key));
            Object value = src.get(key);
            Assert.assertThat(value, equalTo(expectedFieldValues.get(key)));
        });
    }
}
