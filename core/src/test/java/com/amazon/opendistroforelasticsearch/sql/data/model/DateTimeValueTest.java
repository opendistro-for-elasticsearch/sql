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

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.integerValue;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.TIME;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.TIMESTAMP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.amazon.opendistroforelasticsearch.sql.exception.SemanticCheckException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

public class DateTimeValueTest {

  @Test
  public void timeValueInterfaceTest() {
    ExprValue timeValue = new ExprTimeValue("01:01:01");

    assertEquals(TIME, timeValue.type());
    assertEquals(LocalTime.parse("01:01:01"), timeValue.timeValue());
    assertEquals("01:01:01", timeValue.value());
    assertEquals("TIME '01:01:01'", timeValue.toString());
  }

  @Test
  public void timestampValueInterfaceTest() {
    ExprValue timestampValue = new ExprTimestampValue("2020-07-07 01:01:01");

    assertEquals(TIMESTAMP, timestampValue.type());
    assertEquals(Instant.ofEpochSecond(1594083661), timestampValue.timestampValue());
    assertEquals("2020-07-07 01:01:01", timestampValue.value());
    assertEquals("TIMESTAMP '2020-07-07 01:01:01'", timestampValue.toString());
  }

  @Test
  public void dateValueInterfaceTest() {
    ExprValue dateValue = new ExprDateValue("2012-07-07");

    assertEquals(LocalDate.parse("2012-07-07").atStartOfDay(ZoneId.of("UTC")),
        dateValue.dateValue());
    ExpressionEvaluationException exception =
        assertThrows(ExpressionEvaluationException.class,
            () -> ExprValueUtils.getDateValue(integerValue(1)));
    assertEquals("invalid to get dateValue from value of type INTEGER",
        exception.getMessage());
  }

  @Test
  public void dateInUnsupportedFormat() {
    SemanticCheckException exception =
        assertThrows(SemanticCheckException.class, () -> new ExprDateValue("2020-07-07Z"));
    assertEquals("date:2020-07-07Z in unsupported format, please use yyyy-MM-dd",
        exception.getMessage());
  }

  @Test
  public void timeInUnsupportedFormat() {
    SemanticCheckException exception =
        assertThrows(SemanticCheckException.class, () -> new ExprTimeValue("01:01:0"));
    assertEquals("time:01:01:0 in unsupported format, please use HH:mm:ss",
        exception.getMessage());
  }

  @Test
  public void timestampInUnsupportedFormat() {
    SemanticCheckException exception =
        assertThrows(SemanticCheckException.class,
            () -> new ExprTimestampValue("2020-07-07T01:01:01Z"));
    assertEquals(
        "timestamp:2020-07-07T01:01:01Z in unsupported format, please use yyyy-MM-dd HH:mm:ss",
        exception.getMessage());
  }
}
