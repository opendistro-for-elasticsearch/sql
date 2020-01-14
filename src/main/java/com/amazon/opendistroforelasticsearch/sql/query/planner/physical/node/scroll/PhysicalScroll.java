/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.query.planner.physical.node.scroll;

import com.amazon.opendistroforelasticsearch.sql.exception.SqlParseException;
import com.amazon.opendistroforelasticsearch.sql.expression.domain.BindingTuple;
import com.amazon.opendistroforelasticsearch.sql.expression.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.query.AggregationQueryAction;
import com.amazon.opendistroforelasticsearch.sql.query.QueryAction;
import com.amazon.opendistroforelasticsearch.sql.query.planner.core.ExecuteParams;
import com.amazon.opendistroforelasticsearch.sql.query.planner.core.PlanNode;
import com.amazon.opendistroforelasticsearch.sql.query.planner.physical.PhysicalOperator;
import com.amazon.opendistroforelasticsearch.sql.query.planner.physical.Row;
import com.amazon.opendistroforelasticsearch.sql.query.planner.physical.estimation.Cost;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.search.aggregations.Aggregations;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.amazon.opendistroforelasticsearch.sql.expression.model.ExprValueFactory.fromJson;
import static com.amazon.opendistroforelasticsearch.sql.query.planner.physical.node.scroll.SearchAggregationResponseHelper.flatten;

/**
 * The definition of Scroll Operator.
 */
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
        try {
            ActionResponse response = queryAction.explain().get();
            if (queryAction instanceof AggregationQueryAction) {
                populateSearchAggregationResponse(((SearchResponse) response).getAggregations());
            } else {
                throw new IllegalStateException("Not support QueryAction type: " + queryAction.getClass());
            }
        } catch (SqlParseException e) {
            throw new RuntimeException(e);
        }
    }

    private void populateSearchAggregationResponse(Aggregations aggs) {
        List<Map<String, Object>> flatten = flatten(aggs);
        List<BindingTupleRow> bindingTupleList = flatten.stream().map(map -> {
            Map<String, ExprValue> ssValueMap = new HashMap<>();
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                ssValueMap.put(entry.getKey(), fromJson(entry.getValue()));
            }
            return BindingTuple.builder().bindingMap(ssValueMap).build();
        }).map(bindingTuple -> new BindingTupleRow(bindingTuple)).collect(Collectors.toList());
        rowIterator = bindingTupleList.iterator();
    }

    @SneakyThrows
    @Override
    public String toString() {
        return queryAction.explain().toString();
    }
}
