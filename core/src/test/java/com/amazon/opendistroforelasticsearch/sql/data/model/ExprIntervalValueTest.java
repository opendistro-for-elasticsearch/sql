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

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTERVAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import java.time.Duration;
import java.time.Period;
import org.junit.jupiter.api.Test;

public class ExprIntervalValueTest {
  @Test
  public void equals_to_self() {
    ExprValue interval = ExprValueUtils.intervalValue(Duration.ofNanos(1000));
    assertEquals(ExprValueUtils.getIntervalValue(interval), Duration.ofNanos(1000));
  }

  @Test
  public void equal() {
    ExprValue v1 = new ExprIntervalValue(Duration.ofMinutes(1));
    ExprValue v2 = ExprValueUtils.intervalValue(Duration.ofSeconds(60));
    assertTrue(v1.equals(v2));
  }

  @Test
  public void compare() {
    ExprIntervalValue v1 = new ExprIntervalValue(Period.ofDays(1));
    ExprIntervalValue v2 = new ExprIntervalValue(Period.ofDays(2));
    assertEquals(v1.compare(v2), -1);
  }

  @Test
  public void invalid_compare() {
    ExprIntervalValue v1 = new ExprIntervalValue(Period.ofYears(1));
    ExprIntervalValue v2 = new ExprIntervalValue(Duration.ofHours(1));
    assertThrows(ExpressionEvaluationException.class, () -> v1.compare(v2),
        String.format("invalid to compare intervals with units %s and %s", v1.unit(), v2.unit()));
  }

  @Test
  public void value() {
    ExprValue value = new ExprIntervalValue(Period.ofWeeks(1));
    assertEquals(value.value(), Period.ofWeeks(1));
  }

  @Test
  public void type() {
    ExprValue interval = new ExprIntervalValue(Period.ofYears(1));
    assertEquals(interval.type(), INTERVAL);
  }
}
