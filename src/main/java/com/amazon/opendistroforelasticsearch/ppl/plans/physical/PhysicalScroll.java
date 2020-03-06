package com.amazon.opendistroforelasticsearch.ppl.plans.physical;

import com.amazon.opendistroforelasticsearch.ppl.planner.dsl.QueryAction;
import com.amazon.opendistroforelasticsearch.sql.expression.domain.BindingTuple;
import com.amazon.opendistroforelasticsearch.sql.query.planner.core.ExecuteParams;
import com.amazon.opendistroforelasticsearch.sql.query.planner.core.PlanNode;
import com.amazon.opendistroforelasticsearch.sql.query.planner.physical.PhysicalOperator;
import com.amazon.opendistroforelasticsearch.sql.query.planner.physical.Row;
import com.amazon.opendistroforelasticsearch.sql.query.planner.physical.estimation.Cost;
import com.amazon.opendistroforelasticsearch.sql.query.planner.physical.node.scroll.BindingTupleRow;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.search.SearchResponse;

import java.util.Iterator;

@RequiredArgsConstructor
public class PhysicalScroll implements PhysicalOperator<BindingTuple> {
    private final QueryAction queryAction;

    private Iterator<BindingTupleRow> rowIterator;

    @Override
    public Cost estimate() {
        return null;
    }

    @Override
    public PlanNode[] children() {
        return new PlanNode[0];
    }

    @Override
    public boolean hasNext() {
        return rowIterator.hasNext();
    }

    @Override
    public Row<BindingTuple> next() {
        return rowIterator.next();
    }

    @Override
    public void open(ExecuteParams params) {
        SearchResponse response = queryAction.run();
        rowIterator = SearchResponseHelper
                .populateSearchResponse(response)
                .iterator();
    }

    @Override
    public String toString() {
        return queryAction.explain();
    }
}
