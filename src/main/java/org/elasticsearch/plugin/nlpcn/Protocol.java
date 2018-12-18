package org.elasticsearch.plugin.nlpcn;

import org.elasticsearch.client.Client;
import org.elasticsearch.plugin.nlpcn.DataRows.Row;
import org.elasticsearch.plugin.nlpcn.Schema.Column;
import org.json.JSONArray;
import org.json.JSONObject;
import org.nlpcn.es4sql.domain.Query;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class Protocol {

    static final int OK_STATUS = 200;
    static final int ERROR_STATUS = 500;

    private final String formatType;
    private int status;
    private long size;
    private long total;
    private ResultSet resultSet;
    private ErrorMessage error;

    public Protocol(Client client, Query query, Object queryResult, String formatType) {
        this.formatType = formatType;

        this.status = OK_STATUS;
        this.resultSet = new ResultSet(client, query, queryResult);
        this.size = resultSet.getDataRows().getSize();
        this.total = resultSet.getDataRows().getTotalHits();
    }

    public Protocol(Exception e) {
        this.formatType = null;
        this.status = ERROR_STATUS;
        this.error = new ErrorMessage(e, ERROR_STATUS);
    }

    public int getStatus() { return status; }

    public ResultSet getResultSet() { return resultSet; }

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
        if (alias != null) { entry.put("alias", alias); }
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
            String columnName = column.getName();
            entry.put(dataRow.getDataOrDefault(columnName, JSONObject.NULL));
        }
        return entry;
    }
}
