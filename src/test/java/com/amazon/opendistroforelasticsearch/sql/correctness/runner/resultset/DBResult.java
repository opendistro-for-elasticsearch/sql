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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Query result for equality comparison. Based on different type of query, such as query with/without ORDER BY and
 * query with SELECT columns or just *, order of column and row may matter or not. So the internal data structure of this
 * class is passed in from outside either list or set, hash map or linked hash map etc.
 */
public class DBResult {
    private final String databaseName;
    private final Map<String, String> colTypeByName;
    private final Collection<Row> dataRows;
    private final List<String> colNames = new ArrayList<>(); // for equals & hashCode

    public DBResult(String databaseName, Map<String, String> colTypeByName, Collection<Row> rows) {
        this.databaseName = databaseName;
        this.colTypeByName = colTypeByName;
        this.dataRows = rows;

        colTypeByName.forEach((name, type) -> this.colNames.add(name.toUpperCase()));
    }

    public void addRow(Row row) {
        dataRows.add(row);
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public Map<String, String> getColumnNameAndTypes() {
        return colTypeByName;
    }

    public Collection<Row> getDataRows() {
        return dataRows;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DBResult result = (DBResult) o;
        return colNames.equals(result.colNames) &&
            dataRows.equals(result.dataRows);
    }

    @Override
    public int hashCode() {
        return Objects.hash(colNames, dataRows);
    }

    @Override
    public String toString() {
        return "DBResult: " + dataRows.stream().map(Row::toString).collect(Collectors.joining("\n"));
    }
}
