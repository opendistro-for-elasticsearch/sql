/*
 *
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.data.type;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Expression Type.
 */
public enum ExprCoreType implements ExprType {
  /**
   * UNKNOWN.
   */
  UNKNOWN,

  /**
   * Numbers.
   */
  SHORT,
  INTEGER(SHORT),
  LONG(INTEGER),
  FLOAT(LONG),
  DOUBLE(FLOAT),

  /**
   * Boolean.
   */
  BOOLEAN,

  /**
   * String.
   */
  STRING,


  /**
   * Date.
   * Todo. compatible relationship.
   */
  TIMESTAMP,
  DATE,
  TIME,

  /**
   * Struct.
   */
  STRUCT,

  /**
   * Array.
   */
  ARRAY;

  /**
   * Parent of current base type.
   */
  private ExprCoreType parent;

  ExprCoreType(ExprCoreType... compatibleTypes) {
    for (ExprCoreType subType : compatibleTypes) {
      subType.parent = this;
    }
  }

  @Override
  public List<ExprType> getParent() {
    return Arrays.asList(parent == null ? UNKNOWN : parent);
  }

  @Override
  public String typeName() {
    return this.name();
  }

  /**
   * Retrun all the valid ExprCoreType.
   */
  public static List<ExprType> coreTypes() {
    return Arrays.stream(ExprCoreType.values()).filter(type -> type != UNKNOWN)
        .collect(Collectors.toList());
  }
}
