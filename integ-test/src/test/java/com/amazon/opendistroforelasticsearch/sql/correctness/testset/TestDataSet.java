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

package com.amazon.opendistroforelasticsearch.sql.correctness.testset;

import static com.amazon.opendistroforelasticsearch.sql.legacy.utils.StringUtils.unquoteSingleField;
import static java.util.stream.Collectors.joining;

import com.amazon.opendistroforelasticsearch.sql.legacy.utils.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.json.JSONObject;

/**
 * Test data set
 */
public class TestDataSet {

  private final String tableName;
  private final String schema;
  private final List<Object[]> dataRows;

  public TestDataSet(String tableName, String schemaFileContent, String dataFileContent) {
    this.tableName = tableName;
    this.schema = schemaFileContent;
    this.dataRows = convertStringDataToActualType(splitColumns(dataFileContent, ','));
  }

  public String getTableName() {
    return tableName;
  }

  public String getSchema() {
    return schema;
  }

  public List<Object[]> getDataRows() {
    return dataRows;
  }

  /**
   * Split columns in each line by separator and ignore escaped separator(s) in quoted string.
   */
  private List<String[]> splitColumns(String content, char separator) {
    List<String[]> result = new ArrayList<>();
    for (String line : content.split("\\r?\\n")) {

      List<String> columns = new ArrayList<>();
      boolean isQuoted = false;
      int start = 0;
      for (int i = 0; i < line.length(); i++) {

        char c = line.charAt(i);
        if (c == separator) {
          if (isQuoted) { // Ignore comma(s) in quoted string
            continue;
          }

          String column = line.substring(start, i);
          columns.add(unquoteSingleField(column, "\""));
          start = i + 1;

        } else if (c == '\"') {
          isQuoted = !isQuoted;
        }
      }

      columns.add(unquoteSingleField(line.substring(start), "\""));
      result.add(columns.toArray(new String[0]));
    }
    return result;
  }

  /**
   * Convert column string values (read from CSV file) to objects of its real type
   * based on the type information in index mapping file.
   */
  private List<Object[]> convertStringDataToActualType(List<String[]> rows) {
    JSONObject types = new JSONObject(schema);
    String[] columnNames = rows.get(0);

    List<Object[]> result = new ArrayList<>();
    result.add(columnNames);

    rows.stream()
        .skip(1)
        .map(row -> convertStringArrayToObjectArray(types, columnNames, row))
        .forEach(result::add);
    return result;
  }

  private Object[] convertStringArrayToObjectArray(JSONObject types, String[] columnNames, String[] row) {
    Object[] result = new Object[row.length];
    for (int i = 0; i < row.length; i++) {
      String colName = columnNames[i];
      String colTypePath = "/mappings/properties/" + colName;
      String colType = ((JSONObject) types.query(colTypePath)).getString("type");
      result[i] = convertStringToObject(colType, row[i]);
    }
    return result;
  }

  private Object convertStringToObject(String type, String str) {
    switch (type.toLowerCase()) {
      case "text":
      case "keyword":
      case "date":
        return str;
      case "integer":
        return Integer.valueOf(str);
      case "float":
      case "half_float":
        return Float.valueOf(str);
      case "double":
        return Double.valueOf(str);
      case "boolean":
        return Boolean.valueOf(str);
      default:
        throw new IllegalStateException(StringUtils.format(
            "Data type %s is not supported yet for value: %s", type, str));
    }
  }

  @Override
  public String toString() {
    int total = dataRows.size();
    return "Test data set :\n"
        + " Table name: " + tableName + '\n'
        + " Schema: " + schema + '\n'
        + " Data rows (first 5 in " + total + "):"
        + dataRows.stream().
        limit(5).
        map(Arrays::toString).
        collect(joining("\n ", "\n ", "\n"));
  }

}
