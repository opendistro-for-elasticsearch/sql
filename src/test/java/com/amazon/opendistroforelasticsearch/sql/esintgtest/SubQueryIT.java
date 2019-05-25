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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_ACCOUNT;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_PEOPLE;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class SubQueryIT extends SQLIntegTestCase {

    @Override
    protected void init() throws Exception {

        loadIndex(Index.PEOPLE);
        loadIndex(Index.ACCOUNT);
    }

    @Test
    public void inClauseWithSimpleSubquery() throws IOException {

        final String query = String.format(Locale.ROOT, "SELECT firstname FROM %s " +
                        "WHERE firstname IN (SELECT firstname FROM %s)", TEST_INDEX_ACCOUNT, TEST_INDEX_PEOPLE);
        final JSONObject result = executeQuery(query);
        final JSONArray hits = getHits(result);

        final Set<String> expectedNames = new HashSet<>(Arrays.asList("Hattie", "Nanette", "Dale", "Elinor", "Virginia",
                "Dillard", "Mcgee", "Aurelia", "Fulton", "Burton", "Josie"));
        Assert.assertThat(hits.length(), equalTo(expectedNames.size()));
        Set<String> actualNames = new HashSet<>();
        hits.forEach(obj -> {
            JSONObject hit = (JSONObject)obj;
            actualNames.add(hit.getJSONObject("_source").getString("firstname"));
        });

        Assert.assertThat(actualNames, equalTo(expectedNames));
    }

    @Test
    public void explainInClauseWithSimpleSubquery() throws IOException {

        final String query = String.format(Locale.ROOT, "SELECT firstname FROM %s " +
                "WHERE firstname IN (SELECT firstname FROM %s)", TEST_INDEX_ACCOUNT, TEST_INDEX_PEOPLE);
        final String result = explainQuery(query);

        final List<String> subQueryResult = Arrays.asList("Daenerys", "Hattie", "Nanette", "Dale", "Elinor", "Virginia",
                "Dillard", "Mcgee", "Aurelia", "Fulton", "Burton", "Josie");

        final String matchTemplate = "\"term\":{\"firstname.keyword\":{\"value\":\"%s\",\"boost\":1.0}";
        subQueryResult.forEach(name ->
                Assert.assertThat(result, containsString(String.format(Locale.ROOT, matchTemplate, name))));
    }
}
