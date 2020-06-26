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

package com.amazon.opendistroforelasticsearch.sql.data.model;

import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.amazon.opendistroforelasticsearch.sql.storage.bindingtuple.BindingTuple;
import com.amazon.opendistroforelasticsearch.sql.storage.bindingtuple.LazyBindingTuple;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ExprTupleValue implements ExprValue {

  private final LinkedHashMap<String, ExprValue> valueMap;

  public static ExprTupleValue fromExprValueMap(Map<String, ExprValue> map) {
    LinkedHashMap<String, ExprValue> linkedHashMap = new LinkedHashMap<>(map);
    return new ExprTupleValue(linkedHashMap);
  }

  @Override
  public Object value() {
    return valueMap;
  }

  @Override
  public ExprType type() {
    return ExprType.STRUCT;
  }

  @Override
  public String toString() {
    return valueMap.entrySet()
        .stream()
        .map(entry -> String.format("%s:%s", entry.getKey(), entry.getValue()))
        .collect(Collectors.joining(",", "{", "}"));
  }

  @Override
  public BindingTuple bindingTuples() {
    return new LazyBindingTuple(
        bindingName -> valueMap.getOrDefault(bindingName, ExprMissingValue.of()));
  }

  /**
   * Override the equals method.
   * @return true for equal, otherwise false.
   */
  public boolean equals(Object o) {
    if (o == this) {
      return true;
    } else if (!(o instanceof ExprTupleValue)) {
      return false;
    } else {
      ExprTupleValue other = (ExprTupleValue) o;
      Iterator<Entry<String, ExprValue>> thisIterator = this.valueMap.entrySet().iterator();
      Iterator<Entry<String, ExprValue>> otherIterator = other.valueMap.entrySet().iterator();
      while (thisIterator.hasNext() && otherIterator.hasNext()) {
        if (!thisIterator.next().equals(otherIterator.next())) {
          return false;
        }
      }
      return !(thisIterator.hasNext() || otherIterator.hasNext());
    }
  }

}
