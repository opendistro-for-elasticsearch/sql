/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.opendistroforelasticsearch.sql.ppl;

import java.io.IOException;
import org.junit.Test;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_ACCOUNT;

public class StatsCommandIT extends PPLIntegTestCase {

    @Override
    public void init() throws IOException {
        loadIndex(Index.ACCOUNT);
    }

    @Test
    public void testStatsAvg() throws IOException {
        String result = executeQuery(String.format("source=%s | stats avg(age)", TEST_INDEX_ACCOUNT));
        String expected = getExpectedOutput("stats_avg.json");
        assertEquals(expected, result);
    }

    @Test
    public void testStatsSum() throws IOException {
        String result = executeQuery(String.format("source=%s | stats sum(balance)", TEST_INDEX_ACCOUNT));
        String expected = getExpectedOutput("stats_sum.json");
        assertEquals(expected, result);
    }

    //TODO: each stats aggregate function should be tested here when implemented
}
