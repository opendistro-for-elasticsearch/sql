package com.amazon.opendistroforelasticsearch.ppl.plans.physical;

import com.amazon.opendistroforelasticsearch.sql.expression.domain.BindingTuple;
import com.amazon.opendistroforelasticsearch.sql.query.planner.physical.node.scroll.BindingTupleRow;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SearchResponseHelper {

    public static List<BindingTupleRow> populateSearchResponse(SearchResponse searchResponse) {
        List<Map<String, Object>> flatten = flatten(searchResponse.getHits());
        List<BindingTupleRow> bindingTupleList = flatten.stream()
                .map(BindingTuple::from)
                .map(bindingTuple -> new BindingTupleRow(bindingTuple))
                .collect(Collectors.toList());
        return bindingTupleList;
    }

    public static List<Map<String, Object>> flatten(SearchHits searchHits) {
        return Arrays.asList(searchHits.getHits())
                .stream()
                .map(SearchHit::getSourceAsMap)
                .collect(Collectors.toList());
    }
}
