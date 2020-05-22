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

package com.amazon.opendistroforelasticsearch.sql.plugin.rest;

import com.amazon.opendistroforelasticsearch.sql.common.response.ResponseListener;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.client.ElasticsearchNodeClient;
import com.amazon.opendistroforelasticsearch.sql.plugin.request.PPLQueryRequestFactory;
import com.amazon.opendistroforelasticsearch.sql.ppl.PPLService;
import com.amazon.opendistroforelasticsearch.sql.protocol.response.QueryResponse;
import com.amazon.opendistroforelasticsearch.sql.protocol.response.format.SimpleJsonResponseFormatter;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static com.amazon.opendistroforelasticsearch.sql.protocol.response.format.JsonResponseFormatter.Style.PRETTY;
import static org.elasticsearch.rest.RestStatus.INTERNAL_SERVER_ERROR;
import static org.elasticsearch.rest.RestStatus.OK;

public class RestPPLQueryAction extends BaseRestHandler {
    public static final String QUERY_API_ENDPOINT = "/_opendistro/_ppl";

    /**
     * Spring container
     */
    private final AnnotationConfigApplicationContext context;


    public RestPPLQueryAction(RestController restController, AnnotationConfigApplicationContext context) {
        super();
        restController.registerHandler(RestRequest.Method.POST, QUERY_API_ENDPOINT, this);
        this.context = context;
    }

    @Override
    public String getName() {
        return "ppl_query_action";
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient nodeClient) {
        ElasticsearchNodeClient client = context.getBean(ElasticsearchNodeClient.class);
        client.setClient(nodeClient);

        PPLService pplService = context.getBean(PPLService.class);
        return channel -> pplService.execute(
            PPLQueryRequestFactory.getPPLRequest(request), createListener(channel));
    }

    private ResponseListener<List<ExprValue>> createListener(RestChannel channel) {
        SimpleJsonResponseFormatter formatter = new SimpleJsonResponseFormatter(PRETTY); // TODO: decide format and pretty from URL param
        return new ResponseListener<List<ExprValue>>() {
            @Override
            public void onResponse(List<ExprValue> values) {
                sendResponse(OK, formatter.format(new QueryResponse(values)));
            }

            @Override
            public void onFailure(Exception e) {
                sendResponse(INTERNAL_SERVER_ERROR, formatter.format(e));
            }

            private void sendResponse(RestStatus status, String content) {
                channel.sendResponse(new BytesRestResponse(status, "application/json; charset=UTF-8", content));
            }
        };
    }
}
