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

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_MISSING;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_NULL;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.stringValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.tupleValue;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.ARRAY;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRUCT;
import static com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.type.ElasticsearchDataType.ES_TEXT;
import static com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.type.ElasticsearchDataType.ES_TEXT_KEYWORD;
import static com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine.Schema;
import static com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine.Schema.Column;
import static com.amazon.opendistroforelasticsearch.sql.protocol.response.format.JsonResponseFormatter.Style.COMPACT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.opendistroforelasticsearch.sql.common.antlr.SyntaxCheckException;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTupleValue;
import com.amazon.opendistroforelasticsearch.sql.exception.SemanticCheckException;
import com.amazon.opendistroforelasticsearch.sql.protocol.response.QueryResult;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonParser;
import java.util.Arrays;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class JdbcResponseFormatterTest {

  private final JdbcResponseFormatter formatter = new JdbcResponseFormatter(COMPACT);

  @Test
  void format_response() {
    QueryResult response = new QueryResult(
        new Schema(ImmutableList.of(
            new Column("name", "name", STRING),
            new Column("address1", "address1", ES_TEXT),
            new Column("address2", "address2", ES_TEXT_KEYWORD),
            new Column("location", "location", STRUCT),
            new Column("employer", "employer", ARRAY),
            new Column("age", "age", INTEGER))),
        ImmutableList.of(
            tupleValue(ImmutableMap.<String, Object>builder()
                    .put("name", "John")
                    .put("address1", "Seattle")
                    .put("address2", "WA")
                    .put("location", ImmutableMap.of("x", "1", "y", "2"))
                    .put("employments", ImmutableList.of(
                        ImmutableMap.of("name", "Amazon"),
                        ImmutableMap.of("name", "AWS")))
                    .put("age", 20)
                .build())));

    assertJsonEquals(
        "{"
            + "\"schema\":["
            + "{\"name\":\"name\",\"alias\":\"name\",\"type\":\"keyword\"},"
            + "{\"name\":\"address1\",\"alias\":\"address1\",\"type\":\"text\"},"
            + "{\"name\":\"address2\",\"alias\":\"address2\",\"type\":\"text\"},"
            + "{\"name\":\"location\",\"alias\":\"location\",\"type\":\"object\"},"
            + "{\"name\":\"employer\",\"alias\":\"employer\",\"type\":\"nested\"},"
            + "{\"name\":\"age\",\"alias\":\"age\",\"type\":\"integer\"}"
            + "],"
            + "\"datarows\":["
            + "[\"John\",\"Seattle\",\"WA\",{\"x\":\"1\",\"y\":\"2\"},"
            + "[{\"name\":\"Amazon\"}," + "{\"name\":\"AWS\"}],"
            + "20]],"
            + "\"total\":1,"
            + "\"size\":1,"
            + "\"status\":200}",
        formatter.format(response));
  }

  @Test
  void format_response_with_missing_and_null_value() {
    QueryResult response =
        new QueryResult(
            new Schema(ImmutableList.of(
                new Column("name", null, STRING),
                new Column("age", null, INTEGER))),
            Arrays.asList(
                ExprTupleValue.fromExprValueMap(
                    ImmutableMap.of("name", stringValue("John"), "age", LITERAL_MISSING)),
                ExprTupleValue.fromExprValueMap(
                    ImmutableMap.of("name", stringValue("Allen"), "age", LITERAL_NULL)),
                tupleValue(ImmutableMap.of("name", "Smith", "age", 30))));

    assertEquals(
        "{\"schema\":[{\"name\":\"name\",\"type\":\"keyword\"},"
            + "{\"name\":\"age\",\"type\":\"integer\"}],"
            + "\"datarows\":[[\"John\",null],[\"Allen\",null],"
            + "[\"Smith\",30]],\"total\":3,\"size\":3,\"status\":200}",
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
            + "\"status\":503}",
        formatter.format(new IllegalStateException("Execution error"))
    );
  }

  private static void assertJsonEquals(String expected, String actual) {
    assertEquals(
        JsonParser.parseString(expected),
        JsonParser.parseString(actual));
  }

}