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
import java.io.IOException;
import java.util.Map;
import org.apache.lucene.index.LeafReaderContext;
import org.elasticsearch.script.AggregationScript;
import org.elasticsearch.search.lookup.SearchLookup;

/**
 * Expression script leaf factory that produces script executor for each leaf.
 */
public class ExpressionAggregationScriptLeafFactory implements AggregationScript.LeafFactory {

  /**
   * Expression to execute.
   */
  private final Expression expression;

  /**
   * Expression to execute.
   */
  private final Map<String, Object> params;

  /**
   * Expression to execute.
   */
  private final SearchLookup lookup;

  /**
   * Constructor of ExpressionAggregationScriptLeafFactory.
   */
  public ExpressionAggregationScriptLeafFactory(
      Expression expression, Map<String, Object> params, SearchLookup lookup) {
    this.expression = expression;
    this.params = params;
    this.lookup = lookup;
  }

  @Override
  public AggregationScript newInstance(LeafReaderContext ctx) {
    return new ExpressionAggregationScript(expression, lookup, ctx, params);
  }

  @Override
  public boolean needs_score() {
    return false;
  }
}
