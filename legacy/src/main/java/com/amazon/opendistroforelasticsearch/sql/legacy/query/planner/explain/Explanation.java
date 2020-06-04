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

package com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.explain;

import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core.Plan;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core.PlanNode;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core.PlanNode.Visitor;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.logical.LogicalOperator;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.logical.node.Group;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.PhysicalOperator;
import com.google.common.collect.ImmutableMap;

/**
 * Base class for different explanation implementation
 */
public class Explanation implements Visitor {

    /**
     * Hard coding description to be consistent with old nested join explanation
     */
    private static final String DESCRIPTION =
            "Hash Join algorithm builds hash table based on result of first query, "
                    + "and then probes hash table to find matched rows for each row returned by second query";

    /**
     * Plans to be explained
     */
    private final Plan logicalPlan;
    private final Plan physicalPlan;

    /**
     * Explanation format
     */
    private final ExplanationFormat format;

    public Explanation(Plan logicalPlan,
                       Plan physicalPlan,
                       ExplanationFormat format) {
        this.logicalPlan = logicalPlan;
        this.physicalPlan = physicalPlan;
        this.format = format;
    }

    @Override
    public String toString() {
        format.prepare(ImmutableMap.of("description", DESCRIPTION));

        format.start("Logical Plan");
        logicalPlan.traverse(this);
        format.end();

        format.start("Physical Plan");
        physicalPlan.traverse(this);
        format.end();

        return format.toString();
    }

    @Override
    public boolean visit(PlanNode node) {
        if (isValidOp(node)) {
            format.explain(node);
        }
        return true;
    }

    @Override
    public void endVisit(PlanNode node) {
        if (isValidOp(node)) {
            format.end();
        }
    }

    /**
     * Check if node is a valid logical or physical operator
     */
    private boolean isValidOp(PlanNode node) {
        return isValidLogical(node) || isPhysical(node);
    }

    /**
     * Valid logical operator means it's Group OR NOT a no-op because Group clarify explanation
     */
    private boolean isValidLogical(PlanNode node) {
        return (node instanceof LogicalOperator)
                && (node instanceof Group || !((LogicalOperator) node).isNoOp());
    }

    /**
     * Right now all physical operators are valid and non-no-op
     */
    private boolean isPhysical(PlanNode node) {
        return node instanceof PhysicalOperator;
    }

}
