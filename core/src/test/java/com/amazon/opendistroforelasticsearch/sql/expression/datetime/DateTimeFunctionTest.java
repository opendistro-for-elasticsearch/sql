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
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.longValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.missingValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.nullValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.stringValue;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DATE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DATETIME;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTERVAL;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.LONG;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.TIME;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.TIMESTAMP;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprDateValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprDatetimeValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprLongValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTimeValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTimestampValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.exception.SemanticCheckException;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionTestBase;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.AllArgsConstructor;
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

  final List<DateFormatTester> dateFormatTesters = ImmutableList.of(
      new DateFormatTester("1998-01-31 13:14:15.012345",
          ImmutableList.of("%H","%I","%k","%l","%i","%p","%r","%S","%T"," %M",
              "%W","%D","%Y","%y","%a","%b","%j","%m","%d","%h","%s","%w","%f",
              "%q","%"),
          ImmutableList.of("13","01","13","1","14","PM","01:14:15 PM","15","13:14:15"," January",
              "Saturday","31st","1998","98","Sat","Jan","031","01","31","01","15","6","12345",
              "q","%")
      ),
      new DateFormatTester("1999-12-01",
          ImmutableList.of("%D"),
          ImmutableList.of("1st")
      ),
      new DateFormatTester("1999-12-02",
          ImmutableList.of("%D"),
          ImmutableList.of("2nd")
      ),
      new DateFormatTester("1999-12-03",
          ImmutableList.of("%D"),
          ImmutableList.of("3rd")
      ),
      new DateFormatTester("1999-12-04",
          ImmutableList.of("%D"),
          ImmutableList.of("4th")
      ),
      new DateFormatTester("1999-12-11",
          ImmutableList.of("%D"),
          ImmutableList.of("11th")
      ),
      new DateFormatTester("1999-12-12",
          ImmutableList.of("%D"),
          ImmutableList.of("12th")
      ),
      new DateFormatTester("1999-12-13",
          ImmutableList.of("%D"),
          ImmutableList.of("13th")
      ),
      new DateFormatTester("1999-12-31",
          ImmutableList.of("%x","%v","%X","%V","%u","%U"),
          ImmutableList.of("1999", "52", "1999", "52", "52", "52")
      ),
      new DateFormatTester("2000-01-01",
          ImmutableList.of("%x","%v","%X","%V","%u","%U"),
          ImmutableList.of("1999", "52", "1999", "52", "0", "0")
      ),
      new DateFormatTester("1998-12-31",
          ImmutableList.of("%x","%v","%X","%V","%u","%U"),
          ImmutableList.of("1998", "52", "1998", "52", "52", "52")
      ),
      new DateFormatTester("1999-01-01",
          ImmutableList.of("%x","%v","%X","%V","%u","%U"),
          ImmutableList.of("1998", "52", "1998", "52", "0", "0")
      ),
      new DateFormatTester("2020-01-04",
          ImmutableList.of("%x","%X"),
          ImmutableList.of("2020", "2019")
      ),
      new DateFormatTester("2008-12-31",
          ImmutableList.of("%v","%V","%u","%U"),
          ImmutableList.of("53","52","53","52")
      )
  );

  @AllArgsConstructor
  private class DateFormatTester {
    private final String date;
    private final List<String> formatterList;
    private final List<String> formattedList;
    private static final String DELIMITER = "|";

    String getFormatter() {
      return String.join(DELIMITER, formatterList);
    }

    String getFormatted() {
      return String.join(DELIMITER, formattedList);
    }

    FunctionExpression getDateFormatExpression() {
      return dsl.date_format(DSL.literal(date), DSL.literal(getFormatter()));
    }
  }

  @Test
  public void adddate() {
    FunctionExpression expr = dsl.adddate(dsl.date(DSL.literal("2020-08-26")), DSL.literal(7));
    assertEquals(DATE, expr.type());
    assertEquals(new ExprDateValue("2020-09-02"), expr.valueOf(env));
    assertEquals("adddate(date(\"2020-08-26\"), 7)", expr.toString());

    expr = dsl.adddate(dsl.timestamp(DSL.literal("2020-08-26 12:05:00")), DSL.literal(7));
    assertEquals(DATETIME, expr.type());
    assertEquals(new ExprDatetimeValue("2020-09-02 12:05:00"), expr.valueOf(env));
    assertEquals("adddate(timestamp(\"2020-08-26 12:05:00\"), 7)", expr.toString());

    expr = dsl.adddate(
        dsl.date(DSL.literal("2020-08-26")), dsl.interval(DSL.literal(1), DSL.literal("hour")));
    assertEquals(DATETIME, expr.type());
    assertEquals(new ExprDatetimeValue("2020-08-26 01:00:00"), expr.valueOf(env));
    assertEquals("adddate(date(\"2020-08-26\"), interval(1, \"hour\"))", expr.toString());

    expr = dsl.adddate(DSL.literal("2020-08-26"), DSL.literal(7));
    assertEquals(DATETIME, expr.type());
    assertEquals(new ExprDateValue("2020-09-02"), expr.valueOf(env));
    assertEquals("adddate(\"2020-08-26\", 7)", expr.toString());

    expr = dsl.adddate(DSL.literal("2020-08-26 12:05:00"), DSL.literal(7));
    assertEquals(DATETIME, expr.type());
    assertEquals(new ExprDatetimeValue("2020-09-02 12:05:00"), expr.valueOf(env));
    assertEquals("adddate(\"2020-08-26 12:05:00\", 7)", expr.toString());

    expr = dsl
        .adddate(DSL.literal("2020-08-26"), dsl.interval(DSL.literal(1), DSL.literal("hour")));
    assertEquals(DATETIME, expr.type());
    assertEquals(new ExprDatetimeValue("2020-08-26 01:00:00"), expr.valueOf(env));
    assertEquals("adddate(\"2020-08-26\", interval(1, \"hour\"))", expr.toString());

    expr = dsl
        .adddate(DSL.literal("2020-08-26"), dsl.interval(DSL.literal(1), DSL.literal("day")));
    assertEquals(DATETIME, expr.type());
    assertEquals(new ExprDateValue("2020-08-27"), expr.valueOf(env));
    assertEquals("adddate(\"2020-08-26\", interval(1, \"day\"))", expr.toString());

    when(nullRef.type()).thenReturn(DATE);
    assertEquals(nullValue(), eval(dsl.adddate(nullRef, DSL.literal(1L))));
    assertEquals(nullValue(),
        eval(dsl.adddate(nullRef, dsl.interval(DSL.literal(1), DSL.literal("month")))));

    when(missingRef.type()).thenReturn(DATE);
    assertEquals(missingValue(), eval(dsl.adddate(missingRef, DSL.literal(1L))));
    assertEquals(missingValue(),
        eval(dsl.adddate(missingRef, dsl.interval(DSL.literal(1), DSL.literal("month")))));

    when(nullRef.type()).thenReturn(LONG);
    when(missingRef.type()).thenReturn(LONG);
    assertEquals(nullValue(), eval(dsl.adddate(dsl.date(DSL.literal("2020-08-26")), nullRef)));
    assertEquals(missingValue(),
        eval(dsl.adddate(dsl.date(DSL.literal("2020-08-26")), missingRef)));

    when(nullRef.type()).thenReturn(INTERVAL);
    when(missingRef.type()).thenReturn(INTERVAL);
    assertEquals(nullValue(), eval(dsl.adddate(dsl.date(DSL.literal("2020-08-26")), nullRef)));
    assertEquals(missingValue(),
        eval(dsl.adddate(dsl.date(DSL.literal("2020-08-26")), missingRef)));

    when(nullRef.type()).thenReturn(DATE);
    when(missingRef.type()).thenReturn(INTERVAL);
    assertEquals(missingValue(), eval(dsl.adddate(nullRef, missingRef)));
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
  public void date_add() {
    FunctionExpression expr = dsl.date_add(dsl.date(DSL.literal("2020-08-26")), DSL.literal(7));
    assertEquals(DATE, expr.type());
    assertEquals(new ExprDateValue("2020-09-02"), expr.valueOf(env));
    assertEquals("date_add(date(\"2020-08-26\"), 7)", expr.toString());

    expr = dsl.date_add(DSL.literal("2020-08-26 12:05:00"), DSL.literal(7));
    assertEquals(DATETIME, expr.type());
    assertEquals(new ExprDatetimeValue("2020-09-02 12:05:00"), expr.valueOf(env));
    assertEquals("date_add(\"2020-08-26 12:05:00\", 7)", expr.toString());

    expr = dsl.date_add(dsl.timestamp(DSL.literal("2020-08-26 12:05:00")), DSL.literal(7));
    assertEquals(DATETIME, expr.type());
    assertEquals(new ExprDatetimeValue("2020-09-02 12:05:00"), expr.valueOf(env));
    assertEquals("date_add(timestamp(\"2020-08-26 12:05:00\"), 7)", expr.toString());

    expr = dsl.date_add(
        dsl.date(DSL.literal("2020-08-26")), dsl.interval(DSL.literal(1), DSL.literal("hour")));
    assertEquals(DATETIME, expr.type());
    assertEquals(new ExprDatetimeValue("2020-08-26 01:00:00"), expr.valueOf(env));
    assertEquals("date_add(date(\"2020-08-26\"), interval(1, \"hour\"))", expr.toString());

    expr = dsl
        .date_add(DSL.literal("2020-08-26"), dsl.interval(DSL.literal(1), DSL.literal("hour")));
    assertEquals(DATETIME, expr.type());
    assertEquals(new ExprDatetimeValue("2020-08-26 01:00:00"), expr.valueOf(env));
    assertEquals("date_add(\"2020-08-26\", interval(1, \"hour\"))", expr.toString());

    when(nullRef.type()).thenReturn(DATE);
    assertEquals(nullValue(), eval(dsl.date_add(nullRef, DSL.literal(1L))));
    assertEquals(nullValue(),
        eval(dsl.date_add(nullRef, dsl.interval(DSL.literal(1), DSL.literal("month")))));

    when(missingRef.type()).thenReturn(DATE);
    assertEquals(missingValue(), eval(dsl.date_add(missingRef, DSL.literal(1L))));
    assertEquals(missingValue(),
        eval(dsl.date_add(missingRef, dsl.interval(DSL.literal(1), DSL.literal("month")))));

    when(nullRef.type()).thenReturn(LONG);
    when(missingRef.type()).thenReturn(LONG);
    assertEquals(nullValue(), eval(dsl.date_add(dsl.date(DSL.literal("2020-08-26")), nullRef)));
    assertEquals(missingValue(),
        eval(dsl.date_add(dsl.date(DSL.literal("2020-08-26")), missingRef)));

    when(nullRef.type()).thenReturn(INTERVAL);
    when(missingRef.type()).thenReturn(INTERVAL);
    assertEquals(nullValue(), eval(dsl.date_add(dsl.date(DSL.literal("2020-08-26")), nullRef)));
    assertEquals(missingValue(),
        eval(dsl.date_add(dsl.date(DSL.literal("2020-08-26")), missingRef)));

    when(nullRef.type()).thenReturn(DATE);
    when(missingRef.type()).thenReturn(INTERVAL);
    assertEquals(missingValue(), eval(dsl.date_add(nullRef, missingRef)));
  }

  @Test
  public void date_sub() {
    FunctionExpression expr = dsl.date_sub(dsl.date(DSL.literal("2020-08-26")), DSL.literal(7));
    assertEquals(DATE, expr.type());
    assertEquals(new ExprDateValue("2020-08-19"), expr.valueOf(env));
    assertEquals("date_sub(date(\"2020-08-26\"), 7)", expr.toString());

    expr = dsl.date_sub(DSL.literal("2020-08-26"), DSL.literal(7));
    assertEquals(DATETIME, expr.type());
    assertEquals(new ExprDateValue("2020-08-19"), expr.valueOf(env));
    assertEquals("date_sub(\"2020-08-26\", 7)", expr.toString());

    expr = dsl.date_sub(dsl.timestamp(DSL.literal("2020-08-26 12:05:00")), DSL.literal(7));
    assertEquals(DATETIME, expr.type());
    assertEquals(new ExprDatetimeValue("2020-08-19 12:05:00"), expr.valueOf(env));
    assertEquals("date_sub(timestamp(\"2020-08-26 12:05:00\"), 7)", expr.toString());

    expr = dsl.date_sub(DSL.literal("2020-08-26 12:05:00"), DSL.literal(7));
    assertEquals(DATETIME, expr.type());
    assertEquals(new ExprDatetimeValue("2020-08-19 12:05:00"), expr.valueOf(env));
    assertEquals("date_sub(\"2020-08-26 12:05:00\", 7)", expr.toString());

    expr = dsl.date_sub(dsl.timestamp(DSL.literal("2020-08-26 12:05:00")),
        dsl.interval(DSL.literal(1), DSL.literal("hour")));
    assertEquals(DATETIME, expr.type());
    assertEquals(new ExprDatetimeValue("2020-08-26 11:05:00"), expr.valueOf(env));
    assertEquals("date_sub(timestamp(\"2020-08-26 12:05:00\"), interval(1, \"hour\"))",
        expr.toString());

    expr = dsl.date_sub(DSL.literal("2020-08-26 12:05:00"),
        dsl.interval(DSL.literal(1), DSL.literal("year")));
    assertEquals(DATETIME, expr.type());
    assertEquals(new ExprDatetimeValue("2019-08-26 12:05:00"), expr.valueOf(env));
    assertEquals("date_sub(\"2020-08-26 12:05:00\", interval(1, \"year\"))",
        expr.toString());

    when(nullRef.type()).thenReturn(DATE);
    assertEquals(nullValue(), eval(dsl.date_sub(nullRef, DSL.literal(1L))));
    assertEquals(nullValue(),
        eval(dsl.date_sub(nullRef, dsl.interval(DSL.literal(1), DSL.literal("month")))));

    when(missingRef.type()).thenReturn(DATE);
    assertEquals(missingValue(), eval(dsl.date_sub(missingRef, DSL.literal(1L))));
    assertEquals(missingValue(),
        eval(dsl.date_sub(missingRef, dsl.interval(DSL.literal(1), DSL.literal("month")))));

    when(nullRef.type()).thenReturn(LONG);
    when(missingRef.type()).thenReturn(LONG);
    assertEquals(nullValue(), eval(dsl.date_sub(dsl.date(DSL.literal("2020-08-26")), nullRef)));
    assertEquals(missingValue(),
        eval(dsl.date_sub(dsl.date(DSL.literal("2020-08-26")), missingRef)));

    when(nullRef.type()).thenReturn(INTERVAL);
    when(missingRef.type()).thenReturn(INTERVAL);
    assertEquals(nullValue(), eval(dsl.date_sub(dsl.date(DSL.literal("2020-08-26")), nullRef)));
    assertEquals(missingValue(),
        eval(dsl.date_sub(dsl.date(DSL.literal("2020-08-26")), missingRef)));

    when(nullRef.type()).thenReturn(DATE);
    when(missingRef.type()).thenReturn(INTERVAL);
    assertEquals(missingValue(), eval(dsl.date_sub(nullRef, missingRef)));
  }

  @Test
  public void day() {
    when(nullRef.type()).thenReturn(DATE);
    when(missingRef.type()).thenReturn(DATE);
    assertEquals(nullValue(), eval(dsl.day(nullRef)));
    assertEquals(missingValue(), eval(dsl.day(missingRef)));

    FunctionExpression expression = dsl.day(DSL.literal(new ExprDateValue("2020-08-07")));
    assertEquals(INTEGER, expression.type());
    assertEquals("day(DATE '2020-08-07')", expression.toString());
    assertEquals(integerValue(7), eval(expression));

    expression = dsl.day(DSL.literal("2020-08-07"));
    assertEquals(INTEGER, expression.type());
    assertEquals("day(\"2020-08-07\")", expression.toString());
    assertEquals(integerValue(7), eval(expression));
  }

  @Test
  public void dayName() {
    when(nullRef.type()).thenReturn(DATE);
    when(missingRef.type()).thenReturn(DATE);
    assertEquals(nullValue(), eval(dsl.dayname(nullRef)));
    assertEquals(missingValue(), eval(dsl.dayname(missingRef)));

    FunctionExpression expression = dsl.dayname(DSL.literal(new ExprDateValue("2020-08-07")));
    assertEquals(STRING, expression.type());
    assertEquals("dayname(DATE '2020-08-07')", expression.toString());
    assertEquals(stringValue("Friday"), eval(expression));

    expression = dsl.dayname(DSL.literal("2020-08-07"));
    assertEquals(STRING, expression.type());
    assertEquals("dayname(\"2020-08-07\")", expression.toString());
    assertEquals(stringValue("Friday"), eval(expression));
  }

  @Test
  public void dayOfMonth() {
    when(nullRef.type()).thenReturn(DATE);
    when(missingRef.type()).thenReturn(DATE);
    assertEquals(nullValue(), eval(dsl.dayofmonth(nullRef)));
    assertEquals(missingValue(), eval(dsl.dayofmonth(missingRef)));

    FunctionExpression expression = dsl.dayofmonth(DSL.literal(new ExprDateValue("2020-08-07")));
    assertEquals(INTEGER, expression.type());
    assertEquals("dayofmonth(DATE '2020-08-07')", expression.toString());
    assertEquals(integerValue(7), eval(expression));

    expression = dsl.dayofmonth(DSL.literal("2020-07-08"));
    assertEquals(INTEGER, expression.type());
    assertEquals("dayofmonth(\"2020-07-08\")", expression.toString());
    assertEquals(integerValue(8), eval(expression));
  }

  @Test
  public void dayOfWeek() {
    when(nullRef.type()).thenReturn(DATE);
    when(missingRef.type()).thenReturn(DATE);
    assertEquals(nullValue(), eval(dsl.dayofweek(nullRef)));
    assertEquals(missingValue(), eval(dsl.dayofweek(missingRef)));

    FunctionExpression expression = dsl.dayofweek(DSL.literal(new ExprDateValue("2020-08-07")));
    assertEquals(INTEGER, expression.type());
    assertEquals("dayofweek(DATE '2020-08-07')", expression.toString());
    assertEquals(integerValue(6), eval(expression));

    expression = dsl.dayofweek(DSL.literal(new ExprDateValue("2020-08-09")));
    assertEquals(INTEGER, expression.type());
    assertEquals("dayofweek(DATE '2020-08-09')", expression.toString());
    assertEquals(integerValue(1), eval(expression));

    expression = dsl.dayofweek(DSL.literal("2020-08-09"));
    assertEquals(INTEGER, expression.type());
    assertEquals("dayofweek(\"2020-08-09\")", expression.toString());
    assertEquals(integerValue(1), eval(expression));

    expression = dsl.dayofweek(DSL.literal("2020-08-09 01:02:03"));
    assertEquals(INTEGER, expression.type());
    assertEquals("dayofweek(\"2020-08-09 01:02:03\")", expression.toString());
    assertEquals(integerValue(1), eval(expression));
  }

  @Test
  public void dayOfYear() {
    when(nullRef.type()).thenReturn(DATE);
    when(missingRef.type()).thenReturn(DATE);
    assertEquals(nullValue(), eval(dsl.dayofyear(nullRef)));
    assertEquals(missingValue(), eval(dsl.dayofyear(missingRef)));

    FunctionExpression expression = dsl.dayofyear(DSL.literal(new ExprDateValue("2020-08-07")));
    assertEquals(INTEGER, expression.type());
    assertEquals("dayofyear(DATE '2020-08-07')", expression.toString());
    assertEquals(integerValue(220), eval(expression));

    expression = dsl.dayofyear(DSL.literal("2020-08-07"));
    assertEquals(INTEGER, expression.type());
    assertEquals("dayofyear(\"2020-08-07\")", expression.toString());
    assertEquals(integerValue(220), eval(expression));

    expression = dsl.dayofyear(DSL.literal("2020-08-07 01:02:03"));
    assertEquals(INTEGER, expression.type());
    assertEquals("dayofyear(\"2020-08-07 01:02:03\")", expression.toString());
    assertEquals(integerValue(220), eval(expression));
  }

  @Test
  public void from_days() {
    when(nullRef.type()).thenReturn(LONG);
    when(missingRef.type()).thenReturn(LONG);
    assertEquals(nullValue(), eval(dsl.from_days(nullRef)));
    assertEquals(missingValue(), eval(dsl.from_days(missingRef)));

    FunctionExpression expression = dsl.from_days(DSL.literal(new ExprLongValue(730669)));
    assertEquals(DATE, expression.type());
    assertEquals("from_days(730669)", expression.toString());
    assertEquals(new ExprDateValue("2000-07-03"), expression.valueOf(env));
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

    expression = dsl.hour(DSL.literal("01:02:03"));
    assertEquals(INTEGER, expression.type());
    assertEquals(integerValue(1), eval(expression));
    assertEquals("hour(\"01:02:03\")", expression.toString());

    expression = dsl.hour(DSL.literal(new ExprTimestampValue("2020-08-17 01:02:03")));
    assertEquals(INTEGER, expression.type());
    assertEquals(integerValue(1), expression.valueOf(env));
    assertEquals("hour(TIMESTAMP '2020-08-17 01:02:03')", expression.toString());

    expression = dsl.hour(DSL.literal(new ExprDatetimeValue("2020-08-17 01:02:03")));
    assertEquals(INTEGER, expression.type());
    assertEquals(integerValue(1), expression.valueOf(env));
    assertEquals("hour(DATETIME '2020-08-17 01:02:03')", expression.toString());

    expression = dsl.hour(DSL.literal("2020-08-17 01:02:03"));
    assertEquals(INTEGER, expression.type());
    assertEquals(integerValue(1), expression.valueOf(env));
    assertEquals("hour(\"2020-08-17 01:02:03\")", expression.toString());
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

    expression = dsl.microsecond(DSL.literal("01:02:03.12"));
    assertEquals(INTEGER, expression.type());
    assertEquals(integerValue(120000), eval(expression));
    assertEquals("microsecond(\"01:02:03.12\")", expression.toString());

    expression = dsl.microsecond(DSL.literal(new ExprDatetimeValue("2020-08-17 01:02:03.000010")));
    assertEquals(INTEGER, expression.type());
    assertEquals(integerValue(10), expression.valueOf(env));
    assertEquals("microsecond(DATETIME '2020-08-17 01:02:03.00001')", expression.toString());

    expression = dsl.microsecond(DSL.literal(new ExprDatetimeValue("2020-08-17 01:02:03.123456")));
    assertEquals(INTEGER, expression.type());
    assertEquals(integerValue(123456), expression.valueOf(env));
    assertEquals("microsecond(DATETIME '2020-08-17 01:02:03.123456')", expression.toString());

    expression = dsl.microsecond(DSL.literal("2020-08-17 01:02:03.123456"));
    assertEquals(INTEGER, expression.type());
    assertEquals(integerValue(123456), expression.valueOf(env));
    assertEquals("microsecond(\"2020-08-17 01:02:03.123456\")", expression.toString());

    expression = dsl.microsecond(DSL.literal(new ExprTimestampValue("2020-08-17 01:02:03.000010")));
    assertEquals(INTEGER, expression.type());
    assertEquals(integerValue(10), expression.valueOf(env));
    assertEquals("microsecond(TIMESTAMP '2020-08-17 01:02:03.000010')", expression.toString());
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

    expression = dsl.minute(DSL.literal("01:02:03"));
    assertEquals(INTEGER, expression.type());
    assertEquals(integerValue(2), eval(expression));
    assertEquals("minute(\"01:02:03\")", expression.toString());

    expression = dsl.minute(DSL.literal(new ExprTimestampValue("2020-08-17 01:02:03")));
    assertEquals(INTEGER, expression.type());
    assertEquals(integerValue(2), expression.valueOf(env));
    assertEquals("minute(TIMESTAMP '2020-08-17 01:02:03')", expression.toString());

    expression = dsl.minute(DSL.literal(new ExprDatetimeValue("2020-08-17 01:02:03")));
    assertEquals(INTEGER, expression.type());
    assertEquals(integerValue(2), expression.valueOf(env));
    assertEquals("minute(DATETIME '2020-08-17 01:02:03')", expression.toString());

    expression = dsl.minute(DSL.literal("2020-08-17 01:02:03"));
    assertEquals(INTEGER, expression.type());
    assertEquals(integerValue(2), expression.valueOf(env));
    assertEquals("minute(\"2020-08-17 01:02:03\")", expression.toString());
  }

  @Test
  public void month() {
    when(nullRef.type()).thenReturn(DATE);
    when(missingRef.type()).thenReturn(DATE);
    assertEquals(nullValue(), eval(dsl.month(nullRef)));
    assertEquals(missingValue(), eval(dsl.month(missingRef)));

    FunctionExpression expression = dsl.month(DSL.literal(new ExprDateValue("2020-08-07")));
    assertEquals(INTEGER, expression.type());
    assertEquals("month(DATE '2020-08-07')", expression.toString());
    assertEquals(integerValue(8), eval(expression));

    expression = dsl.month(DSL.literal("2020-08-07"));
    assertEquals(INTEGER, expression.type());
    assertEquals("month(\"2020-08-07\")", expression.toString());
    assertEquals(integerValue(8), eval(expression));

    expression = dsl.month(DSL.literal("2020-08-07 01:02:03"));
    assertEquals(INTEGER, expression.type());
    assertEquals("month(\"2020-08-07 01:02:03\")", expression.toString());
    assertEquals(integerValue(8), eval(expression));
  }

  @Test
  public void monthName() {
    when(nullRef.type()).thenReturn(DATE);
    when(missingRef.type()).thenReturn(DATE);
    assertEquals(nullValue(), eval(dsl.monthname(nullRef)));
    assertEquals(missingValue(), eval(dsl.monthname(missingRef)));

    FunctionExpression expression = dsl.monthname(DSL.literal(new ExprDateValue("2020-08-07")));
    assertEquals(STRING, expression.type());
    assertEquals("monthname(DATE '2020-08-07')", expression.toString());
    assertEquals(stringValue("August"), eval(expression));

    expression = dsl.monthname(DSL.literal("2020-08-07"));
    assertEquals(STRING, expression.type());
    assertEquals("monthname(\"2020-08-07\")", expression.toString());
    assertEquals(stringValue("August"), eval(expression));

    expression = dsl.monthname(DSL.literal("2020-08-07 01:02:03"));
    assertEquals(STRING, expression.type());
    assertEquals("monthname(\"2020-08-07 01:02:03\")", expression.toString());
    assertEquals(stringValue("August"), eval(expression));
  }

  @Test
  public void quarter() {
    when(nullRef.type()).thenReturn(DATE);
    when(missingRef.type()).thenReturn(DATE);
    assertEquals(nullValue(), eval(dsl.quarter(nullRef)));
    assertEquals(missingValue(), eval(dsl.quarter(missingRef)));

    FunctionExpression expression = dsl.quarter(DSL.literal(new ExprDateValue("2020-08-07")));
    assertEquals(INTEGER, expression.type());
    assertEquals("quarter(DATE '2020-08-07')", expression.toString());
    assertEquals(integerValue(3), eval(expression));

    expression = dsl.quarter(DSL.literal("2020-12-07"));
    assertEquals(INTEGER, expression.type());
    assertEquals("quarter(\"2020-12-07\")", expression.toString());
    assertEquals(integerValue(4), eval(expression));

    expression = dsl.quarter(DSL.literal("2020-12-07 01:02:03"));
    assertEquals(INTEGER, expression.type());
    assertEquals("quarter(\"2020-12-07 01:02:03\")", expression.toString());
    assertEquals(integerValue(4), eval(expression));
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

    expression = dsl.second(DSL.literal("01:02:03"));
    assertEquals(INTEGER, expression.type());
    assertEquals(integerValue(3), eval(expression));
    assertEquals("second(\"01:02:03\")", expression.toString());

    expression = dsl.second(DSL.literal("2020-08-17 01:02:03"));
    assertEquals(INTEGER, expression.type());
    assertEquals(integerValue(3), eval(expression));
    assertEquals("second(\"2020-08-17 01:02:03\")", expression.toString());

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
  public void subdate() {
    FunctionExpression expr = dsl.subdate(dsl.date(DSL.literal("2020-08-26")), DSL.literal(7));
    assertEquals(DATE, expr.type());
    assertEquals(new ExprDateValue("2020-08-19"), expr.valueOf(env));
    assertEquals("subdate(date(\"2020-08-26\"), 7)", expr.toString());

    expr = dsl.subdate(DSL.literal("2020-08-26"), DSL.literal(7));
    assertEquals(DATETIME, expr.type());
    assertEquals(new ExprDateValue("2020-08-19"), expr.valueOf(env));
    assertEquals("subdate(\"2020-08-26\", 7)", expr.toString());

    expr = dsl.subdate(dsl.timestamp(DSL.literal("2020-08-26 12:05:00")), DSL.literal(7));
    assertEquals(DATETIME, expr.type());
    assertEquals(new ExprDatetimeValue("2020-08-19 12:05:00"), expr.valueOf(env));
    assertEquals("subdate(timestamp(\"2020-08-26 12:05:00\"), 7)", expr.toString());

    expr = dsl.subdate(DSL.literal("2020-08-26 12:05:00"), DSL.literal(7));
    assertEquals(DATETIME, expr.type());
    assertEquals(new ExprDatetimeValue("2020-08-19 12:05:00"), expr.valueOf(env));
    assertEquals("subdate(\"2020-08-26 12:05:00\", 7)", expr.toString());

    expr = dsl.subdate(dsl.timestamp(DSL.literal("2020-08-26 12:05:00")),
        dsl.interval(DSL.literal(1), DSL.literal("hour")));
    assertEquals(DATETIME, expr.type());
    assertEquals(new ExprDatetimeValue("2020-08-26 11:05:00"), expr.valueOf(env));
    assertEquals("subdate(timestamp(\"2020-08-26 12:05:00\"), interval(1, \"hour\"))",
        expr.toString());

    expr = dsl.subdate(DSL.literal("2020-08-26 12:05:00"),
        dsl.interval(DSL.literal(1), DSL.literal("hour")));
    assertEquals(DATETIME, expr.type());
    assertEquals(new ExprDatetimeValue("2020-08-26 11:05:00"), expr.valueOf(env));
    assertEquals("subdate(\"2020-08-26 12:05:00\", interval(1, \"hour\"))",
        expr.toString());

    expr = dsl.subdate(DSL.literal("2020-08-26"),
        dsl.interval(DSL.literal(1), DSL.literal("day")));
    assertEquals(DATETIME, expr.type());
    assertEquals(new ExprDateValue("2020-08-25"), expr.valueOf(env));
    assertEquals("subdate(\"2020-08-26\", interval(1, \"day\"))",
        expr.toString());

    when(nullRef.type()).thenReturn(DATE);
    assertEquals(nullValue(), eval(dsl.subdate(nullRef, DSL.literal(1L))));
    assertEquals(nullValue(),
        eval(dsl.subdate(nullRef, dsl.interval(DSL.literal(1), DSL.literal("month")))));

    when(missingRef.type()).thenReturn(DATE);
    assertEquals(missingValue(), eval(dsl.subdate(missingRef, DSL.literal(1L))));
    assertEquals(missingValue(),
        eval(dsl.subdate(missingRef, dsl.interval(DSL.literal(1), DSL.literal("month")))));

    when(nullRef.type()).thenReturn(LONG);
    when(missingRef.type()).thenReturn(LONG);
    assertEquals(nullValue(), eval(dsl.subdate(dsl.date(DSL.literal("2020-08-26")), nullRef)));
    assertEquals(missingValue(),
        eval(dsl.subdate(dsl.date(DSL.literal("2020-08-26")), missingRef)));

    when(nullRef.type()).thenReturn(INTERVAL);
    when(missingRef.type()).thenReturn(INTERVAL);
    assertEquals(nullValue(), eval(dsl.subdate(dsl.date(DSL.literal("2020-08-26")), nullRef)));
    assertEquals(missingValue(),
        eval(dsl.subdate(dsl.date(DSL.literal("2020-08-26")), missingRef)));

    when(nullRef.type()).thenReturn(DATE);
    when(missingRef.type()).thenReturn(INTERVAL);
    assertEquals(missingValue(), eval(dsl.subdate(nullRef, missingRef)));
  }

  @Test
  public void time_to_sec() {
    when(nullRef.type()).thenReturn(TIME);
    when(missingRef.type()).thenReturn(TIME);
    assertEquals(nullValue(), eval(dsl.time_to_sec(nullRef)));
    assertEquals(missingValue(), eval(dsl.time_to_sec(missingRef)));

    FunctionExpression expression = dsl.time_to_sec(DSL.literal(new ExprTimeValue("22:23:00")));
    assertEquals(LONG, expression.type());
    assertEquals("time_to_sec(TIME '22:23:00')", expression.toString());
    assertEquals(longValue(80580L), eval(expression));

    expression = dsl.time_to_sec(DSL.literal("22:23:00"));
    assertEquals(LONG, expression.type());
    assertEquals("time_to_sec(\"22:23:00\")", expression.toString());
    assertEquals(longValue(80580L), eval(expression));
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

  private void testWeek(String date, int mode, int expectedResult) {
    FunctionExpression expression = dsl
        .week(DSL.literal(new ExprDateValue(date)), DSL.literal(mode));
    assertEquals(INTEGER, expression.type());
    assertEquals(String.format("week(DATE '%s', %d)", date, mode), expression.toString());
    assertEquals(integerValue(expectedResult), eval(expression));
  }

  private void testNullMissingWeek(ExprCoreType date) {
    when(nullRef.type()).thenReturn(date);
    when(missingRef.type()).thenReturn(date);
    assertEquals(nullValue(), eval(dsl.week(nullRef)));
    assertEquals(missingValue(), eval(dsl.week(missingRef)));
  }

  @Test
  public void week() {
    testNullMissingWeek(DATE);
    testNullMissingWeek(DATETIME);
    testNullMissingWeek(TIMESTAMP);
    testNullMissingWeek(STRING);

    when(nullRef.type()).thenReturn(INTEGER);
    when(missingRef.type()).thenReturn(INTEGER);
    assertEquals(nullValue(), eval(dsl.week(DSL.literal("2019-01-05"), nullRef)));
    assertEquals(missingValue(), eval(dsl.week(DSL.literal("2019-01-05"), missingRef)));

    when(nullRef.type()).thenReturn(DATE);
    when(missingRef.type()).thenReturn(INTEGER);
    assertEquals(missingValue(), eval(dsl.week(nullRef, missingRef)));

    FunctionExpression expression = dsl
        .week(DSL.literal(new ExprTimestampValue("2019-01-05 01:02:03")));
    assertEquals(INTEGER, expression.type());
    assertEquals("week(TIMESTAMP '2019-01-05 01:02:03')", expression.toString());
    assertEquals(integerValue(0), eval(expression));

    expression = dsl.week(DSL.literal("2019-01-05"));
    assertEquals(INTEGER, expression.type());
    assertEquals("week(\"2019-01-05\")", expression.toString());
    assertEquals(integerValue(0), eval(expression));

    expression = dsl.week(DSL.literal("2019-01-05 00:01:00"));
    assertEquals(INTEGER, expression.type());
    assertEquals("week(\"2019-01-05 00:01:00\")", expression.toString());
    assertEquals(integerValue(0), eval(expression));

    testWeek("2019-01-05", 0, 0);
    testWeek("2019-01-05", 1, 1);
    testWeek("2019-01-05", 2, 52);
    testWeek("2019-01-05", 3, 1);
    testWeek("2019-01-05", 4, 1);
    testWeek("2019-01-05", 5, 0);
    testWeek("2019-01-05", 6, 1);
    testWeek("2019-01-05", 7, 53);

    testWeek("2019-01-06", 0, 1);
    testWeek("2019-01-06", 1, 1);
    testWeek("2019-01-06", 2, 1);
    testWeek("2019-01-06", 3, 1);
    testWeek("2019-01-06", 4, 2);
    testWeek("2019-01-06", 5, 0);
    testWeek("2019-01-06", 6, 2);
    testWeek("2019-01-06", 7, 53);

    testWeek("2019-01-07", 0, 1);
    testWeek("2019-01-07", 1, 2);
    testWeek("2019-01-07", 2, 1);
    testWeek("2019-01-07", 3, 2);
    testWeek("2019-01-07", 4, 2);
    testWeek("2019-01-07", 5, 1);
    testWeek("2019-01-07", 6, 2);
    testWeek("2019-01-07", 7, 1);

    testWeek("2000-01-01", 0, 0);
    testWeek("2000-01-01", 2, 52);
    testWeek("1999-12-31", 0, 52);
  }

  @Test
  public void modeInUnsupportedFormat() {
    testNullMissingWeek(DATE);

    FunctionExpression expression1 = dsl
        .week(DSL.literal(new ExprDateValue("2019-01-05")), DSL.literal(8));
    SemanticCheckException exception =
        assertThrows(SemanticCheckException.class, () -> eval(expression1));
    assertEquals("mode:8 is invalid, please use mode value between 0-7",
        exception.getMessage());

    FunctionExpression expression2 = dsl
        .week(DSL.literal(new ExprDateValue("2019-01-05")), DSL.literal(-1));
    exception = assertThrows(SemanticCheckException.class, () -> eval(expression2));
    assertEquals("mode:-1 is invalid, please use mode value between 0-7",
        exception.getMessage());
  }

  @Test
  public void to_days() {
    when(nullRef.type()).thenReturn(DATE);
    when(missingRef.type()).thenReturn(DATE);
    assertEquals(nullValue(), eval(dsl.to_days(nullRef)));
    assertEquals(missingValue(), eval(dsl.to_days(missingRef)));

    FunctionExpression expression = dsl.to_days(DSL.literal(new ExprDateValue("2008-10-07")));
    assertEquals(LONG, expression.type());
    assertEquals("to_days(DATE '2008-10-07')", expression.toString());
    assertEquals(longValue(733687L), eval(expression));

    expression = dsl.to_days(DSL.literal("1969-12-31"));
    assertEquals(LONG, expression.type());
    assertEquals("to_days(\"1969-12-31\")", expression.toString());
    assertEquals(longValue(719527L), eval(expression));

    expression = dsl.to_days(DSL.literal("1969-12-31 01:01:01"));
    assertEquals(LONG, expression.type());
    assertEquals("to_days(\"1969-12-31 01:01:01\")", expression.toString());
    assertEquals(longValue(719527L), eval(expression));
  }

  @Test
  public void year() {
    when(nullRef.type()).thenReturn(DATE);
    when(missingRef.type()).thenReturn(DATE);
    assertEquals(nullValue(), eval(dsl.year(nullRef)));
    assertEquals(missingValue(), eval(dsl.year(missingRef)));

    FunctionExpression expression = dsl.year(DSL.literal(new ExprDateValue("2020-08-07")));
    assertEquals(INTEGER, expression.type());
    assertEquals("year(DATE '2020-08-07')", expression.toString());
    assertEquals(integerValue(2020), eval(expression));

    expression = dsl.year(DSL.literal("2020-08-07"));
    assertEquals(INTEGER, expression.type());
    assertEquals("year(\"2020-08-07\")", expression.toString());
    assertEquals(integerValue(2020), eval(expression));

    expression = dsl.year(DSL.literal("2020-08-07 01:01:01"));
    assertEquals(INTEGER, expression.type());
    assertEquals("year(\"2020-08-07 01:01:01\")", expression.toString());
    assertEquals(integerValue(2020), eval(expression));
  }

  @Test
  public void date_format() {
    dateFormatTesters.forEach(this::testDateFormat);
    String timestamp = "1998-01-31 13:14:15.012345";
    String timestampFormat = "%a %b %c %D %d %e %f %H %h %I %i %j %k %l %M "
        + "%m %p %r %S %s %T %% %P";
    String timestampFormatted = "Sat Jan 01 31st 31 31 12345 13 01 01 14 031 13 1 "
        + "January 01 PM 01:14:15 PM 15 15 13:14:15 % P";

    FunctionExpression expr = dsl.date_format(DSL.literal(timestamp), DSL.literal(timestampFormat));
    assertEquals(STRING, expr.type());
    assertEquals(timestampFormatted, eval(expr).stringValue());

    when(nullRef.type()).thenReturn(DATE);
    when(missingRef.type()).thenReturn(DATE);
    assertEquals(nullValue(), eval(dsl.date_format(nullRef, DSL.literal(""))));
    assertEquals(missingValue(), eval(dsl.date_format(missingRef, DSL.literal(""))));

    when(nullRef.type()).thenReturn(DATETIME);
    when(missingRef.type()).thenReturn(DATETIME);
    assertEquals(nullValue(), eval(dsl.date_format(nullRef, DSL.literal(""))));
    assertEquals(missingValue(), eval(dsl.date_format(missingRef, DSL.literal(""))));

    when(nullRef.type()).thenReturn(TIMESTAMP);
    when(missingRef.type()).thenReturn(TIMESTAMP);
    assertEquals(nullValue(), eval(dsl.date_format(nullRef, DSL.literal(""))));
    assertEquals(missingValue(), eval(dsl.date_format(missingRef, DSL.literal(""))));

    when(nullRef.type()).thenReturn(STRING);
    when(missingRef.type()).thenReturn(STRING);
    assertEquals(nullValue(), eval(dsl.date_format(nullRef, DSL.literal(""))));
    assertEquals(missingValue(), eval(dsl.date_format(missingRef, DSL.literal(""))));
    assertEquals(nullValue(), eval(dsl.date_format(DSL.literal(""), nullRef)));
    assertEquals(missingValue(), eval(dsl.date_format(DSL.literal(""), missingRef)));
  }

  void testDateFormat(DateFormatTester dft) {
    FunctionExpression expr = dft.getDateFormatExpression();
    assertEquals(STRING, expr.type());
    assertEquals(dft.getFormatted(), eval(expr).stringValue());
  }

  private ExprValue eval(Expression expression) {
    return expression.valueOf(env);
  }
}