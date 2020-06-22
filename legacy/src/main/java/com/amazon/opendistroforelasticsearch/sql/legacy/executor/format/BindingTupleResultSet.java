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

package com.amazon.opendistroforelasticsearch.sql.legacy.executor.format;

import static com.amazon.opendistroforelasticsearch.sql.legacy.executor.format.DateFieldFormatter.FORMAT_JDBC;

import com.amazon.opendistroforelasticsearch.sql.legacy.expression.domain.BindingTuple;
import com.amazon.opendistroforelasticsearch.sql.legacy.expression.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.planner.core.ColumnNode;
import com.google.common.annotations.VisibleForTesting;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The definition of BindingTuple ResultSet.
 */
public class BindingTupleResultSet extends ResultSet {

    public BindingTupleResultSet(List<ColumnNode> columnNodes, List<BindingTuple> bindingTuples) {
        this.schema = buildSchema(columnNodes);
        this.dataRows = buildDataRows(columnNodes, bindingTuples);
    }

    @VisibleForTesting
    public static Schema buildSchema(List<ColumnNode> columnNodes) {
        List<Schema.Column> columnList = columnNodes.stream()
                                                 .map(node -> new Schema.Column(
                                                         node.getName(),
                                                         node.getAlias(),
                                                         node.getType()))
                                                 .collect(Collectors.toList());
        return new Schema("dummy", "dummy", columnList);
    }

    @VisibleForTesting
    public static DataRows buildDataRows(List<ColumnNode> columnNodes, List<BindingTuple> bindingTuples) {
        List<DataRows.Row> rowList = bindingTuples.stream().map(tuple -> {
            Map<String, ExprValue> bindingMap = tuple.getBindingMap();
            Map<String, Object> rowMap = new HashMap<>();
            for (ColumnNode column : columnNodes) {
                String columnName = column.columnName();
                Object value = bindingMap.get(columnName).value();
                if (column.getType() == Schema.Type.DATE) {
                    value = DateFormat.getFormattedDate(new Date((Long) value), FORMAT_JDBC);
                }
                rowMap.put(columnName, value);
            }
            return new DataRows.Row(rowMap);
        }).collect(Collectors.toList());

        return new DataRows(bindingTuples.size(), bindingTuples.size(), rowList);
    }
}

