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

package com.amazon.opendistroforelasticsearch.sql.ppl;

import com.amazon.opendistroforelasticsearch.sql.DatabaseEngine;
import com.amazon.opendistroforelasticsearch.sql.executor.ResponseListener;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.ppl.domain.PPLQueryRequest;
import com.amazon.opendistroforelasticsearch.sql.storage.BindingTuple;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public class PPLService {

    private final DatabaseEngine queryEnvironment;

    public void execute(PPLQueryRequest request, ResponseListener<List<BindingTuple>> listener) {
        try {
            PhysicalPlan plan = queryEnvironment.getQueryEngine().plan(request.getRequest());
            queryEnvironment.getExecutionEngine().execute(plan, listener);
        } catch (Exception e) {
            listener.onFailure(e);
        }
    }
}
