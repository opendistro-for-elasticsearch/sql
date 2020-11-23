/*
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.protocol.response.format;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.tupleValue;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.protocol.response.format.JsonResponseFormatter.Style.COMPACT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.opendistroforelasticsearch.sql.common.antlr.SyntaxCheckException;
import com.amazon.opendistroforelasticsearch.sql.exception.SemanticCheckException;
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine;
import com.amazon.opendistroforelasticsearch.sql.protocol.response.QueryResult;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

class JdbcResponseFormatterTest {

  private final JdbcResponseFormatter formatter = new JdbcResponseFormatter(COMPACT);

  @Test
  void format_response() {
    QueryResult response = new QueryResult(
        new ExecutionEngine.Schema(ImmutableList.of(
            new ExecutionEngine.Schema.Column("name", "name", STRING),
            new ExecutionEngine.Schema.Column("age", "name", INTEGER))),
        ImmutableList.of(
            tupleValue(ImmutableMap.of("name", "John", "age", 20))));

    assertJsonEquals(
        "{"
            + "\"schema\":["
            + "{\"name\":\"name\",\"alias\":\"name\",\"type\":\"text\"},"
            + "{\"name\":\"age\",\"alias\":\"name\",\"type\":\"integer\"}"
            + "],"
            + "\"datarows\":[[\"John\",20]],"
            + "\"version\":\"2.0\","
            + "\"total\":1,"
            + "\"size\":1,"
            + "\"status\":200}",
        formatter.format(response));
  }

  @Test
  void format_client_error_response_due_to_syntax_exception() {
    assertJsonEquals(
        "{\"error\":"
            + "{\""
            + "type\":\"SyntaxCheckException\","
            + "\"reason\":\"Invalid query syntax\","
            + "\"details\":\"Invalid query syntax\""
            + "},"
            + "\"status\":400}",
        formatter.format(new SyntaxCheckException("Invalid query syntax"))
    );
  }

  @Test
  void format_client_error_response_due_to_semantic_exception() {
    assertJsonEquals(
        "{\"error\":"
            + "{\""
            + "type\":\"SemanticCheckException\","
            + "\"reason\":\"Invalid query semantics\","
            + "\"details\":\"Invalid query semantics\""
            + "},"
            + "\"status\":400}",
        formatter.format(new SemanticCheckException("Invalid query semantics"))
    );
  }

  @Test
  void format_server_error_response() {
    assertJsonEquals(
        "{\"error\":"
            + "{\""
            + "type\":\"IllegalStateException\","
            + "\"reason\":\"Execution error\","
            + "\"details\":\"Execution error\""
            + "},"
            + "\"status\":500}",
        formatter.format(new IllegalStateException("Execution error"))
    );
  }

  private static void assertJsonEquals(String expected, String actual) {
    assertEquals(
        JsonParser.parseString(expected),
        JsonParser.parseString(actual));
  }

}