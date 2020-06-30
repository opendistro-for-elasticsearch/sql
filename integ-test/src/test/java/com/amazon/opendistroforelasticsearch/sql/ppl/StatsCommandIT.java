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
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_BANK_WITH_NULL_VALUES;

import java.io.IOException;
import org.elasticsearch.client.ResponseException;
import org.junit.jupiter.api.Test;

public class StatsCommandIT extends PPLIntegTestCase {

  @Override
  public void init() throws IOException {
    loadIndex(Index.ACCOUNT);
    loadIndex(Index.BANK_WITH_NULL_VALUES);
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

  @Test
  public void testStatsMin() throws IOException {
    String result =
        executeQueryToString(
            String.format("source=%s | stats min(age)", TEST_INDEX_ACCOUNT));
    assertEquals(
        "{\n"
            + "  \"schema\": [{\n"
            + "    \"name\": \"min(age)\",\n"
            + "    \"type\": \"long\"\n"
            + "  }],\n"
            + "  \"total\": 1,\n"
            + "  \"datarows\": [[20]],\n"
            + "  \"size\": 1\n"
            + "}\n",
        result);
  }

  @Test
  public void testStatsMax() throws IOException {
    String result =
        executeQueryToString(
            String.format("source=%s | stats max(age)", TEST_INDEX_ACCOUNT));
    assertEquals(
        "{\n"
            + "  \"schema\": [{\n"
            + "    \"name\": \"max(age)\",\n"
            + "    \"type\": \"long\"\n"
            + "  }],\n"
            + "  \"total\": 1,\n"
            + "  \"datarows\": [[40]],\n"
            + "  \"size\": 1\n"
            + "}\n",
        result);
  }

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

  @Test
  public void testStatsWithNull() {
    String query = String.format("source=%s | stats avg(age)", TEST_INDEX_BANK_WITH_NULL_VALUES);
    aggregationWithNullOrMissingShouldThrowException(
        query,
        "invalid to call type operation on null value"
    );
  }

  @Test
  public void testStatsWithMissing() {
    String query = String.format(
        "source=%s | stats avg(balance)", TEST_INDEX_BANK_WITH_NULL_VALUES);
    aggregationWithNullOrMissingShouldThrowException(
        query,
        "invalid to call type operation on null value"
    );
  }

  private void aggregationWithNullOrMissingShouldThrowException(String query, String... errMsgs) {
    try {
      executeQuery(query);
      fail();
    } catch (ResponseException e) {
      String message = e.getMessage();
      assertTrue(message.contains("ExpressionEvaluationException"));
      for (String msg: errMsgs) {
        assertTrue(message.contains(msg));
      }
    } catch (IOException e) {
      throw new IllegalStateException("Unexpected exception raised for query: " + query);
    }
  }
}
