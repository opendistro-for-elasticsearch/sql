/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.expression.function;

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.BYTE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.FLOAT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.LONG;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.SHORT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.UNKNOWN;
import static com.amazon.opendistroforelasticsearch.sql.data.type.WideningTypeRule.IMPOSSIBLE_WIDENING;
import static com.amazon.opendistroforelasticsearch.sql.data.type.WideningTypeRule.TYPE_EQUAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.data.type.WideningTypeRule;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class WideningTypeRuleTest {
  private static Table<ExprCoreType, ExprCoreType, Integer> numberWidenRule =
      new ImmutableTable.Builder<ExprCoreType, ExprCoreType,
          Integer>()
          .put(BYTE, SHORT, 1)
          .put(BYTE, INTEGER, 2)
          .put(BYTE, LONG, 3)
          .put(BYTE, FLOAT, 4)
          .put(BYTE, DOUBLE, 5)
          .put(SHORT, INTEGER, 1)
          .put(SHORT, LONG, 2)
          .put(SHORT, FLOAT, 3)
          .put(SHORT, DOUBLE, 4)
          .put(INTEGER, LONG, 1)
          .put(INTEGER, FLOAT, 2)
          .put(INTEGER, DOUBLE, 3)
          .put(LONG, FLOAT, 1)
          .put(LONG, DOUBLE, 2)
          .put(FLOAT, DOUBLE, 1)
          .build();

  private static Stream<Arguments> distanceArguments() {
    List<ExprCoreType> exprTypes =
        Arrays.asList(ExprCoreType.values()).stream().filter(type -> type != UNKNOWN).collect(
            Collectors.toList());
    return Lists.cartesianProduct(exprTypes, exprTypes).stream()
        .map(list -> {
          ExprCoreType type1 = list.get(0);
          ExprCoreType type2 = list.get(1);
          if (type1 == type2) {
            return Arguments.of(type1, type2, TYPE_EQUAL);
          } else if (numberWidenRule.contains(type1, type2)) {
            return Arguments.of(type1, type2, numberWidenRule.get(type1, type2));
          } else {
            return Arguments.of(type1, type2, IMPOSSIBLE_WIDENING);
          }
        });
  }

  private static Stream<Arguments> validMaxTypes() {
    List<ExprCoreType> exprTypes =
        Arrays.asList(ExprCoreType.values()).stream().filter(type -> type != UNKNOWN).collect(
            Collectors.toList());
    return Lists.cartesianProduct(exprTypes, exprTypes).stream()
        .map(list -> {
          ExprCoreType type1 = list.get(0);
          ExprCoreType type2 = list.get(1);
          if (type1 == type2) {
            return Arguments.of(type1, type2, type1);
          } else if (numberWidenRule.contains(type1, type2)) {
            return Arguments.of(type1, type2, type2);
          } else if (numberWidenRule.contains(type2, type1)) {
            return Arguments.of(type1, type2, type1);
          } else {
            return Arguments.of(type1, type2, null);
          }
        });
  }

  @ParameterizedTest
  @MethodSource("distanceArguments")
  public void distance(ExprCoreType v1, ExprCoreType v2, Integer expected) {
    assertEquals(expected, WideningTypeRule.distance(v1, v2));
  }

  @ParameterizedTest
  @MethodSource("validMaxTypes")
  public void max(ExprCoreType v1, ExprCoreType v2, ExprCoreType expected) {
    if (null == expected) {
      ExpressionEvaluationException exception = assertThrows(
          ExpressionEvaluationException.class, () -> WideningTypeRule.max(v1, v2));
      assertEquals(String.format("no max type of %s and %s ", v1, v2), exception.getMessage());
    } else {
      assertEquals(expected, WideningTypeRule.max(v1, v2));
    }
  }
}