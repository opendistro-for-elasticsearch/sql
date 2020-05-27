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
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_ACCOUNT;

public class FieldsCommandIT extends PPLIntegTestCase {

    @Override
    public void init() throws IOException {
        loadIndex(Index.ACCOUNT);
    }

    @Test
    public void testFieldsWithOneField() throws IOException {
        String result = executeQuery(String.format("source=%s | fields firstname", TEST_INDEX_ACCOUNT));
        assertTrue(result.contains("firstname"));
    }

    @Test
    public void testFieldsWithMultiFields() throws IOException {
        String result = executeQuery(String.format("source=%s | fields firstname, lastname", TEST_INDEX_ACCOUNT));
        assertTrue(result.contains("firstname") && result.contains("lastname"));
    }

    @Ignore("Cannot resolve wildcard yet")
    @Test
    public void testFieldsWildCard() throws IOException {
        String result = executeQuery(String.format("source=%s | fields ", TEST_INDEX_ACCOUNT) + "firstnam%");
        assertTrue(result.contains("firstname"));
    }
}
