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

import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.argument;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.booleanLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.field;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.intLiteral;
import static com.amazon.opendistroforelasticsearch.sql.ast.dsl.AstDSL.qualifiedName;
import static com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.NullOrder.NULL_FIRST;
import static com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.NullOrder.NULL_LAST;
import static com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.SortOrder.ASC;
import static com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.SortOrder.DESC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.SortOption;
import com.amazon.opendistroforelasticsearch.sql.ast.tree.UnresolvedPlan;
import com.amazon.opendistroforelasticsearch.sql.sql.antlr.parser.OpenDistroSQLParser.OrderByClauseContext;
import com.amazon.opendistroforelasticsearch.sql.sql.parser.context.QuerySpecification;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class AstSortBuilderTest {

  @Mock
  private QuerySpecification querySpec;

  @Mock
  private OrderByClauseContext orderByClause;

  @Mock
  private UnresolvedPlan child;

  @Test
  void can_build_sort_node() {
    doAnswer(returnsFirstArg()).when(querySpec).replaceIfAliasOrOrdinal(any());
    when(querySpec.getOrderByItems()).thenReturn(ImmutableList.of(qualifiedName("name")));

    ImmutableMap<SortOption, Pair<Boolean, Boolean>> expects =
        ImmutableMap.<SortOption, Pair<Boolean, Boolean>>builder()
            .put(new SortOption(null, null), ImmutablePair.of(true, true))
            .put(new SortOption(ASC, NULL_FIRST), ImmutablePair.of(true, true))
            .put(new SortOption(DESC, NULL_LAST), ImmutablePair.of(false, false))
            .build();

    expects.forEach((option, expect) -> {
      when(querySpec.getOrderByOptions()).thenReturn(ImmutableList.of(option));

      AstSortBuilder sortBuilder = new AstSortBuilder(querySpec);
      assertEquals(
          new Sort(
              child, // has to mock and attach child otherwise Guava ImmutableList NPE in getChild()
              ImmutableList.of(argument("count", intLiteral(0))),
              ImmutableList.of(
                  field("name",
                      argument("asc", booleanLiteral(expect.getLeft())),
                      argument("nullFirst", booleanLiteral(expect.getRight()))))),
          sortBuilder.visitOrderByClause(orderByClause).attach(child));
    });
  }

}