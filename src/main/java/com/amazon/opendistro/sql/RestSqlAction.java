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

package com.amazon.opendistro.sql;

import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import com.amazon.opendistro.sql.executor.ActionRequestRestExecutorFactory;
import com.amazon.opendistro.sql.executor.RestExecutor;
import org.elasticsearch.rest.*;
import com.amazon.opendistro.sql.request.SqlRequest;
import com.amazon.opendistro.sql.request.SqlRequestFactory;
import com.amazon.opendistro.sql.exception.SqlParseException;
import com.amazon.opendistro.sql.query.QueryAction;

import java.sql.SQLFeatureNotSupportedException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class RestSqlAction extends BaseRestHandler {


    public RestSqlAction(Settings settings, RestController restController) {
        super(settings);
        restController.registerHandler(RestRequest.Method.POST, "/_sql/_explain", this);
        restController.registerHandler(RestRequest.Method.GET, "/_sql/_explain", this);
        restController.registerHandler(RestRequest.Method.POST, "/_sql", this);
        restController.registerHandler(RestRequest.Method.GET, "/_sql", this);
    }

    @Override
    public String getName() {
        return "sql_action";
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) {
        SqlRequest sqlRequest;
        try {
            sqlRequest = SqlRequestFactory.getSqlRequest(request);
        } catch(IllegalArgumentException e) {
            // FIXME: need to send proper error response to client.
            return null;
        }

        try {
            SearchDao searchDao = new SearchDao(client);
            QueryAction queryAction= null;

            queryAction = searchDao.explain(sqlRequest.getSql());
            queryAction.setSqlRequest(sqlRequest);

            // TODO add unittests to explain. (rest level?)
            if (request.path().endsWith("/_explain")) {
                final String jsonExplanation = queryAction.explain().explain();
                return channel -> channel.sendResponse(new BytesRestResponse(RestStatus.OK, jsonExplanation));
            } else {
                Map<String, String> params = request.params();
                RestExecutor restExecutor = ActionRequestRestExecutorFactory.createExecutor(params.get("format"), queryAction);
                final QueryAction finalQueryAction = queryAction;
                //doing this hack because elasticsearch throws exception for un-consumed props
                Map<String,String> additionalParams = new HashMap<>();
                for (String paramName : responseParams()) {
                    if (request.hasParam(paramName)) {
                        additionalParams.put(paramName, request.param(paramName));
                    }
                }
                return channel -> restExecutor.execute(client, additionalParams, finalQueryAction, channel);
            }
        } catch (SqlParseException | SQLFeatureNotSupportedException e) {
            // FIXME: need to catch all exceptions to avoid ES process from crashing
            e.printStackTrace();
            return null;
        }
    }

    @Override
    protected Set<String> responseParams() {
        Set<String> responseParams = new HashSet<>(super.responseParams());
        responseParams.addAll(Arrays.asList("sql", "flat", "separator", "_score", "_type", "_id", "newLine", "format"));
        return responseParams;
    }

}