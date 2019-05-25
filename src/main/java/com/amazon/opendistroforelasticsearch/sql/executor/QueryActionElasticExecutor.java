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

package com.amazon.opendistroforelasticsearch.sql.executor;

import com.amazon.opendistroforelasticsearch.sql.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.executor.join.ElasticJoinExecutor;
import com.amazon.opendistroforelasticsearch.sql.executor.multi.MultiRequestExecutorFactory;
import com.amazon.opendistroforelasticsearch.sql.query.AggregationQueryAction;
import com.amazon.opendistroforelasticsearch.sql.query.DefaultQueryAction;
import com.amazon.opendistroforelasticsearch.sql.query.DeleteQueryAction;
import com.amazon.opendistroforelasticsearch.sql.query.DescribeQueryAction;
import com.amazon.opendistroforelasticsearch.sql.query.QueryAction;
import com.amazon.opendistroforelasticsearch.sql.query.ShowQueryAction;
import com.amazon.opendistroforelasticsearch.sql.query.SqlElasticRequestBuilder;
import com.amazon.opendistroforelasticsearch.sql.query.SqlElasticSearchRequestBuilder;
import com.amazon.opendistroforelasticsearch.sql.query.join.ESJoinQueryAction;
import com.amazon.opendistroforelasticsearch.sql.query.multi.MultiQueryAction;
import com.amazon.opendistroforelasticsearch.sql.query.multi.MultiQueryRequestBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.threadpool.ThreadPool;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

/**
 * Created by Eliran on 3/10/2015.
 */
public class QueryActionElasticExecutor {

    private static final Logger LOG = LogManager.getLogger(QueryActionElasticExecutor.class);

    private static SearchHits executeSearchAction(DefaultQueryAction searchQueryAction) throws SqlParseException {
        SqlElasticSearchRequestBuilder builder  =  searchQueryAction.explain();
        return ((SearchResponse) builder.get()).getHits();
    }

    private static SearchHits executeJoinSearchAction(Client client , ESJoinQueryAction joinQueryAction) throws IOException, SqlParseException {
        SqlElasticRequestBuilder joinRequestBuilder = joinQueryAction.explain();
        ElasticJoinExecutor executor = ElasticJoinExecutor.createJoinExecutor(client,joinRequestBuilder);
        executor.run();
        return executor.getHits();
    }

    private static Aggregations executeAggregationAction(AggregationQueryAction aggregationQueryAction) throws SqlParseException {
        SqlElasticSearchRequestBuilder select =  aggregationQueryAction.explain();
        return ((SearchResponse)select.get()).getAggregations();
    }

    private static ActionResponse executeShowQueryAction(ShowQueryAction showQueryAction) {
        return showQueryAction.explain().get();
    }

    private static ActionResponse executeDescribeQueryAction(DescribeQueryAction describeQueryAction) {
        return describeQueryAction.explain().get();
    }

    private static ActionResponse executeDeleteAction(DeleteQueryAction deleteQueryAction) throws SqlParseException {
        return deleteQueryAction.explain().get();
    }

    private static SearchHits executeMultiQueryAction(Client client, MultiQueryAction queryAction) throws SqlParseException, IOException {
        SqlElasticRequestBuilder multiRequestBuilder = queryAction.explain();
        ElasticHitsExecutor executor = MultiRequestExecutorFactory.createExecutor(client, (MultiQueryRequestBuilder) multiRequestBuilder);
        executor.run();
        return executor.getHits();
    }

    public static Object executeAnyAction(Client client, QueryAction queryAction) throws SqlParseException, IOException {
        if (queryAction instanceof DefaultQueryAction)
            return executeSearchAction((DefaultQueryAction) queryAction);
        if (queryAction instanceof AggregationQueryAction)
            return executeAggregationAction((AggregationQueryAction) queryAction);
        if (queryAction instanceof ShowQueryAction)
            return executeShowQueryAction((ShowQueryAction) queryAction);
        if (queryAction instanceof DescribeQueryAction)
            return executeDescribeQueryAction((DescribeQueryAction) queryAction);
        if (queryAction instanceof ESJoinQueryAction)
            return executeJoinSearchAction(client, (ESJoinQueryAction) queryAction);
        if (queryAction instanceof MultiQueryAction)
            return executeMultiQueryAction(client, (MultiQueryAction) queryAction);
        if (queryAction instanceof DeleteQueryAction )
            return executeDeleteAction((DeleteQueryAction) queryAction);
        return null;
    }

    public static Future<Object> executeAnyActionAsync(Client client, QueryAction queryAction) {

        CompletableFuture<Object> future = new CompletableFuture<>();
        final Runnable task = () -> {
            try {
                future.complete(executeAnyAction(client, queryAction));
            } catch (Exception ex) {
                LOG.error("[{}] async action execution failed: {}", queryAction.getSqlRequest().getId(),
                        ex.getMessage());
                throw new RuntimeException(ex);
            }
        };

        final ThreadPool threadPool = client.threadPool();
        threadPool.schedule(threadPool.preserveContext(task), new TimeValue(0L),
                AsyncRestExecutor.SQL_WORKER_THREAD_POOL_NAME);

        return future;
    }
}
