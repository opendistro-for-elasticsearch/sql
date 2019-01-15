package org.nlpcn.es4sql.query.planner.physical.node.join;

import org.nlpcn.es4sql.query.planner.physical.Row;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Combined row to store matched relation from single right row to N left rows.
 *
 * @param <T>   data object underlying, ex. SearchHit
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
        return "CombinedRow{" +
            "rightRow=" + rightRow +
            ", leftRows=" + leftRows +
            '}';
    }
}
