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

package com.amazon.opendistroforelasticsearch.sql.expression.operator.predicate;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_FALSE;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_MISSING;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_NULL;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_TRUE;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionBuilder;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionResolver;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionSignature;
import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Map;
import lombok.experimental.UtilityClass;

/**
 * The definition of unary predicate function
 * not, Accepts one Boolean value and produces a Boolean.
 */
@UtilityClass
public class UnaryPredicateOperator {
  public static void register(BuiltinFunctionRepository repository) {
    repository.register(not());
  }

  /**
   * The not logic.
   * A       NOT A
   * TRUE    FALSE
   * FALSE   TRUE
   * NULL    NULL
   * MISSING MISSING
   */
  private static Map<ExprValue, ExprValue> notMap =
      new ImmutableMap.Builder<ExprValue, ExprValue>()
          .put(LITERAL_TRUE, LITERAL_FALSE)
          .put(LITERAL_FALSE, LITERAL_TRUE)
          .put(LITERAL_NULL, LITERAL_NULL)
          .put(LITERAL_MISSING, LITERAL_MISSING)
          .build();

  private static FunctionResolver not() {
    FunctionName functionName = BuiltinFunctionName.NOT.getName();
    return FunctionResolver.builder()
        .functionName(functionName)
        .functionBundle(new FunctionSignature(functionName,
            Arrays.asList(ExprType.BOOLEAN)), predicateFunction(functionName,
            notMap, ExprType.BOOLEAN))
        .build();
  }

  private static FunctionBuilder predicateFunction(
      FunctionName functionName,
      Map<ExprValue, ExprValue> map,
      ExprType returnType) {
    return arguments -> new FunctionExpression(functionName, arguments) {
      @Override
      public ExprValue valueOf(Environment<Expression, ExprValue> env) {
        return map.get(arguments.get(0).valueOf(env));
      }

      @Override
      public ExprType type(Environment<Expression, ExprType> env) {
        return returnType;
      }

      @Override
      public String toString() {
        return String.format("%s %s", functionName, arguments.get(0).toString());
      }
    };
  }
}
