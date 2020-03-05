package com.amazon.opendistroforelasticsearch.ppl.planner.dsl;

import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public class SearchRequestBuilder {
    private final String index;
    private final Query query;

    public SearchRequest build() {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));
        sourceBuilder.size(1);
        sourceBuilder.from(0);
        https://www.elastic.co/guide/en/elasticsearch/reference/current/search-request-body.html#request-body-search-source-filtering
        sourceBuilder.fetchSource(new String[]{"FlightNum"}, new String[0]);
        sourceBuilder.query(query.build());

        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices(index);
        searchRequest.source(sourceBuilder);

        return searchRequest;
    }

}
