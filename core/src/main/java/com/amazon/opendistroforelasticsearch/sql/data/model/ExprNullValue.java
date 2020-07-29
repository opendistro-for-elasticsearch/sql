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

import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import java.util.Objects;

/**
 * Expression Null Value.
 * Null value
 *  <li>equal to null value.
 *  <li>large than missing value.
 *  <li>less than any other value.
 */
public class ExprNullValue extends AbstractExprValue {
  private static final ExprValue instance = new ExprNullValue();

  private ExprNullValue() {
  }

  @Override
  public int hashCode() {
    return Objects.hashCode("NULL");
  }

  public static ExprValue of() {
    return instance;
  }

  @Override
  public Object value() {
    return null;
  }

  @Override
  public ExprType type() {
    throw new ExpressionEvaluationException("invalid to call type operation on null value");
  }

  @Override
  public boolean isNull() {
    return true;
  }

  /**
   * When NULL value compare to other expression value.
   * 1) NULL is equal to NULL.
   * 2) NULL is large than MISSING.
   * 3) NULL is less than all other expression values.
   */
  @Override
  public int compare(ExprValue other) {
    return other.isNull() ? 0 : other.isMissing() ? 1 : -1;
  }

  /**
   * NULL value is equal to NULL value.
   */
  @Override
  public boolean equal(ExprValue other) {
    return other.isNull();
  }

}
