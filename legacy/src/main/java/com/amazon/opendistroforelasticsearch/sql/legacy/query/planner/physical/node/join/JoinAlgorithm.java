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

package com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.node.join;

import com.alibaba.druid.sql.ast.statement.SQLJoinTableSource.JoinType;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core.ExecuteParams;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core.PlanNode;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.logical.node.Join.JoinCondition;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.PhysicalOperator;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.Row;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.node.BatchPhysicalOperator;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.resource.blocksize.BlockSize;
import com.google.common.collect.Sets;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.Collections.emptyList;

/**
 * Join algorithm base class
 *
 * @param <T>
 */
public abstract class JoinAlgorithm<T> extends BatchPhysicalOperator<T> {

    protected static final Logger LOG = LogManager.getLogger();

    /**
     * Left child operator
     */
    private final PhysicalOperator<T> left;

    /**
     * Right child operator handled by concrete join algorithm subclass
     */
    protected final PhysicalOperator<T> right;

    /**
     * Join type ex. inner join, left join
     */
    private final JoinType type;

    /**
     * Joined columns in ON conditions
     */
    private final JoinCondition condition;

    /**
     * Block size calculator
     */
    private final BlockSize blockSize;

    /**
     * Bookkeeping unmatched rows in current block from left
     */
    private final Set<Row<T>> leftMismatch;

    /**
     * Hash table for right table probing
     */
    protected HashTable<T> hashTable;

    /**
     * Execute params to reset right side for each left block
     */
    protected ExecuteParams params;

    JoinAlgorithm(PhysicalOperator<T> left,
                  PhysicalOperator<T> right,
                  JoinType type,
                  JoinCondition condition,
                  BlockSize blockSize) {
        this.left = left;
        this.right = right;
        this.type = type;
        this.condition = condition;
        this.blockSize = blockSize;
        this.hashTable = new HashTableGroup<>(condition);
        this.leftMismatch = Sets.newIdentityHashSet();
    }

    @Override
    public PlanNode[] children() {
        return new PlanNode[]{left, right};
    }

    @Override
    public void open(ExecuteParams params) throws Exception {
        super.open(params);
        left.open(params);
        this.params = params;
    }

    @Override
    public void close() {
        super.close();
        hashTable.clear();
        leftMismatch.clear();
        LOG.debug("Cleared all resources used by join");
    }

    /**
     * Build-probe left and right block by block to prefetch next matches (and mismatches if outer join).
     * <p>
     * 1) Build hash table and open right side.
     * 2) Keep probing right to find matched rows (meanwhile update mismatched set)
     * 3) Check if any row in mismatched set to return in the case of outer join.
     * 4) Nothing remained now, move on to next block of left. Go back to step 1.
     * <p>
     * This is a new run AND no block from left means algorithm should stop and return empty.
     */
    @Override
    protected Collection<Row<T>> prefetch() throws Exception {
        while (!isNewRunButNoMoreBlockFromLeft()) {

            // 1.Build hash table and (re-)open right side for the new run
            if (isNewRun()) {
                buildHashTableByNextBlock();
                reopenRight();
            }

            // 2.Keep probing right by the hash table and bookkeeping mismatch
            while (isAnyMoreDataFromRight()) {
                Collection<Row<T>> matched = probeMatchAndBookkeepMismatch();
                if (!matched.isEmpty()) {
                    return matched;
                }
            }

            // 3.You know it's a mismatch only after this run finished (left block + all right).
            if (isAnyMismatchForOuterJoin()) {
                return returnAndClearMismatch();
            }

            // 4.Clean up and close right
            cleanUpAndCloseRight();
        }
        return emptyList();
    }

    /**
     * Probe right by hash table built from left. Handle matched and mismatched rows.
     */
    private Collection<Row<T>> probeMatchAndBookkeepMismatch() {
        if (hashTable.isEmpty()) {
            throw new IllegalStateException("Hash table is NOT supposed to be empty");
        }

        List<CombinedRow<T>> combinedRows = probe();

        List<Row<T>> matchRows = new ArrayList<>();
        if (combinedRows.isEmpty()) {
            LOG.debug("No matched row found");
        } else {
            if (LOG.isTraceEnabled()) {
                combinedRows.forEach(row -> LOG.trace("Matched row before combined: {}", row));
            }

            for (CombinedRow<T> row : combinedRows) {
                matchRows.addAll(row.combine());
            }

            if (LOG.isTraceEnabled()) {
                matchRows.forEach(row -> LOG.trace("Matched row after combined: {}", row));
            }

            bookkeepMismatchedRows(combinedRows);
        }
        return matchRows;
    }

    private boolean isNewRunButNoMoreBlockFromLeft() {
        return isNewRun() && !isAnyMoreBlockFromLeft();
    }

    private boolean isNewRun() {
        return hashTable.isEmpty();
    }

    private boolean isAnyMoreBlockFromLeft() {
        return left.hasNext();
    }

    private boolean isAnyMoreDataFromRight() {
        return right.hasNext();
    }

    private boolean isAnyMismatchForOuterJoin() {
        return !leftMismatch.isEmpty();
    }

    /**
     * Clone mismatch list and clear it so that we won't return it forever
     */
    @SuppressWarnings("unchecked")
    private Collection<Row<T>> returnAndClearMismatch() {
        if (LOG.isTraceEnabled()) {
            leftMismatch.forEach(row -> LOG.trace("Mismatched rows before combined: {}", row));
        }

        List<Row<T>> result = new ArrayList<>();
        for (Row<T> row : leftMismatch) {
            result.add(row.combine(Row.NULL));
        }

        if (LOG.isTraceEnabled()) {
            result.forEach(row -> LOG.trace("Mismatched rows after combined: {}", row));
        }
        leftMismatch.clear();
        return result;
    }

    /**
     * Building phase:
     * Build hash table from data block.
     */
    private void buildHashTableByNextBlock() {
        List<Row<T>> block = loadNextBlockFromLeft(blockSize.size());
        if (LOG.isTraceEnabled()) {
            LOG.trace("Build hash table on conditions with block: {}, {}", condition, block);
        }

        for (Row<T> data : block) {
            hashTable.add(data);
        }

        if (type == JoinType.LEFT_OUTER_JOIN) {
            leftMismatch.addAll(block);
        }
    }

    private void cleanUpAndCloseRight() {
        LOG.debug("No more data from right. Clean up and close right.");
        hashTable.clear();
        leftMismatch.clear();
        right.close();
    }

    private List<Row<T>> loadNextBlockFromLeft(int blockSize) {
        List<Row<T>> block = new ArrayList<>();
        for (int i = 0; i < blockSize && left.hasNext(); i++) {
            block.add(left.next());
        }
        return block;
    }

    private void bookkeepMismatchedRows(List<CombinedRow<T>> combinedRows) {
        if (type == JoinType.LEFT_OUTER_JOIN) {
            for (CombinedRow<T> row : combinedRows) {
                leftMismatch.removeAll(row.leftMatchedRows());
            }
        }
    }

    /**
     * (Re-)open right side by params.
     */
    protected abstract void reopenRight() throws Exception;


    /**
     * Probing phase
     *
     * @return matched rows from left and right in
     */
    protected abstract List<CombinedRow<T>> probe();


    @Override
    public String toString() {
        return getClass().getSimpleName() + "[ conditions=" + condition
                + ", type=" + type + ", blockSize=[" + blockSize + "] ]";
    }

}
