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

package com.amazon.opendistroforelasticsearch.sql.expression.window.aggregation;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprTupleValue.fromExprValueMap;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.LONG;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprIntegerValue;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionTestBase;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.Aggregator;
import com.amazon.opendistroforelasticsearch.sql.expression.window.frame.PeerRowsWindowFrame;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Aggregate window function test collection.
 */
@SuppressWarnings("unchecked")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@ExtendWith(MockitoExtension.class)
class AggregateWindowFunctionTest extends ExpressionTestBase {

  @SuppressWarnings("rawtypes")
  @Test
  void test_delegated_methods() {
    Aggregator aggregator = mock(Aggregator.class);
    when(aggregator.type()).thenReturn(LONG);
    when(aggregator.accept(any(), any())).thenReturn(123);
    when(aggregator.toString()).thenReturn("avg(age)");

    AggregateWindowFunction windowFunction = new AggregateWindowFunction(aggregator);
    assertEquals(LONG, windowFunction.type());
    assertEquals(123, (Integer) windowFunction.accept(null, null));
    assertEquals("avg(age)", windowFunction.toString());
  }

  @Test
  void should_accumulate_all_peer_values_and_not_reset_state_if_same_partition() {
    PeerRowsWindowFrame windowFrame = mock(PeerRowsWindowFrame.class);
    AggregateWindowFunction windowFunction =
        new AggregateWindowFunction(dsl.sum(DSL.ref("age", INTEGER)));

    when(windowFrame.isNewPartition()).thenReturn(true);
    when(windowFrame.next()).thenReturn(ImmutableList.of(
        fromExprValueMap(ImmutableMap.of("age", new ExprIntegerValue(10))),
        fromExprValueMap(ImmutableMap.of("age", new ExprIntegerValue(20)))));
    assertEquals(new ExprIntegerValue(30), windowFunction.valueOf(windowFrame));

    when(windowFrame.isNewPartition()).thenReturn(false);
    when(windowFrame.next()).thenReturn(ImmutableList.of(
        fromExprValueMap(ImmutableMap.of("age", new ExprIntegerValue(30)))));
    assertEquals(new ExprIntegerValue(60), windowFunction.valueOf(windowFrame));
  }

}
