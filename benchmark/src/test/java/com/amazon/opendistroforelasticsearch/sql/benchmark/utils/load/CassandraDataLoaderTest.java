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

package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load;

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.cassandra.CassandraDataFormat;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.cassandra.CassandraDataLoader;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.query.cassandra.CassandraQueryRunner;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;



/**
 * Simple class to test that the query runner for Cassandra works.
 * Needs Cassandra instance running on local machine to run the test.
 */
@Disabled
public class CassandraDataLoaderTest {

  @Test
  void cassandraLoaderTest() {
    try {
      final CassandraDataLoader dataLoader = new CassandraDataLoader();
      final List<String> files = new ArrayList<>();
      files.add("casstest.txt");
      final DataFormat dataFormat = new CassandraDataFormat(files);
      dataLoader.loadData(dataFormat);
      CassandraQueryRunner cassandraQueryRunner = new CassandraQueryRunner();
      cassandraQueryRunner.prepareQueryRunner("SELECT * FROM tcphkeyspace.customer");
      cassandraQueryRunner.runQuery();
      cassandraQueryRunner.checkQueryExecutionStatus("C:\\work");
    } catch (Exception e) {
      System.out.println("Exception: " + e);
      Assertions.fail();
    }
  }
}
