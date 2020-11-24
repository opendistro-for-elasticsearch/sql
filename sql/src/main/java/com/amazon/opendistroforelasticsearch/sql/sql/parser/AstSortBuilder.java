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

import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.booleanLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.expression.DataType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.NullOrder.NULL_FIRST;
import static com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.SortOrder.DESC;
import static com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.OrderByClauseContext;

import com.amazon.opendistroforelasticsearch.sql.ast.expression.Argument;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Field;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.Literal;
import com.amazon.opendistroforelasticsearch.sql.ast.expression.UnresolvedExpression;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.NullOrder;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.SortOption;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.SortOrder;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.UnresolvedPlan;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParserBaseVisitor;
import com.amazon.opendistroforelasticsearch.sql.sql.parser.context.QuerySpecification;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * AST sort builder that builds Sort AST node from ORDER BY clause. During this process, the item
 * in order by may be replaced by item in project list if it's an alias or ordinal. This is same as
 * GROUP BY building process.
 */
@RequiredArgsConstructor
public class AstSortBuilder extends OpenDistroSQLParserBaseVisitor<UnresolvedPlan> {

  private final QuerySpecification querySpec;

  @Override
  public UnresolvedPlan visitOrderByClause(OrderByClauseContext ctx) {
    return new Sort(
        createSortFields()
    );
  }

  private List<Field> createSortFields() {
    List<Field> fields = new ArrayList<>();
    List<UnresolvedExpression> items = querySpec.getOrderByItems();
    List<SortOption> options = querySpec.getOrderByOptions();
    for (int i = 0; i < items.size(); i++) {
      fields.add(
          new Field(
              querySpec.replaceIfAliasOrOrdinal(items.get(i)),
              createSortArguments(options.get(i))));
    }
    return fields;
  }

  /**
   * Argument "asc" is required.
   * Argument "nullFirst" is optional and determined by Analyzer later if absent.
   */
  private List<Argument> createSortArguments(SortOption option) {
    SortOrder sortOrder = option.getSortOrder();
    NullOrder nullOrder = option.getNullOrder();
    ImmutableList.Builder<Argument> args = ImmutableList.builder();
    args.add(new Argument("asc", booleanLiteral(sortOrder != DESC))); // handle both null and ASC

    if (nullOrder != null) {
      args.add(new Argument("nullFirst", booleanLiteral(nullOrder == NULL_FIRST)));
    }
    return args.build();
  }

}
