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

package com.amazon.opendistroforelasticsearch.sql.config;

import com.amazon.opendistroforelasticsearch.sql.analysis.symbol.Namespace;
import com.amazon.opendistroforelasticsearch.sql.analysis.symbol.Symbol;
import com.amazon.opendistroforelasticsearch.sql.analysis.symbol.SymbolTable;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import com.amazon.opendistroforelasticsearch.sql.planner.logical.LogicalPlan;
import com.amazon.opendistroforelasticsearch.sql.planner.physical.PhysicalPlan;
import com.amazon.opendistroforelasticsearch.sql.storage.StorageEngine;
import com.amazon.opendistroforelasticsearch.sql.storage.Table;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration will be used for UT.
 */
@Configuration
public class TestConfig {
  public static final String INT_TYPE_NULL_VALUE_FIELD = "int_null_value";
  public static final String INT_TYPE_MISSING_VALUE_FIELD = "int_missing_value";
  public static final String DOUBLE_TYPE_NULL_VALUE_FIELD = "double_null_value";
  public static final String DOUBLE_TYPE_MISSING_VALUE_FIELD = "double_missing_value";
  public static final String BOOL_TYPE_NULL_VALUE_FIELD = "null_value_boolean";
  public static final String BOOL_TYPE_MISSING_VALUE_FIELD = "missing_value_boolean";
  public static final String STRING_TYPE_NULL_VALUE_FILED = "string_null_value";
  public static final String STRING_TYPE_MISSING_VALUE_FILED = "string_missing_value";

  public static Map<String, ExprType> typeMapping = new ImmutableMap.Builder<String, ExprType>()
      .put("integer_value", ExprCoreType.INTEGER)
      .put(INT_TYPE_NULL_VALUE_FIELD, ExprCoreType.INTEGER)
      .put(INT_TYPE_MISSING_VALUE_FIELD, ExprCoreType.INTEGER)
      .put("long_value", ExprCoreType.LONG)
      .put("float_value", ExprCoreType.FLOAT)
      .put("double_value", ExprCoreType.DOUBLE)
      .put(DOUBLE_TYPE_NULL_VALUE_FIELD, ExprCoreType.DOUBLE)
      .put(DOUBLE_TYPE_MISSING_VALUE_FIELD, ExprCoreType.DOUBLE)
      .put("boolean_value", ExprCoreType.BOOLEAN)
      .put(BOOL_TYPE_NULL_VALUE_FIELD, ExprCoreType.BOOLEAN)
      .put(BOOL_TYPE_MISSING_VALUE_FIELD, ExprCoreType.BOOLEAN)
      .put("string_value", ExprCoreType.STRING)
      .put(STRING_TYPE_NULL_VALUE_FILED, ExprCoreType.STRING)
      .put(STRING_TYPE_MISSING_VALUE_FILED, ExprCoreType.STRING)
      .put("struct_value", ExprCoreType.STRUCT)
      .put("array_value", ExprCoreType.ARRAY)
      .build();

  @Bean
  protected StorageEngine storageEngine() {
    return new StorageEngine() {
      @Override
      public Table getTable(String name) {
        return new Table() {
          @Override
          public Map<String, ExprType> getFieldTypes() {
            return typeMapping;
          }

          @Override
          public PhysicalPlan implement(LogicalPlan plan) {
            throw new UnsupportedOperationException();
          }
        };
      }
    };
  }


  @Bean
  protected SymbolTable symbolTable() {
    SymbolTable symbolTable = new SymbolTable();
    typeMapping.entrySet()
        .forEach(
            entry -> symbolTable
                .store(new Symbol(Namespace.FIELD_NAME, entry.getKey()), entry.getValue()));
    return symbolTable;
  }

  @Bean
  protected Environment<Expression, ExprType> typeEnv() {
    return var -> {
      if (var instanceof ReferenceExpression) {
        ReferenceExpression refExpr = (ReferenceExpression) var;
        if (typeMapping.containsKey(refExpr.getAttr())) {
          return typeMapping.get(refExpr.getAttr());
        }
      }
      throw new ExpressionEvaluationException("type resolved failed");
    };
  }
}
