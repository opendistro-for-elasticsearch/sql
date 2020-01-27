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
//import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.json.JSONObject;

import java.util.Map;

import static org.elasticsearch.rest.RestStatus.OK;

public class CursorResultExecutor implements CursorRestExecutor {

    public static final int SCROLL_TIMEOUT = 120; // 2 minutes

    private String cursorId;
    private Format format;

    private static final Logger LOG = LogManager.getLogger(CursorResultExecutor.class);

    public CursorResultExecutor(String cursorId, Format format) {
        this.cursorId = cursorId;
        this.format = format;
    }

    public void execute(Client client, Map<String, String> params, RestChannel channel) throws Exception {
        LOG.info("executing something inside CursorResultExecutor execute");
        String formattedResponse = execute(client, params);
        LOG.info("{} : {}", cursorId, formattedResponse);
        channel.sendResponse(new BytesRestResponse(OK, "application/json; charset=UTF-8", formattedResponse));
    }

    public String execute(Client client, Map<String, String> params) throws Exception {
        SearchResponse scrollResponse = client.prepareSearchScroll(cursorId).
            setScroll(TimeValue.timeValueSeconds(SCROLL_TIMEOUT)).get();
        int r = scrollResponse.getHits().getHits().length;
        JSONObject json = new JSONObject();
        json.put("size", r);
        LOG.info("new scollID : {}", scrollResponse.getScrollId());
        json.put("cursor", scrollResponse.getScrollId());
        // TODO : close the cursor on the last page
        return json.toString();
    }
}
