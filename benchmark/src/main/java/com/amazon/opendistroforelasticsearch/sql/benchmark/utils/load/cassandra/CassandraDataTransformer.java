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

import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.CommandExecution;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.DataFormat;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.DataTransformer;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Data transformer for Cassandra database.
 */
public class CassandraDataTransformer implements DataTransformer {

  private String transformedDataPath;

  /**
   * Data transforming function for Cassandra.
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

    CassandraDataFormat result = new CassandraDataFormat();

    // Create directory to store transformed csv files
    CommandExecution.executeCommand("mkdir " + dataPath + "cassandra/");
    transformedDataPath = dataPath + "cassandra/";

    for (String tablename : CassandraTpchSchema.schemaMap.keySet()) {
      File table = new File(dataPath + tablename + ".tbl");
      if (!table.exists() || !table.isFile()) {
        throw new FileNotFoundException(tablename + ".tbl not found");
      }

      FileReader fileReader = new FileReader(table);
      BufferedReader reader = new BufferedReader(fileReader);
      int csvFilesNumber = 1;
      String filename = tablename + "_data_" + csvFilesNumber++ + ".csv";
      BufferedWriter writer = new BufferedWriter(
          new FileWriter(transformedDataPath + filename, true));
      int dataLineCounter = 0;
      try {
        result.addFile(transformedDataPath + filename);
        addSchemaToCSV(writer, tablename);
        String line;

        while ((line = reader.readLine()) != null) {
          List<String> argsList = Arrays.asList(line.split("\\|"));
          int fieldIndex = 0;
          for (String field : CassandraTpchSchema.schemaMap.get(tablename).keySet()) {
            String type = CassandraTpchSchema.schemaMap.get(tablename).get(field);

            if (type == "text" || type == "date") {
              writer.write("\'" + argsList.get(fieldIndex++) + "'");
            } else {
              writer.write(argsList.get(fieldIndex++));
            }

            if (fieldIndex < CassandraTpchSchema.schemaMap.get(tablename).size()) {
              writer.write(", ");
            }
          }

          writer.newLine();
          dataLineCounter++;

          if (dataLineCounter == 1000 * (csvFilesNumber - 1)) {
            writer.close();
            filename = tablename + "_data_" + csvFilesNumber++ + ".csv";
            writer = new BufferedWriter(new FileWriter(transformedDataPath + filename, true));
            result.addFile(transformedDataPath + filename);
            addSchemaToCSV(writer, tablename);
          }
        }
      } finally {
        fileReader.close();
        writer.close();
      }
    }
    return result;
  }

  private void addSchemaToCSV(BufferedWriter writer, String table) throws IOException {

    writer.write(CassandraTpchSchema.keyspaceName);
    writer.newLine();
    writer.write(table);
    writer.newLine();

    int index = 1;
    for (String field : CassandraTpchSchema.schemaMap.get(table).keySet()) {
      writer.write(field + " " + CassandraTpchSchema.schemaMap.get(table).get(field));
      if (CassandraTpchSchema.primaryKeyMap.get(table).contains(field)) {
        writer.write(" PRIMARY KEY");
      }
      if (index < CassandraTpchSchema.schemaMap.get(table).size()) {
        writer.write(", ");
        index++;
      }
    }
    writer.newLine();
    index = 1;
    for (String field : CassandraTpchSchema.schemaMap.get(table).keySet()) {
      writer.write(field);
      if (index < CassandraTpchSchema.schemaMap.get(table).size()) {
        writer.write(", ");
        index++;
      }
    }
    writer.newLine();
  }
}
