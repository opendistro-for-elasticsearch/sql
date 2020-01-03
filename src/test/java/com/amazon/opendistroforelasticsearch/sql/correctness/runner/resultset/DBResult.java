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

package com.amazon.opendistroforelasticsearch.sql.correctness.runner.resultset;

import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Query result for equality comparison. Based on different type of query, such as query with/without ORDER BY and
 * query with SELECT columns or just *, order of column and row may matter or not. So the internal data structure of this
 * class is passed in from outside either list or set, hash map or linked hash map etc.
 */
public class DBResult {

    /** Database name for display */
    private final String databaseName;

    /** Column name and types from result set meta data */
    private final Map<String, String> colNameAndTypes;

    /** Data rows from result set */
    private final Collection<Row> dataRows;

    /**
     * By default treat both columns and data rows in order. This makes sense for typical query
     * with specific column names in SELECT but without ORDER BY.
     */
    public DBResult(String databaseName) {
        this(databaseName, new LinkedHashMap<>(), new HashSet<>());
    }

    public DBResult(String databaseName, Map<String, String> colNameAndTypes, Collection<Row> rows) {
        this.databaseName = databaseName;
        this.colNameAndTypes = colNameAndTypes;
        this.dataRows = rows;
    }

    public int columnSize() {
        return colNameAndTypes.size();
    }

    public void addColumn(String name, String type) {
        colNameAndTypes.put(StringUtils.toUpper(name), StringUtils.toUpper(type));
    }

    public void addRow(Row row) {
        dataRows.add(row);
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public Map<String, String> getColumnNameAndTypes() {
        return colNameAndTypes;
    }

    public Collection<Row> getDataRows() {
        return dataRows;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DBResult result = (DBResult) o;
        return colNameAndTypes.equals(result.colNameAndTypes) &&
            dataRows.equals(result.dataRows);
    }

    @Override
    public int hashCode() {
        return Objects.hash(colNameAndTypes, dataRows);
    }

    @Override
    public String toString() {
        return "DBResult{" +
            "databaseName='" + databaseName + '\'' +
            ", colNameAndTypes=" + colNameAndTypes +
            ", dataRows=" + dataRows +
            '}';
    }
}
