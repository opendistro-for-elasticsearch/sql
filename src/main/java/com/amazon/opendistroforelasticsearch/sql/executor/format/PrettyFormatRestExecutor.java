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

package com.amazon.opendistroforelasticsearch.sql.executor.format;

import com.amazon.opendistroforelasticsearch.sql.executor.QueryActionElasticExecutor;
import com.amazon.opendistroforelasticsearch.sql.executor.RestExecutor;
import org.elasticsearch.client.Client;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestStatus;
import com.amazon.opendistroforelasticsearch.sql.query.QueryAction;

import java.util.Map;

public class PrettyFormatRestExecutor implements RestExecutor {

    private final String format;

    public PrettyFormatRestExecutor(String format) {
        this.format = format.toLowerCase();
    }

    /**
     * Execute the QueryAction and return the REST response using the channel.
     */
    @Override
    public void execute(Client client, Map<String, String> params, QueryAction queryAction, RestChannel channel) {
        String formattedResponse = execute(client, params, queryAction);
        BytesRestResponse bytesRestResponse;
        if (format.equals("jdbc")) {
            bytesRestResponse = new BytesRestResponse(RestStatus.OK,
                    "application/json; charset=UTF-8",
                    formattedResponse);
        } else {
            bytesRestResponse = new BytesRestResponse(RestStatus.OK, formattedResponse);
        }

        channel.sendResponse(bytesRestResponse);
    }

    @Override
    public String execute(Client client, Map<String, String> params, QueryAction queryAction) {
        Protocol protocol;

        try {
            Object queryResult = QueryActionElasticExecutor.executeAnyAction(client, queryAction);
            protocol = new Protocol(client, queryAction.getQueryStatement(), queryResult, format);
        } catch (Exception e) {
            // TODO Might require some refactoring, Exceptions that happen in RestSqAction code before invoking execution
            // TODO are being caught in RestController (line 242) and being sent as a bytesRestResponse
            // ex. "SELECT * FROM WHERE balance > 30000", results in ParserException and ErrorMessage is never made
            protocol = new Protocol(e);
        }

        return protocol.format();
    }
}
