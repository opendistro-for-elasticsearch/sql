/*
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.protocol.response;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine;
import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine.Schema.Column;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Query response that encapsulates query results and isolate {@link ExprValue}
 * related from formatter implementation.
 */
@RequiredArgsConstructor
public class QueryResult implements Iterable<Object[]> {

  @Getter
  private final ExecutionEngine.Schema schema;

  /**
   * Results which are collection of expression.
   */
  private final Collection<ExprValue> exprValues;


  /**
   * size of results.
   * @return size of results
   */
  public int size() {
    return exprValues.size();
  }

  /**
   * Parse column name from results.
   *
   * @return mapping from column names to its expression type.
   *        note that column name could be original name or its alias if any.
   */
  public Map<String, String> columnNameTypes() {
    Map<String, String> colNameTypes = new LinkedHashMap<>();
    schema.getColumns().forEach(column -> colNameTypes.put(
        getColumnName(column),
        column.getExprType().typeName().toLowerCase()));
    return colNameTypes;
  }

  @Override
  public Iterator<Object[]> iterator() {
    // Any chance to avoid copy for json response generation?
    return exprValues.stream()
        .map(ExprValueUtils::getTupleValue)
        .map(Map::values)
        .map(this::convertExprValuesToValues)
        .iterator();
  }

  private String getColumnName(Column column) {
    return (column.getAlias() != null) ? column.getAlias() : column.getName();
  }

  private Object[] convertExprValuesToValues(Collection<ExprValue> exprValues) {
    return exprValues
        .stream()
        .map(ExprValue::value)
        .toArray(Object[]::new);
  }
}
