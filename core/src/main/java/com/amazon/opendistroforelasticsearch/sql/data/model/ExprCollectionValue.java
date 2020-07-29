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

import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.google.common.base.Objects;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

/**
 * Expression Collection Value.
 */
@RequiredArgsConstructor
public class ExprCollectionValue extends AbstractExprValue {
  private final List<ExprValue> valueList;

  @Override
  public Object value() {
    return valueList;
  }

  @Override
  public ExprType type() {
    return ExprCoreType.ARRAY;
  }

  @Override
  public List<ExprValue> collectionValue() {
    return valueList;
  }

  @Override
  public String toString() {
    return valueList.stream()
        .map(Object::toString)
        .collect(Collectors.joining(",", "[", "]"));
  }

  @Override
  public boolean equal(ExprValue o) {
    if (!(o instanceof ExprCollectionValue)) {
      return false;
    } else {
      ExprCollectionValue other = (ExprCollectionValue) o;
      Iterator<ExprValue> thisIterator = this.valueList.iterator();
      Iterator<ExprValue> otherIterator = other.valueList.iterator();

      while (thisIterator.hasNext() && otherIterator.hasNext()) {
        ExprValue thisEntry = thisIterator.next();
        ExprValue otherEntry = otherIterator.next();
        if (!thisEntry.equals(otherEntry)) {
          return false;
        }
      }
      return !(thisIterator.hasNext() || otherIterator.hasNext());
    }
  }

  /**
   * Only compare the size of the list.
   */
  @Override
  public int compare(ExprValue other) {
    return Integer.compare(valueList.size(), other.collectionValue().size());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(valueList);
  }
}
