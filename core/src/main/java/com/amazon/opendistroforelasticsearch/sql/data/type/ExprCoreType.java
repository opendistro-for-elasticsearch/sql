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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Expression Type.
 */
public enum ExprCoreType implements ExprType {
  /**
   * Unknown due to unsupported data type.
   */
  UNKNOWN,

  /**
   * Undefined type for special literal such as NULL.
   * As the root of data type tree, this is compatible with any other type.
   */
  UNDEFINED {
    @Override
    public boolean isCompatible(ExprType other) {
      return true;
    }
  },

  /**
   * Numbers.
   */
  BYTE,
  SHORT(BYTE),
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
  DATETIME,
  INTERVAL,

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

  /**
   * The mapping between Type and legacy JDBC type name.
   */
  private static final Map<ExprCoreType, String> LEGACY_TYPE_NAME_MAPPING =
      new ImmutableMap.Builder<ExprCoreType, String>()
          .put(STRUCT, "object")
          .put(ARRAY, "nested")
          .put(STRING, "keyword")
          .build();

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

  @Override
  public String legacyTypeName() {
    return LEGACY_TYPE_NAME_MAPPING.getOrDefault(this, this.name());
  }

  /**
   * Return all the valid ExprCoreType.
   */
  public static List<ExprCoreType> coreTypes() {
    return Arrays.stream(ExprCoreType.values())
                 .filter(type -> type != UNKNOWN)
                 .filter(type -> type != UNDEFINED)
                 .collect(Collectors.toList());
  }

  public static List<ExprType> numberTypes() {
    return ImmutableList.of(INTEGER, LONG, FLOAT, DOUBLE);
  }
}
