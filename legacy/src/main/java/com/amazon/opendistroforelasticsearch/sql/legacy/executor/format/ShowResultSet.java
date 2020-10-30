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

import com.amazon.opendistroforelasticsearch.sql.legacy.domain.IndexStatement;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.format.DataRows.Row;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.format.Schema.Column;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.format.Schema.Type;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.client.Client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ShowResultSet extends ResultSet {

    private static final String TABLE_TYPE = "BASE TABLE";

    private IndexStatement statement;
    private Object queryResult;

    public ShowResultSet(Client client, IndexStatement statement, Object queryResult) {
        this.client = client;
        this.clusterName = getClusterName();
        this.statement = statement;
        this.queryResult = queryResult;

        this.schema = new Schema(statement, loadColumns());
        this.dataRows = new DataRows(loadRows());
    }

    private List<Column> loadColumns() {
        List<Column> columns = new ArrayList<>();
        // Unused Columns are still included in Schema to match JDBC/ODBC standard
        columns.add(new Column("TABLE_CAT", null, Type.KEYWORD));
        columns.add(new Column("TABLE_SCHEM", null, Type.KEYWORD)); // Not used
        columns.add(new Column("TABLE_NAME", null, Type.KEYWORD));
        columns.add(new Column("TABLE_TYPE", null, Type.KEYWORD));
        columns.add(new Column("REMARKS", null, Type.KEYWORD)); // Not used
        columns.add(new Column("TYPE_CAT", null, Type.KEYWORD)); // Not used
        columns.add(new Column("TYPE_SCHEM", null, Type.KEYWORD)); // Not used
        columns.add(new Column("TYPE_NAME", null, Type.KEYWORD)); // Not used
        columns.add(new Column("SELF_REFERENCING_COL_NAME", null, Type.KEYWORD)); // Not used
        columns.add(new Column("REF_GENERATION", null, Type.KEYWORD)); // Not used

        return columns;
    }

    private List<Row> loadRows() {
        List<Row> rows = new ArrayList<>();
        for (String index : extractIndices()) {
            rows.add(new Row(loadData(index)));
        }

        return rows;
    }

    private List<String> extractIndices() {
        String indexPattern = statement.getIndexPattern();
        String[] indices = ((GetIndexResponse) queryResult).getIndices();

        return Arrays.stream(indices)
                .filter(index -> matchesPatternIfRegex(index, indexPattern))
                .collect(Collectors.toList());
    }

    private Map<String, Object> loadData(String tableName) {
        Map<String, Object> data = new HashMap<>();
        data.put("TABLE_CAT", clusterName);
        data.put("TABLE_NAME", tableName);
        data.put("TABLE_TYPE", TABLE_TYPE);

        return data;
    }
}
