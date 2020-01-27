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

package com.amazon.opendistroforelasticsearch.sql.executor.cursor;

import com.amazon.opendistroforelasticsearch.sql.executor.Format;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.json.JSONObject;

import java.util.Map;

import static org.elasticsearch.rest.RestStatus.OK;

public class CursorCloseExecutor implements CursorRestExecutor {

    private String cursorId;
    private Format format;


    private static final Logger LOG = LogManager.getLogger(CursorResultExecutor.class);

    public CursorCloseExecutor(String cursorId, Format format) {
        this.cursorId = cursorId;
        this.format = format;
    }

    public void execute(Client client, Map<String, String> params, RestChannel channel) throws Exception {
        LOG.info("executing something inside CursorCloseExecutor execute ");
        String formattedResponse = execute(client, params);
        LOG.info("{} : {}", cursorId, formattedResponse);
        channel.sendResponse(new BytesRestResponse(OK, "application/json; charset=UTF-8", formattedResponse));
    }

    public String execute(Client client, Map<String, String> params) throws Exception {
        ClearScrollResponse clearScrollResponse = client.prepareClearScroll().addScrollId(cursorId).get();
        if (clearScrollResponse.isSucceeded()) {
            return new JSONObject().put("success", true).toString();
        } else {
            return new JSONObject().put("success", false).toString();
        }
    }
}
