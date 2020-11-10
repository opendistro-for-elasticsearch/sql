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
import java.util.stream.Collectors;

/**
 * Data transformer for MySQL database.
 */
public class MysqlDataTransformer implements DataTransformer {

  /**
   * Data transforming function for MySQL.
   *
   * @param dataPath Directory for data to transform.
   * @return Path to transformed data.
   * @throws Exception Throws and exception if file read fails.
   */
  @Override
  public DataFormat transformData(String dataPath) throws Exception {
    final File path = new File(dataPath);
    if (!path.exists() || !path.isDirectory()) {
      throw new FileNotFoundException("Invalid Directory");
    }

    MysqlDataFormat result = new MysqlDataFormat();

    // Create directory to store transformed csv files.
    CommandExecution.executeCommand("mkdir " + dataPath + "mysql/");
    final String transformedDataPath = dataPath + "mysql/";

    for (String tableName : MysqlTpchSchema.schemaMap.keySet()) {
      final File table = new File(dataPath + tableName + ".tbl");
      if (!table.exists() || !table.isFile()) {
        throw new FileNotFoundException(tableName + ".tbl not found");
      }

      final FileReader fileReader = new FileReader(table);
      final BufferedReader bufferedReader = new BufferedReader(fileReader);

      int tableDataFilesIndex = 1;
      String filename = tableName + "_data_" + tableDataFilesIndex++ + ".csv";

      BufferedWriter writer = new BufferedWriter(
          new FileWriter(transformedDataPath + filename, true));

      long tableLineIndex = 1;

      // Create list of csv files for all tables. Each file contains 1000 data rows.
      try {
        result.addFile(tableName, transformedDataPath + filename);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
          List<String> argsList = Arrays.asList(line.split("\\|"));
          String row = argsList.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(","));
          writer.write(row);
          tableLineIndex++;
          writer.newLine();

          // Close file when 1000 data rows are added and create new file to store next set of rows.
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
