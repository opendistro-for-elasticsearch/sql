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

import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_ACCOUNT;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.rows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.schema;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifyDataRows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifySchema;

import java.io.IOException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class StatsCommandIT extends PPLIntegTestCase {

  @Override
  public void init() throws IOException {
    loadIndex(Index.ACCOUNT);
    setQuerySizeLimit(2000);
  }

  @Test
  public void testStatsAvg() throws IOException {
    JSONObject response =
        executeQuery(String.format("source=%s | stats avg(age)", TEST_INDEX_ACCOUNT));
    verifySchema(response, schema("avg(age)", null, "double"));
    verifyDataRows(response, rows(30.171D));
  }

  @Test
  public void testStatsSum() throws IOException {
    JSONObject response =
        executeQuery(String.format("source=%s | stats sum(balance)", TEST_INDEX_ACCOUNT));
    verifySchema(response, schema("sum(balance)", null, "long"));
    verifyDataRows(response, rows(25714837));
  }

  @Test
  public void testStatsCount() throws IOException {
    JSONObject response =
        executeQuery(String.format("source=%s | stats count(account_number)", TEST_INDEX_ACCOUNT));
    verifySchema(response, schema("count(account_number)", null, "integer"));
    verifyDataRows(response, rows(1000));
  }

  // TODO: each stats aggregate function should be tested here when implemented

  @Test
  public void testStatsNested() throws IOException {
    JSONObject response =
        executeQuery(String.format("source=%s | stats avg(abs(age)*2) as AGE", TEST_INDEX_ACCOUNT));
    verifySchema(response, schema("AGE", null, "double"));
    verifyDataRows(response, rows(60.342));
  }

  @Test
  public void testStatsWhere() throws IOException {
    JSONObject response =
        executeQuery(String.format(
            "source=%s | stats sum(balance) as a by gender | where a > 13000000", TEST_INDEX_ACCOUNT));
    verifySchema(response, schema("a", null, "long"),
        schema("gender", null, "string"));
    verifyDataRows(response, rows(13082527, "M"));
  }

}
