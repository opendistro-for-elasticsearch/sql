package org.nlpcn.es4sql.query.planner.logical.node;

import org.nlpcn.es4sql.query.join.TableInJoinRequestBuilder;
import org.nlpcn.es4sql.query.planner.core.PlanNode;
import org.nlpcn.es4sql.query.planner.logical.LogicalOperator;
import org.nlpcn.es4sql.query.planner.physical.PhysicalOperator;
import org.nlpcn.es4sql.query.planner.physical.node.scroll.Scroll;

import java.util.Map;

/**
 * Table scan
 */
public class TableScan implements LogicalOperator {

    /** Request builder for the table */
    private final TableInJoinRequestBuilder request;

    /** Page size for physical operator */
    private final int pageSize;

    public TableScan(TableInJoinRequestBuilder request, int pageSize) {
        this.request = request;
        this.pageSize = pageSize;
    }

    @Override
    public PlanNode[] children() {
        return new PlanNode[0];
    }

    @Override
    public <T> PhysicalOperator[] toPhysical(Map<LogicalOperator, PhysicalOperator<T>> optimalOps) {
        return new PhysicalOperator[]{
            new Scroll(request, pageSize)
        };
    }

    @Override
    public String toString() {
        return "TableScan";
    }


    /*********************************************
     *          Getters for Explain
     *********************************************/

    public String getTableAlias() {
        return request.getAlias();
    }

    public String getTableName() {
        return request.getOriginalSelect().getFrom().get(0).getIndex();
    }

}
