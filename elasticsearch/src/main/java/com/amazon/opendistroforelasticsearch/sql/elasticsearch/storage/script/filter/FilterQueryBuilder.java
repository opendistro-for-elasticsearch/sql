/*
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.filter;

import static com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.ExpressionScriptEngine.EXPRESSION_LANG_NAME;
import static java.util.Collections.emptyMap;
import static org.elasticsearch.script.Script.DEFAULT_SCRIPT_TYPE;

import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.serialization.ExpressionSerializer;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.ScriptQueryBuilder;
import org.elasticsearch.script.Script;

@RequiredArgsConstructor
public class FilterQueryBuilder extends ExpressionNodeVisitor<QueryBuilder, Object> {

  /**
   * Serializer that serializes expression for build DSL query.
   */
  private final ExpressionSerializer serializer;

  /**
   * Build Elasticsearch filter query from expression.
   * @param expr  expression
   * @return      query
   */
  public QueryBuilder build(Expression expr) {
    try {
      return expr.accept(this, null);
    } catch (IllegalStateException e) {
      //TODO: remove this try-catch once arithmetic and all expressions are serializable
      return null;
    }
  }

  @Override
  public QueryBuilder visitFunction(FunctionExpression node, Object context) {
    switch (node.getFunctionName().getFunctionName()) {
      case "and":
        return buildBoolQuery(node, context, BoolQueryBuilder::must);
      case "or":
        return buildBoolQuery(node, context, BoolQueryBuilder::should);
      case "not":
        return buildBoolQuery(node, context, BoolQueryBuilder::mustNot);
      default:
        return buildScriptQuery(node);
    }
  }

  private BoolQueryBuilder buildBoolQuery(FunctionExpression node,
                                          Object context,
                                          BiFunction<BoolQueryBuilder, QueryBuilder,
                                              QueryBuilder> accumulator) {
    BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
    for (Expression arg : node.getArguments()) {
      accumulator.apply(boolQuery, arg.accept(this, context));
    }
    return boolQuery;
  }

  private ScriptQueryBuilder buildScriptQuery(FunctionExpression node) {
    return new ScriptQueryBuilder(new Script(
        DEFAULT_SCRIPT_TYPE, EXPRESSION_LANG_NAME, serializer.serialize(node), emptyMap()));
  }

}
