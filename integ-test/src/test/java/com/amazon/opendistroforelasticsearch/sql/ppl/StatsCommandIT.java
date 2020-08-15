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

import com.amazon.opendistroforelasticsearch.sql.legacy.SQLIntegTestCase;
import java.io.IOException;
import org.junit.jupiter.api.Test;

public class StatsCommandIT extends PPLIntegTestCase {

  @Override
  public void init() throws IOException {
    loadIndex(Index.ACCOUNT);
    setQuerySizeLimit(2000);
  }

  @Test
  public void testStatsAvg() throws IOException {
    String result =
        executeQueryToString(String.format("source=%s | stats avg(age)", TEST_INDEX_ACCOUNT));
    assertEquals(
        "{\n"
            + "  \"schema\": [{\n"
            + "    \"name\": \"avg(age)\",\n"
            + "    \"type\": \"double\"\n"
            + "  }],\n"
            + "  \"total\": 1,\n"
            + "  \"datarows\": [[30.171]],\n"
            + "  \"size\": 1\n"
            + "}\n",
        result);
  }

  @Test
  public void testStatsSum() throws IOException {
    String result =
        executeQueryToString(String.format("source=%s | stats sum(balance)", TEST_INDEX_ACCOUNT));
    assertEquals(
        "{\n"
            + "  \"schema\": [{\n"
            + "    \"name\": \"sum(balance)\",\n"
            + "    \"type\": \"long\"\n"
            + "  }],\n"
            + "  \"total\": 1,\n"
            + "  \"datarows\": [[25714837]],\n"
            + "  \"size\": 1\n"
            + "}\n",
        result);
  }

  @Test
  public void testStatsCount() throws IOException {
    String result =
        executeQueryToString(
            String.format("source=%s | stats count(account_number)", TEST_INDEX_ACCOUNT));
    assertEquals(
        "{\n"
            + "  \"schema\": [{\n"
            + "    \"name\": \"count(account_number)\",\n"
            + "    \"type\": \"integer\"\n"
            + "  }],\n"
            + "  \"total\": 1,\n"
            + "  \"datarows\": [[1000]],\n"
            + "  \"size\": 1\n"
            + "}\n",
        result);
  }

  // TODO: each stats aggregate function should be tested here when implemented

  @Test
  public void testStatsNested() throws IOException {
    String result =
        executeQueryToString(
            String.format("source=%s | stats avg(abs(age)*2) as AGE", TEST_INDEX_ACCOUNT));
    assertEquals(
        "{\n"
            + "  \"schema\": [{\n"
            + "    \"name\": \"AGE\",\n"
            + "    \"type\": \"double\"\n"
            + "  }],\n"
            + "  \"total\": 1,\n"
            + "  \"datarows\": [[60.342]],\n"
            + "  \"size\": 1\n"
            + "}\n",
        result);
  }
}
