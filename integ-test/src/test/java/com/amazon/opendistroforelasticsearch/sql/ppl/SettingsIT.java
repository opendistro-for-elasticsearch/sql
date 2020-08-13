/*
 *
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.ppl;

import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_BANK;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.rows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifyDataRows;

import java.io.IOException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class SettingsIT extends PPLIntegTestCase {

  @Override
  public void init() throws IOException {
    loadIndex(Index.BANK);
    loadIndex(Index.DOG);
  }

  @Test
  public void testQuerySizeLimit() throws IOException {
    // Default setting, fetch 200 rows from source
    JSONObject result =
        executeQuery(
            String.format(
                "search source=%s age>35 | fields firstname", TEST_INDEX_BANK));
    verifyDataRows(result, rows("Hattie"), rows("Elinor"), rows("Virginia"));

    // Fetch 1 rows from source
    setQuerySizeLimit(1);
    result =
        executeQuery(
            String.format(
                "search source=%s age>35 | fields firstname", TEST_INDEX_BANK));
    verifyDataRows(result, rows("Hattie"));
  }
}
