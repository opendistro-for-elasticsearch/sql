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

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.integerValue;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.ARRAY;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.BOOLEAN;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DATE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DATETIME;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTERVAL;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRUCT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.TIME;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.TIMESTAMP;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.amazon.opendistroforelasticsearch.sql.storage.bindingtuple.BindingTuple;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayName("Test Expression Value Utils")
public class ExprValueUtilsTest {
  private static LinkedHashMap<String, ExprValue> testTuple = new LinkedHashMap<>();

  static {
    testTuple.put("1", new ExprIntegerValue(1));
  }

  private static List<ExprValue> numberValues = Stream.of(1, 1L, 1f, 1D)
      .map(ExprValueUtils::fromObjectValue).collect(Collectors.toList());

  private static List<ExprValue> nonNumberValues = Arrays.asList(
      new ExprStringValue("1"),
      ExprBooleanValue.of(true),
      new ExprCollectionValue(ImmutableList.of(new ExprIntegerValue(1))),
      new ExprTupleValue(testTuple),
      new ExprDateValue("2012-08-07"),
      new ExprTimeValue("18:00:00"),
      new ExprDatetimeValue("2012-08-07 18:00:00"),
      new ExprTimestampValue("2012-08-07 18:00:00"),
      new ExprIntervalValue(Duration.ofSeconds(100)));

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
      ExprValueUtils::getTupleValue
  );
  private static List<Function<ExprValue, Object>> dateAndTimeValueExtractor = Arrays.asList(
      ExprValue::dateValue,
      ExprValue::timeValue,
      ExprValue::datetimeValue,
      ExprValue::timestampValue,
      ExprValue::intervalValue);
  private static List<Function<ExprValue, Object>> allValueExtractor = Lists.newArrayList(
      Iterables.concat(numberValueExtractor, nonNumberValueExtractor, dateAndTimeValueExtractor));

  private static List<ExprCoreType> numberTypes =
      Arrays.asList(ExprCoreType.INTEGER, ExprCoreType.LONG, ExprCoreType.FLOAT,
          ExprCoreType.DOUBLE);
  private static List<ExprCoreType> nonNumberTypes =
      Arrays.asList(STRING, BOOLEAN, ARRAY, STRUCT);
  private static List<ExprCoreType> dateAndTimeTypes =
      Arrays.asList(DATE, TIME, DATETIME, TIMESTAMP, INTERVAL);
  private static List<ExprCoreType> allTypes =
      Lists.newArrayList(Iterables.concat(numberTypes, nonNumberTypes, dateAndTimeTypes));

  private static Stream<Arguments> getValueTestArgumentStream() {
    List<Object> expectedValues = Arrays.asList(1, 1L, 1f, 1D, "1", true,
        Arrays.asList(integerValue(1)),
        ImmutableMap.of("1", integerValue(1)),
        LocalDate.parse("2012-08-07"),
        LocalTime.parse("18:00:00"),
        LocalDateTime.parse("2012-08-07T18:00:00"),
        ZonedDateTime.of(LocalDateTime.parse("2012-08-07T18:00:00"), ZoneId.of("UTC")).toInstant(),
        Duration.ofSeconds(100)
    );
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
    List<Map.Entry<Function<ExprValue, Object>, ExprCoreType>> extractorWithTypeList =
        new ArrayList<>();
    for (int i = 0; i < nonNumberValueExtractor.size(); i++) {
      extractorWithTypeList.add(
          new AbstractMap.SimpleEntry<>(nonNumberValueExtractor.get(i), nonNumberTypes.get(i)));
    }
    return Lists.cartesianProduct(allValues, extractorWithTypeList)
        .stream()
        .filter(list -> {
          ExprValue value = (ExprValue) list.get(0);
          Map.Entry<Function<ExprValue, Object>, ExprCoreType> entry =
              (Map.Entry<Function<ExprValue, Object>,
                  ExprCoreType>) list
                  .get(1);
          return entry.getValue() != value.type();
        })
        .map(list -> {
          Map.Entry<Function<ExprValue, Object>, ExprCoreType> entry =
              (Map.Entry<Function<ExprValue, Object>,
                  ExprCoreType>) list
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
  public void getType(ExprValue value, ExprCoreType expectType) {
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
    assertThat(exception.getMessage(), Matchers.containsString("invalid"));
  }

  /**
   * Test Invalid to convert.
   */
  @ParameterizedTest(name = "invalid convert ExprValue:{0} to ExprType:{2}")
  @MethodSource("invalidConvert")
  public void invalidConvertExprValue(ExprValue value, Function<ExprValue, Object> extractor,
                                      ExprCoreType toType) {
    Exception exception = assertThrows(ExpressionEvaluationException.class,
        () -> extractor.apply(value));
    assertThat(exception.getMessage(), Matchers.containsString("invalid"));
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
      if (STRUCT == value.type()) {
        assertNotEquals(BindingTuple.EMPTY, value.bindingTuples());
      } else {
        assertEquals(BindingTuple.EMPTY, value.bindingTuples());
      }
    }
  }

  @Test
  public void constructDateAndTimeValue() {
    assertEquals(new ExprDateValue("2012-07-07"),
        ExprValueUtils.fromObjectValue("2012-07-07", DATE));
    assertEquals(new ExprTimeValue("01:01:01"),
        ExprValueUtils.fromObjectValue("01:01:01", TIME));
    assertEquals(new ExprDatetimeValue("2012-07-07 01:01:01"),
        ExprValueUtils.fromObjectValue("2012-07-07 01:01:01", DATETIME));
    assertEquals(new ExprTimestampValue("2012-07-07 01:01:01"),
        ExprValueUtils.fromObjectValue("2012-07-07 01:01:01", TIMESTAMP));
  }

  @Test
  public void hashCodeTest() {
    assertEquals(new ExprIntegerValue(1).hashCode(), new ExprIntegerValue(1).hashCode());
    assertEquals(new ExprStringValue("1").hashCode(), new ExprStringValue("1").hashCode());
    assertEquals(new ExprCollectionValue(ImmutableList.of(new ExprIntegerValue(1))).hashCode(),
        new ExprCollectionValue(ImmutableList.of(new ExprIntegerValue(1))).hashCode());
    assertEquals(new ExprTupleValue(testTuple).hashCode(),
        new ExprTupleValue(testTuple).hashCode());
    assertEquals(new ExprDateValue("2012-08-07").hashCode(),
        new ExprDateValue("2012-08-07").hashCode());
    assertEquals(new ExprTimeValue("18:00:00").hashCode(),
        new ExprTimeValue("18:00:00").hashCode());
    assertEquals(new ExprDatetimeValue("2012-08-07 18:00:00").hashCode(),
        new ExprDatetimeValue("2012-08-07 18:00:00").hashCode());
    assertEquals(new ExprTimestampValue("2012-08-07 18:00:00").hashCode(),
        new ExprTimestampValue("2012-08-07 18:00:00").hashCode());
  }
}