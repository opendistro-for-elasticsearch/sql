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

package com.amazon.opendistroforelasticsearch.sql.planner.physical;

import static com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.SortOption.DEFAULT_ASC;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.ref;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.SortOption;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.window.WindowDefinition;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class WindowOperatorTest extends PhysicalPlanTestBase {

  @Test
  void test() {
    window(dsl.rank())
        .partitionBy(ref("action", STRING))
        .sortBy(DEFAULT_ASC, ref("response", INTEGER))
        .expectNext(ImmutableMap.of(
            "ip", "209.160.24.63", "action", "GET", "response", 200, "referer", "www.amazon.com",
            "rank()", 1))
        .expectNext(ImmutableMap.of(
            "ip", "112.111.162.4", "action", "GET", "response", 200, "referer", "www.amazon.com",
            "rank()", 1))
        .expectNext(ImmutableMap.of(
            "ip", "209.160.24.63", "action", "GET", "response", 404, "referer", "www.amazon.com",
            "rank()", 3))
        .expectNext(ImmutableMap.of(
            "ip", "74.125.19.106", "action", "POST", "response", 200, "referer", "www.google.com",
            "rank()", 1))
        .expectNext(ImmutableMap.of(
            "ip", "74.125.19.106", "action", "POST", "response", 500,
            "rank()", 2))
        .done();
  }

  private WindowOperatorAssertion window(FunctionExpression windowFunction) {
    return new WindowOperatorAssertion(windowFunction);
  }

  @RequiredArgsConstructor
  private static class WindowOperatorAssertion {
    private final Expression windowFunction;
    private final List<Expression> partitionByList = new ArrayList<>();
    private final List<Pair<SortOption, Expression>> sortList = new ArrayList<>();

    private WindowOperator windowOperator;

    WindowOperatorAssertion partitionBy(Expression expr) {
      partitionByList.add(expr);
      return this;
    }

    WindowOperatorAssertion sortBy(SortOption option, Expression expr) {
      sortList.add(Pair.of(option, expr));
      return this;
    }

    WindowOperatorAssertion expectNext(Map<String, Object> expected) {
      if (windowOperator == null) {
        WindowDefinition definition = new WindowDefinition(partitionByList, sortList);
        windowOperator = new WindowOperator(
            new SortOperator(new TestScan(), definition.getAllSortItems()),
            windowFunction,
            definition);
        windowOperator.open();
      }

      assertTrue(windowOperator.hasNext());
      assertEquals(ExprValueUtils.tupleValue(expected), windowOperator.next());
      return this;
    }

    void done() {
      Objects.requireNonNull(windowOperator);
      assertFalse(windowOperator.hasNext());
      windowOperator.close();
    }

  }

}