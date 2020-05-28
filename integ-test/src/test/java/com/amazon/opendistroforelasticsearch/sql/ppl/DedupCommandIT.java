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
import org.junit.jupiter.api.Test;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_BANK;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_BANK_WITH_NULL_VALUES;

public class DedupCommandIT extends PPLIntegTestCase {

    @Override
    public void init() throws IOException {
        loadIndex(Index.BANK);
        loadIndex(Index.BANK_WITH_NULL_VALUES);
    }

    @Test
    public void testDedup() throws IOException {
        String result = executeQuery(String.format("source=%s | dedup age | fields firstname, age", TEST_INDEX_BANK));
        String expected = getExpectedOutput("dedup.json");
        assertEquals(expected, result);
    }

    @Test
    public void testConsecutiveDedup() throws IOException {
        String result = executeQuery(String.format(
                "source=%s | fields firstname, male | dedup male consecutive=true", TEST_INDEX_BANK));
        String expected = getExpectedOutput("dedup_consecutive.json");
        assertEquals(expected, result);
    }

    @Test
    public void testAllowMoreDuplicates() throws IOException {
        String result = executeQuery(String.format(
                "source=%s | fields firstname, male | dedup 2 male", TEST_INDEX_BANK));
        String expected = getExpectedOutput("dedup_allow_duplicates.json");
        assertEquals(expected, result);
    }

    @Test
    public void testKeepEmptyDedup() throws IOException {
        String result = executeQuery(String.format(
                "source=%s |fields firstname, balance | dedup firstname, balance keepempty=true",
                TEST_INDEX_BANK_WITH_NULL_VALUES));
        String expected = getExpectedOutput("dedup_keepempty.json");
        assertEquals(expected, result);
    }
}
