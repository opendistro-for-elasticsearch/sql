/*
 *     Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License").
 *     You may not use this file except in compliance with the License.
 *     A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     or in the "license" file accompanying this file. This file is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *     express or implied. See the License for the specific language governing
 *     permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.utils;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

/**
 * The Implementation of Content to represent {@link Object}.
 */
@RequiredArgsConstructor
public class ObjectContent implements Content {

  private final Object value;

  @Override
  public Integer intValue() {
    return parseNumberValue(value, Integer::valueOf, Number::intValue);
  }

  @Override
  public Long longValue() {
    return parseNumberValue(value, Long::valueOf, Number::longValue);
  }

  @Override
  public Short shortValue() {
    return parseNumberValue(value, Short::valueOf, Number::shortValue);
  }

  @Override
  public Byte byteValue() {
    return parseNumberValue(value, Byte::valueOf, Number::byteValue);
  }

  @Override
  public Float floatValue() {
    return parseNumberValue(value, Float::valueOf, Number::floatValue);
  }

  @Override
  public Double doubleValue() {
    return parseNumberValue(value, Double::valueOf, Number::doubleValue);
  }

  @Override
  public String stringValue() {
    return (String) value;
  }

  @Override
  public Boolean booleanValue() {
    return (Boolean) value;
  }

  @Override
  public Object objectValue() {
    return value;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Iterator<Map.Entry<String, Content>> map() {
    return ((Map<String, Object>) value).entrySet().stream()
        .map(entry -> (Map.Entry<String, Content>) new AbstractMap.SimpleEntry<String, Content>(
            entry.getKey(),
            new ObjectContent(entry.getValue())))
        .iterator();
  }

  @SuppressWarnings("unchecked")
  @Override
  public Iterator<? extends Content> array() {
    return ((List<Object>) value).stream().map(ObjectContent::new).iterator();
  }

  @Override
  public boolean isNull() {
    return value == null;
  }

  @Override
  public boolean isNumber() {
    return value instanceof Number;
  }

  @Override
  public boolean isString() {
    return value instanceof String;
  }

  @Override
  public Pair<Double, Double> geoValue() {
    final String[] split = ((String) value).split(",");
    return Pair.of(Double.valueOf(split[0]), Double.valueOf(split[1]));
  }

  private <T> T parseNumberValue(Object value, Function<String, T> stringTFunction,
                                 Function<Number, T> numberTFunction) {
    if (value instanceof String) {
      return stringTFunction.apply((String) value);
    } else {
      return numberTFunction.apply((Number) value);
    }
  }
}
