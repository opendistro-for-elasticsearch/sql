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
import com.amazon.opendistroforelasticsearch.sql.exception.SemanticCheckException;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

/**
 * Time Value.
 */
@EqualsAndHashCode
@RequiredArgsConstructor
public class ExprTimeValue implements ExprValue {
  /**
   * todo. only support UTC now.
   */
  private static final ZoneId ZONE = ZoneId.of("UTC");
  private final LocalTime time;

  /**
   * Constructor.
   */
  public ExprTimeValue(String time) {
    try {
      this.time = LocalTime.parse(time);
    } catch (DateTimeParseException e) {
      throw new SemanticCheckException(String.format("time:%s in unsupported format, please use "
          + "HH:mm:ss", time));
    }
  }

  @Override
  public String value() {
    return DateTimeFormatter.ISO_LOCAL_TIME.withZone(ZONE).format(time);
  }

  @Override
  public ExprCoreType type() {
    return ExprCoreType.TIME;
  }

  @Override
  public String toString() {
    return String.format("TIME '%s'", value());
  }
}
