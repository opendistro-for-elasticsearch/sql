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

package com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.elasticsearch;

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.CommandExecution;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.DataFormat;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.DataLoader;
import java.util.LinkedList;
import java.util.Map;

/**
 * Data loader for Elasticsearch database.
 */
public class ElasticsearchDataLoader implements DataLoader {

  /**
   * Load function for Elasticsearch databases.
   *
   * @param data Data to load in ElasticsearchDataFormat.
   * @throws Exception Throws an Exception if data does not match expected type.
   */
  @Override
  public void loadData(final DataFormat data) throws Exception {
    if (!(data instanceof ElasticsearchDataFormat)) {
      throw new IllegalArgumentException("Wrong data format for Elasticsearch.");
    }
    String commands = "cd " + ((ElasticsearchDataFormat) data).getDataPath();

    // Add table mappings
    for (String tableName : ElasticsearchTpchSchema.schemaMap.keySet()) {
      commands += " && curl -H 'Content-Type: application/x-ndjson' -XPUT 'https://localhost:9200/"
          + tableName + "?pretty' -u admin:admin --insecure --data-binary @" + tableName
          + "_mappings.json";
    }
    CommandExecution.executeCommand(commands);

    // Add table data
    Map<String, LinkedList<String>> tableDataFilesList = ((ElasticsearchDataFormat) data)
        .getTableDataFilesList();
    for (String tableName : tableDataFilesList.keySet()) {
      for (String fileName : tableDataFilesList.get(tableName)) {

        String dataCommand = "cd " + ((ElasticsearchDataFormat) data).getDataPath()
            + " && curl -H 'Content-Type: application/x-ndjson' -XPOST 'https://localhost:9200/"
            + tableName + "/_bulk?pretty' -u admin:admin --insecure --data-binary @" + fileName
            + " >> " + fileName.replace(".json", "") + "_upload.log";
        CommandExecution.executeCommand(dataCommand);
      }
    }
  }
}
