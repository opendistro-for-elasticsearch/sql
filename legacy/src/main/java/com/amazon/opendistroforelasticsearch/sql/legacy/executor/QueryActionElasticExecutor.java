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

package com.amazon.opendistroforelasticsearch.sql.legacy.executor;

import com.amazon.opendistroforelasticsearch.sql.legacy.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.join.ElasticJoinExecutor;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.multi.MultiRequestExecutorFactory;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.adapter.QueryPlanQueryAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.adapter.QueryPlanRequestBuilder;
import com.amazon.opendistroforelasticsearch.sql.legacy.expression.domain.BindingTuple;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.AggregationQueryAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.DefaultQueryAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.DeleteQueryAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.DescribeQueryAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.QueryAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.ShowQueryAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.SqlElasticRequestBuilder;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.SqlElasticSearchRequestBuilder;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.join.ESJoinQueryAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.multi.MultiQueryAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.multi.MultiQueryRequestBuilder;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregations;

import java.io.IOException;
import java.util.List;

/**
 * Created by Eliran on 3/10/2015.
 */
public class QueryActionElasticExecutor {
    public static SearchHits executeSearchAction(DefaultQueryAction searchQueryAction) throws SqlParseException {
        SqlElasticSearchRequestBuilder builder = searchQueryAction.explain();
        return ((SearchResponse) builder.get()).getHits();
    }

    public static SearchHits executeJoinSearchAction(Client client, ESJoinQueryAction joinQueryAction)
            throws IOException, SqlParseException {
        SqlElasticRequestBuilder joinRequestBuilder = joinQueryAction.explain();
        ElasticJoinExecutor executor = ElasticJoinExecutor.createJoinExecutor(client, joinRequestBuilder);
        executor.run();
        return executor.getHits();
    }

    public static Aggregations executeAggregationAction(AggregationQueryAction aggregationQueryAction)
            throws SqlParseException {
        SqlElasticSearchRequestBuilder select = aggregationQueryAction.explain();
        return ((SearchResponse) select.get()).getAggregations();
    }

    public static List<BindingTuple> executeQueryPlanQueryAction(QueryPlanQueryAction queryPlanQueryAction) {
        QueryPlanRequestBuilder select = (QueryPlanRequestBuilder) queryPlanQueryAction.explain();
        return select.execute();
    }

    public static ActionResponse executeShowQueryAction(ShowQueryAction showQueryAction) {
        return showQueryAction.explain().get();
    }

    public static ActionResponse executeDescribeQueryAction(DescribeQueryAction describeQueryAction) {
        return describeQueryAction.explain().get();
    }

    public static ActionResponse executeDeleteAction(DeleteQueryAction deleteQueryAction) throws SqlParseException {
        return deleteQueryAction.explain().get();
    }

    public static SearchHits executeMultiQueryAction(Client client, MultiQueryAction queryAction)
            throws SqlParseException, IOException {
        SqlElasticRequestBuilder multiRequestBuilder = queryAction.explain();
        ElasticHitsExecutor executor = MultiRequestExecutorFactory.createExecutor(client,
                (MultiQueryRequestBuilder) multiRequestBuilder);
        executor.run();
        return executor.getHits();
    }

    public static Object executeAnyAction(Client client, QueryAction queryAction)
            throws SqlParseException, IOException {
        if (queryAction instanceof DefaultQueryAction) {
            return executeSearchAction((DefaultQueryAction) queryAction);
        }
        if (queryAction instanceof AggregationQueryAction) {
            return executeAggregationAction((AggregationQueryAction) queryAction);
        }
        if (queryAction instanceof QueryPlanQueryAction) {
            return executeQueryPlanQueryAction((QueryPlanQueryAction) queryAction);
        }
        if (queryAction instanceof ShowQueryAction) {
            return executeShowQueryAction((ShowQueryAction) queryAction);
        }
        if (queryAction instanceof DescribeQueryAction) {
            return executeDescribeQueryAction((DescribeQueryAction) queryAction);
        }
        if (queryAction instanceof ESJoinQueryAction) {
            return executeJoinSearchAction(client, (ESJoinQueryAction) queryAction);
        }
        if (queryAction instanceof MultiQueryAction) {
            return executeMultiQueryAction(client, (MultiQueryAction) queryAction);
        }
        if (queryAction instanceof DeleteQueryAction) {
            return executeDeleteAction((DeleteQueryAction) queryAction);
        }
        return null;
    }
}
