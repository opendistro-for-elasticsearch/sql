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

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprNullValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.core.ExpressionScript;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import java.util.Map;
import lombok.EqualsAndHashCode;
import org.apache.lucene.index.LeafReaderContext;
import org.elasticsearch.script.AggregationScript;
import org.elasticsearch.search.lookup.SearchLookup;

/**
 * Aggregation expression script that executed on each document.
 */
@EqualsAndHashCode(callSuper = false)
public class ExpressionAggregationScript extends AggregationScript {

  /**
   * Expression Script.
   */
  private final ExpressionScript expressionScript;

  /**
   * Constructor of ExpressionAggregationScript.
   */
  public ExpressionAggregationScript(
      Expression expression,
      SearchLookup lookup,
      LeafReaderContext context,
      Map<String, Object> params) {
    super(params, lookup, context);
    this.expressionScript = new ExpressionScript(expression);
  }

  @Override
  public Object execute() {
    return expressionScript.execute(this::getDoc, this::evaluateExpression);
  }

  private ExprValue evaluateExpression(Expression expression, Environment<Expression,
                                       ExprValue> valueEnv) {
    ExprValue result = expression.valueOf(valueEnv);

    // The missing value is treated as null value in doc_value, so we can't distinguish with them.
    if (result.isNull()) {
      return ExprNullValue.of();
    }
    return result;
  }
}
