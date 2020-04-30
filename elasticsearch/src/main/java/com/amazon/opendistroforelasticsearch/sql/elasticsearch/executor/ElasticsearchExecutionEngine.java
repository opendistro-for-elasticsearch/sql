/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.executor;

import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine;
import com.amazon.opendistroforelasticsearch.sql.executor.ResponseListener;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.storage.BindingTuple;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class ElasticsearchExecutionEngine implements ExecutionEngine {

    private final Client client;

    @Override
    public void execute(PhysicalPlan plan, ResponseListener<List<BindingTuple>> listener) {
        Runnable task = () -> {
            try {
                List<BindingTuple> result = new ArrayList<>();
                while (plan.hasNext()) {
                    result.add(plan.next());
                }
                listener.onResponse(result);
            } catch (Exception e) {
                listener.onFailure(e);
            }
        };

        client.threadPool().schedule(task, new TimeValue(0L), "search");
    }
}
