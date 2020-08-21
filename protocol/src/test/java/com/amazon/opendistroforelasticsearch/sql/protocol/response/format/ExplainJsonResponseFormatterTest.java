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

import static com.amazon.opendistroforelasticsearch.sql.protocol.response.format.JsonResponseFormatter.Style.PRETTY;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine.ExplainResponse;
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine.ExplainResponseNode;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

class ExplainJsonResponseFormatterTest {

  @Test
  void explain() {
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

}