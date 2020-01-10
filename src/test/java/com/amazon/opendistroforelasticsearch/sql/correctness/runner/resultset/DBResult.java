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
import com.google.common.collect.ImmutableSet;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.json.JSONPropertyName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Query result for equality comparison. Based on different type of query, such as query with/without ORDER BY and
 * query with SELECT columns or just *, order of column and row may matter or not. So the internal data structure of this
 * class is passed in from outside either list or set, hash map or linked hash map etc.
 */
@EqualsAndHashCode(exclude = "databaseName")
@ToString
public class DBResult {

    /** Possible types for floating point number */
    private static final Set<String> FLOAT_TYPES = ImmutableSet.of("FLOAT", "DOUBLE", "REAL");

    /** Database name for display */
    private final String databaseName;

    /** Column name and types from result set meta data */
    @Getter
    private final Collection<Type> schema;

    /** Data rows from result set */
    private final Collection<Row> dataRows;

    /**
     * By default treat both columns and data rows in order. This makes sense for typical query
     * with specific column names in SELECT but without ORDER BY.
     */
    public DBResult(String databaseName) {
        this(databaseName, new ArrayList<>(), new HashSet<>());
    }

    public DBResult(String databaseName, Collection<Type> schema, Collection<Row> rows) {
        this.databaseName = databaseName;
        this.schema = schema;
        this.dataRows = rows;
    }

    public int columnSize() {
        return schema.size();
    }

    public void addColumn(String name, String type) {
        type = StringUtils.toUpper(type);

        // Ignore float type by assigning all type names string to it.
        if (FLOAT_TYPES.contains(type)) {
            type = FLOAT_TYPES.toString();
        }
        schema.add(new Type(StringUtils.toUpper(name), type));
    }

    public void addRow(Row row) {
        dataRows.add(row);
    }

    @JSONPropertyName("database")
    public String getDatabaseName() {
        return databaseName;
    }

    /** Flatten for simplifying json generated */
    public Collection<Collection<Object>> getDataRows() {
        return dataRows.stream().map(Row::getValues).collect(Collectors.toSet());
    }

}
