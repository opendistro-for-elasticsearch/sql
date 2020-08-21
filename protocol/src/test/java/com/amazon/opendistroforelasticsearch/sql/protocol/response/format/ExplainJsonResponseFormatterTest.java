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

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.ref;
import static com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName.AVG;
import static com.amazon.opendistroforelasticsearch.sql.protocol.response.format.JsonResponseFormatter.Style.PRETTY;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine.ExplainResponse;
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine.ExplainResponseNode;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.Aggregator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class ExplainJsonResponseFormatterTest {

  @Test
  void can_format_explain_response_for_project_filter_table_scan() {
    ExplainJsonResponseFormatter formatter = new ExplainJsonResponseFormatter(PRETTY);
    assertEquals(
        "{\"ProjectOperator\": {\n"
            + "  \"FakeTableScan\": {\"description\": {\"request\": \"Fake request\"}},\n"
            + "  \"description\": {\"fields\": [\"name\"]}\n"
            + "}}",
        formatter.format(
            new ExplainResponse(
                new ExplainResponseNode(
                    "ProjectOperator",
                    ImmutableMap.of("fields", Arrays.asList("name")),
                    new ExplainResponseNode(
                        "FakeTableScan",
                        ImmutableMap.of("request", "Fake request"),
                        null)))));
  }

  @Test
  void can_format_explain_response_for_aggregations() {
    List<Expression> aggExprs = ImmutableList.of(ref("balance", DOUBLE));
    List<Expression> groupByList = ImmutableList.of(ref("state", STRING));

    ExplainJsonResponseFormatter formatter = new ExplainJsonResponseFormatter(PRETTY);
    assertEquals(
        "",
        formatter.format(
            new ExplainResponse(
                new ExplainResponseNode(
                    "AggregationOperator",
                    ImmutableMap.of(
                        "aggregators", ImmutableMap.of(AVG.getName(), aggExprs),
                        "groupBy", groupByList),
                    new ExplainResponseNode(
                        "FakeTableScan",
                        ImmutableMap.of("request", "Fake DSL request"),
                        null)))));
  }

}