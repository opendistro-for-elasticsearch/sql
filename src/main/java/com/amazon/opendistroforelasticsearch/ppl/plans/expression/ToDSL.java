package com.amazon.opendistroforelasticsearch.ppl.plans.expression;

import org.elasticsearch.index.query.QueryBuilder;

public interface ToDSL {
    QueryBuilder build();
}
