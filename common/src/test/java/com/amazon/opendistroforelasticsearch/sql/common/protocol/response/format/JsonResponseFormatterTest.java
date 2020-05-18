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

package com.amazon.opendistroforelasticsearch.sql.common.protocol.response.format;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonResponseFormatterTest {

    @Test
    public void formatResponse() {
        SimpleJsonResponseFormatter formatter = new SimpleJsonResponseFormatter(false);
        assertEquals(
            "{\"schema\":[{\"name\":\"firstname\"}],\"datarows\":[{\"row\":[\"John\"]}]}",
            formatter.format(Collections.emptyList())
        );
    }

    @Test
    public void formatException() {
        SimpleJsonResponseFormatter formatter = new SimpleJsonResponseFormatter(false);
        assertEquals(
            "{\"reason\":\"This is an exception\",\"type\":\"RuntimeException\"}",
            formatter.format(new RuntimeException("This is an exception"))
        );
    }

}