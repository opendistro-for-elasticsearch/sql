package org.nlpcn.es4sql.query.planner.logical;

import org.json.JSONPropertyIgnore;
import org.nlpcn.es4sql.query.planner.core.PlanNode;
import org.nlpcn.es4sql.query.planner.physical.PhysicalOperator;

import java.util.Map;

/**
 * Logical operator in logical plan tree.
 */
public interface LogicalOperator extends PlanNode {

    /**
     * If current operator is no operation. It depends on specific internal state of operator
     *
     * Ignore this field in explanation because all explainable operator are NOT no-op.
     *
     * @return  true if NoOp
     */
    @JSONPropertyIgnore
    default boolean isNoOp() {
        return false;
    }

    /**
     * Map logical operator to physical operators (possibly 1 to N mapping)
     *
     * Note that generic type on PhysicalOperator[] would enforce all impl convert array to generic type array
     * because generic type array is unable to be created directly.
     *
     * @param optimalOps    optimal physical operators estimated so far
     * @return              list of physical operator
     */
    <T> PhysicalOperator[] toPhysical(Map<LogicalOperator, PhysicalOperator<T>> optimalOps);

}
