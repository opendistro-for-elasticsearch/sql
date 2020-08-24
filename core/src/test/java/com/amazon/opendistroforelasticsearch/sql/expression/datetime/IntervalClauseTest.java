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

package com.amazon.opendistroforelasticsearch.sql.expression.datetime;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.intervalValue;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTERVAL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionTestBase;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import java.time.Duration;
import java.time.Period;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class IntervalClauseTest extends ExpressionTestBase {
  @Mock
  Environment<Expression, ExprValue> env;

  @Test
  public void microsecond() {
    FunctionExpression expr = dsl.interval(DSL.literal(1), DSL.literal("microsecond"));
    assertEquals(INTERVAL, expr.type());
    assertEquals(intervalValue(Duration.ofNanos(1000)), expr.valueOf(env));
  }

  @Test
  public void second() {
    FunctionExpression expr = dsl.interval(DSL.literal(1), DSL.literal("second"));
    assertEquals(INTERVAL, expr.type());
    assertEquals(intervalValue(Duration.ofSeconds(1)), expr.valueOf(env));
  }

  @Test
  public void minute() {
    FunctionExpression expr = dsl.interval(DSL.literal(1), DSL.literal("minute"));
    assertEquals(INTERVAL, expr.type());
    assertEquals(intervalValue(Duration.ofMinutes(1)), expr.valueOf(env));
  }

  @Test
  public void hour() {
    FunctionExpression expr = dsl.interval(DSL.literal(1), DSL.literal("HOUR"));
    assertEquals(INTERVAL, expr.type());
    assertEquals(intervalValue(Duration.ofHours(1)), expr.valueOf(env));
  }

  @Test
  public void day() {
    FunctionExpression expr = dsl.interval(DSL.literal(1), DSL.literal("day"));
    assertEquals(INTERVAL, expr.type());
    assertEquals(intervalValue(Duration.ofDays(1)), expr.valueOf(env));
  }

  @Test
  public void week() {
    FunctionExpression expr = dsl.interval(DSL.literal(1), DSL.literal("week"));
    assertEquals(INTERVAL, expr.type());
    assertEquals(intervalValue(Period.ofWeeks(1)), expr.valueOf(env));
  }

  @Test
  public void month() {
    FunctionExpression expr = dsl.interval(DSL.literal(1), DSL.literal("month"));
    assertEquals(INTERVAL, expr.type());
    assertEquals(intervalValue(Period.ofMonths(1)), expr.valueOf(env));
  }

  @Test
  public void quarter() {
    FunctionExpression expr = dsl.interval(DSL.literal(1), DSL.literal("quarter"));
    assertEquals(INTERVAL, expr.type());
    assertEquals(intervalValue(Period.ofMonths(3)), expr.valueOf(env));
  }

  @Test
  public void year() {
    FunctionExpression expr = dsl.interval(DSL.literal(1), DSL.literal("year"));
    assertEquals(INTERVAL, expr.type());
    assertEquals(intervalValue(Period.ofYears(1)), expr.valueOf(env));
  }

  @Test
  public void unsupported_unit() {
    FunctionExpression expr = dsl.interval(DSL.literal(1), DSL.literal("year_month"));
    assertThrows(ExpressionEvaluationException.class, () -> expr.valueOf(env),
        "interval unit year_month is not supported");
  }

  @Test
  public void to_string() {
    FunctionExpression expr = dsl.interval(DSL.literal(1), DSL.literal("day"));
    assertEquals("interval(1, \"day\")", expr.toString());
  }
}
