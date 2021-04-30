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
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.stringValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.tupleValue;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.protocol.response.format.JsonResponseFormatter.Style.COMPACT;
import static com.amazon.opendistroforelasticsearch.sql.protocol.response.format.JsonResponseFormatter.Style.PRETTY;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTupleValue;
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine;
import com.amazon.opendistroforelasticsearch.sql.protocol.response.QueryResult;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class SimpleJsonResponseFormatterTest {

  private final ExecutionEngine.Schema schema = new ExecutionEngine.Schema(ImmutableList.of(
      new ExecutionEngine.Schema.Column("firstname", null, STRING),
      new ExecutionEngine.Schema.Column("age", null, INTEGER)));

  @Test
  void formatResponse() {
    QueryResult response =
        new QueryResult(
            schema,
            Arrays.asList(
                tupleValue(ImmutableMap.of("firstname", "John", "age", 20)),
                tupleValue(ImmutableMap.of("firstname", "Smith", "age", 30))));
    SimpleJsonResponseFormatter formatter = new SimpleJsonResponseFormatter(COMPACT);
    assertEquals(
        "{\"schema\":[{\"name\":\"firstname\",\"type\":\"string\"},"
            + "{\"name\":\"age\",\"type\":\"integer\"}],\"datarows\":"
            + "[[\"John\",20],[\"Smith\",30]],\"total\":2,\"size\":2}",
        formatter.format(response));
  }

  @Test
  void formatResponsePretty() {
    QueryResult response =
        new QueryResult(
            schema,
            Arrays.asList(
                tupleValue(ImmutableMap.of("firstname", "John", "age", 20)),
                tupleValue(ImmutableMap.of("firstname", "Smith", "age", 30))));
    SimpleJsonResponseFormatter formatter = new SimpleJsonResponseFormatter(PRETTY);
    assertEquals(
        "{\n"
            + "  \"schema\": [\n"
            + "    {\n"
            + "      \"name\": \"firstname\",\n"
            + "      \"type\": \"string\"\n"
            + "    },\n"
            + "    {\n"
            + "      \"name\": \"age\",\n"
            + "      \"type\": \"integer\"\n"
            + "    }\n"
            + "  ],\n"
            + "  \"datarows\": [\n"
            + "    [\n"
            + "      \"John\",\n"
            + "      20\n"
            + "    ],\n"
            + "    [\n"
            + "      \"Smith\",\n"
            + "      30\n"
            + "    ]\n"
            + "  ],\n"
            + "  \"total\": 2,\n"
            + "  \"size\": 2\n"
            + "}",
        formatter.format(response));
  }

  @Test
  void formatResponseSchemaWithAlias() {
    ExecutionEngine.Schema schema = new ExecutionEngine.Schema(ImmutableList.of(
        new ExecutionEngine.Schema.Column("firstname", "name", STRING)));
    QueryResult response =
        new QueryResult(
            schema,
            ImmutableList.of(tupleValue(ImmutableMap.of("name", "John", "age", 20))));
    SimpleJsonResponseFormatter formatter = new SimpleJsonResponseFormatter(COMPACT);
    assertEquals(
        "{\"schema\":[{\"name\":\"name\",\"type\":\"string\"}],"
            + "\"datarows\":[[\"John\",20]],\"total\":1,\"size\":1}",
        formatter.format(response));
  }

  @Test
  void formatResponseWithMissingValue() {
    QueryResult response =
        new QueryResult(
            schema,
            Arrays.asList(
                ExprTupleValue.fromExprValueMap(
                    ImmutableMap.of("firstname", stringValue("John"), "age", LITERAL_MISSING)),
                tupleValue(ImmutableMap.of("firstname", "Smith", "age", 30))));
    SimpleJsonResponseFormatter formatter = new SimpleJsonResponseFormatter(COMPACT);
    assertEquals(
        "{\"schema\":[{\"name\":\"firstname\",\"type\":\"string\"},"
            + "{\"name\":\"age\",\"type\":\"integer\"}],"
            + "\"datarows\":[[\"John\",null],[\"Smith\",30]],\"total\":2,\"size\":2}",
        formatter.format(response));
  }

  @Test
  void formatResponseWithTupleValue() {
    QueryResult response =
        new QueryResult(
            schema,
            Arrays.asList(
                tupleValue(ImmutableMap
                    .of("name", "Smith",
                        "address", ImmutableMap.of("state", "WA", "street",
                            ImmutableMap.of("city", "seattle"))))));
    SimpleJsonResponseFormatter formatter = new SimpleJsonResponseFormatter(COMPACT);

    assertEquals(
        "{\"schema\":[{\"name\":\"firstname\",\"type\":\"string\"},"
            + "{\"name\":\"age\",\"type\":\"integer\"}],"
            + "\"datarows\":[[\"Smith\",{\"state\":\"WA\",\"street\":{\"city\":\"seattle\"}}]],"
            + "\"total\":1,\"size\":1}",
        formatter.format(response));
  }

  @Test
  void formatResponseWithArrayValue() {
    QueryResult response =
        new QueryResult(
            schema,
            Arrays.asList(
                tupleValue(ImmutableMap
                    .of("name", "Smith",
                        "address", Arrays.asList(
                            ImmutableMap.of("state", "WA"), ImmutableMap.of("state", "NYC")
                        )))));
    SimpleJsonResponseFormatter formatter = new SimpleJsonResponseFormatter(COMPACT);
    assertEquals(
        "{\"schema\":[{\"name\":\"firstname\",\"type\":\"string\"},"
            + "{\"name\":\"age\",\"type\":\"integer\"}],"
            + "\"datarows\":[[\"Smith\",[{\"state\":\"WA\"},{\"state\":\"NYC\"}]]],"
            + "\"total\":1,\"size\":1}",
        formatter.format(response));
  }

  @Test
  void formatError() {
    SimpleJsonResponseFormatter formatter = new SimpleJsonResponseFormatter(COMPACT);
    assertEquals(
        "{\"type\":\"RuntimeException\",\"reason\":\"This is an exception\"}",
        formatter.format(new RuntimeException("This is an exception")));
  }

  @Test
  void formatErrorPretty() {
    SimpleJsonResponseFormatter formatter = new SimpleJsonResponseFormatter(PRETTY);
    assertEquals(
        "{\n"
            + "  \"type\": \"RuntimeException\",\n"
            + "  \"reason\": \"This is an exception\"\n"
            + "}",
        formatter.format(new RuntimeException("This is an exception")));
  }
}
