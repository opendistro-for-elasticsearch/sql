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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.aggregation.dsl;

import static com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.ExpressionScriptEngine.EXPRESSION_LANG_NAME;
import static java.util.Collections.emptyMap;
import static org.elasticsearch.script.Script.DEFAULT_SCRIPT_TYPE;

import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.ScriptUtils;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.serialization.ExpressionSerializer;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.script.Script;

/**
 * Abstract Aggregation Builder.
 *
 * @param <T> type of the actual AggregationBuilder to be built.
 */
@RequiredArgsConstructor
public class AggregationBuilderHelper<T> {

  private final ExpressionSerializer serializer;

  /**
   * Build AggregationBuilder from Expression.
   *
   * @param expression Expression
   * @return AggregationBuilder
   */
  public T build(Expression expression, Function<String, T> fieldBuilder,
                 Function<Script, T> scriptBuilder) {
    if (expression instanceof ReferenceExpression) {
      String fieldName = ((ReferenceExpression) expression).getAttr();
      return fieldBuilder.apply(ScriptUtils.convertTextToKeyword(fieldName, expression.type()));
    } else if (expression instanceof FunctionExpression) {
      return scriptBuilder.apply(new Script(
          DEFAULT_SCRIPT_TYPE, EXPRESSION_LANG_NAME, serializer.serialize(expression),
          emptyMap()));
    } else {
      throw new IllegalStateException(String.format("metric aggregation doesn't support "
          + "expression %s", expression));
    }
  }
}
