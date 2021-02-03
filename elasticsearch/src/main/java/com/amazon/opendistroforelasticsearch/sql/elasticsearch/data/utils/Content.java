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

import java.util.Iterator;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Regardless the underling data format, the {@link Content} define the data in abstract manner.
 * which could be parsed by ElasticsearchExprValueFactory.
 * There are two major use cases:
 * 1. Represent the JSON data retrieve from Elasticsearch search response.
 * 2. Represent the Object data extract from the Elasticsearch aggregation response.
 */
public interface Content {

  /**
   * Is null value.
   */
  boolean isNull();

  /**
   * Is number value.
   */
  boolean isNumber();

  /**
   * Is string value.
   */
  boolean isString();

  /**
   * Get integer value.
   */
  Integer intValue();

  /**
   * Get long value.
   */
  Long longValue();

  /**
   * Get short value.
   */
  Short shortValue();

  /**
   * Get byte value.
   */
  Byte byteValue();

  /**
   * Get float value.
   */
  Float floatValue();

  /**
   * Get double value.
   */
  Double doubleValue();

  /**
   * Get string value.
   */
  String stringValue();

  /**
   * Get boolean value.
   */
  Boolean booleanValue();

  /**
   * Get map of {@link Content} value.
   */
  Iterator<Map.Entry<String, Content>> map();

  /**
   * Get array of {@link Content} value.
   */
  Iterator<? extends Content> array();

  /**
   * Get geo point value.
   */
  Pair<Double, Double> geoValue();

  /**
   * Get {@link Object} value.
   */
  Object objectValue();
}
