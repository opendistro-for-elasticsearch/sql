package org.nlpcn.es4sql.query.planner.core;

/**
 * Abstract plan node in query plan.
 */
public interface PlanNode {

    /**
     * All child nodes of current node used for traversal.
     * @return  all children
     */
    PlanNode[] children();

    /**
     * Accept a visitor and traverse the plan tree with it.
     * @param visitor   plan node visitor
     */
    default void accept(Visitor visitor) {
        if (visitor.visit(this)) {
            for (PlanNode node : children()) {
                node.accept(visitor);
            }
        }
        visitor.endVisit(this);
    }

    /**
     * Plan node visitor.
     */
    interface Visitor {

        /**
         * To avoid listing all subclasses of PlanNode here, we dispatch manually in concrete visitor.
         * @param op    plan node being visited
         */
        boolean visit(PlanNode op);

        /**
         * Re-visit current node before return to parent node
         * @param op    plan node finished visit
         */
        default void endVisit(PlanNode op) {}
    }

}
