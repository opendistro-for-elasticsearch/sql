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
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.join.ElasticUtils;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.join.MetaSearchResult;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.multi.MultiRequestExecutorFactory;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.QueryAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.SqlElasticRequestBuilder;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.join.JoinRequestBuilder;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.multi.MultiQueryRequestBuilder;
import com.google.common.collect.Maps;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchScrollRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.reindex.BulkIndexByScrollResponseContentListener;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.rest.action.RestStatusToXContentListener;
import org.elasticsearch.search.SearchHits;

import java.io.IOException;
import java.util.Map;


public class ElasticDefaultRestExecutor implements RestExecutor {

    /**
     * Request builder to generate ES DSL
     */
    private final SqlElasticRequestBuilder requestBuilder;

    private static final Logger LOG = LogManager.getLogger(ElasticDefaultRestExecutor.class);

    public ElasticDefaultRestExecutor(QueryAction queryAction) {
        // Put explain() here to make it run in NIO thread
        try {
            this.requestBuilder = queryAction.explain();
        } catch (SqlParseException e) {
            throw new IllegalStateException("Failed to explain query action", e);
        }
    }

    /**
     * Execute the ActionRequest and returns the REST response using the channel.
     */
    @Override
    public void execute(Client client, Map<String, String> params, QueryAction queryAction, RestChannel channel)
            throws Exception {
        ActionRequest request = requestBuilder.request();

        if (requestBuilder instanceof JoinRequestBuilder) {
            ElasticJoinExecutor executor = ElasticJoinExecutor.createJoinExecutor(client, requestBuilder);
            executor.run();
            executor.sendResponse(channel);
        } else if (requestBuilder instanceof MultiQueryRequestBuilder) {
            ElasticHitsExecutor executor = MultiRequestExecutorFactory.createExecutor(client,
                    (MultiQueryRequestBuilder) requestBuilder);
            executor.run();
            sendDefaultResponse(executor.getHits(), channel);
        } else if (request instanceof SearchRequest) {
            client.search((SearchRequest) request, new RestStatusToXContentListener<>(channel));
        } else if (request instanceof DeleteByQueryRequest) {
            requestBuilder.getBuilder().execute(
                    new BulkIndexByScrollResponseContentListener(channel, Maps.newHashMap()));
        } else if (request instanceof GetIndexRequest) {
            requestBuilder.getBuilder().execute(new GetIndexRequestRestListener(channel, (GetIndexRequest) request));
        } else if (request instanceof SearchScrollRequest) {
            client.searchScroll((SearchScrollRequest) request, new RestStatusToXContentListener<>(channel));
        } else {
            throw new Exception(String.format("Unsupported ActionRequest provided: %s", request.getClass().getName()));
        }
    }

    @Override
    public String execute(Client client, Map<String, String> params, QueryAction queryAction) throws Exception {
        ActionRequest request = requestBuilder.request();

        if (requestBuilder instanceof JoinRequestBuilder) {
            ElasticJoinExecutor executor = ElasticJoinExecutor.createJoinExecutor(client, requestBuilder);
            executor.run();
            return ElasticUtils.hitsAsStringResult(executor.getHits(), new MetaSearchResult());
        } else if (requestBuilder instanceof MultiQueryRequestBuilder) {
            ElasticHitsExecutor executor = MultiRequestExecutorFactory.createExecutor(client,
                    (MultiQueryRequestBuilder) requestBuilder);
            executor.run();
            return ElasticUtils.hitsAsStringResult(executor.getHits(), new MetaSearchResult());
        } else if (request instanceof SearchRequest) {
            ActionFuture<SearchResponse> future = client.search((SearchRequest) request);
            SearchResponse response = future.actionGet();
            return response.toString();
        } else if (request instanceof DeleteByQueryRequest) {
            return requestBuilder.get().toString();
        } else if (request instanceof GetIndexRequest) {
            return requestBuilder.getBuilder().execute().actionGet().toString();
        } else {
            throw new Exception(String.format("Unsupported ActionRequest provided: %s", request.getClass().getName()));
        }

    }

    private void sendDefaultResponse(SearchHits hits, RestChannel channel) {
        try {
            String json = ElasticUtils.hitsAsStringResult(hits, new MetaSearchResult());
            BytesRestResponse bytesRestResponse = new BytesRestResponse(RestStatus.OK, json);
            channel.sendResponse(bytesRestResponse);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
