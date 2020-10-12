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

import static com.amazon.opendistroforelasticsearch.sql.ast.tree.Sort.SortOption.DEFAULT_ASC;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprTupleValue.fromExprValueMap;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.LONG;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.ref;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprIntegerValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprStringValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionTestBase;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.AggregationState;
import com.amazon.opendistroforelasticsearch.sql.expression.aggregation.Aggregator;
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

    AggregateWindowFunction windowFunction = new AggregateWindowFunction(aggregator);
    assertEquals(LONG, windowFunction.type());
    assertEquals(123, (Integer) windowFunction.accept(null, null));
  }

  @Test
  void test_count() {
    windowFunction(dsl.count(ref("age", INTEGER)))
        .add("WA", 20)
        .assertValue(1)
        .add("WA", 25)
        .assertValue(2)
        .add("WA", 30)
        .assertValue(3)
        .add("CA", 15)
        .assertValue(1)
        .add("CA", 20)
        .assertValue(2);
  }

  @Test
  void test_sum() {
    windowFunction(dsl.sum(ref("age", INTEGER)))
        .add("WA", 20)
        .assertValue(20)
        .add("WA", 25)
        .assertValue(45)
        .add("WA", 30)
        .assertValue(75)
        .add("CA", 15)
        .assertValue(15)
        .add("CA", 20)
        .assertValue(35);
  }

  @Test
  void test_avg() {
    windowFunction(dsl.avg(ref("age", INTEGER)))
        .add("WA", 20)
        .assertValue(20.0)
        .add("WA", 30)
        .assertValue(25.0)
        .add("WA", 40)
        .assertValue(30.0)
        .add("CA", 15)
        .assertValue(15.0)
        .add("CA", 25)
        .assertValue(20.0);
  }

  @Test
  void test_min() {
    windowFunction(dsl.min(ref("age", INTEGER)))
        .add("WA", 20)
        .assertValue(20)
        .add("WA", 25)
        .assertValue(20)
        .add("WA", 30)
        .assertValue(20)
        .add("CA", 15)
        .assertValue(15)
        .add("CA", 20)
        .assertValue(15);
  }

  @Test
  void test_max() {
    windowFunction(dsl.max(ref("age", INTEGER)))
        .add("WA", 20)
        .assertValue(20)
        .add("WA", 25)
        .assertValue(25)
        .add("WA", 30)
        .assertValue(30)
        .add("CA", 15)
        .assertValue(15)
        .add("CA", 20)
        .assertValue(20);
  }

  private static AggregateWindowFunctionAssertion windowFunction(
      Aggregator<AggregationState> func) {
    return new AggregateWindowFunctionAssertion(func);
  }

  private static class AggregateWindowFunctionAssertion {
    private final CumulativeWindowFrame windowFrame = new CumulativeWindowFrame(
        new WindowDefinition(
            ImmutableList.of(ref("state", STRING)),
            ImmutableList.of(Pair.of(DEFAULT_ASC, ref("age", INTEGER)))));

    private final Expression windowFunction;

    private AggregateWindowFunctionAssertion(Aggregator<AggregationState> windowFunction) {
      this.windowFunction = new AggregateWindowFunction(windowFunction);
    }

    AggregateWindowFunctionAssertion add(String state, int age) {
      windowFrame.add(fromExprValueMap(ImmutableMap.of(
          "state", new ExprStringValue(state),
          "age", new ExprIntegerValue(age))));
      return this;
    }

    AggregateWindowFunctionAssertion assertValue(Object expected) {
      assertEquals(
          ExprValueUtils.fromObjectValue(expected),
          windowFunction.valueOf(windowFrame));
      return this;
    }
  }

}