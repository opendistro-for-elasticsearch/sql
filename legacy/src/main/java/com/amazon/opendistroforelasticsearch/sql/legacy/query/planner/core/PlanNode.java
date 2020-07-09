/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core;

/**
 * Abstract plan node in query plan.
 */
public interface PlanNode {

    /**
     * All child nodes of current node used for traversal.
     *
     * @return all children
     */
    PlanNode[] children();

    /**
     * Accept a visitor and traverse the plan tree with it.
     *
     * @param visitor plan node visitor
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
         *
         * @param op plan node being visited
         */
        boolean visit(PlanNode op);

        /**
         * Re-visit current node before return to parent node
         *
         * @param op plan node finished visit
         */
        default void endVisit(PlanNode op) {
        }
    }

}
