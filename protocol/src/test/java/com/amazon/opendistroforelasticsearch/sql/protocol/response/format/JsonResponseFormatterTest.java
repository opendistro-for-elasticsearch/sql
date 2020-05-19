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

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static com.amazon.opendistroforelasticsearch.sql.protocol.response.format.JsonResponseFormatter.Style.COMPACT;
import static com.amazon.opendistroforelasticsearch.sql.protocol.response.format.JsonResponseFormatter.Style.PRETTY;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonResponseFormatterTest {

    @Test
    public void formatResponse() {
        SimpleJsonResponseFormatter formatter = new SimpleJsonResponseFormatter(COMPACT);
        assertEquals(
            "{\"schema\":[{\"name\":\"firstname\"}],\"datarows\":[{\"row\":[\"John\"]}]}",
            formatter.format(Collections.emptyList())
        );
    }

    @Test
    public void formatResponsePretty() {
        SimpleJsonResponseFormatter formatter = new SimpleJsonResponseFormatter(PRETTY);
        assertEquals(
            "{\n" +
            "  \"schema\": [{\"name\": \"firstname\"}],\n" +
            "  \"datarows\": [{\"row\": [\"John\"]}]\n" +
            "}",
            formatter.format(Collections.emptyList())
        );
    }

    @Test
    public void formatError() {
        SimpleJsonResponseFormatter formatter = new SimpleJsonResponseFormatter(COMPACT);
        assertEquals(
            "{\"reason\":\"This is an exception\",\"type\":\"RuntimeException\"}",
            formatter.format(new RuntimeException("This is an exception"))
        );
    }

    @Test
    public void formatErrorPretty() {
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