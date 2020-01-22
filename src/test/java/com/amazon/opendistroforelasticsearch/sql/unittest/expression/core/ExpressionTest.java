/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.unittest.expression.core;

import com.amazon.opendistroforelasticsearch.sql.expression.domain.BindingTuple;
import org.json.JSONObject;

public class ExpressionTest {
    protected BindingTuple bindingTuple() {
        String json = "{\n" +
                      "  \"intValue\": 1,\n" +
                      "  \"doubleValue\": 2.0,\n" +
                      "  \"stringValue\": \"string\",\n" +
                      "  \"booleanValue\": true,\n" +
                      "  \"tupleValue\": {\n" +
                      "    \"intValue\": 1,\n" +
                      "    \"doubleValue\": 2.0,\n" +
                      "    \"stringValue\": \"string\"\n" +
                      "  },\n" +
                      "  \"collectValue\": [\n" +
                      "    1,\n" +
                      "    2,\n" +
                      "    3\n" +
                      "  ]\n" +
                      "}";
        return BindingTuple.from(new JSONObject(json));
    }
}
