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

import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import java.util.Map;
import org.apache.lucene.index.LeafReaderContext;
import org.elasticsearch.script.FilterScript;
import org.elasticsearch.search.lookup.SearchLookup;

/**
 * Expression script leaf factory that produces script executor for each leaf.
 */
class ExpressionFilterScriptLeafFactory implements FilterScript.LeafFactory {

  /**
   * Expression to execute.
   */
  private final Expression expression;

  /**
   * Parameters for the expression.
   */
  private final Map<String, Object> params;

  /**
   * Document lookup that returns doc values.
   */
  private final SearchLookup lookup;

  public ExpressionFilterScriptLeafFactory(Expression expression,
                                           Map<String, Object> params,
                                           SearchLookup lookup) {
    this.expression = expression;
    this.params = params;
    this.lookup = lookup;
  }

  @Override
  public FilterScript newInstance(LeafReaderContext ctx) {
    return new ExpressionFilterScript(expression, lookup, ctx, params);
  }

}
