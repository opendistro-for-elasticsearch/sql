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

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Iterators;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

/**
 * The Implementation of Content to represent {@link JsonNode}.
 */
@RequiredArgsConstructor
public class ElasticsearchJsonContent implements Content {

  private final JsonNode value;

  @Override
  public Integer intValue() {
    return value().intValue();
  }

  @Override
  public Long longValue() {
    return value().longValue();
  }

  @Override
  public Short shortValue() {
    return value().shortValue();
  }

  @Override
  public Byte byteValue() {
    return (byte) value().shortValue();
  }

  @Override
  public Float floatValue() {
    return value().floatValue();
  }

  @Override
  public Double doubleValue() {
    return value().doubleValue();
  }

  @Override
  public String stringValue() {
    return value().asText();
  }

  @Override
  public Boolean booleanValue() {
    return value().booleanValue();
  }

  @Override
  public Iterator<Map.Entry<String, Content>> map() {
    LinkedHashMap<String, Content> map = new LinkedHashMap<>();
    final JsonNode mapValue = value();
    mapValue
        .fieldNames()
        .forEachRemaining(
            field -> map.put(field, new ElasticsearchJsonContent(mapValue.get(field))));
    return map.entrySet().iterator();
  }

  @Override
  public Iterator<? extends Content> array() {
    return Iterators.transform(value.elements(), ElasticsearchJsonContent::new);
  }

  @Override
  public boolean isNull() {
    return value == null || value.isNull();
  }

  @Override
  public boolean isNumber() {
    return value().isNumber();
  }

  @Override
  public boolean isString() {
    return value().isTextual();
  }

  @Override
  public Object objectValue() {
    return value();
  }

  @Override
  public Pair<Double, Double> geoValue() {
    return Pair.of(value().get("lat").doubleValue(), value().get("lon").doubleValue());
  }

  /**
   * Return the first element if is Elasticsearch Array.
   * https://www.elastic.co/guide/en/elasticsearch/reference/current/array.html.
   */
  private JsonNode value() {
    return value.isArray() ? value.get(0) : value;
  }
}
