/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
 * Expression Float Value.
 */
public class ExprFloatValue extends AbstractExprNumberValue {

  public ExprFloatValue(Number value) {
    super(value);
  }

  @Override
  public Object value() {
    return floatValue();
  }

  @Override
  public ExprType type() {
    return ExprCoreType.FLOAT;
  }

  @Override
  public String toString() {
    return floatValue().toString();
  }

  @Override
  public int compare(ExprValue other) {
    return Float.compare(floatValue(), other.floatValue());
  }

  @Override
  public boolean equal(ExprValue other) {
    return floatValue().equals(other.floatValue());
  }
}
