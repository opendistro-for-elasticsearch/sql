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

import static java.util.stream.Collectors.joining;

import com.amazon.opendistroforelasticsearch.sql.correctness.runner.resultset.DBResult;
import com.amazon.opendistroforelasticsearch.sql.correctness.runner.resultset.Row;
import com.amazon.opendistroforelasticsearch.sql.legacy.utils.StringUtils;
import com.google.common.base.Strings;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.json.JSONObject;

/**
 * Database connection by JDBC driver.
 */
public class JDBCConnection implements DBConnection {

  private static final String SINGLE_QUOTE = "'";
  private static final String DOUBLE_QUOTE = "''";

  /**
   * Database name for display
   */
  private final String databaseName;

  /**
   * Database connection URL
   */
  private final String connectionUrl;

  /**
   * JDBC driver config properties.
   */
  private final Properties properties;

  /**
   * Current live connection
   */
  private Connection connection;

  public JDBCConnection(String databaseName, String connectionUrl) {
    this(databaseName, connectionUrl, new Properties());
  }

  /**
   * Create a JDBC connection with parameters given (but not connect to database at the moment).
   * @param databaseName    database name
   * @param connectionUrl   connection URL
   * @param properties      config properties
   */
  public JDBCConnection(String databaseName, String connectionUrl, Properties properties) {
    this.databaseName = databaseName;
    this.connectionUrl = connectionUrl;
    this.properties = properties;
  }

  @Override
  public void connect() {
    try {
      connection = DriverManager.getConnection(connectionUrl, properties);
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
    try (Statement stmt = connection.createStatement()) {
      String types = parseColumnNameAndTypesInSchemaJson(schema);
      stmt.executeUpdate(StringUtils.format("CREATE TABLE %s(%s)", tableName, types));
    } catch (SQLException e) {
      throw new IllegalStateException("Failed to create table [" + tableName + "]", e);
    }
  }

  @Override
  public void drop(String tableName) {
    try (Statement stmt = connection.createStatement()) {
      stmt.executeUpdate("DROP TABLE " + tableName);
    } catch (SQLException e) {
      throw new IllegalStateException("Failed to drop table [" + tableName + "]", e);
    }
  }

  @Override
  public void insert(String tableName, String[] columnNames, List<Object[]> batch) {
    try (Statement stmt = connection.createStatement()) {
      String names = String.join(",", columnNames);
      for (Object[] fieldValues : batch) {
        stmt.addBatch(StringUtils.format(
            "INSERT INTO %s(%s) VALUES (%s)", tableName, names, getValueList(fieldValues)));
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
      DBResult result = isOrderByQuery(query)
          ? DBResult.resultInOrder(databaseName) : DBResult.result(databaseName);
      populateMetaData(resultSet, result);
      populateData(resultSet, result);
      return result;
    } catch (SQLException e) {
      throw new IllegalStateException("Failed to execute query [" + query + "]", e);
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

  /**
   * Parse out type in schema json and convert to field name and type pairs for CREATE TABLE statement.
   */
  private String parseColumnNameAndTypesInSchemaJson(String schema) {
    JSONObject json = (JSONObject) new JSONObject(schema).query("/mappings/properties");
    return json.keySet().stream().
        map(colName -> colName + " " + mapToJDBCType(json.getJSONObject(colName).getString("type")))
        .collect(joining(","));
  }

  private String getValueList(Object[] fieldValues) {
    return Arrays.stream(fieldValues).
        map(String::valueOf).
        map(val -> val.replace(SINGLE_QUOTE, DOUBLE_QUOTE)).
        map(val -> SINGLE_QUOTE + val + SINGLE_QUOTE).
        collect(joining(","));
  }

  private void populateMetaData(ResultSet resultSet, DBResult result) throws SQLException {
    ResultSetMetaData metaData = resultSet.getMetaData();
    for (int i = 1; i <= metaData.getColumnCount(); i++) {

      // Use label name (alias) if present.
      String colName = metaData.getColumnLabel(i);
      if (Strings.isNullOrEmpty(colName)) {
        colName = metaData.getColumnName(i);
      }
      result.addColumn(colName, metaData.getColumnTypeName(i));
    }
  }

  private void populateData(ResultSet resultSet, DBResult result) throws SQLException {
    while (resultSet.next()) {
      Row row = new Row();
      for (int i = 1; i <= result.columnSize(); i++) {
        row.add(resultSet.getObject(i));
      }
      result.addRow(row);
    }
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

  private boolean isOrderByQuery(String query) {
    return query.trim().toUpperCase().contains("ORDER BY");
  }

  /**
   * Setter for unit test mock
   */
  public void setConnection(Connection connection) {
    this.connection = connection;
  }
}
