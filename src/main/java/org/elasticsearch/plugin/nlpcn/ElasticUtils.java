package org.elasticsearch.plugin.nlpcn;

import com.google.common.collect.ImmutableMap;
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
import org.nlpcn.es4sql.domain.Select;
import org.nlpcn.es4sql.query.join.BackOffRetryStrategy;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import static org.elasticsearch.common.xcontent.ToXContent.EMPTY_PARAMS;

/**
 * Created by Eliran on 2/9/2016.
 */
public class ElasticUtils {

    public static SearchResponse scrollOneTimeWithHits(Client client, SearchRequestBuilder requestBuilder, Select originalSelect, int resultSize) {
        SearchResponse responseWithHits;SearchRequestBuilder scrollRequest = requestBuilder
                .setScroll(new TimeValue(60000))
                .setSize(resultSize);
        boolean ordered = originalSelect.isOrderdSelect();
        if(!ordered) scrollRequest.addSort(FieldSortBuilder.DOC_FIELD_NAME, SortOrder.ASC);
        responseWithHits = scrollRequest.get();
        //on ordered select - not using SCAN , elastic returns hits on first scroll
        //es5.0 elastic always return docs on scan
//        if(!ordered) {
//            responseWithHits = client.prepareSearchScroll(responseWithHits.getScrollId()).setScroll(new TimeValue(600000)).get();
//        }
        return responseWithHits;
    }


    //use our deserializer instead of results toXcontent because the source field is differnet from sourceAsMap.
    public static String hitsAsStringResult(SearchHits results, MetaSearchResult metaResults) throws IOException {
        if(results == null) return null;
        Object[] searchHits;
        searchHits = new Object[(int) results.getTotalHits()];
        int i = 0;
        for(SearchHit hit : results) {
            HashMap<String,Object> value = new HashMap<>();
            value.put("_id",hit.getId());
            value.put("_type", hit.getType());
            value.put("_score", hit.getScore());
            value.put("_source", hit.getSourceAsMap());
            searchHits[i] = value;
            i++;
        }
        HashMap<String,Object> hits = new HashMap<>();
        hits.put("total",results.getTotalHits());
        hits.put("max_score",results.getMaxScore());
        hits.put("hits",searchHits);
        XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON).prettyPrint();
        builder.startObject();
        builder.field("took", metaResults.getTookImMilli());
        builder.field("timed_out",metaResults.isTimedOut());
        builder.field("_shards", ImmutableMap.of("total", metaResults.getTotalNumOfShards(),
                "successful", metaResults.getSuccessfulShards()
                , "failed", metaResults.getFailedShards()));
        builder.field("hits",hits) ;
        builder.endObject();
        return BytesReference.bytes(builder).utf8ToString();
    }

    /** Generate string by serializing SearchHits in place without any new HashMap copy */
    public static XContentBuilder hitsAsStringResultZeroCopy(List<SearchHit> results, MetaSearchResult metaResults) throws IOException {
        BytesStreamOutput outputStream = new BytesStreamOutput();
        BackOffRetryStrategy retry = new BackOffRetryStrategy(new double[]{1});

        XContentBuilder builder = XContentFactory.contentBuilder(XContentType.JSON, outputStream).prettyPrint();
        builder.startObject();
        builder.field("took", metaResults.getTookImMilli());
        builder.field("timed_out",metaResults.isTimedOut());
        builder.field("_shards", ImmutableMap.of(
            "total", metaResults.getTotalNumOfShards(),
            "successful", metaResults.getSuccessfulShards(),
            "failed", metaResults.getFailedShards()
        ));
        toXContent(builder, EMPTY_PARAMS, results, retry);
        builder.endObject();

        synchronized (BackOffRetryStrategy.class) {
            if (!retry.isHealthy(outputStream.size())) {
                throw new IllegalStateException("Memory may be insufficient when sendResponse()");
            }
        }
        return builder;
    }

    /** Code copy from SearchHits */
    private static void toXContent(XContentBuilder builder, Params params, List<SearchHit> hits, BackOffRetryStrategy retry) throws IOException {
        builder.startObject(SearchHits.Fields.HITS);
        builder.field(SearchHits.Fields.TOTAL, hits.size());
        builder.field(SearchHits.Fields.MAX_SCORE, 1.0f);
        builder.field(SearchHits.Fields.HITS);
        builder.startArray();

        for (int i = 0; i < hits.size() ; i++) {
            if (i % 10000 == 0 && !retry.isHealthy()) {
                throw new IllegalStateException("Memory circuit break when generating json builder");
            }
            toXContent(builder, params, hits.get(i));
        }

        builder.endArray();
        builder.endObject();
    }

    /** Code copy from SearchHit but only keep fields interested and replace source by sourceMap */
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
