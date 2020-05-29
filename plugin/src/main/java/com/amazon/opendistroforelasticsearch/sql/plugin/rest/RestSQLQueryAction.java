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

package com.amazon.opendistroforelasticsearch.sql.plugin.rest;

import com.amazon.opendistroforelasticsearch.sql.common.response.ResponseListener;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.security.SecurityAccess;
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine.QueryResponse;
import com.amazon.opendistroforelasticsearch.sql.protocol.response.QueryResult;
import com.amazon.opendistroforelasticsearch.sql.protocol.response.format.SimpleJsonResponseFormatter;
import com.amazon.opendistroforelasticsearch.sql.sql.SQLService;
import com.amazon.opendistroforelasticsearch.sql.sql.config.SQLServiceConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.security.PrivilegedExceptionAction;

import static com.amazon.opendistroforelasticsearch.sql.protocol.response.format.JsonResponseFormatter.Style.PRETTY;
import static org.elasticsearch.rest.RestStatus.INTERNAL_SERVER_ERROR;
import static org.elasticsearch.rest.RestStatus.OK;

/**
 * SQL REST request handler
 */
public class RestSQLQueryAction extends BaseRestHandler {

    private static final Logger LOG = LogManager.getLogger();

    public static final String SQL_QUERY_ENDPOINT = "_opendistro/_sql";
    public static final String SQL_EXPLAIN_ENDPOINT = SQL_QUERY_ENDPOINT + "/_explain";
    private static final String SQL_QUERY_FIELD_NAME = "query";

    private final ClusterService clusterService;

    public RestSQLQueryAction(RestController restController, ClusterService clusterService) {
        super();
        restController.registerHandler(RestRequest.Method.POST, SQL_QUERY_ENDPOINT, this);
        restController.registerHandler(RestRequest.Method.POST, SQL_EXPLAIN_ENDPOINT, this);
        this.clusterService = clusterService;
    }

    @Override
    public String getName() {
        return "sql_query_action";
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient nodeClient) {
        SQLService sqlService = createSQLService(nodeClient);
        String query = parseQueryFromPayload(request);

        if (isExplainRequest(request)) {
            return channel -> channel.sendResponse(
                new BytesRestResponse(OK, "application/json; charset=UTF-8", sqlService.explain(query)));
        }
        return channel -> sqlService.execute(query, createListener(channel));
    }

    private boolean isExplainRequest(RestRequest request) {
        return request.path().endsWith(SQL_EXPLAIN_ENDPOINT);
    }

    private String parseQueryFromPayload(RestRequest restRequest) {
        String content = restRequest.content().utf8ToString();
        JSONObject jsonContent;
        try {
            jsonContent = new JSONObject(content);
        } catch (JSONException e) {
            throw new IllegalArgumentException("Failed to parse request payload", e);
        }
        return jsonContent.optString(SQL_QUERY_FIELD_NAME);
    }

    private SQLService createSQLService(NodeClient client) {
        return doPrivileged(() -> {
            AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
            context.registerBean(ClusterService.class, () -> clusterService);
            context.registerBean(NodeClient.class, () -> client);
            context.register(ElasticsearchPluginConfig.class);
            context.register(SQLServiceConfig.class);
            context.refresh();
            return context.getBean(SQLService.class);
        });
    }

    // TODO: many duplicate code here and for example SQLServiceConfig
    private ResponseListener<QueryResponse> createListener(RestChannel channel) {
        SimpleJsonResponseFormatter formatter = new SimpleJsonResponseFormatter(PRETTY);
        return new ResponseListener<QueryResponse>() {
            @Override
            public void onResponse(QueryResponse response) {
                sendResponse(OK, formatter.format(new QueryResult(response.getResults())));
            }

            @Override
            public void onFailure(Exception e) {
                LOG.error("Error happened during query handling", e);
                sendResponse(INTERNAL_SERVER_ERROR, formatter.format(e));
            }

            private void sendResponse(RestStatus status, String content) {
                channel.sendResponse(new BytesRestResponse(status, "application/json; charset=UTF-8", content));
            }
        };
    }

    private <T> T doPrivileged(PrivilegedExceptionAction<T> action) {
        try {
            return SecurityAccess.doPrivileged(action);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to perform privileged action", e);
        }
    }

}
