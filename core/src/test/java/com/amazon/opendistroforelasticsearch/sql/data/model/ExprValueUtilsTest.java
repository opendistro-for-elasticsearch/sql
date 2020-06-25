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

package com.amazon.opendistroforelasticsearch.sql.data.model;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.fromObjectValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.integerValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.amazon.opendistroforelasticsearch.sql.storage.bindingtuple.BindingTuple;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Test Expression Value Utils")
public class ExprValueUtilsTest {
  private static List<ExprValue> numberValues = Stream.of(1, 1L, 1f, 1D)
      .map(ExprValueUtils::fromObjectValue).collect(Collectors.toList());
  private static List<ExprValue> nonNumberValues = Stream.of("1", true, Arrays.asList(1),
      ImmutableMap.of("1", 1)).map(ExprValueUtils::fromObjectValue).collect(Collectors.toList());
  private static List<ExprValue> allValues =
      Lists.newArrayList(Iterables.concat(numberValues, nonNumberValues));

  private static List<Function<ExprValue, Object>> numberValueExtractor = Arrays.asList(
      ExprValueUtils::getIntegerValue,
      ExprValueUtils::getLongValue,
      ExprValueUtils::getFloatValue,
      ExprValueUtils::getDoubleValue);
  private static List<Function<ExprValue, Object>> nonNumberValueExtractor = Arrays.asList(
      ExprValueUtils::getStringValue,
      ExprValueUtils::getBooleanValue,
      ExprValueUtils::getCollectionValue,
      ExprValueUtils::getTupleValue);
  private static List<Function<ExprValue, Object>> allValueExtractor = Lists.newArrayList(
      Iterables.concat(numberValueExtractor, nonNumberValueExtractor));

  private static List<ExprType> numberTypes =
      Arrays.asList(ExprType.INTEGER, ExprType.LONG, ExprType.FLOAT,
          ExprType.DOUBLE);
  private static List<ExprType> nonNumberTypes =
      Arrays.asList(ExprType.STRING, ExprType.BOOLEAN, ExprType.ARRAY,
          ExprType.STRUCT);
  private static List<ExprType> allTypes =
      Lists.newArrayList(Iterables.concat(numberTypes, nonNumberTypes));

  private static Stream<Arguments> getValueTestArgumentStream() {
    List<Object> expectedValues = Arrays.asList(1, 1L, 1f, 1D, "1", true,
        Arrays.asList(integerValue(1)),
        ImmutableMap.of("1", integerValue(1)));
    Stream.Builder<Arguments> builder = Stream.builder();
    for (int i = 0; i < expectedValues.size(); i++) {
      builder.add(Arguments.of(
          allValues.get(i),
          allValueExtractor.get(i),
          expectedValues.get(i)));
    }
    return builder.build();
  }

  private static Stream<Arguments> getTypeTestArgumentStream() {
    Stream.Builder<Arguments> builder = Stream.builder();
    for (int i = 0; i < allValues.size(); i++) {
      builder.add(Arguments.of(
          allValues.get(i),
          allTypes.get(i)));
    }
    return builder.build();
  }

  private static Stream<Arguments> invalidGetNumberValueArgumentStream() {
    return Lists.cartesianProduct(nonNumberValues, numberValueExtractor)
        .stream()
        .map(list -> Arguments.of(list.get(0), list.get(1)));
  }

  @SuppressWarnings("unchecked")
  private static Stream<Arguments> invalidConvert() {
    List<Map.Entry<Function<ExprValue, Object>, ExprType>> extractorWithTypeList =
        new ArrayList<>();
    for (int i = 0; i < nonNumberValueExtractor.size(); i++) {
      extractorWithTypeList.add(
          new AbstractMap.SimpleEntry<>(nonNumberValueExtractor.get(i), nonNumberTypes.get(i)));
    }
    return Lists.cartesianProduct(allValues, extractorWithTypeList)
        .stream()
        .filter(list -> {
          ExprValue value = (ExprValue) list.get(0);
          Map.Entry<Function<ExprValue, Object>, ExprType> entry =
              (Map.Entry<Function<ExprValue, Object>,
                  ExprType>) list
                  .get(1);
          return entry.getValue() != value.type();
        })
        .map(list -> {
          Map.Entry<Function<ExprValue, Object>, ExprType> entry =
              (Map.Entry<Function<ExprValue, Object>,
                  ExprType>) list
                  .get(1);
          return Arguments.of(list.get(0), entry.getKey(), entry.getValue());
        });
  }

  @ParameterizedTest(name = "the value of ExprValue:{0} is: {2} ")
  @MethodSource("getValueTestArgumentStream")
  public void getValue(ExprValue value, Function<ExprValue, Object> extractor, Object expect) {
    assertEquals(expect, extractor.apply(value));
  }

  @ParameterizedTest(name = "the type of ExprValue:{0} is: {1} ")
  @MethodSource("getTypeTestArgumentStream")
  public void getType(ExprValue value, ExprType expectType) {
    assertEquals(expectType, value.type());
  }

  /**
   * Test Invalid to get number.
   */
  @ParameterizedTest(name = "invalid to get number value of ExprValue:{0}")
  @MethodSource("invalidGetNumberValueArgumentStream")
  public void invalidGetNumberValue(ExprValue value, Function<ExprValue, Object> extractor) {
    Exception exception = assertThrows(ExpressionEvaluationException.class,
        () -> extractor.apply(value));
    assertEquals(
        String.format("invalid to getNumberValue with expression has type of %s", value.type()),
        exception.getMessage());
  }

  /**
   * Test Invalid to convert.
   */
  @ParameterizedTest(name = "invalid convert ExprValue:{0} to ExprType:{2}")
  @MethodSource("invalidConvert")
  public void invalidConvertExprValue(ExprValue value, Function<ExprValue, Object> extractor,
                                      ExprType toType) {
    Exception exception = assertThrows(ExpressionEvaluationException.class,
        () -> extractor.apply(value));
    assertEquals(String
            .format("invalid to convert expression with type:%s to type:%s", value.type(), toType),
        exception.getMessage());
  }

  @Test
  public void unSupportedObject() {
    Exception exception = assertThrows(ExpressionEvaluationException.class,
        () -> ExprValueUtils.fromObjectValue(integerValue(1)));
    assertEquals(
        "unsupported object "
            + "class com.amazon.opendistroforelasticsearch.sql.data.model.ExprIntegerValue",
        exception.getMessage());
  }

  @Test
  public void bindingTuples() {
    for (ExprValue value : allValues) {
      if (ExprType.STRUCT == value.type()) {
        assertNotEquals(BindingTuple.EMPTY, value.bindingTuples());
      } else {
        assertEquals(BindingTuple.EMPTY, value.bindingTuples());
      }
    }
  }
}