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

package com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.logical.rule;

import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.logical.LogicalPlanVisitor;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.logical.node.Filter;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.logical.node.Group;

/**
 * Push down selection (filter)
 */
public class SelectionPushDown implements LogicalPlanVisitor {

    /**
     * Store the filter found in visit and reused to push down.
     * It's not necessary to create a new one because no need to collect filter condition elsewhere
     */
    private Filter filter;

    @Override
    public boolean visit(Filter filter) {
        this.filter = filter;
        return true;
    }

    @Override
    public boolean visit(Group group) {
        if (filter != null && !filter.isNoOp()) {
            group.pushDown(filter);
        }
        return false; // avoid iterating operators in virtual Group
    }

}
