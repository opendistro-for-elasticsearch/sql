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

package com.amazon.opendistroforelasticsearch.sql.correctness.runner.resultset;

import com.amazon.opendistroforelasticsearch.sql.legacy.utils.StringUtils;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.json.JSONPropertyName;

/**
 * Query result for equality comparison. Based on different type of query, such as query with/without ORDER BY and
 * query with SELECT columns or just *, order of column and row may matter or not. So the internal data structure of this
 * class is passed in from outside either list or set, hash map or linked hash map etc.
 */
@EqualsAndHashCode(exclude = "databaseName")
@ToString
public class DBResult {

  /**
   * Possible types for floating point number
   */
  private static final Set<String> FLOAT_TYPES = ImmutableSet.of("FLOAT", "DOUBLE", "REAL");

  /**
   * Database name for display
   */
  private final String databaseName;

  /**
   * Column name and types from result set meta data
   */
  @Getter
  private final Collection<Type> schema;

  /**
   * Data rows from result set
   */
  private final Collection<Row> dataRows;

  /**
   * By default treat both columns and data rows in order. This makes sense for typical query
   * with specific column names in SELECT but without ORDER BY.
   */
  public DBResult(String databaseName) {
    this(databaseName, new ArrayList<>(), new HashSet<>());
  }

  public DBResult(String databaseName, Collection<Type> schema, Collection<Row> rows) {
    this.databaseName = databaseName;
    this.schema = schema;
    this.dataRows = rows;
  }

  public int columnSize() {
    return schema.size();
  }

  public void addColumn(String name, String type) {
    type = StringUtils.toUpper(type);

    // Ignore float type by assigning all type names string to it.
    if (FLOAT_TYPES.contains(type)) {
      type = FLOAT_TYPES.toString();
    }
    schema.add(new Type(StringUtils.toUpper(name), type));
  }

  public void addRow(Row row) {
    dataRows.add(row);
  }

  @JSONPropertyName("database")
  public String getDatabaseName() {
    return databaseName;
  }

  /**
   * Flatten for simplifying json generated
   */
  public Collection<Collection<Object>> getDataRows() {
    return dataRows.stream().map(Row::getValues).collect(Collectors.toSet());
  }

  /**
   * Explain the difference between this and other DB result which is helpful for
   * troubleshooting in final test report.
   * @param other   other DB result
   * @return        explain the difference
   */
  public String diff(DBResult other) {
    String result = diffSchema(other);
    if (result.isEmpty()) {
      return diffDataRows(other);
    }
    return result;
  }

  private String diffSchema(DBResult other) {
    if (schema.size() != other.schema.size()) {
      return StringUtils.format(
          "Schema size is different: this=[%d], other=[%d]", schema.size(), other.schema.size());
    }

    Iterator<Type> thisIt = schema.iterator();
    Iterator<Type> otherIt = other.schema.iterator();
    int i = 0;
    while (thisIt.hasNext()) {
      Type thisType = thisIt.next();
      Type otherType = otherIt.next();
      if (!thisType.equals(otherType)) {
        return StringUtils.format(
            "Schema type at [%d] is different: thisType=[%s], otherType=[%s]",
              i, thisType, otherType);
      }
      i++;
    }
    return "";
  }

  private String diffDataRows(DBResult other) {
    List<Row> thisRows = sort(dataRows);
    List<Row> otherRows = sort(other.dataRows);
    int thisSize = thisRows.size();
    int otherSize = otherRows.size();

    if (thisSize != otherSize) {
      return StringUtils.format(
          "Data rows size is different: this=[%d], other=[%d]", thisSize, otherSize);
    }

    for (int i = 0; i < thisSize; i++) {
      Row thisRow = thisRows.get(i);
      Row otherRow = otherRows.get(i);
      if (!thisRow.equals(otherRow)) {
        return StringUtils.format(
            "Data row at [%d] is different: this=[%s], other=[%s]", i, thisRow, otherRow);
      }
    }
    return "";
  }

  private static <T extends Comparable<T>> List<T> sort(Collection<T> collection) {
    ArrayList<T> list = new ArrayList<>(collection);
    Collections.sort(list);
    return list;
  }

}
