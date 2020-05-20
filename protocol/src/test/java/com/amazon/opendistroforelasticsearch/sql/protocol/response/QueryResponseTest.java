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

package com.amazon.opendistroforelasticsearch.sql.protocol.response;

import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.tupleValue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class QueryResponseTest {

    @Test
    void size() {
        QueryResponse response = new QueryResponse(Arrays.asList(
            tupleValue(ImmutableMap.of("name", "John", "age", 20)),
            tupleValue(ImmutableMap.of("name", "Allen", "age", 30)),
            tupleValue(ImmutableMap.of("name", "Smith", "age", 40))
        ));
        assertEquals(3, response.size());
    }

    @Test
    void columnNameTypes() {
        QueryResponse response = new QueryResponse(Collections.singletonList(
            tupleValue(ImmutableMap.of("name", "John", "age", 20))
        ));

        assertEquals(
            ImmutableMap.of("name", "string", "age", "integer"),
            response.columnNameTypes()
        );
    }

    @Test
    void columnNameTypesFromEmptyExprValues() {
        QueryResponse response = new QueryResponse(Collections.emptyList());
        assertTrue(response.columnNameTypes().isEmpty());
    }

    @Test
    void iterate() {
        QueryResponse response = new QueryResponse(Arrays.asList(
            tupleValue(ImmutableMap.of("name", "John", "age", 20)),
            tupleValue(ImmutableMap.of("name", "Allen", "age", 30))
        ));

        int i = 0;
        for (Object[] objects : response) {
            if (i == 0) {
                assertArrayEquals(new Object[]{"John", 20}, objects);
            } else if (i == 1) {
                assertArrayEquals(new Object[]{"Allen", 30}, objects);
            } else {
                fail("More rows returned than expected");
            }
            i++;
        }
    }

}