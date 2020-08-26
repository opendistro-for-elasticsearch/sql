/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.data.model;

import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.amazon.opendistroforelasticsearch.sql.storage.bindingtuple.BindingTuple;
import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Map;

/**
 * The definition of the Expression Value.
 */
public interface ExprValue extends Serializable, Comparable<ExprValue> {
  /**
   * Get the Object value of the Expression Value.
   */
  Object value();

  /**
   * Get the {@link ExprCoreType} of the Expression Value.
   */
  ExprType type();

  /**
   * Is null value.
   *
   * @return true: is null value, otherwise false
   */
  default boolean isNull() {
    return false;
  }

  /**
   * Is missing value.
   *
   * @return true: is missing value, otherwise false
   */
  default boolean isMissing() {
    return false;
  }

  /**
   * Is Number value.
   *
   * @return true: is number value, otherwise false
   */
  default boolean isNumber() {
    return false;
  }

  /**
   * Get the {@link BindingTuple}.
   */
  default BindingTuple bindingTuples() {
    return BindingTuple.EMPTY;
  }

  /**
   * Get integer value.
   */
  default Integer integerValue() {
    throw new ExpressionEvaluationException(
        "invalid to get integerValue from value of type " + type());
  }

  /**
   * Get long value.
   */
  default Long longValue() {
    throw new ExpressionEvaluationException(
        "invalid to get longValue from value of type " + type());
  }

  /**
   * Get float value.
   */
  default Float floatValue() {
    throw new ExpressionEvaluationException(
        "invalid to get floatValue from value of type " + type());
  }

  /**
   * Get float value.
   */
  default Double doubleValue() {
    throw new ExpressionEvaluationException(
        "invalid to get doubleValue from value of type " + type());
  }

  /**
   * Get string value.
   */
  default String stringValue() {
    throw new ExpressionEvaluationException(
        "invalid to get stringValue from value of type " + type());
  }

  /**
   * Get boolean value.
   */
  default Boolean booleanValue() {
    throw new ExpressionEvaluationException(
        "invalid to get booleanValue from value of type " + type());
  }

  /**
   * Get timestamp value.
   */
  default Instant timestampValue() {
    throw new ExpressionEvaluationException(
        "invalid to get timestampValue from value of type " + type());
  }

  /**
   * Get time value.
   */
  default LocalTime timeValue() {
    throw new ExpressionEvaluationException(
        "invalid to get timeValue from value of type " + type());
  }

  /**
   * Get date value.
   */
  default LocalDate dateValue() {
    throw new ExpressionEvaluationException(
        "invalid to get dateValue from value of type " + type());
  }

  /**
   * Get datetime value.
   */
  default LocalDateTime datetimeValue() {
    throw new ExpressionEvaluationException(
        "invalid to get datetimeValue from value of type " + type());
  }

  /**
   * Get interval value.
   */
  default TemporalAmount intervalValue() {
    throw new ExpressionEvaluationException(
        "invalid to get intervalValue from value of type " + type());
  }

  /**
   * Get map value.
   */
  default Map<String, ExprValue> tupleValue() {
    throw new ExpressionEvaluationException(
        "invalid to get tupleValue from value of type " + type());
  }

  /**
   * Get collection value.
   */
  default List<ExprValue> collectionValue() {
    throw new ExpressionEvaluationException(
        "invalid to get collectionValue from value of type " + type());
  }
}
