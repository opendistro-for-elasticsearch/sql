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

package com.amazon.opendistroforelasticsearch.sql.sql.parser;

import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.QualifiedNameContext;

import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.sql.parser.context.QuerySpecification;
import lombok.RequiredArgsConstructor;

/**
 * AST Having filter builder that builds HAVING clause condition expressions
 * and replace alias by original expression in SELECT clause.
 * The reason for this is it's hard to replace afterwards since UnresolvedExpression
 * is immutable.
 */
@RequiredArgsConstructor
public class AstHavingFilterBuilder extends AstExpressionBuilder {

  private final QuerySpecification querySpec;

  @Override
  public UnresolvedExpression visitQualifiedName(QualifiedNameContext ctx) {
    return replaceAlias(super.visitQualifiedName(ctx));
  }

  private UnresolvedExpression replaceAlias(UnresolvedExpression expr) {
    if (querySpec.isSelectAlias(expr)) {
      return querySpec.getSelectItemByAlias(expr);
    }
    return expr;
  }

}
