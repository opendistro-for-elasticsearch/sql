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

import java.io.IOException;
import java.util.Locale;

import static org.hamcrest.core.IsEqual.equalTo;

public class DeleteIT extends SQLIntegTestCase {

    @Override
    protected void init() throws Exception {
        loadIndex(Index.ACCOUNT);
        loadIndex(Index.PHRASE);
    }

    @Test
    public void deleteAllTest() throws IOException, InterruptedException {
        String selectQuery = String.format(
            Locale.ROOT,
            "SELECT * FROM %s/account",
            TestsConstants.TEST_INDEX_ACCOUNT
        );
        JSONObject response = executeRequest(makeRequest(selectQuery));
        int totalHits = getTotalHits(response);

        String deleteQuery = String.format(
            Locale.ROOT,
            "DELETE FROM %s/account",
            TestsConstants.TEST_INDEX_ACCOUNT);
        response = executeRequest(makeRequest(deleteQuery));
        assertThat(response.getInt("deleted"), equalTo(totalHits));

        // The documents are not deleted immediately, causing the next search call to return all results.
        // To prevent flakiness, the minimum value of 2000 msec works fine.
        Thread.sleep(2000);

        response = executeRequest(makeRequest(selectQuery));
        assertThat(getTotalHits(response), equalTo(0));
    }

    @Test
    public void deleteWithConditionTest() throws IOException, InterruptedException {
        String selectQuery = String.format(
            Locale.ROOT,
            "SELECT * FROM %s/phrase WHERE match_phrase(phrase, 'quick fox here')",
            TestsConstants.TEST_INDEX_PHRASE
        );
        JSONObject response = executeRequest(makeRequest(selectQuery));
        int totalHits = getTotalHits(response);

        String deleteQuery = String.format(
            Locale.ROOT,
            "DELETE FROM %s/phrase WHERE match_phrase(phrase, 'quick fox here')",
            TestsConstants.TEST_INDEX_PHRASE
        );
        response = executeRequest(makeRequest(deleteQuery));
        assertThat(response.getInt("deleted"), equalTo(totalHits));
        // The documents are not deleted immediately, causing the next search call to return all results.
        // To prevent flakiness, the minimum value of 2000 msec works fine.
        Thread.sleep(2000);

        selectQuery = String.format(
            Locale.ROOT,
            "SELECT * FROM %s/phrase",
            TestsConstants.TEST_INDEX_PHRASE
        );
        response = executeRequest(makeRequest(selectQuery));
        assertThat(getTotalHits(response), equalTo(5));
    }
}
