package com.amazon.opendistroforelasticsearch.ppl.planner.dsl;

import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

public class TermQuery {
    private TermField termField;

    public QueryBuilder build() {
        return QueryBuilders.termQuery(termField.getFieldName(), termField.getValue());
    }
}
