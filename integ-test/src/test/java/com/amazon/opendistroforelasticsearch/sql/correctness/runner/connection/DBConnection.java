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
import java.util.List;

/**
 * Abstraction for different databases.
 */
public interface DBConnection {

  /**
   * @return database name
   */
  String getDatabaseName();

  /**
   * Connect to database by opening a connection.
   */
  void connect();

  /**
   * Create table with the schema.
   *
   * @param tableName table name
   * @param schema    schema json in ES mapping format
   */
  void create(String tableName, String schema);

  /**
   * Insert batch of data to database.
   *
   * @param tableName   table name
   * @param columnNames column names
   * @param batch       batch of rows
   */
  void insert(String tableName, String[] columnNames, List<Object[]> batch);

  /**
   * Fetch data from database.
   *
   * @param query SQL query
   * @return result set
   */
  DBResult select(String query);

  /**
   * Drop table.
   *
   * @param tableName table name
   */
  void drop(String tableName);

  /**
   * Close the database connection.
   */
  void close();

}
