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
import com.amazon.opendistroforelasticsearch.sql.exception.SemanticCheckException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

/**
 * Expression String Value.
 */
@RequiredArgsConstructor
public class ExprStringValue extends AbstractExprValue {
  private final String value;

  @Override
  public Object value() {
    return value;
  }

  @Override
  public ExprType type() {
    return ExprCoreType.STRING;
  }

  @Override
  public String stringValue() {
    return value;
  }

  @Override
  public LocalDateTime datetimeValue() {
    try {
      return new ExprDatetimeValue(value).datetimeValue();
    } catch (SemanticCheckException e) {
      try {
        return new ExprDatetimeValue(
            LocalDateTime.of(new ExprDateValue(value).dateValue(), LocalTime.of(0, 0, 0)))
            .datetimeValue();
      } catch (SemanticCheckException exception) {
        throw new SemanticCheckException(String.format("datetime:%s in unsupported format, please "
            + "use yyyy-MM-dd HH:mm:ss[.SSSSSS]", value));
      }
    }
  }

  @Override
  public LocalDate dateValue() {
    try {
      return new ExprDatetimeValue(value).dateValue();
    } catch (SemanticCheckException e) {
      return new ExprDateValue(value).dateValue();
    }
  }

  @Override
  public LocalTime timeValue() {
    try {
      return new ExprDatetimeValue(value).timeValue();
    } catch (SemanticCheckException e) {
      return new ExprTimeValue(value).timeValue();
    }
  }

  @Override
  public String toString() {
    return String.format("\"%s\"", value);
  }

  @Override
  public int compare(ExprValue other) {
    return value.compareTo(other.stringValue());
  }

  @Override
  public boolean equal(ExprValue other) {
    return value.equals(other.stringValue());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(value);
  }
}
