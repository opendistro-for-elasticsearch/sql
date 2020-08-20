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
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

/**
 * Lucene query that builds wildcard query.
 */
public class WildcardQuery extends LuceneQuery {

  @Override
  protected QueryBuilder doBuild(String fieldName, ExprType fieldType, ExprValue literal) {
    fieldName = convertTextToKeyword(fieldName, fieldType);
    String matchText = convertSqlWildcardToLucene(literal.stringValue());
    return QueryBuilders.wildcardQuery(fieldName, matchText);
  }

  private String convertSqlWildcardToLucene(String text) {
    return text.replace('%', '*')
               .replace('_', '?');
  }

}
