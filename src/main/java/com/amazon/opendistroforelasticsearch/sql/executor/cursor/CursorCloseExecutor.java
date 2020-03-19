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

import com.amazon.opendistroforelasticsearch.sql.metrics.MetricName;
import com.amazon.opendistroforelasticsearch.sql.metrics.Metrics;
import com.amazon.opendistroforelasticsearch.sql.rewriter.matchtoterm.VerificationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Base64;
import java.util.Map;

import static org.elasticsearch.rest.RestStatus.OK;

public class CursorCloseExecutor implements CursorRestExecutor {

    private static final String SUCCESS_TRUE = "{\"sucess\":true}";
    private static final String SUCCESS_FALSE = "{\"sucess\":false}";

    private String cursorId;

    private static final Logger LOG = LogManager.getLogger(CursorResultExecutor.class);

    public CursorCloseExecutor(String cursorId) {
        this.cursorId = cursorId;
    }

    public void execute(Client client, Map<String, String> params, RestChannel channel) throws Exception {
        LOG.info("executing something inside CursorCloseExecutor execute ");
        try {
            String formattedResponse = execute(client, params);
            channel.sendResponse(new BytesRestResponse(OK, "application/json; charset=UTF-8", formattedResponse));
        } catch (IllegalArgumentException | JSONException e) {
            Metrics.getInstance().getNumericalMetric(MetricName.FAILED_REQ_COUNT_CUS).increment();
            e.printStackTrace();
            channel.sendResponse(new BytesRestResponse(channel, e));
        } catch (ElasticsearchException e) {
            int status = (e.status().getStatus());
            if (status > 399 && status < 500) {
                Metrics.getInstance().getNumericalMetric(MetricName.FAILED_REQ_COUNT_CUS).increment();
            } else if (status > 499) {
                Metrics.getInstance().getNumericalMetric(MetricName.FAILED_REQ_COUNT_SYS).increment();
            }
            e.printStackTrace();
            channel.sendResponse(new BytesRestResponse(channel, e));
        }
    }

    public String execute(Client client, Map<String, String> params) throws Exception {
        String decodedCursorContext = new String(Base64.getDecoder().decode(cursorId));
        JSONObject cursorJson = new JSONObject(decodedCursorContext);

        String type = cursorJson.optString("type", null); // see if it is a good case to use Optionals
        CursorType cursorType = null;

        if (type != null) {
            cursorType = CursorType.valueOf(type);
        }

        if (cursorType!=null) {
            switch(cursorType) {
                case DEFAULT:
                    return handleDefaultCursorCloseRequest(client, cursorJson);
                case AGGREGATION:
                    return handleAggregationCursorCloseRequest(client, cursorJson);
                case JOIN:
                    return handleJoinCursorCloseRequest(client, cursorJson);
                default: throw new VerificationException("Unsupported cursor");
            }
        }

        throw new VerificationException("Invalid cursor");
    }

    private String handleDefaultCursorCloseRequest(Client client, JSONObject cursorContext) {
        String scrollId = cursorContext.getString("scrollId");
        ClearScrollResponse clearScrollResponse = client.prepareClearScroll().addScrollId(scrollId).get();
        if (clearScrollResponse.isSucceeded()) {
            return SUCCESS_TRUE;
        } else {
            return SUCCESS_FALSE;
        }
    }

    private String handleAggregationCursorCloseRequest(Client client, JSONObject cursorContext) {
        return SUCCESS_TRUE;
    }

    private String handleJoinCursorCloseRequest(Client client, JSONObject cursorContext) {
        return SUCCESS_FALSE;
    }

}