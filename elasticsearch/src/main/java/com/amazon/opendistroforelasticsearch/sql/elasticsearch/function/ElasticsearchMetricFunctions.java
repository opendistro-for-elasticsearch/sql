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
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRUCT;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.AggregationState;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.Aggregator;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionBuilder;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionResolver;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionSignature;
import com.google.common.collect.ImmutableList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ElasticsearchMetricFunctions {

  public static void register(BuiltinFunctionRepository repository) {
    repository.register(buildUserDefinedAggregate("stats", STRUCT));
    repository.register(buildUserDefinedAggregate("percentiles", STRUCT));
  }

  /**
   *  PERCENTILES(age, '...')
   *  STATS(age, '...')
   */
  private static FunctionResolver buildUserDefinedAggregate(String name, ExprCoreType returnType) {
    FunctionName funcName = new FunctionName(name);
    FunctionBuilder funcBuilder = args -> new ElasticsearchDefinedAggregator(
        funcName, args, returnType);

    Map<FunctionSignature, FunctionBuilder> funcBundle = new HashMap<>();
    for (ExprType type : ExprCoreType.numberTypes()) {
      funcBundle.put(new FunctionSignature(funcName, ImmutableList.of(type)), funcBuilder);
      funcBundle.put(new FunctionSignature(funcName, ImmutableList.of(type, STRING)), funcBuilder);
    }
    return new FunctionResolver(funcName, funcBundle);
  }

  private static class ElasticsearchDefinedAggregator extends Aggregator<AggregationState> {

    public ElasticsearchDefinedAggregator(
        FunctionName functionName,
        List<Expression> arguments,
        ExprCoreType returnType) {
      super(functionName, arguments, returnType);
    }

    @Override
    public AggregationState create() {
      throw new UnsupportedOperationException(
          "Elasticsearch defined aggregator cannot run without push down to DSL");
    }

    @Override
    protected AggregationState iterate(ExprValue value, AggregationState state) {
      throw new UnsupportedOperationException(
          "Elasticsearch defined aggregator cannot run without push down to DSL");
    }
  }

}
