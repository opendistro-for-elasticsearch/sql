package com.amazon.opendistroforelasticsearch.ppl.planner.dsl;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.node.NodeClient;

import java.util.concurrent.ExecutionException;

@AllArgsConstructor
public class QueryAction {
    private final SearchRequest searchRequest;
    private final NodeClient client;

    @SneakyThrows
    public SearchResponse run() {

        return client.search(searchRequest).get();
    }

    public String explain() {
        return searchRequest.source().toString();
    }
}
