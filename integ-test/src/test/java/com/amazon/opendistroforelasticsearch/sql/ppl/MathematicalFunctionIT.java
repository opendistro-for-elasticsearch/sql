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
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.closeTo;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.rows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.schema;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifyDataRows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifyDataRowsInOrder;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifySchema;

import java.io.IOException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class MathematicalFunctionIT extends PPLIntegTestCase {

  @Override
  public void init() throws IOException {
    loadIndex(Index.BANK);
    loadIndex(Index.BANK_WITH_NULL_VALUES);
  }

  @Test
  public void testAbs() throws IOException {
    JSONObject result =
        executeQuery(
            String.format(
                "source=%s | eval f = abs(age) | fields f", TEST_INDEX_BANK));
    verifySchema(result, schema("f", null, "integer"));
    verifyDataRows(
        result,
        rows(32), rows(36), rows(28), rows(33), rows(36), rows(39), rows(34));
  }

  @Test
  public void testCeil() throws IOException {
    JSONObject result =
        executeQuery(
            String.format(
                "source=%s | eval f = ceil(age) | fields f", TEST_INDEX_BANK));
    verifySchema(result, schema("f", null, "integer"));
    verifyDataRows(
        result,
        rows(32), rows(36), rows(28), rows(33), rows(36), rows(39), rows(34));
  }

  @Test
  public void testCeiling() throws IOException {
    JSONObject result =
        executeQuery(
            String.format(
                "source=%s | eval f = ceiling(age) | fields f", TEST_INDEX_BANK));
    verifySchema(result, schema("f", null, "integer"));
    verifyDataRows(
        result,
        rows(32), rows(36), rows(28), rows(33), rows(36), rows(39), rows(34));
  }

  @Test
  public void testExp() throws IOException {
    JSONObject result =
        executeQuery(
            String.format(
                "source=%s | eval f = exp(age) | fields f", TEST_INDEX_BANK));
    verifySchema(result, schema("f", null, "double"));
    verifyDataRows(
        result, rows(Math.exp(32)), rows(Math.exp(36)), rows(Math.exp(28)), rows(Math.exp(33)),
        rows(Math.exp(36)), rows(Math.exp(39)), rows(Math.exp(34)));
  }

  @Test
  public void testFloor() throws IOException {
    JSONObject result =
        executeQuery(
            String.format(
                "source=%s | eval f = floor(age) | fields f", TEST_INDEX_BANK));
    verifySchema(result, schema("f", null, "integer"));
    verifyDataRows(
        result,
        rows(32), rows(36), rows(28), rows(33), rows(36), rows(39), rows(34));
  }

  @Test
  public void testLn() throws IOException {
    JSONObject result =
        executeQuery(
            String.format(
                "source=%s | eval f = ln(age) | fields f", TEST_INDEX_BANK));
    verifySchema(result, schema("f", null, "double"));
    verifyDataRows(
        result, rows(Math.log(32)), rows(Math.log(36)), rows(Math.log(28)), rows(Math.log(33)),
        rows(Math.log(36)), rows(Math.log(39)), rows(Math.log(34)));
  }

  @Test
  public void testLogOneArg() throws IOException {
    JSONObject result =
        executeQuery(
            String.format(
                "source=%s | eval f = log(age) | fields f", TEST_INDEX_BANK));
    verifySchema(result, schema("f", null, "double"));
    verifyDataRows(result,
        rows(Math.log(28)), rows(Math.log(32)), rows(Math.log(33)), rows(Math.log(34)),
        rows(Math.log(36)), rows(Math.log(36)), rows(Math.log(39))
    );
  }

  @Test
  public void testLogTwoArgs() throws IOException {
    JSONObject result =
        executeQuery(
            String.format(
                "source=%s | eval f = log(age, balance) | fields f", TEST_INDEX_BANK));
    verifySchema(result, schema("f", null, "double"));
    verifyDataRows(
        result, closeTo(Math.log(39225) / Math.log(32)), closeTo(Math.log(5686) / Math.log(36)),
        closeTo(Math.log(32838) / Math.log(28)), closeTo(Math.log(4180) / Math.log(33)),
        closeTo(Math.log(16418) / Math.log(36)), closeTo(Math.log(40540) / Math.log(39)),
        closeTo(Math.log(48086) / Math.log(34)));
  }

  @Test
  public void testLog10() throws IOException {
    JSONObject result =
        executeQuery(
            String.format(
                "source=%s | eval f = log10(age) | fields f", TEST_INDEX_BANK));
    verifySchema(result, schema("f", null, "double"));
    verifyDataRows(
        result, rows(Math.log10(32)), rows(Math.log10(36)), rows(Math.log10(28)),
        rows(Math.log10(33)), rows(Math.log10(36)), rows(Math.log10(39)), rows(Math.log10(34)));
  }

  @Test
  public void testLog2() throws IOException {
    JSONObject result =
        executeQuery(
            String.format(
                "source=%s | eval f = log2(age) | fields f", TEST_INDEX_BANK));
    verifySchema(result, schema("f", null, "double"));
    verifyDataRows(
        result,
        closeTo(Math.log(32) / Math.log(2)), closeTo(Math.log(36) / Math.log(2)),
        closeTo(Math.log(28) / Math.log(2)), closeTo(Math.log(33) / Math.log(2)),
        closeTo(Math.log(36) / Math.log(2)), closeTo(Math.log(39) / Math.log(2)),
        closeTo(Math.log(34) / Math.log(2)));
  }
}
