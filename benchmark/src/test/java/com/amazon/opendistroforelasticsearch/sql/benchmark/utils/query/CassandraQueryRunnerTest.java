/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.query;

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.query.cassandra.CassandraQueryRunner;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;


/**
 * Simple class to test that the query runner for Cassandra works.
 * Needs Cassandra instance running on local machine to run the test.
 */
@Disabled
public class CassandraQueryRunnerTest {

  @Test
  void cassandraDataTest() {
    try {
      final String createKeyspace = "CREATE KEYSPACE IF NOT EXISTS cycling "
          + "WITH REPLICATION = "
          + "{ 'class' : 'SimpleStrategy', 'replication_factor' : 1 };";
      final String createTable = "CREATE TABLE IF NOT EXISTS cycling.cyclist_name "
          + "(id UUID PRIMARY KEY, lastname text, firstname text );";
      final String upload = "INSERT INTO cycling.cyclist_name (id, lastname, firstname) "
          + "VALUES (c4b65263-fe58-4846-83e8-f0e1c13d518f, 'RATTO', 'Rissella') "
          + "IF NOT EXISTS";
      final String retrieve = "SELECT * FROM cycling.cyclist_name;";
      final CassandraQueryRunner queryRunner = new CassandraQueryRunner();
      queryRunner.prepareQueryRunner(createKeyspace);
      queryRunner.runQuery();
      queryRunner.checkQueryExecutionStatus("C:\\work");
      queryRunner.prepareQueryRunner(createTable);
      queryRunner.runQuery();
      queryRunner.checkQueryExecutionStatus("C:\\work");
      queryRunner.prepareQueryRunner(upload);
      queryRunner.runQuery();
      queryRunner.checkQueryExecutionStatus("C:\\work");
      queryRunner.prepareQueryRunner(retrieve);
      queryRunner.runQuery();
      queryRunner.checkQueryExecutionStatus("C:\\work");
    } catch (Exception e) {
      System.out.println("Exception: " + e);
      Assertions.fail();
    }
  }
}
