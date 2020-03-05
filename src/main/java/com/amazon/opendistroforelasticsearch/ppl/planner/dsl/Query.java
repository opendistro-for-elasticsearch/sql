package com.amazon.opendistroforelasticsearch.ppl.planner.dsl;

import org.elasticsearch.index.query.QueryBuilder;

public interface Query {
    QueryBuilder build();
}
