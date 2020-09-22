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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.aggregation;

import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import java.util.Map;
import lombok.EqualsAndHashCode;
import org.elasticsearch.script.AggregationScript;
import org.elasticsearch.search.lookup.SearchLookup;

/**
 * Aggregation Expression script factory that generates leaf factory.
 */
@EqualsAndHashCode
public class ExpressionAggregationScriptFactory implements AggregationScript.Factory {

  private final Expression expression;

  public ExpressionAggregationScriptFactory(Expression expression) {
    this.expression = expression;
  }

  @Override
  public boolean isResultDeterministic() {
    // This implies the results are cacheable
    return true;
  }

  @Override
  public AggregationScript.LeafFactory newFactory(Map<String, Object> params, SearchLookup lookup) {
    return new ExpressionAggregationScriptLeafFactory(expression, params, lookup);
  }
}
