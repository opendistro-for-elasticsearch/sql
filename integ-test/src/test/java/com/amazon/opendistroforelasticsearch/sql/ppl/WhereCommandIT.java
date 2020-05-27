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

public class WhereCommandIT extends PPLIntegTestCase {

    @Override
    public void init() throws IOException {
        loadIndex(Index.ACCOUNT);
    }

    @Test
    public void testWhereWithLogicalExpr() throws IOException {
        String result = executeQuery(String.format(
                "source=%s | fields firstname | where firstname='Amber'", TEST_INDEX_ACCOUNT));
        assertTrue(result.contains("\"datarows\": [[\"Amber\"]]"));
    }

    @Test
    public void testWhereWithMultiLogicalExpr() throws IOException {
        String result = executeQuery(String.format(
                "source=%s | where firstname='Amber' lastname='Duke' age=32", TEST_INDEX_ACCOUNT));
        assertTrue(result.contains("Amber") && result.contains("Duke") && result.contains("32"));
    }

    @Test
    public void testWhereEquivalentSortCommand() throws IOException {
        assertEquals(
                executeQuery(String.format("source=%s | where firstname='Amber'", TEST_INDEX_ACCOUNT)),
                executeQuery(String.format("source=%s firstname='Amber'", TEST_INDEX_ACCOUNT))
        );
    }
}
