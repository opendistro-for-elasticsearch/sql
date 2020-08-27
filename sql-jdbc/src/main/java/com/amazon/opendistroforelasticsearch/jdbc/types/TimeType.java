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

package com.amazon.opendistroforelasticsearch.jdbc.types;

import java.sql.SQLException;
import java.sql.Time;
import java.time.LocalTime;
import java.util.Map;

public class TimeType implements TypeHelper<Time>{

  public static final TimeType INSTANCE = new TimeType();

  private TimeType() {

  }

  @Override
  public Time fromValue(Object value, Map<String, Object> conversionParams) throws SQLException {
    if (value == null) {
      return null;
    }
    if (value instanceof Time) {
      return asTime((Time) value);
    } else if (value instanceof String) {
      return asTime((String) value);
    } else if (value instanceof Number) {
      return this.asTime((Number) value);
    } else {
      throw objectConversionException(value);
    }
  }

  public Time asTime(Time value) {
    return localTimetoSqlTime(value.toLocalTime());
  }

  public Time asTime(String value) throws SQLException {
    return localTimetoSqlTime(toLocalTime(value));
  }

  private Time localTimetoSqlTime(LocalTime localTime) {
    return new Time(localTime.getHour(), localTime.getMinute(), localTime.getSecond());
  }

  public Time asTime(Number value) {
    return new Time(value.longValue());
  }

  private LocalTime toLocalTime(String value) throws SQLException {
    if (value == null)
      throw stringConversionException(value, null);
    return LocalTime.parse(value);
  }

  @Override
  public String getTypeName() {
    return "Time";
  }
}
