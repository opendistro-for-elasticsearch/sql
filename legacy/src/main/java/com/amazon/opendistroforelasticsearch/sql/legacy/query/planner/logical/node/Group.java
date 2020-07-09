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

package com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.logical.node;

import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core.PlanNode;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.logical.LogicalOperator;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.PhysicalOperator;

import java.util.Map;

/**
 * Project-Filter-TableScan group for push down optimization convenience.
 */
public class Group implements LogicalOperator {

    /**
     * Optional pushed down projection
     */
    private Project<?> project;

    /**
     * Optional pushed down filter (selection)
     */
    private Filter filter;

    /**
     * Required table scan operator
     */
    private final TableScan tableScan;


    public Group(TableScan tableScan) {
        this.tableScan = tableScan;
        this.filter = new Filter(tableScan);
        this.project = new Project<>(filter);
    }

    @Override
    public boolean isNoOp() {
        return true;
    }

    @Override
    public <T> PhysicalOperator[] toPhysical(Map<LogicalOperator, PhysicalOperator<T>> optimalOps) {
        return tableScan.toPhysical(optimalOps);
    }

    @Override
    public PlanNode[] children() {
        return new PlanNode[]{topNonNullNode()};
    }

    private PlanNode topNonNullNode() {
        return project != null ? project : (filter != null ? filter : tableScan);
    }

    public String id() {
        return tableScan.getTableAlias();
    }

    public void pushDown(Project<?> project) {
        this.project.pushDown(id(), project);
    }

    public void pushDown(Filter filter) {
        this.filter.pushDown(id(), filter);
    }

    @Override
    public String toString() {
        return "Group";
    }
}
