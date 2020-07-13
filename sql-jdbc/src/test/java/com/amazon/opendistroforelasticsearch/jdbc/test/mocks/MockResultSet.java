package com.amazon.opendistroforelasticsearch.jdbc.test.mocks;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MockResultSet {

    MockResultSetMetaData mockResultSetMetaData;
    MockResultSetRows mockResultSetRows;

    public MockResultSet(MockResultSetMetaData mockResultSetMetaData, MockResultSetRows mockResultSetRows) {
        if (mockResultSetMetaData == null || mockResultSetRows == null) {
            throw new IllegalArgumentException("Neither metadata nor rows can be null");
        }

        if (!mockResultSetRows.isEmpty() && mockResultSetMetaData.getColumnCount() != mockResultSetRows.getColumnCount()) {
            throw new IllegalArgumentException(
                    "Column count mismatch. MetaData has " + mockResultSetMetaData.getColumnCount() +
                            " columns, but rows have " + mockResultSetRows.getColumnCount());
        }

        this.mockResultSetMetaData = mockResultSetMetaData;
        this.mockResultSetRows = mockResultSetRows;
    }

    public void assertMatches(ResultSet rs) throws SQLException {
        mockResultSetMetaData.assertMatches(rs.getMetaData());
        mockResultSetRows.assertMatches(rs);
    }
}
