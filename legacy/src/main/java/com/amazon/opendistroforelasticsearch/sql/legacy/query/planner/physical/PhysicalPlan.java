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

package com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical;

import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core.ExecuteParams;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core.Plan;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core.PlanNode.Visitor;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.estimation.Estimation;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.resource.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.search.SearchHit;

import java.util.ArrayList;
import java.util.List;

/**
 * Physical plan
 */
public class PhysicalPlan implements Plan {

    private static final Logger LOG = LogManager.getLogger();

    /**
     * Optimized logical plan that being ready for physical planning
     */
    private final LogicalPlan logicalPlan;

    /**
     * Root of physical plan tree
     */
    private PhysicalOperator<SearchHit> root;

    public PhysicalPlan(LogicalPlan logicalPlan) {
        this.logicalPlan = logicalPlan;
    }

    @Override
    public void traverse(Visitor visitor) {
        if (root != null) {
            root.accept(visitor);
        }
    }

    @Override
    public void optimize() {
        Estimation<SearchHit> estimation = new Estimation<>();
        logicalPlan.traverse(estimation);
        root = estimation.optimalPlan();
    }

    /**
     * Execute physical plan after verifying if system is healthy at the moment
     */
    public List<SearchHit> execute(ExecuteParams params) {
        if (shouldReject(params)) {
            throw new IllegalStateException("Query request rejected due to insufficient resource");
        }

        try (PhysicalOperator<SearchHit> op = root) {
            return doExecutePlan(op, params);
        } catch (Exception e) {
            LOG.error("Error happened during execution", e);
            // Runtime error or circuit break. Should we return partial result to customer?
            throw new IllegalStateException("Error happened during execution", e);
        }
    }

    /**
     * Reject physical plan execution of new query request if unhealthy
     */
    private boolean shouldReject(ExecuteParams params) {
        return !((ResourceManager) params.get(ExecuteParams.ExecuteParamType.RESOURCE_MANAGER)).isHealthy();
    }

    /**
     * Execute physical plan in order: open, fetch result, close
     */
    private List<SearchHit> doExecutePlan(PhysicalOperator<SearchHit> op,
                                          ExecuteParams params) throws Exception {
        List<SearchHit> hits = new ArrayList<>();
        op.open(params);

        while (op.hasNext()) {
            hits.add(op.next().data());
        }

        if (LOG.isTraceEnabled()) {
            hits.forEach(hit -> LOG.trace("Final result row: {}", hit.getSourceAsMap()));
        }
        return hits;
    }

}
