package org.nlpcn.es4sql.query.planner.physical;

import org.nlpcn.es4sql.query.planner.core.ExecuteParams;
import org.nlpcn.es4sql.query.planner.core.PlanNode;
import org.nlpcn.es4sql.query.planner.physical.estimation.Cost;

import java.util.Iterator;

/**
 * Physical operator
 */
public interface PhysicalOperator<T> extends PlanNode, Iterator<Row<T>>, AutoCloseable {

    /**
     * Estimate the cost of current physical operator
     *
     * @return  cost
     */
    Cost estimate();


    /**
     * Initialize operator.
     *
     * @param params    exuecution parameters needed
     */
    default void open(ExecuteParams params) throws Exception {
        for (PlanNode node : children()) {
            ((PhysicalOperator) node).open(params);
        }
    }


    /**
     * Close resources related to the operator.
     *
     * @throws Exception    potential exception raised
     */
    @Override
    default void close() {
        for (PlanNode node : children()) {
            ((PhysicalOperator) node).close();
        }
    }
}
