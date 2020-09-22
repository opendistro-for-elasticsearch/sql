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
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.rows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.schema;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifyDataRows;
import static com.amazon.opendistroforelasticsearch.sql.util.MatcherUtils.verifySchema;

import java.io.IOException;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;

public class TextCommandIT extends PPLIntegTestCase {
  @Override
  public void init() throws IOException {
    loadIndex(Index.BANK);
    loadIndex(Index.BANK_WITH_NULL_VALUES);
  }

  void verifyQuery(String query, String output) throws IOException {
    JSONObject result = executeQuery(String.format("source=%s | eval v = %s", TEST_INDEX_BANK, query));
    verifySchema(result, schema("f", null, "string"));
    verifyDataRows(result, rows(output));
  }

  void verifyQuery(String query, Integer output) throws IOException {
    JSONObject result = executeQuery(String.format("source=%s | eval v = %s", TEST_INDEX_BANK, query));
    verifySchema(result, schema("f", null, "integer"));
    verifyDataRows(result, rows(output));
  }

  @Test
  public void testRegexp() throws IOException {
    verifyQuery("'a' regexp 'b'", 0);
    verifyQuery("'a' regexp '.*'", 1);
  }
 @Test
  public void testSubstr() throws IOException {
    verifyQuery("substr('hello', 2)", "ello");
    verifyQuery("substr('hello', 2, 2)", "el");
  }

  @Test
  public void testSubstring() throws IOException {
    verifyQuery("substring('hello', 2)", "ello");
    verifyQuery("substring('hello', 2, 2)", "el");
  }

  @Test
  public void testUpper() throws IOException {
    verifyQuery("upper('hello')", "HELLO");
    verifyQuery("upper('HELLO')", "HELLO");
  }

  @Test
  public void testLower() throws IOException {
    verifyQuery("lower('hello')", "hello");
    verifyQuery("lower('HELLO')", "hello");
  }

  @Test
  public void testTrim() throws IOException {
      verifyQuery("trim(' hello')", "hello");
      verifyQuery("trim('hello ')", "hello");
      verifyQuery("trim('  hello  ')", "hello");
  }

  @Test
  public void testRtrim() throws IOException {
    verifyQuery("rtrim(' hello')", " hello");
    verifyQuery("rtrim('hello ')", "hello");
    verifyQuery("rtrim('  hello  ')", "  hello");
  }

  @Test
  public void testLtrim() throws IOException {
    verifyQuery("ltrim(' hello')", "hello");
    verifyQuery("ltrim('hello ')", "hello ");
    verifyQuery("ltrim('  hello  ')", "hello  ");
  }

  @Test
  public void testConcat() throws IOException {
    verifyQuery("concat('hello', 'world')", "helloworld");
    verifyQuery("concat('', 'hello')", "hello");
  }

  @Test
  public void testConcat_ws() throws IOException {
    verifyQuery("concat_ws(',', 'hello', 'world')", "hello,world");
    verifyQuery("concat_ws(',', '', 'hello')", ",hello");
  }

  @Test
  public void testLength() throws IOException {
    verifyQuery("length('hello')", 5);
  }

  @Test
  public void testStrcmp() throws IOException {
    verifyQuery("strcmp('hello', 'world')", -1);
    verifyQuery("strcmp('hello', 'hello')", 0);
  }
}
