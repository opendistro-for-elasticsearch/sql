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

package com.amazon.opendistroforelasticsearch.sql.ast.expression;

import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * The DataType defintion in AST.
 * Question, could we use {@link ExprCoreType} directly in AST?
 */
@RequiredArgsConstructor
public enum DataType {
  TYPE_ERROR(ExprCoreType.UNKNOWN),
  NULL(ExprCoreType.UNKNOWN),

  INTEGER(ExprCoreType.INTEGER),
  LONG(ExprCoreType.LONG),
  DOUBLE(ExprCoreType.DOUBLE),
  STRING(ExprCoreType.STRING),
  BOOLEAN(ExprCoreType.BOOLEAN),

  DATE(ExprCoreType.DATE),
  TIME(ExprCoreType.TIME),
  TIMESTAMP(ExprCoreType.TIMESTAMP),
  INTERVAL(ExprCoreType.INTERVAL);

  @Getter
  private final ExprCoreType coreType;
}
