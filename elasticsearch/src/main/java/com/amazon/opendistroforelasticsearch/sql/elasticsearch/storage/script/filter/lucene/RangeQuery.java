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
import lombok.RequiredArgsConstructor;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;

/**
 * Lucene query that builds range query for non-quality comparison.
 */
@RequiredArgsConstructor
public class RangeQuery extends LuceneQuery {

  public enum Comparison {
    LT, GT, LTE, GTE, BETWEEN
  }

  /**
   * Comparison that range query build for.
   */
  private final Comparison comparison;

  @Override
  protected QueryBuilder doBuild(String fieldName, ExprType fieldType, ExprValue literal) {
    Object value = literal.value();

    RangeQueryBuilder query = QueryBuilders.rangeQuery(fieldName);
    switch (comparison) {
      case LT:
        return query.lt(value);
      case GT:
        return query.gt(value);
      case LTE:
        return query.lte(value);
      case GTE:
        return query.gte(value);
      default:
        throw new IllegalStateException("Comparison is supported by range query: " + comparison);
    }
  }

}
