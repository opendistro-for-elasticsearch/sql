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

package com.amazon.opendistroforelasticsearch.sql.ast.expression;

import static com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName.CAST_TO_BOOLEAN;
import static com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName.CAST_TO_DATE;
import static com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName.CAST_TO_DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName.CAST_TO_FLOAT;
import static com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName.CAST_TO_INT;
import static com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName.CAST_TO_LONG;
import static com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName.CAST_TO_STRING;
import static com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName.CAST_TO_TIME;
import static com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName.CAST_TO_TIMESTAMP;

import com.amazon.opendistroforelasticsearch.sql.ast.AbstractNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.ast.Node;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionName;
import com.google.common.collect.ImmutableMap;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

/**
 * AST node that represents Cast clause.
 */
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@Getter
@ToString
public class Cast extends UnresolvedExpression {

  private static Map<String, FunctionName> CONVERTED_TYPE_FUNCTION_NAME_MAP =
      new ImmutableMap.Builder<String, FunctionName>()
          .put("string", CAST_TO_STRING.getName())
          .put("int", CAST_TO_INT.getName())
          .put("long", CAST_TO_LONG.getName())
          .put("float", CAST_TO_FLOAT.getName())
          .put("double", CAST_TO_DOUBLE.getName())
          .put("boolean", CAST_TO_BOOLEAN.getName())
          .put("date", CAST_TO_DATE.getName())
          .put("time", CAST_TO_TIME.getName())
          .put("timestamp", CAST_TO_TIMESTAMP.getName())
          .build();

  /**
   * The source expression cast from.
   */
  private final UnresolvedExpression expression;

  /**
   * Expression that represents ELSE statement result.
   */
  private final UnresolvedExpression convertedType;

  /**
   * Get the converted type.
   *
   * @return converted type
   */
  public FunctionName convertFunctionName() {
    String type = convertedType.toString().toLowerCase(Locale.ROOT);
    if (CONVERTED_TYPE_FUNCTION_NAME_MAP.containsKey(type)) {
      return CONVERTED_TYPE_FUNCTION_NAME_MAP.get(type);
    } else {
      throw new IllegalStateException("unsupported cast type: " + type);
    }
  }

  @Override
  public List<? extends Node> getChild() {
    return Collections.singletonList(expression);
  }

  @Override
  public <T, C> T accept(AbstractNodeVisitor<T, C> nodeVisitor, C context) {
    return nodeVisitor.visitCast(this, context);
  }
}
