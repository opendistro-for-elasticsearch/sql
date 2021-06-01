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
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;

/**
 * Expression Timestamp Value.
 */
@RequiredArgsConstructor
public class ExprTimestampValue extends AbstractExprValue {
  /**
   * todo. only support UTC now.
   */
  private static final ZoneId ZONE = ZoneId.of("UTC");
  /**
   * todo. only support timestamp in format yyyy-MM-dd HH:mm:ss.
   */
  private static final DateTimeFormatter FORMATTER_WITNOUT_NANO = DateTimeFormatter
      .ofPattern("yyyy-MM-dd HH:mm:ss");
  private final Instant timestamp;

  private static final DateTimeFormatter FORMATTER_VARIABLE_MICROS;
  private static final DateTimeFormatter FORMATTER_VARIABLE_MICROS_WITH_T;
  private static final DateTimeFormatter FORMATTER_VARIABLE_MICROS_WITH_T_Z;
  public static final List<DateTimeFormatter> DATE_FORMATS_ALLOWED;
  private static final int MIN_FRACTION_SECONDS = 0;
  private static final int MAX_FRACTION_SECONDS = 6;


  static {
    FORMATTER_VARIABLE_MICROS = new DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd HH:mm:ss")
        .appendFraction(
                ChronoField.MICRO_OF_SECOND,
                MIN_FRACTION_SECONDS,
                MAX_FRACTION_SECONDS,
                true)
        .toFormatter();
    FORMATTER_VARIABLE_MICROS_WITH_T = new DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd'T'HH:mm:ss")
        .appendFraction(
            ChronoField.MICRO_OF_SECOND,
            MIN_FRACTION_SECONDS,
            MAX_FRACTION_SECONDS,
            true)
        .toFormatter();

    FORMATTER_VARIABLE_MICROS_WITH_T_Z = new DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        .appendFraction(
            ChronoField.MICRO_OF_SECOND,
            MIN_FRACTION_SECONDS,
            MAX_FRACTION_SECONDS,
            true)
        .toFormatter();
    DATE_FORMATS_ALLOWED = Arrays.asList(
        FORMATTER_VARIABLE_MICROS,
        FORMATTER_VARIABLE_MICROS_WITH_T,
        FORMATTER_VARIABLE_MICROS_WITH_T_Z);
  }

  /**
   * Constructor.
   */
  public ExprTimestampValue(String timestamp) {
    Instant localTimestamp = null;
    for (DateTimeFormatter format : DATE_FORMATS_ALLOWED) {
      try {
        localTimestamp = LocalDateTime.parse(timestamp, format).atZone(ZONE).toInstant();
      } catch (DateTimeParseException ignored) {
        // ignored
      }
    }
    if (localTimestamp == null) {
      throw  new SemanticCheckException(String.format("timestamp:%s in unsupported format, please "
          + "use yyyy-MM-dd HH:mm:ss[.SSSSSS]", timestamp));
    }
    this.timestamp = localTimestamp;
  }

  @Override
  public String value() {
    return timestamp.getNano() == 0 ? FORMATTER_WITNOUT_NANO.withZone(ZONE)
        .format(timestamp.truncatedTo(ChronoUnit.SECONDS))
        : FORMATTER_VARIABLE_MICROS.withZone(ZONE).format(timestamp);
  }

  @Override
  public ExprType type() {
    return ExprCoreType.TIMESTAMP;
  }

  @Override
  public Instant timestampValue() {
    return timestamp;
  }

  @Override
  public LocalDate dateValue() {
    return timestamp.atZone(ZONE).toLocalDate();
  }

  @Override
  public LocalTime timeValue() {
    return timestamp.atZone(ZONE).toLocalTime();
  }

  @Override
  public LocalDateTime datetimeValue() {
    return timestamp.atZone(ZONE).toLocalDateTime();
  }

  @Override
  public String toString() {
    return String.format("TIMESTAMP '%s'", value());
  }

  @Override
  public int compare(ExprValue other) {
    return timestamp.compareTo(other.timestampValue().atZone(ZONE).toInstant());
  }

  @Override
  public boolean equal(ExprValue other) {
    return timestamp.equals(other.timestampValue().atZone(ZONE).toInstant());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(timestamp);
  }
}
