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

package com.amazon.opendistroforelasticsearch.sql.expression.window;

import static java.util.Collections.emptyList;

import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionBuilder;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionResolver;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionSignature;
import com.amazon.opendistroforelasticsearch.sql.expression.window.ranking.DenseRankFunction;
import com.amazon.opendistroforelasticsearch.sql.expression.window.ranking.RankFunction;
import com.amazon.opendistroforelasticsearch.sql.expression.window.ranking.RankingWindowFunction;
import com.amazon.opendistroforelasticsearch.sql.expression.window.ranking.RowNumberFunction;
import com.google.common.collect.ImmutableMap;
import java.util.function.Supplier;
import lombok.experimental.UtilityClass;

/**
 * Window functions that register all window functions in function repository.
 */
@UtilityClass
public class WindowFunctions {

  /**
   * Register all window functions to function repository.
   * @param repository  function repository
   */
  public void register(BuiltinFunctionRepository repository) {
    repository.register(rowNumber());
    repository.register(rank());
    repository.register(denseRank());
  }

  private FunctionResolver rowNumber() {
    return rankingFunction(BuiltinFunctionName.ROW_NUMBER.getName(), RowNumberFunction::new);
  }

  private FunctionResolver rank() {
    return rankingFunction(BuiltinFunctionName.RANK.getName(), RankFunction::new);
  }

  private FunctionResolver denseRank() {
    return rankingFunction(BuiltinFunctionName.DENSE_RANK.getName(), DenseRankFunction::new);
  }

  private FunctionResolver rankingFunction(FunctionName functionName,
                                           Supplier<RankingWindowFunction> constructor) {
    FunctionSignature functionSignature = new FunctionSignature(functionName, emptyList());
    FunctionBuilder functionBuilder = arguments -> constructor.get();
    return new FunctionResolver(functionName, ImmutableMap.of(functionSignature, functionBuilder));
  }

}
