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
import java.util.List;
import org.junit.Test;
import org.junit.jupiter.api.Disabled;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_ACCOUNT;

@Disabled("Rename target cannot be resolved yet")
public class RenameCommandIT extends PPLIntegTestCase {

    @Override
    public void init() throws IOException {
        loadIndex(Index.ACCOUNT);
    }

    @Test
    public void testRenameOneField() throws IOException {
        String result = executeQuery(String.format("source=%s | rename firstname as first_name", TEST_INDEX_ACCOUNT));
        List<String> columns = getColumnNames(result);
        assertTrue(columns.contains("first_name") && !columns.contains("firstname"));
    }

    @Test
    public void testRenameWildcardFields() throws IOException {
        String result = executeQuery("source=" + TEST_INDEX_ACCOUNT + " | rename %name as %NAME");
        List<String> columns = getColumnNames(result);
        assertTrue(columns.contains("NAME") && !columns.contains("name"));
    }
}
