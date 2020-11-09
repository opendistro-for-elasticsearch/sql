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

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.CommandExecution;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.DataFormat;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.DataTransformer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;

/**
 * Data transformer for MySQL database.
 */
public class MysqlDataTransformer implements DataTransformer {

  private String transformedDataPath;

  /**
   * Data transforming function for MySQL.
   *
   * @param dataPath Directory for data to transform.
   * @return Path to transformed data.
   * @throws Exception Throws and exception if file read fails.
   */
  @Override
  public DataFormat transformData(String dataPath) throws Exception {
    File path = new File(dataPath);
    if (!path.exists() || !path.isDirectory()) {
      throw new FileNotFoundException("Invalid Directory");
    }

    MysqlDataFormat result = new MysqlDataFormat();

    // Create directory to store transformed json files
    CommandExecution.executeCommand("mkdir " + dataPath + "mysql/");

    // Create new json file for every  table / .tbl file
    transformedDataPath = dataPath + "mysql/";

    for (String tableName : MysqlTpchSchema.schemaMap.keySet()) {
      File table = new File(dataPath + tableName + ".tbl");
      if (!table.exists() || !table.isFile()) {
        throw new FileNotFoundException(tableName + ".tbl not found");
      }

      FileReader fileReader = new FileReader(table);
      BufferedReader bufferedReader = new BufferedReader(fileReader);

      int tableDataFilesIndex = 1;
      String filename = tableName + "_data_" + tableDataFilesIndex++ + ".csv";

      BufferedWriter writer = new BufferedWriter(
          new FileWriter(transformedDataPath + filename, true));

      long tableLineIndex = 1;
      try {
        result.addFile(tableName, transformedDataPath + filename);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
          List<String> argsList = Arrays.asList(line.split("\\|"));
          for (String field : argsList) {
            writer.write("\"" + field + "\"");
            if (argsList.indexOf(field) < argsList.size() - 1) {
              writer.write(",");
            }
          }
          tableLineIndex++;
          writer.newLine();

          if (tableLineIndex == 1000 * (tableDataFilesIndex - 1)) {
            writer.close();
            filename = tableName + "_data_" + tableDataFilesIndex++ + ".csv";
            writer = new BufferedWriter(
                new FileWriter(transformedDataPath + filename, true));
            result.addFile(tableName, transformedDataPath + filename);
          }
        }
      } finally {
        writer.close();
        fileReader.close();
      }
    }

    return result;
  }
}
