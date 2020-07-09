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

import com.amazon.opendistroforelasticsearch.jdbc.internal.JdbcWrapper;
import com.amazon.opendistroforelasticsearch.jdbc.types.ElasticsearchType;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MockResultSetMetaData implements ResultSetMetaData, JdbcWrapper {

    private List<MockColumnRsmd> mockColumnRsmds;

    public MockResultSetMetaData(List<MockColumnRsmd> mockColumnRsmds) {
        this.mockColumnRsmds = Collections.unmodifiableList(mockColumnRsmds);
    }

    public static Builder builder() {
        return new Builder();
    }
    @Override
    public int getColumnCount() {
        return mockColumnRsmds.size();
    }

    @Override
    public boolean isAutoIncrement(int column) throws SQLException {
        return mockColumnRsmds.get(column - 1).isAutoIncrement;
    }

    @Override
    public boolean isCaseSensitive(int column) throws SQLException {
        return mockColumnRsmds.get(column - 1).isCaseSensitive;
    }

    @Override
    public boolean isSearchable(int column) throws SQLException {
        return mockColumnRsmds.get(column - 1).isSearchable;
    }

    @Override
    public boolean isCurrency(int column) throws SQLException {
        return mockColumnRsmds.get(column - 1).isCurrency;
    }

    @Override
    public int isNullable(int column) throws SQLException {
        return  mockColumnRsmds.get(column - 1).isNullable;
    }

    @Override
    public boolean isSigned(int column) throws SQLException {
        return  mockColumnRsmds.get(column - 1).isSigned;
    }

    @Override
    public int getColumnDisplaySize(int column) throws SQLException {
        return  mockColumnRsmds.get(column - 1).columnDisplaySize;
    }

    @Override
    public String getColumnLabel(int column) throws SQLException {
        String label = mockColumnRsmds.get(column - 1).columnLabel;

        if (label == null) {
            // expected behavior per JDBC spec
            label = getColumnName(column);
        }
        return label;
    }

    @Override
    public String getColumnName(int column) throws SQLException {
        return  mockColumnRsmds.get(column - 1).columnName;
    }

    @Override
    public String getSchemaName(int column) throws SQLException {
        return  mockColumnRsmds.get(column - 1).schemaName;
    }

    @Override
    public int getPrecision(int column) throws SQLException {
        return  mockColumnRsmds.get(column - 1).precision;
    }

    @Override
    public int getScale(int column) throws SQLException {
        return  mockColumnRsmds.get(column - 1).scale;
    }

    @Override
    public String getTableName(int column) throws SQLException {
        return  mockColumnRsmds.get(column - 1).tableName;
    }

    @Override
    public String getCatalogName(int column) throws SQLException {
        return  mockColumnRsmds.get(column - 1).catalogName;
    }

    @Override
    public int getColumnType(int column) throws SQLException {
        return  mockColumnRsmds.get(column - 1).columnType;
    }

    @Override
    public String getColumnTypeName(int column) throws SQLException {
        return  mockColumnRsmds.get(column - 1).columnTypeName;
    }

    @Override
    public boolean isReadOnly(int column) throws SQLException {
        return  mockColumnRsmds.get(column - 1).isReadOnly;
    }

    @Override
    public boolean isWritable(int column) throws SQLException {
        return  mockColumnRsmds.get(column - 1).isWritable;
    }

    @Override
    public boolean isDefinitelyWritable(int column) throws SQLException {
        return mockColumnRsmds.get(column - 1).isDefinitelyWritable;
    }

    @Override
    public String getColumnClassName(int column) throws SQLException {
        return mockColumnRsmds.get(column - 1).columnClassName;
    }

    public static class Builder {

        private MockColumnRsmd currentColumnRsmd;

        private List<MockColumnRsmd> columnRsmds = new ArrayList<>();

        private Builder() {

        }

        Builder column() {
            MockColumnRsmd columnRsmd = new MockColumnRsmd();
            columnRsmds.add(columnRsmd);
            currentColumnRsmd = columnRsmd;
            return this;
        }

        public Builder column(String columnName) {
            column(columnName, ElasticsearchType.TEXT);
            return this;
        }

        public Builder column(String columnName, ElasticsearchType columnType) {
            column();
            setColumnName(columnName);
            setColumnESType(columnType);
            return this;
        }

        public MockResultSetMetaData build() {
            return new MockResultSetMetaData(columnRsmds);
        }


        public Builder setAutoIncrement(boolean autoIncrement) {
            currentColumnRsmd.isAutoIncrement = autoIncrement;
            return this;
        }

        public Builder setCaseSensitive(boolean caseSensitive) {
            currentColumnRsmd.isCaseSensitive = caseSensitive;
            return this;
        }

        public Builder setSearchable(boolean searchable) {
            currentColumnRsmd.isSearchable = searchable;
            return this;
        }

        public Builder setIsNullable(int isNullable) {
            currentColumnRsmd.isNullable = isNullable;
            return this;
        }

        public Builder setCurrency(boolean currency) {
            currentColumnRsmd.isCurrency = currency;
            return this;
        }

        public Builder setColumnTypeName(String columnTypeName) {
            currentColumnRsmd.columnTypeName = columnTypeName;
            return this;
        }

        public Builder setSigned(boolean signed) {
            currentColumnRsmd.isSigned = signed;
            return this;
        }

        public Builder setColumnDisplaySize(int columnDisplaySize) {
            currentColumnRsmd.columnDisplaySize = columnDisplaySize;
            return this;
        }

        public Builder setColumnLabel(String columnLabel) {
            currentColumnRsmd.columnLabel = columnLabel;
            return this;
        }

        public Builder setColumnName(String columnName) {
            currentColumnRsmd.columnName = columnName;
            return this;
        }

        public Builder setColumnESType(final ElasticsearchType esType) {
            setColumnType(esType.getJdbcType().getVendorTypeNumber());
            setPrecision(esType.getPrecision());
            setColumnDisplaySize(esType.getDisplaySize());
            setColumnClassName(esType.getJavaClassName());
            setColumnTypeName(esType.getJdbcType().getName());
            setSigned(esType.isSigned());
            return this;
        }

        public Builder setSchemaName(String schemaName) {
            currentColumnRsmd.schemaName = schemaName;
            return this;
        }

        public Builder setPrecision(int precision) {
            currentColumnRsmd.precision = precision;
            return this;
        }

        public Builder setScale(int scale) {
            currentColumnRsmd.scale = scale;
            return this;
        }

        public Builder setTableName(String tableName) {
            currentColumnRsmd.tableName = tableName;
            return this;
        }

        public Builder setCatalogName(String catalogName) {
            currentColumnRsmd.catalogName = catalogName;
            return this;
        }

        public Builder setColumnType(int columnType) {
            currentColumnRsmd.columnType = columnType;
            return this;
        }

        public Builder setReadOnly(boolean readOnly) {
            currentColumnRsmd.isReadOnly = readOnly;
            return this;
        }

        public Builder setWritable(boolean writable) {
            currentColumnRsmd.isWritable = writable;
            return this;
        }

        public Builder setDefinitelyWritable(boolean definitelyWritable) {
            currentColumnRsmd.isDefinitelyWritable = definitelyWritable;
            return this;
        }

        public Builder setColumnClassName(String columnClassName) {
            currentColumnRsmd.columnClassName = columnClassName;
            return this;
        }

    }

    public void assertMatches(ResultSetMetaData other) throws SQLException {
        assertEquals(this.getColumnCount(), other.getColumnCount(), "column count");

        for (int i = 1; i <= this.getColumnCount(); i++) {
            assertEquals(this.getCatalogName(i), other.getCatalogName(i), "column "+i+" catalog name");
            assertEquals(this.getColumnClassName(i), other.getColumnClassName(i), "column "+i+" column class name");
            assertEquals(this.getColumnDisplaySize(i), other.getColumnDisplaySize(i), "column "+i+" column disp size");
            assertEquals(this.getColumnName(i), other.getColumnName(i), "column "+i+" column name");
            assertEquals(this.getColumnLabel(i), other.getColumnLabel(i), "column "+i+" column label");
            assertEquals(this.getColumnType(i), other.getColumnType(i), "column "+i+" column type");
            assertEquals(this.isAutoIncrement(i), other.isAutoIncrement(i), "column "+i+" auto increment");
            assertEquals(this.isCaseSensitive(i), other.isCaseSensitive(i), "column "+i+" case sensitive");
            assertEquals(this.isSearchable(i), other.isSearchable(i), "column "+i+" searchable");
            assertEquals(this.isNullable(i), other.isNullable(i), "column "+i+" nullable");
            assertEquals(this.isCurrency(i), other.isCurrency(i), "column "+i+" is currency");
            assertEquals(this.getColumnTypeName(i), other.getColumnTypeName(i), "column "+i+" column type name");
            assertEquals(this.isSigned(i), other.isSigned(i), "column "+i+" signed");
            assertEquals(this.getSchemaName(i), other.getSchemaName(i), "column "+i+" schema name");
            assertEquals(this.getPrecision(i), other.getPrecision(i), "column "+i+" precision");
            assertEquals(this.getScale(i), other.getScale(i), "column "+i+" scale");
            assertEquals(this.getTableName(i), other.getTableName(i), "column "+i+" table name");
            assertEquals(this.isReadOnly(i), other.isReadOnly(i), "column "+i+" read only");
            assertEquals(this.isWritable(i), other.isWritable(i), "column "+i+" writable");
            assertEquals(this.isDefinitelyWritable(i), other.isDefinitelyWritable(i), "column "+i+" definitely writable");
        }
    }

    private static class MockColumnRsmd {

        // initialized to defaults used in the implementation
        private boolean isAutoIncrement = false;
        private boolean isCaseSensitive = true;
        private boolean isSearchable = true;
        private int isNullable = columnNullableUnknown;
        private boolean isCurrency = false;
        private String columnTypeName;
        private boolean isSigned = false;
        private int columnDisplaySize;
        private String columnLabel;
        private String columnName;
        private String schemaName = "";
        private int precision;
        private int scale = 0;
        private String tableName = "";
        private String catalogName = "";
        private int columnType;
        private boolean isReadOnly = true;
        private boolean isWritable = false;
        private boolean isDefinitelyWritable = false;
        private String columnClassName;

    }

}
