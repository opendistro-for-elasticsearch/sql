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

package com.amazon.opendistroforelasticsearch.sql.legacy.executor.join;

import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Select;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.join.BackOffRetryStrategy;
import com.google.common.collect.ImmutableMap;
import org.apache.lucene.search.TotalHits.Relation;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.ToXContent.Params;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.elasticsearch.common.xcontent.ToXContent.EMPTY_PARAMS;

/**
 * Created by Eliran on 2/9/2016.
 */
public class ElasticUtils {

    public static SearchResponse scrollOneTimeWithHits(Client client, SearchRequestBuilder requestBuilder,
                                                       Select originalSelect, int resultSize) {
        SearchRequestBuilder scrollRequest = requestBuilder
                .setScroll(new TimeValue(60000)).setSize(resultSize);
        boolean ordered = originalSelect.isOrderdSelect();
        if (!ordered) {
            scrollRequest.addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC);
        }
        SearchResponse responseWithHits = scrollRequest.get();
        //on ordered select - not using SCAN , elastic returns hits on first scroll
        //es5.0 elastic always return docs on scan
//        if(!ordered) {
//            responseWithHits = client.prepareSearchScroll(responseWithHits.getScrollId())
//            .setScroll(new TimeValue(600000)).get();
//        }
        return responseWithHits;
    }


    //use our deserializer instead of results toXcontent because the source field is different from sourceAsMap.
    public static String hitsAsStringResult(SearchHits results, MetaSearchResult metaResults) throws IOException {
        if (results == null) {
            return null;
        }
        Object[] searchHits;
        searchHits = new Object[Optional.ofNullable(results.getTotalHits()).map(th -> th.value).orElse(0L).intValue()];
        int i = 0;
        for (SearchHit hit : results) {
            HashMap<String, Object> value = new HashMap<>();
            value.put("_id", hit.getId());
            value.put("_type", hit.getType());
            value.put("_score", hit.getScore());
            value.put("_source", hit.getSourceAsMap());
            searchHits[i] = value;
            i++;
        }
        HashMap<String, Object> hits = new HashMap<>();
        hits.put("total", ImmutableMap.of(
                "value", Optional.ofNullable(results.getTotalHits()).map(th -> th.value).orElse(0L),
                "relation", Optional.ofNullable(results.getTotalHits()).map(th -> th.relation).orElse(Relation.EQUAL_TO)
        ));
        hits.put("max_score", results.getMaxScore());
        hits.put("hits", searchHits);
        XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON).prettyPrint();
        builder.startObject();
        builder.field("took", metaResults.getTookImMilli());
        builder.field("timed_out", metaResults.isTimedOut());
        builder.field("_shards", ImmutableMap.of("total", metaResults.getTotalNumOfShards(),
                "successful", metaResults.getSuccessfulShards()
                , "failed", metaResults.getFailedShards()));
        builder.field("hits", hits);
        builder.endObject();
        return BytesReference.bytes(builder).utf8ToString();
    }

    /**
     * Generate string by serializing SearchHits in place without any new HashMap copy
     */
    public static XContentBuilder hitsAsStringResultZeroCopy(List<SearchHit> results, MetaSearchResult metaResults,
                                                             ElasticJoinExecutor executor) throws IOException {
        BytesStreamOutput outputStream = new BytesStreamOutput();

        XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON, outputStream).prettyPrint();
        builder.startObject();
        builder.field("took", metaResults.getTookImMilli());
        builder.field("timed_out", metaResults.isTimedOut());
        builder.field("_shards", ImmutableMap.of(
                "total", metaResults.getTotalNumOfShards(),
                "successful", metaResults.getSuccessfulShards(),
                "failed", metaResults.getFailedShards()
        ));
        toXContent(builder, EMPTY_PARAMS, results, executor);
        builder.endObject();

        if (!BackOffRetryStrategy.isHealthy(2 * outputStream.size(), executor)) {
            throw new IllegalStateException("Memory could be insufficient when sendResponse().");
        }

        return builder;
    }

    /**
     * Code copy from SearchHits
     */
    private static void toXContent(XContentBuilder builder, Params params, List<SearchHit> hits,
                                   ElasticJoinExecutor executor) throws IOException {
        builder.startObject(SearchHits.Fields.HITS);
        builder.field(SearchHits.Fields.TOTAL, ImmutableMap.of(
                "value", hits.size(),
                "relation", Relation.EQUAL_TO
        ));
        builder.field(SearchHits.Fields.MAX_SCORE, 1.0f);
        builder.field(SearchHits.Fields.HITS);
        builder.startArray();

        for (int i = 0; i < hits.size(); i++) {
            if (i % 10000 == 0 && !BackOffRetryStrategy.isHealthy()) {
                throw new IllegalStateException("Memory circuit break when generating json builder");
            }
            toXContent(builder, params, hits.get(i));
        }

        builder.endArray();
        builder.endObject();
    }

    /**
     * Code copy from SearchHit but only keep fields interested and replace source by sourceMap
     */
    private static void toXContent(XContentBuilder builder, Params params, SearchHit hit) throws IOException {
        builder.startObject();
        if (hit.getType() != null) {
            builder.field("_type", hit.getType());
        }
        if (hit.getId() != null) {
            builder.field("_id", hit.getId());
        }

        if (Float.isNaN(hit.getScore())) {
            builder.nullField("_score");
        } else {
            builder.field("_score", hit.getScore());
        }

        /*
         * Use sourceMap rather than binary source because source is out-of-date
         * and only used when creating a new instance of SearchHit
         */
        builder.field("_source", hit.getSourceAsMap());
        builder.endObject();
    }
}
