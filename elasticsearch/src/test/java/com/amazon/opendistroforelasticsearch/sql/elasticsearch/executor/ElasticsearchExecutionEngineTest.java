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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.executor;

import com.amazon.opendistroforelasticsearch.sql.common.response.ResponseListener;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.client.ElasticsearchClient;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.storage.TableScanOperator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.tupleValue;
import static com.google.common.collect.ImmutableMap.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@ExtendWith(MockitoExtension.class)
class ElasticsearchExecutionEngineTest {

    @Mock
    private ElasticsearchClient client;

    @BeforeEach
    void setUp() {
        doAnswer(invocation -> {
            // Run task immediately
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(client).schedule(any());
    }

    @Test
    void execute() {
        List<ExprValue> expected = Arrays.asList(
            tupleValue(of("name", "John", "age", 20)),
            tupleValue(of("name", "Allen", "age", 30))
        );
        PhysicalPlan plan = mockPlan(expected);

        ElasticsearchExecutionEngine executor = new ElasticsearchExecutionEngine(client);
        List<ExprValue> actual = new ArrayList<>();
        executor.execute(plan, new ResponseListener<List<ExprValue>>() {
            @Override
            public void onResponse(List<ExprValue> values) {
                actual.addAll(values);
            }

            @Override
            public void onFailure(Exception e) {
                fail("Error occurred during execution", e);
            }
        });
        assertEquals(expected, actual);
    }

    private PhysicalPlan mockPlan(List<ExprValue> values) {
        return new TableScanOperator() {
            private final Iterator<ExprValue> it = values.iterator();

            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public ExprValue next() {
                return it.next();
            }
        };
    }

}