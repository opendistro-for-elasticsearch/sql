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
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;

/**
 * Query response that encapsulates query results and isolate {@link ExprValue}
 * related from formatter implementation.
 */
@RequiredArgsConstructor
public class QueryResult implements Iterable<Object[]> {

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
   * @return mapping from column names to its expression type
   */
  public Map<String, String> columnNameTypes() {
    if (exprValues.isEmpty()) {
      return Collections.emptyMap();
    }

    // TODO: Need other way to extract header than inferring from data implicitly
    Map<String, ExprValue> tupleValue = getFirstTupleValue();
    return populateColumnNameAndTypes(tupleValue);
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

  private Map<String, ExprValue> getFirstTupleValue() {
    // Assume expression is always tuple on first level
    //  and columns (keys) of all tuple values are exactly same
    ExprValue firstValue = exprValues.iterator().next();
    return ExprValueUtils.getTupleValue(firstValue);
  }

  private Map<String, String> populateColumnNameAndTypes(Map<String, ExprValue> tupleValue) {
    // Use linked hashmap to maintain original order in tuple expression
    Map<String, String> colNameTypes = new LinkedHashMap<>();
    tupleValue.forEach((name, expr) -> colNameTypes.put(name, getTypeString(expr)));
    return colNameTypes;
  }

  private Object[] convertExprValuesToValues(Collection<ExprValue> exprValues) {
    return exprValues
        .stream()
        .map(ExprValue::value)
        .toArray(Object[]::new);
  }

  private String getTypeString(ExprValue exprValue) {
    return exprValue.type().typeName().toLowerCase();
  }

}
