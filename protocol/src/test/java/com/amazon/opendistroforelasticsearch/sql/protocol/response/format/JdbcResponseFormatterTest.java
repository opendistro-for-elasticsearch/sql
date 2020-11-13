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
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.protocol.response.format.JsonResponseFormatter.Style.COMPACT;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine;
import com.amazon.opendistroforelasticsearch.sql.protocol.response.QueryResult;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

class JdbcResponseFormatterTest {

  @Test
  void format_response_pretty() {
    QueryResult response = new QueryResult(
        new ExecutionEngine.Schema(ImmutableList.of(
            new ExecutionEngine.Schema.Column("firstname", "name", STRING))),
        ImmutableList.of(
            tupleValue(ImmutableMap.of("firstname", "John", "age", 20))));

    JdbcResponseFormatter formatter = new JdbcResponseFormatter(COMPACT);

    assertEquals(
        JsonParser.parseString(
            "{"
                + "\"schema\":[{\"name\":\"firstname\",\"alias\":\"name\",\"type\":\"text\"}],"
                + "\"datarows\":[[\"John\",20]],"
                + "\"version\":\"v2.0\","
                + "\"total\":1,"
                + "\"size\":1,"
                + "\"status\":200}"),
        JsonParser.parseString(formatter.format(response)));
  }

}