/*
 *
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.data.model;

import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.exception.SemanticCheckException;
import com.google.common.base.Objects;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.RequiredArgsConstructor;

/**
 * Expression Date Value.
 */
@RequiredArgsConstructor
public class ExprDateValue extends AbstractExprValue {

  private final LocalDate date;

  /**
   * Constructor of ExprDateValue.
   */
  public ExprDateValue(String date) {
    try {
      this.date = LocalDate.parse(date);
    } catch (DateTimeParseException e) {
      throw new SemanticCheckException(String.format("date:%s in unsupported format, please use "
          + "yyyy-MM-dd", date));
    }
  }

  @Override
  public String value() {
    return DateTimeFormatter.ISO_LOCAL_DATE.format(date);
  }

  @Override
  public ExprType type() {
    return ExprCoreType.DATE;
  }

  @Override
  public LocalDate dateValue() {
    return date;
  }

  @Override
  public LocalTime timeValue() {
    return LocalTime.of(0, 0, 0);
  }

  @Override
  public LocalDateTime datetimeValue() {
    return LocalDateTime.of(date, timeValue());
  }

  @Override
  public Instant timestampValue() {
    return ZonedDateTime.of(date, timeValue(), ZoneId.systemDefault()).toInstant();
  }

  @Override
  public String toString() {
    return String.format("DATE '%s'", value());
  }

  @Override
  public int compare(ExprValue other) {
    return date.compareTo(other.dateValue());
  }

  @Override
  public boolean equal(ExprValue other) {
    return date.equals(other.dateValue());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(date);
  }
}
