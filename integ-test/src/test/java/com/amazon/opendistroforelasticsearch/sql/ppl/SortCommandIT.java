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
import org.junit.Ignore;
import org.junit.Test;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_BANK;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_BANK_WITH_NULL_VALUES;

public class SortCommandIT extends PPLIntegTestCase {

    @Override
    public void init() throws IOException {
        loadIndex(Index.BANK);
    }

    @Ignore
    @Test
    public void testSortCommand() throws IOException {
        String result = executeQuery(String.format("source=%s | sort age | fields firstname, age", TEST_INDEX_BANK));
        String expected = getExpectedOutput("sort.json");
        assertEquals(expected, result);
    }

    @Ignore
    @Test
    public void testSortWithNullValue() throws IOException {
        String result = executeQuery(String.format(
                "source=%s | sort age | fields firstname, age", TEST_INDEX_BANK_WITH_NULL_VALUES));
        String expected = getExpectedOutput("sort_null.json");
        assertEquals(expected, result);
    }

    @Test
    public void testSortStringField() throws IOException {
        String result = executeQuery(String.format("source=%s | sort lastname | fields lastname", TEST_INDEX_BANK));
        String expected = getExpectedOutput("sort_string_field.json");
        assertEquals(expected, result);
    }

    @Test
    public void testSortMultipleFields() throws IOException {
        String result = executeQuery(String.format(
                "source=%s | sort age, balance | fields firstname, age, balance", TEST_INDEX_BANK));
        String expected = getExpectedOutput("sort_multi_fields.json");
        assertEquals(expected, result);
    }
}
