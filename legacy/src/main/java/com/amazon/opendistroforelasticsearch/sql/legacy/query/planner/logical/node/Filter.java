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

import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Select;
import com.amazon.opendistroforelasticsearch.sql.legacy.domain.Where;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.join.TableInJoinRequestBuilder;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core.PlanNode;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.logical.LogicalOperator;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.PhysicalOperator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Selection expression
 */
public class Filter implements LogicalOperator {

    private final LogicalOperator next;

    /**
     * Alias to WHERE clause mapping
     */
    private final Map<String, Where> aliasWhereMap = new HashMap<>();

    public Filter(LogicalOperator next, List<TableInJoinRequestBuilder> tables) {
        this.next = next;

        for (TableInJoinRequestBuilder table : tables) {
            Select select = table.getOriginalSelect();
            if (select.getWhere() != null) {
                aliasWhereMap.put(table.getAlias(), select.getWhere());
            }
        }
    }

    public Filter(LogicalOperator next) {
        this.next = next;
    }

    @Override
    public PlanNode[] children() {
        return new PlanNode[]{next};
    }

    @Override
    public boolean isNoOp() {
        return aliasWhereMap.isEmpty();
    }

    @Override
    public <T> PhysicalOperator[] toPhysical(Map<LogicalOperator, PhysicalOperator<T>> optimalOps) {
        // Always no-op after push down, skip it by returning next
        return new PhysicalOperator[]{optimalOps.get(next)};
    }

    public void pushDown(String tableAlias, Filter pushedDownFilter) {
        Where pushedDownWhere = pushedDownFilter.aliasWhereMap.remove(tableAlias);
        if (pushedDownWhere != null) {
            aliasWhereMap.put(tableAlias, pushedDownWhere);
        }
    }

    @Override
    public String toString() {
        return "Filter [ conditions=" + aliasWhereMap.values() + " ]";
    }

}
