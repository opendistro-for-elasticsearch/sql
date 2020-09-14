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

package com.amazon.opendistroforelasticsearch.sql.expression.datetime;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.integerValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.missingValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.nullValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.stringValue;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DATE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DATETIME;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.TIME;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.TIMESTAMP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprDateValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprDatetimeValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTimeValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTimestampValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionTestBase;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DateTimeFunctionTest extends ExpressionTestBase {
  @Mock
  Environment<Expression, ExprValue> env;

  @Mock
  Expression nullRef;

  @Mock
  Expression missingRef;

  @BeforeEach
  public void setup() {
    when(nullRef.valueOf(env)).thenReturn(nullValue());
    when(missingRef.valueOf(env)).thenReturn(missingValue());
  }

  @Test
  public void dayName() {
    when(nullRef.type()).thenReturn(DATE);
    when(missingRef.type()).thenReturn(DATE);

    FunctionExpression expression = dsl.dayname(DSL.literal(new ExprDateValue("2020-08-07")));
    assertEquals(STRING, expression.type());
    assertEquals("dayname(DATE '2020-08-07')", expression.toString());
    assertEquals(stringValue("Friday"), eval(expression));
    assertEquals(nullValue(), eval(dsl.dayname(nullRef)));
    assertEquals(missingValue(), eval(dsl.dayname(missingRef)));
  }

  @Test
  public void monthName() {
    when(nullRef.type()).thenReturn(DATE);
    when(missingRef.type()).thenReturn(DATE);

    FunctionExpression expression = dsl.monthname(DSL.literal(new ExprDateValue("2020-08-07")));
    assertEquals(STRING, expression.type());
    assertEquals("monthname(DATE '2020-08-07')", expression.toString());
    assertEquals(stringValue("August"), eval(expression));
    assertEquals(nullValue(), eval(dsl.monthname(nullRef)));
    assertEquals(missingValue(), eval(dsl.monthname(missingRef)));
  }

  @Test
  public void dayOfMonth() {
    when(nullRef.type()).thenReturn(DATE);
    when(missingRef.type()).thenReturn(DATE);

    FunctionExpression expression = dsl.dayofmonth(DSL.literal(new ExprDateValue("2020-08-07")));
    assertEquals(INTEGER, expression.type());
    assertEquals("dayofmonth(DATE '2020-08-07')", expression.toString());
    assertEquals(integerValue(7), eval(expression));
    assertEquals(nullValue(), eval(dsl.dayofmonth(nullRef)));
    assertEquals(missingValue(), eval(dsl.dayofmonth(missingRef)));
  }

  @Test
  public void dayOfWeek() {
    when(nullRef.type()).thenReturn(DATE);
    when(missingRef.type()).thenReturn(DATE);

    FunctionExpression expression = dsl.dayofweek(DSL.literal(new ExprDateValue("2020-08-07")));
    assertEquals(INTEGER, expression.type());
    assertEquals("dayofweek(DATE '2020-08-07')", expression.toString());
    assertEquals(integerValue(6), eval(expression));

    expression = dsl.dayofweek(DSL.literal(new ExprDateValue("2020-08-09")));
    assertEquals(INTEGER, expression.type());
    assertEquals("dayofweek(DATE '2020-08-09')", expression.toString());
    assertEquals(integerValue(1), eval(expression));

    assertEquals(nullValue(), eval(dsl.dayofweek(nullRef)));
    assertEquals(missingValue(), eval(dsl.dayofweek(missingRef)));
  }

  @Test
  public void dayOfYear() {
    when(nullRef.type()).thenReturn(DATE);
    when(missingRef.type()).thenReturn(DATE);

    FunctionExpression expression = dsl.dayofyear(DSL.literal(new ExprDateValue("2020-08-07")));
    assertEquals(INTEGER, expression.type());
    assertEquals("dayofyear(DATE '2020-08-07')", expression.toString());
    assertEquals(integerValue(220), eval(expression));
    assertEquals(nullValue(), eval(dsl.dayofyear(nullRef)));
    assertEquals(missingValue(), eval(dsl.dayofyear(missingRef)));
  }

  @Test
  public void day() {
    when(nullRef.type()).thenReturn(DATE);
    when(missingRef.type()).thenReturn(DATE);

    FunctionExpression expression = dsl.day(DSL.literal(new ExprDateValue("2020-08-07")));
    assertEquals(INTEGER, expression.type());
    assertEquals("day(DATE '2020-08-07')", expression.toString());
    assertEquals(integerValue(7), eval(expression));
    assertEquals(nullValue(), eval(dsl.day(nullRef)));
    assertEquals(missingValue(), eval(dsl.day(missingRef)));
  }

  @Test
  public void hour() {
    when(nullRef.type()).thenReturn(TIME);
    when(missingRef.type()).thenReturn(TIME);
    assertEquals(nullValue(), eval(dsl.hour(nullRef)));
    assertEquals(missingValue(), eval(dsl.hour(missingRef)));

    FunctionExpression expression = dsl.hour(DSL.literal(new ExprTimeValue("01:02:03")));
    assertEquals(INTEGER, expression.type());
    assertEquals(integerValue(1), eval(expression));
    assertEquals("hour(TIME '01:02:03')", expression.toString());

    expression = dsl.hour(DSL.literal(new ExprTimestampValue("2020-08-17 01:02:03")));
    assertEquals(INTEGER, expression.type());
    assertEquals(integerValue(1), expression.valueOf(env));
    assertEquals("hour(TIMESTAMP '2020-08-17 01:02:03')", expression.toString());

    expression = dsl.hour(DSL.literal(new ExprDatetimeValue("2020-08-17 01:02:03")));
    assertEquals(INTEGER, expression.type());
    assertEquals(integerValue(1), expression.valueOf(env));
    assertEquals("hour(DATETIME '2020-08-17 01:02:03')", expression.toString());
  }

  @Test
  public void minute() {
    when(nullRef.type()).thenReturn(TIME);
    when(missingRef.type()).thenReturn(TIME);
    assertEquals(nullValue(), eval(dsl.minute(nullRef)));
    assertEquals(missingValue(), eval(dsl.minute(missingRef)));

    FunctionExpression expression = dsl.minute(DSL.literal(new ExprTimeValue("01:02:03")));
    assertEquals(INTEGER, expression.type());
    assertEquals(integerValue(2), eval(expression));
    assertEquals("minute(TIME '01:02:03')", expression.toString());

    expression = dsl.minute(DSL.literal(new ExprTimestampValue("2020-08-17 01:02:03")));
    assertEquals(INTEGER, expression.type());
    assertEquals(integerValue(2), expression.valueOf(env));
    assertEquals("minute(TIMESTAMP '2020-08-17 01:02:03')", expression.toString());

    expression = dsl.minute(DSL.literal(new ExprDatetimeValue("2020-08-17 01:02:03")));
    assertEquals(INTEGER, expression.type());
    assertEquals(integerValue(2), expression.valueOf(env));
    assertEquals("minute(DATETIME '2020-08-17 01:02:03')", expression.toString());
  }

  @Test
  public void second() {
    when(nullRef.type()).thenReturn(TIME);
    when(missingRef.type()).thenReturn(TIME);
    assertEquals(nullValue(), eval(dsl.second(nullRef)));
    assertEquals(missingValue(), eval(dsl.second(missingRef)));

    FunctionExpression expression = dsl.second(DSL.literal(new ExprTimeValue("01:02:03")));
    assertEquals(INTEGER, expression.type());
    assertEquals(integerValue(3), eval(expression));
    assertEquals("second(TIME '01:02:03')", expression.toString());

    expression = dsl.second(DSL.literal(new ExprTimestampValue("2020-08-17 01:02:03")));
    assertEquals(INTEGER, expression.type());
    assertEquals(integerValue(3), expression.valueOf(env));
    assertEquals("second(TIMESTAMP '2020-08-17 01:02:03')", expression.toString());

    expression = dsl.second(DSL.literal(new ExprDatetimeValue("2020-08-17 01:02:03")));
    assertEquals(INTEGER, expression.type());
    assertEquals(integerValue(3), expression.valueOf(env));
    assertEquals("second(DATETIME '2020-08-17 01:02:03')", expression.toString());
  }

  @Test
  public void microsecond() {
    when(nullRef.type()).thenReturn(TIME);
    when(missingRef.type()).thenReturn(TIME);
    assertEquals(nullValue(), eval(dsl.microsecond(nullRef)));
    assertEquals(missingValue(), eval(dsl.microsecond(missingRef)));

    FunctionExpression expression = dsl
        .microsecond(DSL.literal(new ExprTimeValue("01:02:03.123456")));
    assertEquals(INTEGER, expression.type());
    assertEquals(integerValue(123456), eval(expression));
    assertEquals("microsecond(TIME '01:02:03.123456')", expression.toString());

    expression = dsl.microsecond(DSL.literal(new ExprTimeValue("01:02:03.00")));
    assertEquals(INTEGER, expression.type());
    assertEquals(integerValue(0), eval(expression));
    assertEquals("microsecond(TIME '01:02:03')", expression.toString());

    /*
    expression = dsl.microsecond(DSL.literal(new ExprTimestampValue("2020-08-17 01:02:03.123456")));
    assertEquals(INTEGER, expression.type());
    assertEquals(integerValue(123456), expression.valueOf(env));
    assertEquals("microsecond(TIMESTAMP '2020-08-17 01:02:03.123456')", expression.toString());

    expression = dsl.microsecond(DSL.literal(new ExprDatetimeValue("2020-08-17 01:02:03.123456")));
    assertEquals(INTEGER, expression.type());
    assertEquals(integerValue(123456), expression.valueOf(env));
    assertEquals("microsecond(DATETIME '2020-08-17 01:02:03.123456')", expression.toString());*/
  }

  @Test
  public void month() {
    when(nullRef.type()).thenReturn(DATE);
    when(missingRef.type()).thenReturn(DATE);

    FunctionExpression expression = dsl.month(DSL.literal(new ExprDateValue("2020-08-07")));
    assertEquals(INTEGER, expression.type());
    assertEquals("month(DATE '2020-08-07')", expression.toString());
    assertEquals(integerValue(8), eval(expression));
    assertEquals(nullValue(), eval(dsl.month(nullRef)));
    assertEquals(missingValue(), eval(dsl.month(missingRef)));
  }

  @Test
  public void quarter() {
    when(nullRef.type()).thenReturn(DATE);
    when(missingRef.type()).thenReturn(DATE);

    FunctionExpression expression = dsl.quarter(DSL.literal(new ExprDateValue("2020-08-07")));
    assertEquals(INTEGER, expression.type());
    assertEquals("quarter(DATE '2020-08-07')", expression.toString());
    assertEquals(integerValue(3), eval(expression));

    expression = dsl.quarter(DSL.literal(new ExprDateValue("2020-12-07")));
    assertEquals(INTEGER, expression.type());
    assertEquals("quarter(DATE '2020-12-07')", expression.toString());
    assertEquals(integerValue(4), eval(expression));

    assertEquals(nullValue(), eval(dsl.quarter(nullRef)));
    assertEquals(missingValue(), eval(dsl.quarter(missingRef)));
  }

  @Test
  public void year() {
    when(nullRef.type()).thenReturn(DATE);
    when(missingRef.type()).thenReturn(DATE);

    FunctionExpression expression = dsl.year(DSL.literal(new ExprDateValue("2020-08-07")));
    assertEquals(INTEGER, expression.type());
    assertEquals("year(DATE '2020-08-07')", expression.toString());
    assertEquals(integerValue(2020), eval(expression));
    assertEquals(nullValue(), eval(dsl.year(nullRef)));
    assertEquals(missingValue(), eval(dsl.year(missingRef)));
  }

  @Test
  public void date() {
    when(nullRef.type()).thenReturn(DATE);
    when(missingRef.type()).thenReturn(DATE);
    assertEquals(nullValue(), eval(dsl.date(nullRef)));
    assertEquals(missingValue(), eval(dsl.date(missingRef)));

    FunctionExpression expr = dsl.date(DSL.literal("2020-08-17"));
    assertEquals(DATE, expr.type());
    assertEquals(new ExprDateValue("2020-08-17"), eval(expr));
    assertEquals("date(\"2020-08-17\")", expr.toString());

    expr = dsl.date(DSL.literal(new ExprDateValue("2020-08-17")));
    assertEquals(DATE, expr.type());
    assertEquals(new ExprDateValue("2020-08-17"), eval(expr));
    assertEquals("date(DATE '2020-08-17')", expr.toString());
  }

  @Test
  public void time() {
    when(nullRef.type()).thenReturn(TIME);
    when(missingRef.type()).thenReturn(TIME);
    assertEquals(nullValue(), eval(dsl.time(nullRef)));
    assertEquals(missingValue(), eval(dsl.time(missingRef)));

    FunctionExpression expr = dsl.time(DSL.literal("01:01:01"));
    assertEquals(TIME, expr.type());
    assertEquals(new ExprTimeValue("01:01:01"), eval(expr));
    assertEquals("time(\"01:01:01\")", expr.toString());

    expr = dsl.time(DSL.literal(new ExprTimeValue("01:01:01")));
    assertEquals(TIME, expr.type());
    assertEquals(new ExprTimeValue("01:01:01"), eval(expr));
    assertEquals("time(TIME '01:01:01')", expr.toString());
  }

  @Test
  public void timestamp() {
    when(nullRef.type()).thenReturn(TIMESTAMP);
    when(missingRef.type()).thenReturn(TIMESTAMP);
    assertEquals(nullValue(), eval(dsl.timestamp(nullRef)));
    assertEquals(missingValue(), eval(dsl.timestamp(missingRef)));

    FunctionExpression expr = dsl.timestamp(DSL.literal("2020-08-17 01:01:01"));
    assertEquals(TIMESTAMP, expr.type());
    assertEquals(new ExprTimestampValue("2020-08-17 01:01:01"), expr.valueOf(env));
    assertEquals("timestamp(\"2020-08-17 01:01:01\")", expr.toString());

    expr = dsl.timestamp(DSL.literal(new ExprTimestampValue("2020-08-17 01:01:01")));
    assertEquals(TIMESTAMP, expr.type());
    assertEquals(new ExprTimestampValue("2020-08-17 01:01:01"), expr.valueOf(env));
    assertEquals("timestamp(TIMESTAMP '2020-08-17 01:01:01')", expr.toString());
  }

  private ExprValue eval(Expression expression) {
    return expression.valueOf(env);
  }
}