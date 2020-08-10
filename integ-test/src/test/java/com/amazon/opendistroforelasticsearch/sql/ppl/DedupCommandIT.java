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

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_BANK;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_BANK_WITH_NULL_VALUES;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.rows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifyDataRows;

public class DedupCommandIT extends PPLIntegTestCase {

  @Override
  public void init() throws IOException {
    loadIndex(Index.BANK);
    loadIndex(Index.BANK_WITH_NULL_VALUES);
  }

  @Test
  public void testDedup() throws IOException {
    JSONObject result =
        executeQuery(String.format("source=%s | dedup male | fields male", TEST_INDEX_BANK));
    verifyDataRows(result, rows(true), rows(false));
  }

  @Test
  public void testConsecutiveDedup() throws IOException {
    JSONObject result =
        executeQuery(
            String.format(
                "source=%s | dedup male consecutive=true | fields male", TEST_INDEX_BANK));
    verifyDataRows(result, rows(true), rows(false), rows(true), rows(false));
  }

  @Test
  public void testAllowMoreDuplicates() throws IOException {
    JSONObject result =
        executeQuery(String.format("source=%s | dedup 2 male | fields male", TEST_INDEX_BANK));
    verifyDataRows(result, rows(true), rows(true), rows(false), rows(false));
  }

  @Test
  public void testKeepEmptyDedup() throws IOException {
    JSONObject result =
        executeQuery(
            String.format(
                "source=%s | dedup balance keepempty=true | fields firstname, balance",
                TEST_INDEX_BANK_WITH_NULL_VALUES));
    verifyDataRows(
        result,
        rows("Amber JOHnny", 39225),
        rows("Hattie", null),
        rows("Nanette", 32838),
        rows("Dale", 4180),
        rows("Elinor", null),
        rows("Virginia", null),
        rows("Dillard", 48086));
  }
}
