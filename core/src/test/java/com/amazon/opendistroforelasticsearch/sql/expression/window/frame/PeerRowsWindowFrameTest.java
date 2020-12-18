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

package com.amazon.opendistroforelasticsearch.sql.expression.window.frame;

import static com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.SortOption.DEFAULT_ASC;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprTupleValue.fromExprValueMap;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprIntegerValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprStringValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.window.WindowDefinition;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class PeerRowsWindowFrameTest {

  private final PeerRowsWindowFrame windowFrame = new PeerRowsWindowFrame(
      new WindowDefinition(
          ImmutableList.of(DSL.ref("state", STRING)),
          ImmutableList.of(Pair.of(DEFAULT_ASC, DSL.ref("age", INTEGER)))));

  @Test
  void test_single_row() {
    PeekingIterator<ExprValue> tuples = Iterators.peekingIterator(
        Iterators.singletonIterator(tuple("WA", 10, 100)));
    windowFrame.load(tuples);
    assertTrue(windowFrame.isNewPartition());
    assertEquals(ImmutableList.of(tuple("WA", 10, 100)), windowFrame.next());
  }

  @Test
  void test_single_partition_with_no_more_rows_after_peers() {
    PeekingIterator<ExprValue> tuples = Iterators.peekingIterator(
        Iterators.forArray(
            tuple("WA", 10, 100),
            tuple("WA", 20, 200),
            tuple("WA", 20, 50)));

    // Here we simulate how WindowFrame interacts with WindowOperator which calls load()
    // and WindowFunction which calls isNewPartition() and move()
    windowFrame.load(tuples);
    assertTrue(windowFrame.isNewPartition());
    assertEquals(ImmutableList.of(tuple("WA", 10, 100)), windowFrame.next());

    windowFrame.load(tuples);
    assertFalse(windowFrame.isNewPartition());
    assertEquals(
        ImmutableList.of(tuple("WA", 20, 200), tuple("WA", 20, 50)),
        windowFrame.next());

    windowFrame.load(tuples);
    assertFalse(windowFrame.isNewPartition());
    assertEquals(ImmutableList.of(), windowFrame.next());
  }

  @Test
  void test_single_partition_with_more_rows_after_peers() {
    PeekingIterator<ExprValue> tuples = Iterators.peekingIterator(
        Iterators.forArray(
            tuple("WA", 10, 100),
            tuple("WA", 20, 200),
            tuple("WA", 20, 50),
            tuple("WA", 35, 150)));

    windowFrame.load(tuples);
    assertTrue(windowFrame.isNewPartition());
    assertEquals(
        ImmutableList.of(
            tuple("WA", 10, 100)),
        windowFrame.next());

    windowFrame.load(tuples);
    assertFalse(windowFrame.isNewPartition());
    assertEquals(
        ImmutableList.of(
            tuple("WA", 20, 200),
            tuple("WA", 20, 50)),
        windowFrame.next());

    windowFrame.load(tuples);
    assertFalse(windowFrame.isNewPartition());
    assertEquals(
        ImmutableList.of(),
        windowFrame.next());

    windowFrame.load(tuples);
    assertFalse(windowFrame.isNewPartition());
    assertEquals(
        ImmutableList.of(
            tuple("WA", 35, 150)),
        windowFrame.next());
  }

  @Test
  void test_two_partitions_with_all_same_peers_in_second_partition() {
    PeekingIterator<ExprValue> tuples = Iterators.peekingIterator(
        Iterators.forArray(
            tuple("WA", 10, 100),
            tuple("CA", 18, 150),
            tuple("CA", 18, 100)));

    windowFrame.load(tuples);
    assertTrue(windowFrame.isNewPartition());
    assertEquals(
        ImmutableList.of(
            tuple("WA", 10, 100)),
        windowFrame.next());

    windowFrame.load(tuples);
    assertTrue(windowFrame.isNewPartition());
    assertEquals(
        ImmutableList.of(
            tuple("CA", 18, 150),
            tuple("CA", 18, 100)),
        windowFrame.next());

    windowFrame.load(tuples);
    assertFalse(windowFrame.isNewPartition());
    assertEquals(
        ImmutableList.of(),
        windowFrame.next());
  }

  @Test
  void test_two_partitions_with_single_row_in_each_partition() {
    PeekingIterator<ExprValue> tuples = Iterators.peekingIterator(
        Iterators.forArray(
            tuple("WA", 10, 100),
            tuple("CA", 30, 200)));

    windowFrame.load(tuples);
    assertTrue(windowFrame.isNewPartition());
    assertEquals(
        ImmutableList.of(
            tuple("WA", 10, 100)),
        windowFrame.next());

    windowFrame.load(tuples);
    assertTrue(windowFrame.isNewPartition());
    assertEquals(
        ImmutableList.of(
            tuple("CA", 30, 200)),
        windowFrame.next());
  }

  @Test
  void test_window_definition_with_no_partition_by() {
    PeerRowsWindowFrame windowFrame = new PeerRowsWindowFrame(
        new WindowDefinition(
            ImmutableList.of(),
            ImmutableList.of(Pair.of(DEFAULT_ASC, DSL.ref("age", INTEGER)))));

    PeekingIterator<ExprValue> tuples = Iterators.peekingIterator(
        Iterators.forArray(
            tuple("WA", 10, 100),
            tuple("CA", 30, 200)));

    windowFrame.load(tuples);
    assertTrue(windowFrame.isNewPartition());
    assertEquals(
        ImmutableList.of(
            tuple("WA", 10, 100)),
        windowFrame.next());

    windowFrame.load(tuples);
    assertFalse(windowFrame.isNewPartition());
    assertEquals(
        ImmutableList.of(
            tuple("CA", 30, 200)),
        windowFrame.next());
  }

  @Test
  void test_window_definition_with_no_order_by() {
    PeerRowsWindowFrame windowFrame = new PeerRowsWindowFrame(
        new WindowDefinition(
            ImmutableList.of(DSL.ref("state", STRING)),
            ImmutableList.of()));

    PeekingIterator<ExprValue> tuples = Iterators.peekingIterator(
        Iterators.forArray(
            tuple("WA", 10, 100),
            tuple("CA", 30, 200)));

    windowFrame.load(tuples);
    assertTrue(windowFrame.isNewPartition());
    assertEquals(
        ImmutableList.of(
            tuple("WA", 10, 100)),
        windowFrame.next());

    windowFrame.load(tuples);
    assertTrue(windowFrame.isNewPartition());
    assertEquals(
        ImmutableList.of(
            tuple("CA", 30, 200)),
        windowFrame.next());
  }

  @Test
  void test_window_definition_with_no_partition_by_and_order_by() {
    PeerRowsWindowFrame windowFrame = new PeerRowsWindowFrame(
        new WindowDefinition(
            ImmutableList.of(),
            ImmutableList.of()));

    PeekingIterator<ExprValue> tuples = Iterators.peekingIterator(
        Iterators.forArray(
            tuple("WA", 10, 100),
            tuple("CA", 30, 200)));

    windowFrame.load(tuples);
    assertTrue(windowFrame.isNewPartition());
    assertEquals(
        ImmutableList.of(
            tuple("WA", 10, 100),
            tuple("CA", 30, 200)),
        windowFrame.next());

    windowFrame.load(tuples);
    assertFalse(windowFrame.isNewPartition());
    assertEquals(
        ImmutableList.of(),
        windowFrame.next());
  }

  private ExprValue tuple(String state, int age, int balance) {
    return fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue(state),
        "age", new ExprIntegerValue(age),
        "balance", new ExprIntegerValue(balance)));
  }

}
