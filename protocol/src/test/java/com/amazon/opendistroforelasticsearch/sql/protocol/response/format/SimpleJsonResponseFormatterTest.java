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

import com.amazon.opendistroforelasticsearch.sql.protocol.response.QueryResult;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.tupleValue;
import static com.amazon.opendistroforelasticsearch.sql.protocol.response.format.JsonResponseFormatter.Style.COMPACT;
import static com.amazon.opendistroforelasticsearch.sql.protocol.response.format.JsonResponseFormatter.Style.PRETTY;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleJsonResponseFormatterTest {

    @Test
    void formatResponse() {
        QueryResult response = new QueryResult(Arrays.asList(
            tupleValue(ImmutableMap.of("firstname", "John", "age", 20)),
            tupleValue(ImmutableMap.of("firstname", "Smith", "age", 30))
        ));
        SimpleJsonResponseFormatter formatter = new SimpleJsonResponseFormatter(COMPACT);
        assertEquals(
            "{\"schema\":[{\"name\":\"firstname\",\"type\":\"string\"},{\"name\":\"age\",\"type\":\"integer\"}]," +
                "\"total\":2,\"datarows\":[{\"row\":[\"John\",20]},{\"row\":[\"Smith\",30]}],\"size\":2}",
            formatter.format(response)
        );
    }

    @Test
    void formatResponsePretty() {
        QueryResult response = new QueryResult(Arrays.asList(
            tupleValue(ImmutableMap.of("firstname", "John", "age", 20)),
            tupleValue(ImmutableMap.of("firstname", "Smith", "age", 30))
        ));
        SimpleJsonResponseFormatter formatter = new SimpleJsonResponseFormatter(PRETTY);
        assertEquals(
            "{\n" +
            "  \"schema\": [\n" +
            "    {\n" +
            "      \"name\": \"firstname\",\n" +
            "      \"type\": \"string\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"age\",\n" +
            "      \"type\": \"integer\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"total\": 2,\n" +
            "  \"datarows\": [\n" +
            "    {\"row\": [\n" +
            "      \"John\",\n" +
            "      20\n" +
            "    ]},\n" +
            "    {\"row\": [\n" +
            "      \"Smith\",\n" +
            "      30\n" +
            "    ]}\n" +
            "  ],\n" +
            "  \"size\": 2\n" +
            "}",
            formatter.format(response)
        );
    }

    @Disabled("Need to figure out column headers in some other way than inferring from data implicitly")
    @Test
    void formatResponseWithMissingValue() {
        QueryResult response = new QueryResult(Arrays.asList(
            tupleValue(ImmutableMap.of("firstname", "John")),
            tupleValue(ImmutableMap.of("firstname", "Smith", "age", 30))
        ));
        SimpleJsonResponseFormatter formatter = new SimpleJsonResponseFormatter(COMPACT);
        assertEquals(
            "{\"schema\":[{\"name\":\"firstname\",\"type\":\"string\"},{\"name\":\"age\",\"type\":\"integer\"}]," +
                "\"total\":2,\"datarows\":[{\"row\":[\"John\",null]},{\"row\":[\"Smith\",30]}],\"size\":2}",
            formatter.format(response)
        );
    }

    @Test
    void formatError() {
        SimpleJsonResponseFormatter formatter = new SimpleJsonResponseFormatter(COMPACT);
        assertEquals(
            "{\"reason\":\"This is an exception\",\"type\":\"RuntimeException\"}",
            formatter.format(new RuntimeException("This is an exception"))
        );
    }

    @Test
    void formatErrorPretty() {
        SimpleJsonResponseFormatter formatter = new SimpleJsonResponseFormatter(PRETTY);
        assertEquals(
            "{\n" +
            "  \"reason\": \"This is an exception\",\n" +
            "  \"type\": \"RuntimeException\"\n" +
            "}",
            formatter.format(new RuntimeException("This is an exception"))
        );
    }

}