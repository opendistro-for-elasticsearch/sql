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

package com.amazon.opendistroforelasticsearch.sql.correctness.runner.connection;

import com.amazon.opendistroforelasticsearch.sql.correctness.runner.resultset.DBResult;
import com.amazon.opendistroforelasticsearch.sql.correctness.runner.resultset.Row;
import com.amazon.opendistroforelasticsearch.sql.utils.StringUtils;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static java.util.stream.Collectors.joining;

/**
 * Database connection by JDBC driver.
 */
public class JDBCConnection implements DBConnection {

    private static final String SINGLE_QUOTE = "'";
    private static final String DOUBLE_QUOTE = "''";

    /** Database name for display */
    private final String databaseName;

    /** Database connection URL */
    private final String connectionUrl;

    /** Current live connection */
    private Connection connection;

    public JDBCConnection(String databaseName, String connectionUrl) {
        this.databaseName = databaseName;
        this.connectionUrl = connectionUrl;
    }

    @Override
    public void connect() {
        try {
            connection = DriverManager.getConnection(connectionUrl);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to open connection", e);
        }
    }

    @Override
    public String getDatabaseName() {
        return databaseName;
    }

    @Override
    public void create(String tableName, String schema) {
        // Parse out type in schema json and convert to field name and type pairs for CREATE TABLE statement.
        JSONObject json = (JSONObject) new JSONObject(schema).query("/mappings/properties");
        String types = json.keySet().stream().
                                     map(colName -> colName + " " + mapToJDBCType(json.getJSONObject(colName).getString("type"))).
                                     collect(joining(","));

        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(StringUtils.format("CREATE TABLE %s(%s)", tableName, types));
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to execute update", e);
        }
    }

    @Override
    public void insert(String tableName, String[] columnNames, List<String[]> batch) {
        try (Statement stmt = connection.createStatement()) {
            String names = String.join(",", columnNames);

            for (String[] fieldValues : batch) {
                String values = Arrays.stream(fieldValues).
                                       map(val -> val.replace(SINGLE_QUOTE, DOUBLE_QUOTE)).
                                       map(val -> SINGLE_QUOTE + val + SINGLE_QUOTE).
                                       collect(joining(","));

                stmt.addBatch(StringUtils.format("INSERT INTO %s(%s) VALUES (%s)", tableName, names, values));
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to execute update", e);
        }
    }

    @Override
    public DBResult select(String query) {
        try (Statement stmt = connection.createStatement()) {
            ResultSet resultSet = stmt.executeQuery(query);
            Map<String, String> nameAndTypes = getColumnNameAndTypes(resultSet);

            DBResult result = new DBResult(databaseName, nameAndTypes, new HashSet<>()); //TODO: List for ORDER BY
            while (resultSet.next()) {
                List<Object> row = new ArrayList<>(); //TODO: set for SELECT *
                for (int i = 1; i <= nameAndTypes.size(); i++) {
                    row.add(roundFloatNum(resultSet.getObject(i)));
                }
                result.addRow(new Row(row));
            }
            return result;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to execute query", e);
        }
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (SQLException e) {
            // Ignore
        }
    }

    private Map<String, String> getColumnNameAndTypes(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        Map<String, String> nameAndTypes = new HashMap<>();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            nameAndTypes.put(metaData.getColumnName(i), metaData.getColumnTypeName(i));
        }
        return nameAndTypes;
    }

    private Object roundFloatNum(Object value) {
        if (value instanceof Float) {
            BigDecimal decimal = BigDecimal.valueOf((Float) value).setScale(2, RoundingMode.CEILING);
            value = decimal.floatValue();
        } else if (value instanceof Double) {
            BigDecimal decimal = BigDecimal.valueOf((Double) value).setScale(2, RoundingMode.CEILING);
            value = decimal.doubleValue();
        }
        return value;
    }

    private String mapToJDBCType(String esType) {
        switch (esType.toUpperCase()) {
            case "KEYWORD":
            case "TEXT":
                return "VARCHAR";
            case "DATE":
                return "TIMESTAMP";
            case "HALF_FLOAT":
                return "FLOAT";
            default:
                return esType;
        }
    }

    /** Setter for unit test mock */
    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
