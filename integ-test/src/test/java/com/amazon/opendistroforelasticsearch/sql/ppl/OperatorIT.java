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

import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_BANK;
import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_BANK_WITH_NULL_VALUES;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.rows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifyDataRows;

import java.io.IOException;
import org.elasticsearch.client.ResponseException;
import org.hamcrest.Matchers;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class OperatorIT extends PPLIntegTestCase {
  @Override
  public void init() throws IOException {
    loadIndex(Index.BANK);
    loadIndex(Index.BANK_WITH_NULL_VALUES);
  }

  @Test
  public void testAddOperator() throws IOException {
    JSONObject result =
        executeQuery(
            String.format(
                "source=%s | where age = 31 + 1 | fields age",
                TEST_INDEX_BANK));
    verifyDataRows(result, rows(32));
  }

  @Test
  public void testSubtractOperator() throws IOException {
    JSONObject result =
        executeQuery(
            String.format(
                "source=%s | where age = 33 - 1 | fields age",
                TEST_INDEX_BANK));
    verifyDataRows(result, rows(32));
  }

  @Test
  public void testMultiplyOperator() throws IOException {
    JSONObject result =
        executeQuery(
            String.format(
                "source=%s | where age = 16 * 2 | fields age",
                TEST_INDEX_BANK));
    verifyDataRows(result, rows(32));
  }

  @Test
  public void testDivideOperator() throws IOException {
    JSONObject result =
        executeQuery(
            String.format(
                "source=%s | where age / 2 = 16 | fields age",
                TEST_INDEX_BANK));
    verifyDataRows(result, rows(32), rows(33));
  }

  @Test
  public void testModuleOperator() throws IOException {
    JSONObject result =
        executeQuery(
            String.format(
                "source=%s | where age %s 32 = 0 | fields age",
                TEST_INDEX_BANK, "%"));
    verifyDataRows(result, rows(32));
  }

  @Test
  public void testArithmeticOperatorWithNullValue() throws IOException {
    String result =
        executeQueryToString(
            String.format(
                "source=%s | eval f = age + 0 | fields f",
                TEST_INDEX_BANK_WITH_NULL_VALUES));
    assertEquals(
        "{\n"
            + "  \"schema\": [{\n"
            + "    \"name\": \"f\",\n"
            + "    \"type\": \"integer\"\n"
            + "  }],\n"
            + "  \"total\": 7,\n"
            + "  \"datarows\": [\n"
            + "    [32],\n"
            + "    [36],\n"
            + "    [28],\n"
            + "    [33],\n"
            + "    [36],\n"
            + "    [null],\n"
            + "    [34]\n"
            + "  ],\n"
            + "  \"size\": 7\n"
            + "}\n",
        result);
  }

  @Test
  public void testArithmeticOperatorWithMissingValue() throws IOException {
    JSONObject result =
        executeQuery(
            String.format(
                "source=%s | eval f = balance * 1 | fields f", TEST_INDEX_BANK_WITH_NULL_VALUES));
    verifyDataRows(
        result, rows(39225), rows(32838), rows(4180), rows(48086), rows(), rows(), rows());
  }

  @Test
  public void testMultipleArithmeticOperators() throws IOException {
    JSONObject result =
        executeQuery(
            String.format(
                "source=%s | where (age+2) * 3 / 2 - 1 = 50 | fields age",
                TEST_INDEX_BANK));
    verifyDataRows(result, rows(32));
  }

  @Test
  public void testAndOperator() throws IOException {
    JSONObject result =
        executeQuery(
            String.format(
                "source=%s | where firstname='Amber JOHnny' and age=32 | fields firstname, age",
                TEST_INDEX_BANK));
    verifyDataRows(result, rows("Amber JOHnny", 32));

    result =
        executeQuery(
            String.format(
                "source=%s | where age=32 and firstname='Amber JOHnny' | fields firstname, age",
                TEST_INDEX_BANK));
    verifyDataRows(result, rows("Amber JOHnny", 32));
  }

  @Test
  public void testOrOperator() throws IOException {
    JSONObject result =
        executeQuery(
            String.format(
                "source=%s | where age=32 or age=34 | fields age", TEST_INDEX_BANK));
    verifyDataRows(result, rows(32), rows(34));

    result =
        executeQuery(
            String.format(
                "source=%s | where age=34 or age=32| fields age", TEST_INDEX_BANK));
    verifyDataRows(result, rows(32), rows(34));
  }

  @Test
  public void testXorOperator() throws IOException {
    JSONObject result =
        executeQuery(
            String.format(
                "source=%s | where firstname='Hattie' xor age=36 | fields firstname, age",
                TEST_INDEX_BANK));
    verifyDataRows(result, rows("Elinor", 36));

    result =
        executeQuery(
            String.format(
                "source=%s | where age=36 xor firstname='Hattie' | fields firstname, age",
                TEST_INDEX_BANK));
    verifyDataRows(result, rows("Elinor", 36));
  }

  @Test
  public void testNotOperator() throws IOException {
    JSONObject result =
        executeQuery(
            String.format(
                "source=%s not age > 32 | fields age",
                TEST_INDEX_BANK));
    verifyDataRows(result, rows(28), rows(32));
  }

  @Test
  public void testEqualOperator() throws IOException {
    JSONObject result =
        executeQuery(
            String.format(
                "source=%s age = 32 | fields age",
                TEST_INDEX_BANK));
    verifyDataRows(result, rows(32));

    result =
        executeQuery(
            String.format(
                "source=%s 32 = age | fields age",
                TEST_INDEX_BANK));
    verifyDataRows(result, rows(32));
  }

  @Test
  public void testNotEqualOperator() throws IOException {
    JSONObject result =
        executeQuery(
            String.format(
                "source=%s age != 32 | fields age",
                TEST_INDEX_BANK));
    verifyDataRows(result, rows(28), rows(33), rows(34), rows(36), rows(36), rows(39));

    result =
        executeQuery(
            String.format(
                "source=%s 32 != age | fields age",
                TEST_INDEX_BANK));
    verifyDataRows(result, rows(28), rows(33), rows(34), rows(36), rows(36), rows(39));
  }

  @Test
  public void testLessOperator() throws IOException {
    JSONObject result =
        executeQuery(
            String.format(
                "source=%s age < 32 | fields age",
                TEST_INDEX_BANK));
    verifyDataRows(result, rows(28));
  }

  @Test
  public void testLteOperator() throws IOException {
    JSONObject result =
        executeQuery(
            String.format(
                "source=%s age <= 32 | fields age",
                TEST_INDEX_BANK));
    verifyDataRows(result, rows(28), rows(32));
  }

  @Test
  public void testGreaterOperator() throws IOException {
    JSONObject result =
        executeQuery(
            String.format(
                "source=%s age > 36 | fields age",
                TEST_INDEX_BANK));
    verifyDataRows(result, rows(39));
  }

  @Test
  public void testGteOperator() throws IOException {
    JSONObject result =
        executeQuery(
            String.format(
                "source=%s age >= 36 | fields age",
                TEST_INDEX_BANK));
    verifyDataRows(result, rows(36), rows(36), rows(39));
  }

  @Test
  public void testLikeOperator() throws IOException {
    JSONObject result =
        executeQuery(
            String.format("source=%s firstname like 'Hatti_' | fields firstname", TEST_INDEX_BANK));
    verifyDataRows(result, rows("Hattie"));
  }

  @Test
  public void testBinaryPredicateWithNullValue() throws IOException {
    JSONObject result =
        executeQuery(
            String.format("source=%s | where age >= 36 | fields age",
                TEST_INDEX_BANK_WITH_NULL_VALUES));
    verifyDataRows(result, rows(36), rows(36));
  }

  @Test
  public void testBinaryPredicateWithMissingValue() throws IOException {
    JSONObject result =
        executeQuery(
            String.format("source=%s | where balance > 40000 | fields balance",
                TEST_INDEX_BANK_WITH_NULL_VALUES));
    verifyDataRows(result, rows(48086));
  }

  private void queryExecutionShouldThrowExceptionDueToNullOrMissingValue(
      String query, String... errorMsgs) {
    try {
      executeQuery(query);
      fail("Expected to throw ExpressionEvaluationException, but none was thrown for query: "
          + query);
    } catch (ResponseException e) {
      String errorMsg = e.getMessage();
      assertTrue(errorMsg.contains("ExpressionEvaluationException"));
      for (String msg: errorMsgs) {
        assertTrue(errorMsg.contains(msg));
      }
    } catch (IOException e) {
      throw new IllegalStateException("Unexpected exception raised for query: " + query);
    }
  }
}
