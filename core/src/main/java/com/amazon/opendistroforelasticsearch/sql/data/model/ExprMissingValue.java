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

import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import java.util.Objects;

/**
 * Expression Missing Value.
 * Missing value only equal to missing value, and is smaller than any other value.
 */
public class ExprMissingValue extends AbstractExprValue {
  private static final ExprValue instance = new ExprMissingValue();

  private ExprMissingValue() {
  }

  public static ExprValue of() {
    return instance;
  }

  @Override
  public Object value() {
    throw new ExpressionEvaluationException("invalid to call value operation on missing value");
  }

  @Override
  public ExprType type() {
    throw new ExpressionEvaluationException("invalid to call type operation on missing value");
  }

  @Override
  public boolean isMissing() {
    return true;
  }

  /**
   * When MISSING value compare to other expression value.
   * 1) MISSING is equal to MISSING.
   * 2) MISSING is less than all other expression values.
   */
  @Override
  public int compare(ExprValue other) {
    return other.isMissing() ? 0 : -1;
  }

  /**
   * Missing value is equal to Missing value.
   */
  @Override
  public boolean equal(ExprValue other) {
    return other.isMissing();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode("MISSING");
  }
}