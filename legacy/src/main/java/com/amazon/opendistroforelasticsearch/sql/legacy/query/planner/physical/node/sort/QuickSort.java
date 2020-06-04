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

package com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.node.sort;

import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core.ExecuteParams;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core.PlanNode;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.PhysicalOperator;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.Row;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.estimation.Cost;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.node.BatchPhysicalOperator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static java.util.Collections.emptyList;

/**
 * Physical operator to sort by quick sort implementation in JDK.
 * Note that this is all in-memory operator which may be a problem for large index.
 *
 * @param <T> actual data type, ex.SearchHit
 */
public class QuickSort<T> extends BatchPhysicalOperator<T> {

    private static final Logger LOG = LogManager.getLogger();

    private final PhysicalOperator<T> next;

    /**
     * Column name list in ORDER BY
     */
    private final String[] orderByColNames;

    /**
     * Order by type, ex. ASC, DESC
     */
    private final String orderByType;

    private boolean isDone = false;

    public QuickSort(PhysicalOperator<T> next, List<String> orderByColNames, String orderByType) {
        this.next = next;
        this.orderByColNames = orderByColNames.toArray(new String[0]);
        this.orderByType = orderByType;
    }

    @Override
    public PlanNode[] children() {
        return new PlanNode[]{next};
    }

    @Override
    public Cost estimate() {
        return new Cost();
    }

    @Override
    public void open(ExecuteParams params) throws Exception {
        super.open(params);
        next.open(params);
    }

    /**
     * Only load all data once and return one batch
     */
    @Override
    protected Collection<Row<T>> prefetch() {
        if (isDone) {
            return emptyList();
        }

        List<Row<T>> allRowsSorted = new ArrayList<>();
        next.forEachRemaining(allRowsSorted::add);
        allRowsSorted.sort(createRowComparator());

        if (LOG.isTraceEnabled()) {
            LOG.trace("All rows being sorted in RB-Tree: {}", allRowsSorted);
        }

        isDone = true;
        return allRowsSorted;
    }

    private Comparator<Row<T>> createRowComparator() {
        Comparator<Row<T>> comparator = Comparator.comparing(o -> o.key(orderByColNames));
        if ("DESC".equals(orderByType)) {
            comparator = comparator.reversed();
        }
        return comparator;
    }

    @Override
    public String toString() {
        return "QuickSort [ columns=" + Arrays.toString(orderByColNames) + ", order=" + orderByType + " ]";
    }

}
