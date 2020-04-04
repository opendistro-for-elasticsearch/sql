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

import com.amazon.opendistroforelasticsearch.sql.ppl.PPLService;
import com.amazon.opendistroforelasticsearch.sql.ppl.ResponseListener;
import com.amazon.opendistroforelasticsearch.sql.ppl.domain.PPLQueryRequest;
import com.amazon.opendistroforelasticsearch.sql.ppl.domain.PPLQueryResponse;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;

import java.io.IOException;

import static org.elasticsearch.rest.RestStatus.OK;

public class RestPPLQueryAction extends BaseRestHandler {
    public static final String QUERY_API_ENDPOINT = "/_opendistro/_ppl";

    public RestPPLQueryAction(RestController restController) {
        super();
        restController.registerHandler(RestRequest.Method.POST, QUERY_API_ENDPOINT, this);
    }

    @Override
    public String getName() {
        return "ppl_query_action";
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
        PPLService pplService = new PPLService();
        return channel -> pplService.execute(new PPLQueryRequest(), new ResponseListener<PPLQueryResponse>() {
            @Override
            public void onResponse(PPLQueryResponse pplQueryResponse) {
                channel.sendResponse(new BytesRestResponse(OK, "application/json; charset=UTF-8", "ok"));
            }

            @Override
            public void onFailure(Exception e) {
                channel.sendResponse(new BytesRestResponse(OK, "application/json; charset=UTF-8", "error"));
            }
        });
    }
}
