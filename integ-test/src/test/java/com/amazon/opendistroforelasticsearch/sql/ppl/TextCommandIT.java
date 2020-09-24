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

import static com.amazon.opendistroforelasticsearch.sql.legacy.TestsConstants.TEST_INDEX_STRINGS;
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
    loadIndex(Index.BANK_WITH_STRING_VALUES);
  }

  void verifyQuery(String command, String initialArgs, String additionalArgs,
                   String outputRow1, String outputRow2, String outputRow3) throws IOException {
    String query = String.format(
        "source=%s | eval f=%s(%sname%s) | fields f", TEST_INDEX_STRINGS, command, initialArgs, additionalArgs);
    JSONObject result = executeQuery(query);
    verifySchema(result, schema("f", null, "string"));
    verifyDataRows(result, rows(outputRow1), rows(outputRow2), rows(outputRow3));
  }

  void verifyQuery(String command, String initialArgs, String additionalArgs,
                   Integer outputRow1, Integer outputRow2, Integer outputRow3) throws IOException {
    String query = String.format(
        "source=%s | eval f=%s(%sname%s) | fields f", TEST_INDEX_STRINGS, command, initialArgs, additionalArgs);
    JSONObject result = executeQuery(query);
    verifySchema(result, schema("f", null, "integer"));
    verifyDataRows(result, rows(outputRow1), rows(outputRow2), rows(outputRow3));
  }

  void verifyRegexQuery(String pattern, Integer outputRow1, Integer outputRow2, Integer outputRow3) throws IOException {
    String query = String.format(
        "source=%s | eval f=name regexp '%s' | fields f", TEST_INDEX_STRINGS, pattern);
    JSONObject result = executeQuery(query);
    verifySchema(result, schema("f", null, "integer"));
    verifyDataRows(result, rows(outputRow1), rows(outputRow2), rows(outputRow3));
  }

  @Test
  public void testRegexp() throws IOException {
    verifyRegexQuery("hello", 1, 0, 0);
    verifyRegexQuery(".*", 1, 1, 1);
  }

 @Test
  public void testSubstr() throws IOException {
    verifyQuery("substr", "", ", 2", "ello", "orld", "elloworld");
    verifyQuery("substr", "", ", 2, 2", "el", "or", "el");
  }

  @Test
  public void testSubstring() throws IOException {
    verifyQuery("substring", "", ", 2", "ello", "orld", "elloworld");
    verifyQuery("substring", "", ", 2, 2", "el", "or", "el");
  }

  @Test
  public void testUpper() throws IOException {
    verifyQuery("upper", "", "", "HELLO", "WORLD", "HELLOWORLD");
  }

  @Test
  public void testLower() throws IOException {
    verifyQuery("lower", "", "", "hello", "world", "helloworld");
  }

  @Test
  public void testTrim() throws IOException {
    verifyQuery("trim", "", "", "hello", "world", "helloworld");
  }

  @Test
  public void testRtrim() throws IOException {
    verifyQuery("rtrim", "", "", "hello", "world", "helloworld");
  }

  @Test
  public void testLtrim() throws IOException {
    verifyQuery("ltrim", "", "", "hello", "world", "helloworld");
  }

  @Test
  public void testConcat() throws IOException {
    verifyQuery("concat", "", ", 'there'",
        "hellothere", "worldthere", "helloworldthere");
  }

  @Test
  public void testConcat_ws() throws IOException {
    verifyQuery("concat_ws", "',', ", ", 'there'",
        "hello,there", "world,there", "helloworld,there");
  }

  @Test
  public void testLength() throws IOException {
    verifyQuery("length", "", "", 5, 5, 10);
  }

  @Test
  public void testStrcmp() throws IOException {
    verifyQuery("strcmp", "", ", 'world'", -1, 0, -1);
  }
}
