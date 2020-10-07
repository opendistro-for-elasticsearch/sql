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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprIntegerValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprStringValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTupleValue;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.window.frame.WindowFrame;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.junit.jupiter.api.Test;

class CumulativeWindowFrameTest {

  private final WindowDefinition windowDefinition = new WindowDefinition(
      ImmutableList.of(DSL.ref("state", STRING)),
      ImmutableList.of(ImmutablePair.of(DEFAULT_ASC, DSL.ref("age", INTEGER))));

  private final WindowFrame windowFrame = new CumulativeWindowFrame(windowDefinition);

  @Test
  void should_return_new_partition_if_partition_by_field_value_changed() {
    ExprTupleValue tuple1 = ExprTupleValue.fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("WA"),
        "age", new ExprIntegerValue(20)));
    windowFrame.add(tuple1);
    assertTrue(windowFrame.isNewPartition());

    ExprTupleValue tuple2 = ExprTupleValue.fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("WA"),
        "age", new ExprIntegerValue(30)));
    windowFrame.add(tuple2);
    assertFalse(windowFrame.isNewPartition());

    ExprTupleValue tuple3 = ExprTupleValue.fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("CA"),
        "age", new ExprIntegerValue(18)));
    windowFrame.add(tuple3);
    assertTrue(windowFrame.isNewPartition());
  }

  @Test
  void can_resolve_single_expression_value() {
    windowFrame.add(ExprTupleValue.fromExprValueMap(ImmutableMap.of(
        "state", new ExprStringValue("WA"),
        "age", new ExprIntegerValue(20))));
    assertEquals(
        new ExprIntegerValue(20),
        windowFrame.resolve(DSL.ref("age", INTEGER)));
  }

  @Test
  void should_throw_exception_if_access_row_out_of_boundary() {
    assertThrows(IndexOutOfBoundsException.class, () -> windowFrame.get(2));
  }

}