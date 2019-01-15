package org.nlpcn.es4sql.query.planner.core;

import org.nlpcn.es4sql.query.planner.core.PlanNode.Visitor;

/**
 * Query plan
 */
public interface Plan {

    /**
     * Explain current query plan by visitor
     *
     * @param explanation   visitor to explain the plan
     */
    void traverse(Visitor explanation);

    /**
     * Optimize current query plan to get the optimal one
     */
    void optimize();

}
