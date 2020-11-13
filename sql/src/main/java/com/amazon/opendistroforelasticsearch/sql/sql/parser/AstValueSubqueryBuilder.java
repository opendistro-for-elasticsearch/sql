/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.sql.parser;

import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.SelectClauseContext;

import com.amazon.opendistroforelasticsearch.sql.ast.tree.UnresolvedPlan;
import java.util.Locale;

public class AstValueSubqueryBuilder extends AstBuilder {
  private final String query;

  public AstValueSubqueryBuilder(String query) {
    super(query);
    this.query = query;
  }

  @Override
  public UnresolvedPlan visitSelectClause(SelectClauseContext ctx) {
    if (ctx.selectElements().selectElement().size() > 1) {
      throw new IllegalStateException(String.format(Locale.ROOT,
          "illegal to have more than one select fields in subquery: %s", query));
    }
    return super.visitSelectClause(ctx);
  }

}
