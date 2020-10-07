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

package com.amazon.opendistroforelasticsearch.sql.expression.window.ranking;

import static com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.SortOption.DEFAULT_ASC;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprTupleValue.fromExprValueMap;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprIntegerValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprStringValue;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionTestBase;
import com.amazon.opendistroforelasticsearch.sql.expression.window.CumulativeWindowFrame;
import com.amazon.opendistroforelasticsearch.sql.expression.window.WindowDefinition;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Rank window function test collection.
 */
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class RankingWindowFunctionTest extends ExpressionTestBase {

  private final CumulativeWindowFrame windowFrame1 = new CumulativeWindowFrame(
      new WindowDefinition(
          ImmutableList.of(DSL.ref("state", STRING)),
          ImmutableList.of(Pair.of(DEFAULT_ASC, DSL.ref("age", INTEGER)))));

  private final CumulativeWindowFrame windowFrame2 = new CumulativeWindowFrame(
      new WindowDefinition(
          ImmutableList.of(DSL.ref("state", STRING)),
          ImmutableList.of())); // No sort items defined

  @Test
  void test_value_of() {
    windowFrame1.add(fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("WA"), "age", new ExprIntegerValue(30))));

    RankingWindowFunction rowNumber = dsl.rowNumber();
    assertEquals(new ExprIntegerValue(1), rowNumber.valueOf(windowFrame1));
  }

  @Test
  void test_row_number() {
    RankingWindowFunction rowNumber = dsl.rowNumber();

    windowFrame1.add(fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("WA"), "age", new ExprIntegerValue(30))));
    assertEquals(1, rowNumber.rank(windowFrame1));

    windowFrame1.add(fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("WA"), "age", new ExprIntegerValue(30))));
    assertEquals(2, rowNumber.rank(windowFrame1));

    windowFrame1.add(fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("WA"), "age", new ExprIntegerValue(40))));
    assertEquals(3, rowNumber.rank(windowFrame1));

    windowFrame1.add(fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("CA"), "age", new ExprIntegerValue(20))));
    assertEquals(1, rowNumber.rank(windowFrame1));
  }

  @Test
  void test_rank() {
    RankingWindowFunction rank = dsl.rank();

    windowFrame1.add(fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("WA"), "age", new ExprIntegerValue(30))));
    assertEquals(1, rank.rank(windowFrame1));

    windowFrame1.add(fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("WA"), "age", new ExprIntegerValue(30))));
    assertEquals(1, rank.rank(windowFrame1));

    windowFrame1.add(fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("WA"), "age", new ExprIntegerValue(50))));
    assertEquals(3, rank.rank(windowFrame1));

    windowFrame1.add(fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("WA"), "age", new ExprIntegerValue(55))));
    assertEquals(4, rank.rank(windowFrame1));

    windowFrame1.add(fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("CA"), "age", new ExprIntegerValue(15))));
    assertEquals(1, rank.rank(windowFrame1));
  }

  @Test
  void test_dense_rank() {
    RankingWindowFunction denseRank = dsl.denseRank();

    windowFrame1.add(fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("WA"), "age", new ExprIntegerValue(30))));
    assertEquals(1, denseRank.rank(windowFrame1));

    windowFrame1.add(fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("WA"), "age", new ExprIntegerValue(30))));
    assertEquals(1, denseRank.rank(windowFrame1));

    windowFrame1.add(fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("WA"), "age", new ExprIntegerValue(50))));
    assertEquals(2, denseRank.rank(windowFrame1));

    windowFrame1.add(fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("WA"), "age", new ExprIntegerValue(55))));
    assertEquals(3, denseRank.rank(windowFrame1));

    windowFrame1.add(fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("CA"), "age", new ExprIntegerValue(15))));
    assertEquals(1, denseRank.rank(windowFrame1));
  }

  @Test
  void row_number_should_work_if_no_sort_items_defined() {
    RankingWindowFunction rowNumber = dsl.rowNumber();

    windowFrame2.add(fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("WA"), "age", new ExprIntegerValue(30))));
    assertEquals(1, rowNumber.rank(windowFrame2));

    windowFrame2.add(fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("WA"), "age", new ExprIntegerValue(30))));
    assertEquals(2, rowNumber.rank(windowFrame2));

    windowFrame2.add(fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("WA"), "age", new ExprIntegerValue(40))));
    assertEquals(3, rowNumber.rank(windowFrame2));

    windowFrame2.add(fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("CA"), "age", new ExprIntegerValue(20))));
    assertEquals(1, rowNumber.rank(windowFrame2));
  }

  @Test
  void rank_should_always_return_1_if_no_sort_items_defined() {
    RankingWindowFunction rank = dsl.rank();

    windowFrame2.add(fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("WA"), "age", new ExprIntegerValue(30))));
    assertEquals(1, rank.rank(windowFrame2));

    windowFrame2.add(fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("WA"), "age", new ExprIntegerValue(30))));
    assertEquals(1, rank.rank(windowFrame2));

    windowFrame2.add(fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("WA"), "age", new ExprIntegerValue(50))));
    assertEquals(1, rank.rank(windowFrame2));

    windowFrame2.add(fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("WA"), "age", new ExprIntegerValue(55))));
    assertEquals(1, rank.rank(windowFrame2));

    windowFrame2.add(fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("CA"), "age", new ExprIntegerValue(15))));
    assertEquals(1, rank.rank(windowFrame2));
  }

  @Test
  void dense_rank_should_always_return_1_if_no_sort_items_defined() {
    RankingWindowFunction denseRank = dsl.denseRank();

    windowFrame2.add(fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("WA"), "age", new ExprIntegerValue(30))));
    assertEquals(1, denseRank.rank(windowFrame2));

    windowFrame2.add(fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("WA"), "age", new ExprIntegerValue(30))));
    assertEquals(1, denseRank.rank(windowFrame2));

    windowFrame2.add(fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("WA"), "age", new ExprIntegerValue(50))));
    assertEquals(1, denseRank.rank(windowFrame2));

    windowFrame2.add(fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("WA"), "age", new ExprIntegerValue(55))));
    assertEquals(1, denseRank.rank(windowFrame2));

    windowFrame2.add(fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("CA"), "age", new ExprIntegerValue(15))));
    assertEquals(1, denseRank.rank(windowFrame2));
  }

}