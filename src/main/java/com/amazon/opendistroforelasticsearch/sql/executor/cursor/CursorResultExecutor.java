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

import com.amazon.opendistroforelasticsearch.sql.esdomain.LocalClusterState;
import com.amazon.opendistroforelasticsearch.sql.executor.Format;
import com.amazon.opendistroforelasticsearch.sql.executor.format.Protocol;
import com.amazon.opendistroforelasticsearch.sql.metrics.MetricName;
import com.amazon.opendistroforelasticsearch.sql.metrics.Metrics;
import com.amazon.opendistroforelasticsearch.sql.rewriter.matchtoterm.VerificationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.ClearScrollResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.search.SearchHits;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLFeatureNotSupportedException;
import java.util.Base64;
import java.util.Map;

import static com.amazon.opendistroforelasticsearch.sql.plugin.SqlSettings.CURSOR_KEEPALIVE;
import static org.elasticsearch.rest.RestStatus.OK;

public class CursorResultExecutor implements CursorRestExecutor {

    private String cursorId;
    private Format format;

    private static final Logger LOG = LogManager.getLogger(CursorResultExecutor.class);

    public CursorResultExecutor(String cursorId, Format format) {
        this.cursorId = cursorId;
        this.format = format;
    }

    public void execute(Client client, Map<String, String> params, RestChannel channel) throws Exception {
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
                    return handleDefaultCursorRequest(client, cursorJson);
                case AGGREGATION:
                    return handleAggregationCursorRequest(client, cursorJson);
                case JOIN:
                    return handleJoinCursorRequest(client, cursorJson);
                default: throw new VerificationException("Unsupported cursor");
            }
        }

        throw new VerificationException("Invalid cursor");
    }

    private String handleDefaultCursorRequest(Client client, JSONObject cursorContext) {
        String previousScrollId = cursorContext.getString("scrollId");
        LocalClusterState clusterState = LocalClusterState.state();
        TimeValue scrollTimeout = clusterState.getSettingValue(CURSOR_KEEPALIVE);
        SearchResponse scrollResponse = client.prepareSearchScroll(previousScrollId).setScroll(scrollTimeout).get();
        SearchHits searchHits = scrollResponse.getHits();
        String newScrollId = scrollResponse.getScrollId();

        int pagesLeft = cursorContext.getInt("left");
        pagesLeft--;

        if (pagesLeft <=0) {
            // Close the scroll context on last page
            ClearScrollResponse clearScrollResponse = client.prepareClearScroll().addScrollId(newScrollId).get();
            if (!clearScrollResponse.isSucceeded()) {
                Metrics.getInstance().getNumericalMetric(MetricName.FAILED_REQ_COUNT_SYS).increment();
                LOG.info("Problem closing the cursor context {} ", newScrollId);
            }

            Protocol protocol = new Protocol(client, searchHits, cursorContext, format.name().toLowerCase());
            protocol.setCursor(null);
            return protocol.cursorFormat();

        } else {
            cursorContext.put("left", pagesLeft);
            cursorContext.put("scrollId", newScrollId);
            Protocol protocol = new Protocol(client, searchHits, cursorContext, format.name().toLowerCase());
            String cursorId = protocol.encodeCursorContext(cursorContext);
            protocol.setCursor(cursorId);
            return protocol.cursorFormat();
        }
    }

    private String handleAggregationCursorRequest(Client client, JSONObject cursorContext)
            throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("Aggregations not supported over cursor");
    }

    private String handleJoinCursorRequest(Client client, JSONObject cursorContext)
            throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("Joins not supported over cursor");
    }

}