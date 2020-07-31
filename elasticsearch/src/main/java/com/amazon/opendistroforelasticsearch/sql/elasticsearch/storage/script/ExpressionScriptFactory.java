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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script;

import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import java.util.Map;
import org.elasticsearch.script.FilterScript;
import org.elasticsearch.search.lookup.SearchLookup;

/**
 * Expression script factory that generates leaf factory.
 */
class ExpressionScriptFactory implements FilterScript.Factory {

  /**
   * Expression to execute.
   */
  private final Expression expression;

  public ExpressionScriptFactory(Expression expression) {
    this.expression = expression;
  }

  @Override
  public FilterScript.LeafFactory newFactory(Map<String, Object> params, SearchLookup lookup) {
    return new ExpressionScriptLeafFactory(expression, params, lookup);
  }

}
