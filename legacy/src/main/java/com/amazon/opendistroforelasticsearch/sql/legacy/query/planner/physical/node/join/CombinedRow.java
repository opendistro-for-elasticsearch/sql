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

import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.physical.Row;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Combined row to store matched relation from single right row to N left rows.
 *
 * @param <T> data object underlying, ex. SearchHit
 */
public class CombinedRow<T> {

    private Row<T> rightRow;
    private Collection<Row<T>> leftRows;

    public CombinedRow(Row<T> rightRow, Collection<Row<T>> leftRows) {
        this.rightRow = rightRow;
        this.leftRows = leftRows;
    }

    public List<Row<T>> combine() {
        List<Row<T>> combinedRows = new ArrayList<>();
        for (Row<T> leftRow : leftRows) {
            combinedRows.add(leftRow.combine(rightRow));
        }
        return combinedRows;
    }

    public Collection<Row<T>> leftMatchedRows() {
        return Collections.unmodifiableCollection(leftRows);
    }

    @Override
    public String toString() {
        return "CombinedRow{rightRow=" + rightRow + ", leftRows=" + leftRows + '}';
    }
}
