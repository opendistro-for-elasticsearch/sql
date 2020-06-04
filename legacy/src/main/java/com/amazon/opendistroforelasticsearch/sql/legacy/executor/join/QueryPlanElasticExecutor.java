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

package com.amazon.opendistroforelasticsearch.sql.legacy.executor.join;

import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.HashJoinQueryPlanRequestBuilder;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core.QueryPlanner;
import org.elasticsearch.search.SearchHit;

import java.util.List;

/**
 * Executor for generic QueryPlanner execution. This executor is just acting as adaptor to integrate with
 * existing framework. In future, QueryPlanner should be executed by itself and leave the response sent back
 * or other post-processing logic to ElasticDefaultRestExecutor.
 */
class QueryPlanElasticExecutor extends ElasticJoinExecutor {

    private final QueryPlanner queryPlanner;

    QueryPlanElasticExecutor(HashJoinQueryPlanRequestBuilder request) {
        super(request);
        this.queryPlanner = request.plan();
    }

    @Override
    protected List<SearchHit> innerRun() {
        List<SearchHit> result = queryPlanner.execute();
        populateMetaResult();
        return result;
    }

    private void populateMetaResult() {
        metaResults.addTotalNumOfShards(queryPlanner.getMetaResult().getTotalNumOfShards());
        metaResults.addSuccessfulShards(queryPlanner.getMetaResult().getSuccessfulShards());
        metaResults.addFailedShards(queryPlanner.getMetaResult().getFailedShards());
        metaResults.updateTimeOut(queryPlanner.getMetaResult().isTimedOut());
    }

}
