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
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Elasticsearch execution engine implementation.
 */
@RequiredArgsConstructor
public class ElasticsearchExecutionEngine implements ExecutionEngine<List<ExprValue>> {

    private final ElasticsearchClient client;

    @Override
    public void execute(PhysicalPlan plan, ResponseListener<List<ExprValue>> listener) {
        client.schedule(() -> {
            try {
                List<ExprValue> result = new ArrayList<>();
                while (plan.hasNext()) {
                    result.add(plan.next());
                }
                listener.onResponse(result);
            } catch (Exception e) {
                listener.onFailure(e);
            }
        });
    }

}
