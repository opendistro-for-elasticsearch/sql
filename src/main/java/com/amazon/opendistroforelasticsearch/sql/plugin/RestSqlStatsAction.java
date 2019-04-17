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

package com.amazon.opendistroforelasticsearch.sql.plugin;

import com.alibaba.druid.sql.parser.ParserException;
import com.amazon.opendistroforelasticsearch.sql.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.executor.ActionRequestRestExecutorFactory;
import com.amazon.opendistroforelasticsearch.sql.executor.RestExecutor;
import com.amazon.opendistroforelasticsearch.sql.executor.format.ErrorMessage;
import com.amazon.opendistroforelasticsearch.sql.query.QueryAction;
import com.amazon.opendistroforelasticsearch.sql.request.SqlRequest;
import com.amazon.opendistroforelasticsearch.sql.request.SqlRequestFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.json.JSONStringer;

import java.sql.SQLFeatureNotSupportedException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.elasticsearch.rest.RestStatus.BAD_REQUEST;
import static org.elasticsearch.rest.RestStatus.OK;
import static org.elasticsearch.rest.RestStatus.SERVICE_UNAVAILABLE;

public class RestSqlStatsAction extends BaseRestHandler {
    private static final Logger LOG = LogManager.getLogger(RestSqlStatsAction.class);

    /** API endpoint path */
    public static final String STATS_API_ENDPOINT = "/_opendistro/_sql/stats";

    public RestSqlStatsAction(Settings settings, RestController restController) {
        super(settings);
        restController.registerHandler(RestRequest.Method.POST, STATS_API_ENDPOINT, this);
        restController.registerHandler(RestRequest.Method.GET, STATS_API_ENDPOINT, this);
    }

    @Override
    public String getName() {
        return "sql_stats_action";
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) {
        try {
            JSONStringer jsonStringer = new JSONStringer();
            jsonStringer.object().key("request_count").value(0L).key("failed_request_count_syserr").value(0L).
                    key("failed_request_count_cuserr").value(0L).endObject();
            return channel -> channel.sendResponse(new BytesRestResponse(RestStatus.OK, jsonStringer.toString()));
        } catch (Exception e) {
            LOG.error("Failed during Query SQL STATS Action.", e);
            return reportError(e, isClientError(e) ? BAD_REQUEST : SERVICE_UNAVAILABLE);
        }
    }

    @Override
    protected Set<String> responseParams() {
        Set<String> responseParams = new HashSet<>(super.responseParams());
        responseParams.addAll(Arrays.asList("sql", "flat", "separator", "_score", "_type", "_id", "newLine", "format"));
        return responseParams;
    }

    private boolean isClientError(Exception e) {
        return e instanceof NullPointerException | // NPE is hard to differentiate but more likely caused by bad query
               e instanceof SqlParseException |
               e instanceof ParserException |
               e instanceof SQLFeatureNotSupportedException |
               e instanceof IllegalArgumentException |
               e instanceof IndexNotFoundException;
    }

    private RestChannelConsumer reportError(Exception e, RestStatus status) {
        return sendResponse(new ErrorMessage(e, status.getStatus()).toString(), status);
    }

    private RestChannelConsumer sendResponse(String message, RestStatus status) {
        return channel -> channel.sendResponse(new BytesRestResponse(status, message));
    }
}