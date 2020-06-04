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

package com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core;

import com.alibaba.druid.sql.ast.expr.SQLQueryExpr;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.ColumnTypeProvider;
import com.amazon.opendistroforelasticsearch.sql.legacy.expression.domain.BindingTuple;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.converter.SQLToOperatorConverter;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.PhysicalOperator;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.node.scroll.PhysicalScroll;
import lombok.Getter;
import org.elasticsearch.client.Client;

import java.util.ArrayList;
import java.util.List;

/**
 * The definition of QueryPlanner which return the {@link BindingTuple} as result.
 */
public class BindingTupleQueryPlanner {
    private PhysicalOperator<BindingTuple> physicalOperator;
    @Getter
    private List<ColumnNode> columnNodes;

    public BindingTupleQueryPlanner(Client client, SQLQueryExpr sqlExpr, ColumnTypeProvider columnTypeProvider) {
        SQLToOperatorConverter converter = new SQLToOperatorConverter(client, columnTypeProvider);
        sqlExpr.accept(converter);
        this.physicalOperator = converter.getPhysicalOperator();
        this.columnNodes = converter.getColumnNodes();
    }

    /**
     * Execute the QueryPlanner.
     * @return list of {@link BindingTuple}.
     */
    public List<BindingTuple> execute() {
        PhysicalOperator<BindingTuple> op = physicalOperator;
        List<BindingTuple> tuples = new ArrayList<>();
        try {
            op.open(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        while (op.hasNext()) {
            tuples.add(op.next().data());
        }
        return tuples;
    }

    /**
     * Explain the physical execution plan.
     * @return execution plan.
     */
    public String explain() {
        Explanation explanation = new Explanation();
        physicalOperator.accept(explanation);
        return explanation.explain();
    }

    private static class Explanation implements PlanNode.Visitor {
        private String explain;

        public String explain() {
            return explain;
        }

        @Override
        public boolean visit(PlanNode planNode) {
            if (planNode instanceof PhysicalScroll) {
                explain = planNode.toString();
            }
            return true;
        }
    }
}
