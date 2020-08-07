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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.storage.script.filter.lucene;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.LiteralExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import org.elasticsearch.index.query.QueryBuilder;

/**
 * Lucene query abstraction that builds Lucene query from function expression.
 */
public abstract class LuceneQuery {

  /**
   * Check if function expression supported by current Lucene query.
   * Default behavior is that report supported if:
   *  1. Left is a reference
   *  2. Right side is a literal
   *
   * @param func    function
   * @return        return true if supported, otherwise false.
   */
  public boolean canSupport(FunctionExpression func) {
    return (func.getArguments().size() == 2)
        && (func.getArguments().get(0) instanceof ReferenceExpression)
        && (func.getArguments().get(1) instanceof LiteralExpression);
  }

  /**
   * Build Lucene query from function expression.
   *
   * @param func  function
   * @return      query
   */
  public QueryBuilder build(FunctionExpression func) {
    ReferenceExpression ref = (ReferenceExpression) func.getArguments().get(0);
    LiteralExpression literal = (LiteralExpression) func.getArguments().get(1);
    return doBuild(ref.getAttr(), ref.type(), literal.valueOf(null));
  }

  /**
   * Build method that subclass implements by default which is to build query
   * from reference and literal in function arguments.
   *
   * @param fieldName   field name
   * @param fieldType        expr fieldType
   * @param literal       expr literal
   * @return            query
   */
  protected QueryBuilder doBuild(String fieldName, ExprType fieldType, ExprValue literal) {
    throw new UnsupportedOperationException(
        "Subclass doesn't implement this and build method either");
  }

}
