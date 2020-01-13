/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.executor.adapter;

import com.amazon.opendistroforelasticsearch.sql.expression.domain.BindingTuple;
import com.amazon.opendistroforelasticsearch.sql.query.planner.core.ColNode;
import com.amazon.opendistroforelasticsearch.sql.query.planner.core.BindingTupleQueryPlanner;
import com.amazon.opendistroforelasticsearch.sql.query.SqlElasticRequestBuilder;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.ActionRequestBuilder;
import org.elasticsearch.action.ActionResponse;

import java.util.List;

/**
 * The definition of QueryPlan SqlElasticRequestBuilder.
 */
@RequiredArgsConstructor
public class QueryPlanRequestBuilder implements SqlElasticRequestBuilder {
    private final BindingTupleQueryPlanner queryPlanner;

    public List<BindingTuple> execute() {
        return queryPlanner.execute();
    }

    public List<ColNode> outputColumns() {
        return queryPlanner.getColNodes();
    }

    @Override
    public String explain() {
        return queryPlanner.explain();
    }

    @Override
    public ActionRequest request() {
        throw new RuntimeException("unsupported operation");
    }

    @Override
    public ActionResponse get() {
        throw new RuntimeException("unsupported operation");
    }

    @Override
    public ActionRequestBuilder getBuilder() {
        throw new RuntimeException("unsupported operation");
    }
}
