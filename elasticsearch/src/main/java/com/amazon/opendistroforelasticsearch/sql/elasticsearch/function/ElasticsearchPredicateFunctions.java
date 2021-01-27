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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.function;

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.filter.lucene.LuceneQuery;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.LiteralExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionBuilder;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionResolver;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionSignature;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

public class ElasticsearchPredicateFunctions {

  public static void register(BuiltinFunctionRepository repository) {
    repository.register(match());
  }

  private static FunctionResolver match() {
    FunctionName funcName = new FunctionName("match");
    FunctionBuilder funcBuilder = args -> new ElasticsearchPredicate(funcName, args) {
      @Override
      public QueryBuilder query() {
        ReferenceExpression ref = (ReferenceExpression) getArguments().get(0);
        LiteralExpression literal = (LiteralExpression) getArguments().get(1);
        return QueryBuilders.matchQuery(ref.getAttr(), literal.valueOf(null).value());
      }
    };

    return new FunctionResolver(funcName,
        ImmutableMap.<FunctionSignature, FunctionBuilder>builder()
            .put(
                new FunctionSignature(funcName, ImmutableList.of(STRING, STRING)),
                funcBuilder)
            .build());
  }

  public abstract static class ElasticsearchPredicate extends FunctionExpression {

    public ElasticsearchPredicate(FunctionName functionName, List<Expression> arguments) {
      super(functionName, arguments);
    }

    public abstract QueryBuilder query();

    @Override
    public ExprValue valueOf(Environment<Expression, ExprValue> valueEnv) {
      throw new UnsupportedOperationException(
          "Elasticsearch defined function cannot run without Elasticsearch DSL support");
    }

    @Override
    public ExprType type() {
      return ExprCoreType.BOOLEAN;
    }
  }

}
