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

package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.cassandra;

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.DataFormat;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;

/**
 * Data format for Cassandra database.
 */
public class CassandraDataFormat extends DataFormat {

  /*  Files formatted with following structure:
        Line 0: keyspace name
        Line 1: table name
        Line 2: Comma separated list of types and column names- one or more must contain primary key
        Line 3: Comma separated list of column names without types
        Line 4+: Comma separated list of values for columns

        Data type list available: https://cassandra.apache.org/doc/latest/cql/types.html

        Example:
        Line 0: tpchkeyspace
        Line 1: customer
        Line 2: lastname TEXT PRIMARY KEY, firstname TEXT, myfloat FLOAT, myInt SMALLINT
        Line 3: lastname, firstname, myFloat, myInt
        Line 4+: "lyndon", "bauto", 1.234, 15
     */
  @Getter
  private List<String> files = new LinkedList<>();

  public void addFile(String file) {
    files.add(file);
  }
}
