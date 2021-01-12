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

package com.amazon.opendistroforelasticsearch.sql.protocol.response.format;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_MISSING;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_NULL;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.stringValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.tupleValue;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTupleValue;
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine;
import com.amazon.opendistroforelasticsearch.sql.protocol.response.QueryResult;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link CsvResponseFormatter}.
 */
public class CsvResponseFormatterTest {
  private static final CsvResponseFormatter formatter = new CsvResponseFormatter();

  @Test
  void formatResponse() {
    ExecutionEngine.Schema schema = new ExecutionEngine.Schema(ImmutableList.of(
        new ExecutionEngine.Schema.Column("name", "name", STRING),
        new ExecutionEngine.Schema.Column("age", "age", INTEGER)));
    QueryResult response = new QueryResult(schema, Arrays.asList(
        tupleValue(ImmutableMap.of("name", "John", "age", 20)),
        tupleValue(ImmutableMap.of("name", "Smith", "age", 30))));
    CsvResponseFormatter formatter = new CsvResponseFormatter();
    String expected = "name,age\nJohn,20\nSmith,30";
    assertEquals(expected, formatter.format(response));
  }

  @Test
  void sanitizeHeaders() {
    ExecutionEngine.Schema schema = new ExecutionEngine.Schema(ImmutableList.of(
        new ExecutionEngine.Schema.Column("=firstname", null, STRING),
        new ExecutionEngine.Schema.Column("+lastname", null, STRING),
        new ExecutionEngine.Schema.Column("-city", null, STRING),
        new ExecutionEngine.Schema.Column("@age", null, INTEGER)));
    QueryResult response = new QueryResult(schema, Arrays.asList(
        tupleValue(ImmutableMap.of(
            "=firstname", "John", "+lastname", "Smith", "-city", "Seattle", "@age", 20))));
    String expected = "'=firstname,'+lastname,'-city,'@age\n"
        + "John,Smith,Seattle,20";
    assertEquals(expected, formatter.format(response));
  }

  @Test
  void sanitizeData() {
    ExecutionEngine.Schema schema = new ExecutionEngine.Schema(ImmutableList.of(
        new ExecutionEngine.Schema.Column("city", "city", STRING)));
    QueryResult response = new QueryResult(schema, Arrays.asList(
        tupleValue(ImmutableMap.of("city", "Seattle")),
        tupleValue(ImmutableMap.of("city", "=Seattle")),
        tupleValue(ImmutableMap.of("city", "+Seattle")),
        tupleValue(ImmutableMap.of("city", "-Seattle")),
        tupleValue(ImmutableMap.of("city", "@Seattle")),
        tupleValue(ImmutableMap.of("city", "Seattle="))));
    String expected = "city\n"
        + "Seattle\n"
        + "'=Seattle\n"
        + "'+Seattle\n"
        + "'-Seattle\n"
        + "'@Seattle\n"
        + "Seattle=";
    assertEquals(expected, formatter.format(response));
  }

  @Test
  void quoteIfRequired() {
    ExecutionEngine.Schema schema = new ExecutionEngine.Schema(ImmutableList.of(
        new ExecutionEngine.Schema.Column("na,me", "na,me", STRING),
        new ExecutionEngine.Schema.Column(",,age", ",,age", INTEGER)));
    QueryResult response = new QueryResult(schema, Arrays.asList(
        tupleValue(ImmutableMap.of("na,me", "John,Smith", ",,age", "30,,,"))));
    String expected = "\"na,me\",\",,age\"\n"
        + "\"John,Smith\",\"30,,,\"";
    assertEquals(expected, formatter.format(response));
  }

  @Test
  void formatError() {
    Throwable t = new RuntimeException("This is an exception");
    String expected =
        "{\n  \"type\": \"RuntimeException\",\n  \"reason\": \"This is an exception\"\n}";
    assertEquals(expected, formatter.format(t));
  }

  @Test
  void escapeSanitize() {
    CsvResponseFormatter escapeFormatter = new CsvResponseFormatter(false);
    ExecutionEngine.Schema schema = new ExecutionEngine.Schema(ImmutableList.of(
        new ExecutionEngine.Schema.Column("city", "city", STRING)));
    QueryResult response = new QueryResult(schema, Arrays.asList(
        tupleValue(ImmutableMap.of("city", "=Seattle")),
        tupleValue(ImmutableMap.of("city", ",,Seattle"))));
    String expected = "city\n"
        + "=Seattle\n"
        + ",,Seattle";
    assertEquals(expected, escapeFormatter.format(response));
  }

  @Test
  void replaceNullValues() {
    ExecutionEngine.Schema schema = new ExecutionEngine.Schema(ImmutableList.of(
        new ExecutionEngine.Schema.Column("name", "name", STRING),
        new ExecutionEngine.Schema.Column("city", "city", STRING)));
    QueryResult response = new QueryResult(schema, Arrays.asList(
        tupleValue(ImmutableMap.of("name", "John","city", "Seattle")),
        ExprTupleValue.fromExprValueMap(
            ImmutableMap.of("firstname", LITERAL_NULL, "city", stringValue("Seattle"))),
        ExprTupleValue.fromExprValueMap(
            ImmutableMap.of("firstname", stringValue("John"), "city", LITERAL_MISSING))));
    String expected = "name,city\n"
        + "John,Seattle\n"
        + ",Seattle\n"
        + "John,";
    assertEquals(expected, formatter.format(response));
  }

}
