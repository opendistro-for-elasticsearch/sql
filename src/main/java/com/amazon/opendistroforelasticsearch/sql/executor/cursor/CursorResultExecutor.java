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
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.search.SearchHit;
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
        SearchHit[] hits = scrollResponse.getHits().getHits();
        int size = hits.length;
        JSONObject json = new JSONObject();

        if (size == 0) {
            // TODO : close the cursor on the last page
            LOG.info("Closing the cursor as size is {}", size);
            ClearScrollResponse clearScrollResponse = client.prepareClearScroll().addScrollId(cursorId).get();

            if (clearScrollResponse.isSucceeded()) {
                return new JSONObject().put("success", true).toString();
            } else {
                return new JSONObject().put("success", false).toString();
            }

        } else {
//            XContentBuilder builder = null;
//            builder = ElasticUtils.hitsAsStringResultZeroCopy(results, metaResults, this);
            json.put("size", size);
            LOG.info("new scollID : {}", scrollResponse.getScrollId());
            json.put("cursor", scrollResponse.getScrollId());
        }

        return json.toString();
    }

//    /**
//     * Generate string by serializing SearchHits in place without any new HashMap copy
//     */
//    public static XContentBuilder hitsAsStringResultZeroCopy(List<SearchHit> results, MetaSearchResult metaResults,
//                                                             ElasticJoinExecutor executor) throws IOException {
//        BytesStreamOutput outputStream = new BytesStreamOutput();
//
//        XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON, outputStream).prettyPrint();
//        builder.startObject();
//        builder.field("took", metaResults.getTookImMilli());
//        builder.field("timed_out", metaResults.isTimedOut());
//        builder.field("_shards", ImmutableMap.of(
//            "total", metaResults.getTotalNumOfShards(),
//            "successful", metaResults.getSuccessfulShards(),
//            "failed", metaResults.getFailedShards()
//        ));
//        toXContent(builder, EMPTY_PARAMS, results, executor);
//        builder.endObject();
//
//        if (!BackOffRetryStrategy.isHealthy(2 * outputStream.size(), executor)) {
//            throw new IllegalStateException("Memory could be insufficient when sendResponse().");
//        }
//
//        return builder;
//    }
//    /**
//     * Code copy from SearchHits
//     */
//    private static void toXContent(XContentBuilder builder, ToXContent.Params params, List<SearchHit> hits)
//    throws IOException {
//        builder.startObject(SearchHits.Fields.HITS);
//        builder.field(SearchHits.Fields.TOTAL, ImmutableMap.of(
//            "value", hits.size(),
//            "relation", TotalHits.Relation.EQUAL_TO
//        ));
//        builder.field(SearchHits.Fields.MAX_SCORE, 1.0f);
//        builder.field(SearchHits.Fields.HITS);
//        builder.startArray();
//
//        for (int i = 0; i < hits.size(); i++) {
//            if (i % 10000 == 0 && !BackOffRetryStrategy.isHealthy()) {
//                throw new IllegalStateException("Memory circuit break when generating json builder");
//            }
//            ElasticUtils.toXContent(builder, params, hits.get(i));
//        }
//        builder.endArray();
//        builder.endObject();
//    }

}
