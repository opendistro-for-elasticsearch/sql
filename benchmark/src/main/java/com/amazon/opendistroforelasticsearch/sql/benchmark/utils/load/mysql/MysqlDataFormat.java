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

package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.mysql;

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.DataFormat;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Data format for MySQL database.
 */
public class MysqlDataFormat extends DataFormat {

  private Map<String, LinkedList<String>> tableDataFilesList = new LinkedHashMap<>();

  /**
   * Add a file to tableDataFilesList.
   *
   * @param tablename Table name
   * @param filename  File name
   */
  public void addFile(String tablename, String filename) {
    if (!tableDataFilesList.containsKey(tablename)) {
      tableDataFilesList.put(tablename, new LinkedList<>());
    }
    tableDataFilesList.get(tablename).add(filename);
  }

  public Map<String, LinkedList<String>> getTableDataFilesList() {
    return tableDataFilesList;
  }
}
