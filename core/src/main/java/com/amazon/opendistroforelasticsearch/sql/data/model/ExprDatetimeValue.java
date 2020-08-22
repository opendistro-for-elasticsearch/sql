/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

@RequiredArgsConstructor
public class ExprDatetimeValue extends AbstractExprValue {
  private static final DateTimeFormatter formatter = DateTimeFormatter
      .ofPattern("yyyy-MM-dd HH:mm:ss");
  private final LocalDateTime datetime;

  /**
   * Constructor with datetime string as input.
   */
  public ExprDatetimeValue(String datetime) {
    try {
      this.datetime = LocalDateTime.parse(datetime, formatter);
    } catch (DateTimeParseException e) {
      throw new SemanticCheckException(String.format("datetime:%s in unsupported format, please "
          + "use yyyy-MM-dd HH:mm:ss", datetime));
    }
  }

  @Override
  public LocalDateTime datetimeValue() {
    return datetime;
  }

  @Override
  public LocalDate dateValue() {
    return datetime.toLocalDate();
  }

  @Override
  public LocalTime timeValue() {
    return datetime.toLocalTime();
  }

  @Override
  public Instant timestampValue() {
    return ZonedDateTime.of(datetime, ZoneId.of("UTC")).toInstant();
  }

  @Override
  public int compare(ExprValue other) {
    return datetime.compareTo(other.datetimeValue());
  }

  @Override
  public boolean equal(ExprValue other) {
    return datetime.equals(other.datetimeValue());
  }

  @Override
  public String value() {
    return String.format("%s %s", DateTimeFormatter.ISO_DATE.format(datetime),
        DateTimeFormatter.ISO_TIME.format(datetime));
  }

  @Override
  public ExprType type() {
    return ExprCoreType.DATETIME;
  }

  @Override
  public String toString() {
    return String.format("DATETIME '%s'", value());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(datetime);
  }
}
