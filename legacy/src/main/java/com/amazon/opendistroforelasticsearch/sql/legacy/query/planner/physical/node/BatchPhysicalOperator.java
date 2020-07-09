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

package com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.node;

import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core.ExecuteParams;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.PhysicalOperator;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.Row;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.resource.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

import static com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core.ExecuteParams.ExecuteParamType.RESOURCE_MANAGER;

/**
 * Abstraction for physical operators that load large volume of data and generally prefetch for efficiency.
 *
 * @param <T>
 */
public abstract class BatchPhysicalOperator<T> implements PhysicalOperator<T> {

    protected static final Logger LOG = LogManager.getLogger();

    /**
     * Resource monitor to avoid consuming too much resource
     */
    private ResourceManager resourceMgr;

    /**
     * Current batch of data
     */
    private Iterator<Row<T>> curBatch;

    @Override
    public void open(ExecuteParams params) throws Exception {
        //PhysicalOperator.super.open(params); // Child needs to call this super.open() and open its next node too
        resourceMgr = params.get(RESOURCE_MANAGER);
    }

    @Override
    public boolean hasNext() {
        if (isNoMoreDataInCurrentBatch()) {
            LOG.debug("{} No more data in current batch, pre-fetching next batch", this);
            Collection<Row<T>> nextBatch = prefetchSafely();

            LOG.debug("{} Pre-fetched {} rows", this, nextBatch.size());
            if (LOG.isTraceEnabled()) {
                nextBatch.forEach(row -> LOG.trace("Row pre-fetched: {}", row));
            }

            curBatch = nextBatch.iterator();
        }
        return curBatch.hasNext();
    }

    @Override
    public Row<T> next() {
        return curBatch.next();
    }

    /**
     * Prefetch next batch safely by checking resource monitor
     */
    private Collection<Row<T>> prefetchSafely() {
        Objects.requireNonNull(resourceMgr, "ResourceManager is not set so unable to do sanity check");

        boolean isHealthy = resourceMgr.isHealthy();
        boolean isTimeout = resourceMgr.isTimeout();
        if (isHealthy && !isTimeout) {
            try {
                return prefetch();
            } catch (Exception e) {
                throw new IllegalStateException("Failed to prefetch next batch", e);
            }
        }
        throw new IllegalStateException("Exit due to " + (isHealthy ? "time out" : "insufficient resource"));
    }

    /**
     * Prefetch next batch if current is exhausted.
     *
     * @return next batch
     */
    protected abstract Collection<Row<T>> prefetch() throws Exception;

    private boolean isNoMoreDataInCurrentBatch() {
        return curBatch == null || !curBatch.hasNext();
    }

}
