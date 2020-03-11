package com.amazon.opendistroforelasticsearch.ppl.planner.dsl;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * https://www.elastic.co/guide/en/elasticsearch/reference/current/search-request-body.html#request-body-search-source-filtering
 */
@Getter
public class SourceFilter {
    private final String[] includes;
    private final String[] excludes;

    public SourceFilter(List<String> includes) {
        this.includes = includes.toArray(new String[0]);
        this.excludes = new String[]{};
    }

}
