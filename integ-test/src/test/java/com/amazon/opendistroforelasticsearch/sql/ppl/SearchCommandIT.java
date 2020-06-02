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

import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_BANK;
import static com.amazon.opendistroforelasticsearch.sql.esintgtest.TestsConstants.TEST_INDEX_DOG;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.columnName;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.rows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifyColumn;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifyDataRows;

import java.io.IOException;
import org.elasticsearch.client.ResponseException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class SearchCommandIT extends PPLIntegTestCase {

  @Override
  public void init() throws IOException {
    loadIndex(Index.BANK);
    loadIndex(Index.DOG);
  }

  @Test
  public void testSearchAllFields() throws IOException {
    JSONObject result = executeQuery(String.format("search source=%s", TEST_INDEX_DOG));
    verifyColumn(result, columnName("dog_name"), columnName("holdersName"), columnName("age"));
  }

  @Test
  public void testSearchCommandWithoutSearchKeyword() throws IOException {
    assertEquals(
        executeQueryToString(String.format("search source=%s", TEST_INDEX_BANK)),
        executeQueryToString(String.format("source=%s", TEST_INDEX_BANK)));
  }

  @Test
  public void testSearchCommandWithLogicalExpression() throws IOException {
    JSONObject result =
        executeQuery(
            String.format(
                "search source=%s firstname='Hattie' | fields firstname", TEST_INDEX_BANK));
    verifyDataRows(result, rows("Hattie"));
  }

  @Test
  public void searchCommandWithoutSourceShouldFailToParse() throws IOException {
    try {
      executeQuery("search firstname='Hattie'");
      fail();
    } catch (ResponseException e) {
      assertTrue(e.getMessage().contains("RuntimeException"));
      assertTrue(e.getMessage().contains("Failed to parse query due to offending symbol"));
    }
  }
}
