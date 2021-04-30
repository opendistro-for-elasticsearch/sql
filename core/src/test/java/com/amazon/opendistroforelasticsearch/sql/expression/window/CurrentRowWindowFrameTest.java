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

package com.amazon.opendistroforelasticsearch.sql.expression.window;

import static com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.SortOption.DEFAULT_ASC;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprIntegerValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprStringValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTupleValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.window.frame.CurrentRowWindowFrame;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.Test;

class CurrentRowWindowFrameTest {

  private final CurrentRowWindowFrame windowFrame = new CurrentRowWindowFrame(
      new WindowDefinition(
          ImmutableList.of(DSL.ref("state", STRING)),
          ImmutableList.of(ImmutablePair.of(DEFAULT_ASC, DSL.ref("age", INTEGER)))));

  @Test
  void test_iterator_methods() {
    assertFalse(windowFrame.hasNext());
    assertTrue(windowFrame.next().isEmpty());
  }

  @Test
  void should_return_new_partition_if_partition_by_field_value_changed() {
    PeekingIterator<ExprValue> iterator = Iterators.peekingIterator(
        Iterators.forArray(
            ExprTupleValue.fromExprValueMap(ImmutableMap.of(
                "state", new ExprStringValue("WA"),
                "age", new ExprIntegerValue(20))),
            ExprTupleValue.fromExprValueMap(ImmutableMap.of(
                "state", new ExprStringValue("WA"),
                "age", new ExprIntegerValue(30))),
            ExprTupleValue.fromExprValueMap(ImmutableMap.of(
                "state", new ExprStringValue("CA"),
                "age", new ExprIntegerValue(18)))));

    windowFrame.load(iterator);
    assertTrue(windowFrame.isNewPartition());

    windowFrame.load(iterator);
    assertFalse(windowFrame.isNewPartition());

    windowFrame.load(iterator);
    assertTrue(windowFrame.isNewPartition());
  }

  @Test
  void can_resolve_single_expression_value() {
    windowFrame.load(Iterators.peekingIterator(
        Iterators.singletonIterator(
            ExprTupleValue.fromExprValueMap(ImmutableMap.of(
            "state", new ExprStringValue("WA"),
            "age", new ExprIntegerValue(20))))));
    assertEquals(
        new ExprIntegerValue(20),
        windowFrame.resolve(DSL.ref("age", INTEGER)));
  }

  @Test
  void can_return_previous_and_current_row() {
    ExprValue row1 = ExprTupleValue.fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("WA"),
        "age", new ExprIntegerValue(20)));
    ExprValue row2 = ExprTupleValue.fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("WA"),
        "age", new ExprIntegerValue(30)));
    PeekingIterator<ExprValue> iterator = Iterators.peekingIterator(Iterators.forArray(row1, row2));

    windowFrame.load(iterator);
    assertNull(windowFrame.previous());
    assertEquals(row1, windowFrame.current());

    windowFrame.load(iterator);
    assertEquals(row1, windowFrame.previous());
    assertEquals(row2, windowFrame.current());
  }

}