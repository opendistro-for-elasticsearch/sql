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

import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.filter.lucene.LuceneQuery;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.filter.lucene.RangeQuery;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.filter.lucene.RangeQuery.Comparison;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.filter.lucene.TermQuery;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.filter.lucene.WildcardQuery;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.serialization.ExpressionSerializer;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionNodeVisitor;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionName;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
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
   * Mapping from function name to lucene query builder.
   */
  private final Map<FunctionName, LuceneQuery> luceneQueries =
      ImmutableMap.<FunctionName, LuceneQuery>builder()
          .put(BuiltinFunctionName.EQUAL.getName(), new TermQuery())
          .put(BuiltinFunctionName.LESS.getName(), new RangeQuery(Comparison.LT))
          .put(BuiltinFunctionName.GREATER.getName(), new RangeQuery(Comparison.GT))
          .put(BuiltinFunctionName.LTE.getName(), new RangeQuery(Comparison.LTE))
          .put(BuiltinFunctionName.GTE.getName(), new RangeQuery(Comparison.GTE))
          .put(BuiltinFunctionName.LIKE.getName(), new WildcardQuery())
          .build();

  /**
   * Build Elasticsearch filter query from expression.
   * @param expr  expression
   * @return      query
   */
  public QueryBuilder build(Expression expr) {
    return expr.accept(this, null);
  }

  @Override
  public QueryBuilder visitFunction(FunctionExpression func, Object context) {
    FunctionName name = func.getFunctionName();
    switch (name.getFunctionName()) {
      case "and":
        return buildBoolQuery(func, context, BoolQueryBuilder::filter);
      case "or":
        return buildBoolQuery(func, context, BoolQueryBuilder::should);
      case "not":
        return buildBoolQuery(func, context, BoolQueryBuilder::mustNot);
      default: {
        LuceneQuery query = luceneQueries.get(name);
        if (query != null && query.canSupport(func)) {
          return query.build(func);
        }
        return buildScriptQuery(func);
      }
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
