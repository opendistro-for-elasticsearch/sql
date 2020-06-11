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

import com.amazon.opendistroforelasticsearch.sql.storage.bindingtuple.BindingTuple;

/**
 * The definition of the Expression Value.
 */
public interface ExprValue {
  /**
   * Get the Object value of the Expression Value.
   */
  Object value();

  /**
   * Get the {@link ExprType} of the Expression Value.
   */
  ExprType type();

  /**
   * Compare the Object value of itself to the ExprValue v.
   */
  int compareTo(ExprValue v);

  /**
   * Is null value.
   *
   * @return true: is null value, otherwise false
   */
  default boolean isNull() {
    return false;
  }

  /**
   * Is missing value.
   *
   * @return true: is missing value, otherwise false
   */
  default boolean isMissing() {
    return false;
  }

  /**
   * Get the {@link BindingTuple}.
   */
  default BindingTuple bindingTuples() {
    return BindingTuple.EMPTY;
  }
}
