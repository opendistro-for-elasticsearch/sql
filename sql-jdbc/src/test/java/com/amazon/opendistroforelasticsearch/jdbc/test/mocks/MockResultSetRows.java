/*
 * Copyright <2019> Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.jdbc.test.mocks;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class MockResultSetRows {
    List<List<ColumnData>> rows;

    private MockResultSetRows(List<List<ColumnData>> rows) {
        this.rows = rows;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isEmpty() {
        return rows.isEmpty();
    }

    public int getColumnCount() {
        return isEmpty() ? -1 : rows.get(0).size();
    }

    public void assertMatches(ResultSet rs) throws SQLException {
        int rowNum = 0;

        for (List<ColumnData> columnData : rows) {
            rowNum++;
            rs.next();
            int i = 0;
            for (ColumnData data : columnData) {
                Object resultSetValue = rs.getObject(++i);
                assertEquals(data.getValue(), resultSetValue, "Row [" + rowNum + "], column [" + i + "] value mismatch");
                assertEquals(data.isNull(), rs.wasNull(),
                        "Row [" + rowNum + "], column [" + i + "] expected to be null: " + data.isNull() +
                                " but was: " + rs.wasNull());
            }
        }

        assertFalse(rs.next(), () -> "ResultSet has more rows than expected. Expected: " + rows.size() + " rows.");
    }

    public static class Builder {
        List<List<ColumnData>> rows = new ArrayList<>();

        private ArrayList<ColumnData> currentRow;

        private int rowSize = -1;

        private Builder() {

        }

        public Builder row() {
            if (rows.size() > 0) {
                if (rowSize == -1) {
                    rowSize = currentRow.size();
                } else {
                    validateRowSizes();
                }
            }
            currentRow = new ArrayList<>();
            rows.add(currentRow);
            return this;
        }

        public Builder column(Object value) {
            return column(value, false);
        }

        public Builder column(Object value, boolean isNull) {
            currentRow.add(new ColumnData(value, isNull));
            return this;
        }

        public MockResultSetRows build() {
            if (rows.size() > 1)
                validateRowSizes();
            return new MockResultSetRows(rows);
        }

        private void validateRowSizes() {
            if (rowSize != currentRow.size()) {
                throw new IllegalArgumentException(
                        "Expect the row to have " + rowSize + " elements, but only " + currentRow.size() + " were added.");

            }
        }
    }


    public static class ColumnData {
        private Object value;
        private boolean isNull;

        public ColumnData(Object value, boolean isNull) {
            this.value = value;
            this.isNull = isNull;
        }

        public Object getValue() {
            return value;
        }

        public boolean isNull() {
            return isNull;
        }
    }

    public static MockResultSetRows emptyResultSetRows() {
        return MockResultSetRows.builder().build();
    }
}
