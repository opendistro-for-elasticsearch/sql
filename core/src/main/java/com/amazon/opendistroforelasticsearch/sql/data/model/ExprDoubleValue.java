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

/**
 * Expression Double Value.
 */
public class ExprDoubleValue extends AbstractExprNumberValue {

  public ExprDoubleValue(Number value) {
    super(value);
  }

  @Override
  public Object value() {
    return doubleValue();
  }

  @Override
  public ExprType type() {
    return ExprCoreType.DOUBLE;
  }

  @Override
  public String toString() {
    return doubleValue().toString();
  }

  @Override
  public int compare(ExprValue other) {
    return Double.compare(doubleValue(), other.doubleValue());
  }

  @Override
  public boolean equal(ExprValue other) {
    return doubleValue().equals(other.doubleValue());
  }
}
