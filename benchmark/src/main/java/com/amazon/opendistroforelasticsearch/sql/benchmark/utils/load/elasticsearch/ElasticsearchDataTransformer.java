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
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.DataTransformer;
import com.amazon.opendistroforelasticsearch.sql.benchmark.utils.load.TpchSchema;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.json.simple.JSONObject;

/**
 * Data transformer for Elasticsearch database.
 */
public class ElasticsearchDataTransformer implements DataTransformer {

  private String transformedDataPath;

  /**
   * Data transforming function for Elasticsearch.
   *
   * @param dataPath Directory for data to transform.
   * @return Path to transformed data.
   * @throws Exception Throws and exception if file read fails.
   */
  @Override
  public DataFormat transformData(final String dataPath) throws Exception {
    File path = new File(dataPath);
    if (!path.exists() || !path.isDirectory()) {
      throw new FileNotFoundException("Invalid Directory");
    }

    for (String tableName : TpchSchema.schemaMap.keySet()) {
      File table = new File(dataPath + tableName + ".tbl");
      if (!table.exists() || !table.isFile()) {
        throw new FileNotFoundException(tableName + ".tbl not found");
      }

      FileReader fileReader = new FileReader(table);
      BufferedReader bufferedReader = new BufferedReader(fileReader);

      // Create directory to store transformed json files
      CommandExecution.executeCommand("mkdir " + dataPath + "elasticsearch/");

      // Create new json file for every  table / .tbl file
      transformedDataPath = dataPath + "elasticsearch/";
      BufferedWriter bufferedWriter = new BufferedWriter(
          new FileWriter(transformedDataPath + tableName + "_data.json", true));
      long tableLineIndex = 1;
      try {
        String line;
        while ((line = bufferedReader.readLine()) != null) {
          List<String> argsList = Arrays.asList(line.split("\\|"));

          // Add json object for index
          JSONObject id = new JSONObject();
          id.put("_index", tableName);
          id.put("_id", tableLineIndex++);
          JSONObject index = new JSONObject();
          index.put("index", id);
          String jsonString = index.toJSONString();
          bufferedWriter.write(jsonString);
          bufferedWriter.newLine();

          // Convert one line from .tbl to .json and append to json file.
          JSONObject jsonLine = new JSONObject();
          int tableFieldIndex = 0;
          for (String tableField : TpchSchema.schemaMap.get(tableName).keySet()) {
            jsonLine.put(tableField, argsList.get(tableFieldIndex++));
          }
          bufferedWriter.write(jsonLine.toJSONString());
          bufferedWriter.newLine();
        }
      } finally {
        bufferedWriter.close();
        fileReader.close();
      }
    }

    createTableMappings();
    ElasticsearchDataFormat result = new ElasticsearchDataFormat();
    result.setDataPath(transformedDataPath);
    return result;
  }

  private void createTableMappings() throws IOException {
    for (String tableName : TpchSchema.schemaMap.keySet()) {
      BufferedWriter bufferedWriter = new BufferedWriter(
          new FileWriter(transformedDataPath + tableName + "_mappings.json", true));
      try {
        JSONObject tableFields = new JSONObject();
        for (String tableField : TpchSchema.schemaMap.get(tableName).keySet()) {
          JSONObject tableFieldsType = new JSONObject();
          tableFieldsType.put("type", TpchSchema.schemaMap.get(tableName).get(tableField));
          tableFields.put(tableField, tableFieldsType);
        }
        JSONObject properties = new JSONObject();
        properties.put("properties", tableFields);
        JSONObject mappings = new JSONObject();
        mappings.put("mappings", properties);

        bufferedWriter.write(mappings.toJSONString());
      } finally {
        bufferedWriter.close();
      }
    }
  }
}
