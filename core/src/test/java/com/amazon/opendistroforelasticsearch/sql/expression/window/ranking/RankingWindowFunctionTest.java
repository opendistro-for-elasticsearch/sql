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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprIntegerValue;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionTestBase;
import com.amazon.opendistroforelasticsearch.sql.expression.window.WindowFrame;
import com.google.common.collect.ImmutableList;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class RankingWindowFunctionTest extends ExpressionTestBase {

  @Mock
  private WindowFrame windowFrame;

  @BeforeEach
  void setUp() {
    when(windowFrame.isNewPartition()).thenReturn(true);
  }

  @Test
  void test_row_number() {
    RankingWindowFunction rowNumber = dsl.rowNumber();
    assertEquals(1, rowNumber.rank(windowFrame));

    when(windowFrame.isNewPartition()).thenReturn(false);
    assertEquals(2, rowNumber.rank(windowFrame));
    assertEquals(3, rowNumber.rank(windowFrame));

    when(windowFrame.isNewPartition()).thenReturn(true);
    assertEquals(1, rowNumber.rank(windowFrame));
  }

  @Test
  void test_rank() {
    RankingWindowFunction rank = dsl.rank();
    assertEquals(1, rank.rank(windowFrame));

    when(windowFrame.isNewPartition()).thenReturn(false);
    when(windowFrame.resolveSortItemValues(0)).thenReturn(
        ImmutableList.of(new ExprIntegerValue(30)));
    when(windowFrame.resolveSortItemValues(-1)).thenReturn(
        ImmutableList.of(new ExprIntegerValue(30)));
    assertEquals(1, rank.rank(windowFrame));
    assertEquals(1, rank.rank(windowFrame));

    when(windowFrame.resolveSortItemValues(0)).thenReturn(
        ImmutableList.of(new ExprIntegerValue(50)));
    assertEquals(4, rank.rank(windowFrame));

    when(windowFrame.resolveSortItemValues(0)).thenReturn(
        ImmutableList.of(new ExprIntegerValue(55)));
    assertEquals(5, rank.rank(windowFrame));

    when(windowFrame.isNewPartition()).thenReturn(true);
    assertEquals(1, rank.rank(windowFrame));
  }

  @Test
  void test_dense_rank() {
    RankingWindowFunction denseRank = dsl.denseRank();
    assertEquals(1, denseRank.rank(windowFrame));

    when(windowFrame.isNewPartition()).thenReturn(false);
    when(windowFrame.resolveSortItemValues(0)).thenReturn(
        ImmutableList.of(new ExprIntegerValue(30)));
    when(windowFrame.resolveSortItemValues(-1)).thenReturn(
        ImmutableList.of(new ExprIntegerValue(30)));
    assertEquals(1, denseRank.rank(windowFrame));
    assertEquals(1, denseRank.rank(windowFrame));

    when(windowFrame.resolveSortItemValues(0)).thenReturn(
        ImmutableList.of(new ExprIntegerValue(50)));
    assertEquals(2, denseRank.rank(windowFrame));

    when(windowFrame.resolveSortItemValues(0)).thenReturn(
        ImmutableList.of(new ExprIntegerValue(55)));
    assertEquals(3, denseRank.rank(windowFrame));

    when(windowFrame.isNewPartition()).thenReturn(true);
    assertEquals(1, denseRank.rank(windowFrame));
  }

}