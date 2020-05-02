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

package com.amazon.opendistroforelasticsearch.sql.executor.format;

import com.amazon.opendistroforelasticsearch.sql.domain.ColumnTypeProvider;
import com.amazon.opendistroforelasticsearch.sql.domain.Delete;
import com.amazon.opendistroforelasticsearch.sql.domain.IndexStatement;
import com.amazon.opendistroforelasticsearch.sql.domain.Query;
import com.amazon.opendistroforelasticsearch.sql.domain.QueryStatement;
import com.amazon.opendistroforelasticsearch.sql.executor.adapter.QueryPlanQueryAction;
import com.amazon.opendistroforelasticsearch.sql.executor.adapter.QueryPlanRequestBuilder;
import com.amazon.opendistroforelasticsearch.sql.executor.format.DataRows.Row;
import com.amazon.opendistroforelasticsearch.sql.executor.format.Schema.Column;
import com.amazon.opendistroforelasticsearch.sql.expression.domain.BindingTuple;
import com.amazon.opendistroforelasticsearch.sql.query.DefaultQueryAction;
import com.amazon.opendistroforelasticsearch.sql.query.QueryAction;
import com.amazon.opendistroforelasticsearch.sql.query.planner.core.ColumnNode;
import org.elasticsearch.client.Client;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.amazon.opendistroforelasticsearch.sql.domain.IndexStatement.StatementType;

public class Protocol {

    static final int OK_STATUS = 200;
    static final int ERROR_STATUS = 500;

    private final String formatType;
    private int status;
    private long size;
    private long total;
    private ResultSet resultSet;
    private ErrorMessage error;
    private List<ColumnNode> columnNodeList;
    private ColumnTypeProvider scriptColumnType = new ColumnTypeProvider();

    public Protocol(Client client, QueryAction queryAction, Object queryResult, String formatType) {
        if (queryAction instanceof QueryPlanQueryAction) {
            this.columnNodeList =
                    ((QueryPlanRequestBuilder) (((QueryPlanQueryAction) queryAction).explain())).outputColumns();
        } else if (queryAction instanceof DefaultQueryAction) {
            scriptColumnType = queryAction.getScriptColumnType();
        }
        this.formatType = formatType;
        QueryStatement query = queryAction.getQueryStatement();
        this.status = OK_STATUS;
        this.resultSet = loadResultSet(client, query, queryResult);
        this.size = resultSet.getDataRows().getSize();
        this.total = resultSet.getDataRows().getTotalHits();
    }

    public Protocol(Exception e) {
        this.formatType = null;
        this.status = ERROR_STATUS;
        this.error = new ErrorMessage(e, ERROR_STATUS);
    }

    private ResultSet loadResultSet(Client client, QueryStatement queryStatement, Object queryResult) {
        if (queryResult instanceof List) {
            return new BindingTupleResultSet(columnNodeList, (List<BindingTuple>) queryResult);
        }
        if (queryStatement instanceof Delete) {
            return new DeleteResultSet(client, (Delete) queryStatement, queryResult);
        } else if (queryStatement instanceof Query) {
            return new SelectResultSet(client, (Query) queryStatement, queryResult, scriptColumnType, formatType);
        } else if (queryStatement instanceof IndexStatement) {
            IndexStatement statement = (IndexStatement) queryStatement;
            StatementType statementType = statement.getStatementType();

            if (statementType == StatementType.SHOW) {
                return new ShowResultSet(client, statement, queryResult);
            } else if (statementType == StatementType.DESCRIBE) {
                return new DescribeResultSet(client, statement, queryResult);
            }
        }

        throw new UnsupportedOperationException(
                String.format("The following instance of QueryStatement is not supported: %s",
                        queryStatement.getClass().toString())
        );
    }

    public int getStatus() {
        return status;
    }

    public ResultSet getResultSet() {
        return resultSet;
    }

    public String format() {
        if (status == OK_STATUS) {
            switch (formatType) {
                case "jdbc":
                    return outputInJdbcFormat();
                case "table":
                    return outputInTableFormat();
                case "raw":
                    return outputInRawFormat();
                default:
                    throw new UnsupportedOperationException(
                            String.format("The following format is not supported: %s", formatType));
            }
        }

        return error.toString();
    }

    private String outputInJdbcFormat() {
        JSONObject formattedOutput = new JSONObject();

        formattedOutput.put("status", status);
        formattedOutput.put("size", size);
        formattedOutput.put("total", total);

        formattedOutput.put("schema", getSchemaAsJson());
        formattedOutput.put("datarows", getDataRowsAsJson());

        return formattedOutput.toString(2);
    }

    private String outputInRawFormat() {
        Schema schema = resultSet.getSchema();
        DataRows dataRows = resultSet.getDataRows();

        StringBuilder formattedOutput = new StringBuilder();
        for (Row row : dataRows) {
            formattedOutput.append(rawEntry(row, schema)).append("\n");
        }

        return formattedOutput.toString();
    }

    private String outputInTableFormat() {
        return null;
    }

    private String rawEntry(Row row, Schema schema) {
        // TODO String separator is being kept to "|" for the time being as using "\t" will require formatting since
        // TODO tabs are occurring in multiple of 4 (one option is Guava's Strings.padEnd() method)
        return StreamSupport.stream(schema.spliterator(), false)
                .map(column -> row.getDataOrDefault(column.getName(), "NULL").toString())
                .collect(Collectors.joining("|"));
    }

    private JSONArray getSchemaAsJson() {
        Schema schema = resultSet.getSchema();
        JSONArray schemaJson = new JSONArray();

        for (Column column : schema) {
            schemaJson.put(schemaEntry(column.getName(), column.getAlias(), column.getType()));
        }

        return schemaJson;
    }

    private JSONObject schemaEntry(String name, String alias, String type) {
        JSONObject entry = new JSONObject();
        entry.put("name", name);
        if (alias != null) {
            entry.put("alias", alias);
        }
        entry.put("type", type);

        return entry;
    }

    private JSONArray getDataRowsAsJson() {
        Schema schema = resultSet.getSchema();
        DataRows dataRows = resultSet.getDataRows();
        JSONArray dataRowsJson = new JSONArray();

        for (Row row : dataRows) {
            dataRowsJson.put(dataEntry(row, schema));
        }

        return dataRowsJson;
    }

    private JSONArray dataEntry(Row dataRow, Schema schema) {
        JSONArray entry = new JSONArray();
        for (Column column : schema) {
            String columnName = column.getIdentifier();
            entry.put(dataRow.getDataOrDefault(columnName, JSONObject.NULL));
        }
        return entry;
    }
}
