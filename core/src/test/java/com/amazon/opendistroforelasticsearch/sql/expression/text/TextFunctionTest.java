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

package com.amazon.opendistroforelasticsearch.sql.expression.text;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.missingValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.nullValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.stringValue;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprIntegerValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprStringValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionTestBase;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.TestInstantiationException;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TextFunctionTest extends ExpressionTestBase {
  @Mock
  Environment<Expression, ExprValue> env;

  @Mock
  Expression nullRef;

  @Mock
  Expression missingRef;


  private static List<SubstringInfo> SUBSTRING_STRINGS = ImmutableList.of(
      new SubstringInfo("", 1, 1, ""),
      new SubstringInfo("Quadratically", 5, null, "ratically"),
      new SubstringInfo("foobarbar", 4, null, "barbar"),
      new SubstringInfo("Quadratically", 5, 6, "ratica"),
      new SubstringInfo("Quadratically", 5, 600, "ratically"),
      new SubstringInfo("Quadratically", 500, 1, ""),
      new SubstringInfo("Quadratically", 500, null, ""),
      new SubstringInfo("Sakila", -3, null, "ila"),
      new SubstringInfo("Sakila", -5, 3, "aki"),
      new SubstringInfo("Sakila", -4, 2, "ki"),
      new SubstringInfo("Quadratically", 0, null, ""),
      new SubstringInfo("Sakila", 0, 2, ""),
      new SubstringInfo("Sakila", 2, 0, ""),
      new SubstringInfo("Sakila", 0, 0, ""));
  private static List<String> UPPER_LOWER_STRINGS = ImmutableList.of(
      "test", " test", "test ", " test ", "TesT", "TEST", " TEST", "TEST ", " TEST ", " ", "");
  private static List<StringPatternPair> STRING_PATTERN_PAIRS = ImmutableList.of(
      new StringPatternPair("Michael!", "Michael!"),
      new StringPatternPair("hello", "world"),
      new StringPatternPair("world", "hello"));
  private static List<String> TRIM_STRINGS = ImmutableList.of(
      " test", "     test", "test     ", "test", "     test    ", "", " ");
  private static List<List<String>> CONCAT_STRING_LISTS = ImmutableList.of(
      ImmutableList.of("hello", "world"),
      ImmutableList.of("123", "5325"));

  interface SubstrSubstring {
    FunctionExpression getFunction(SubstringInfo strInfo);
  }

  class Substr implements SubstrSubstring {
    public FunctionExpression getFunction(SubstringInfo strInfo) {
      FunctionExpression expr;
      if (strInfo.getLen() == null) {
        expr = dsl.substr(DSL.literal(strInfo.getExpr()), DSL.literal(strInfo.getStart()));
      } else {
        expr = dsl.substr(DSL.literal(strInfo.getExpr()),
            DSL.literal(strInfo.getStart()),
            DSL.literal(strInfo.getLen()));
      }
      return expr;
    }
  }

  class Substring implements SubstrSubstring {
    public FunctionExpression getFunction(SubstringInfo strInfo) {
      FunctionExpression expr;
      if (strInfo.getLen() == null) {
        expr = dsl.substring(DSL.literal(strInfo.getExpr()), DSL.literal(strInfo.getStart()));
      } else {
        expr = dsl.substring(DSL.literal(strInfo.getExpr()),
            DSL.literal(strInfo.getStart()),
            DSL.literal(strInfo.getLen()));
      }
      return expr;
    }
  }

  @AllArgsConstructor
  @Getter
  static class StringPatternPair {
    private final String str;
    private final String patt;

    int strCmpTest() {
      return Integer.compare(str.compareTo(patt), 0);
    }
  }

  @AllArgsConstructor
  @Getter
  static class SubstringInfo {
    String expr;
    Integer start;
    Integer len;
    String res;
  }

  @BeforeEach
  public void setup() {
    when(nullRef.valueOf(env)).thenReturn(nullValue());
    when(missingRef.valueOf(env)).thenReturn(missingValue());
  }

  @Test
  public void substrSubstring() {
    SUBSTRING_STRINGS.forEach(s -> substrSubstringTest(s, new Substr()));
    SUBSTRING_STRINGS.forEach(s -> substrSubstringTest(s, new Substring()));

    when(nullRef.type()).thenReturn(STRING);
    when(missingRef.type()).thenReturn(STRING);
    assertEquals(missingValue(), eval(dsl.substr(missingRef, DSL.literal(1))));
    assertEquals(nullValue(), eval(dsl.substr(nullRef, DSL.literal(1))));
    assertEquals(missingValue(), eval(dsl.substring(missingRef, DSL.literal(1))));
    assertEquals(nullValue(), eval(dsl.substring(nullRef, DSL.literal(1))));

    when(nullRef.type()).thenReturn(INTEGER);
    when(missingRef.type()).thenReturn(INTEGER);
    assertEquals(missingValue(), eval(dsl.substr(DSL.literal("hello"), missingRef)));
    assertEquals(nullValue(), eval(dsl.substr(DSL.literal("hello"), nullRef)));
    assertEquals(missingValue(), eval(dsl.substring(DSL.literal("hello"), missingRef)));
    assertEquals(nullValue(), eval(dsl.substring(DSL.literal("hello"), nullRef)));
  }

  void substrSubstringTest(SubstringInfo strInfo, SubstrSubstring substrSubstring) {
    FunctionExpression expr = substrSubstring.getFunction(strInfo);
    assertEquals(STRING, expr.type());
    assertEquals(strInfo.getRes(), eval(expr).stringValue());
  }

  @Test
  public void ltrim() {
    TRIM_STRINGS.forEach(this::ltrimString);

    when(nullRef.type()).thenReturn(STRING);
    when(missingRef.type()).thenReturn(STRING);
    assertEquals(missingValue(), eval(dsl.ltrim(missingRef)));
    assertEquals(nullValue(), eval(dsl.ltrim(nullRef)));
  }

  @Test
  public void rtrim() {
    TRIM_STRINGS.forEach(this::rtrimString);

    when(nullRef.type()).thenReturn(STRING);
    when(missingRef.type()).thenReturn(STRING);
    assertEquals(missingValue(), eval(dsl.ltrim(missingRef)));
    assertEquals(nullValue(), eval(dsl.ltrim(nullRef)));
  }

  @Test
  public void trim() {
    TRIM_STRINGS.forEach(this::trimString);

    when(nullRef.type()).thenReturn(STRING);
    when(missingRef.type()).thenReturn(STRING);
    assertEquals(missingValue(), eval(dsl.ltrim(missingRef)));
    assertEquals(nullValue(), eval(dsl.ltrim(nullRef)));
  }

  void ltrimString(String str) {
    FunctionExpression expression = dsl.ltrim(DSL.literal(str));
    assertEquals(STRING, expression.type());
    assertEquals(str.stripLeading(), eval(expression).stringValue());
  }

  void rtrimString(String str) {
    FunctionExpression expression = dsl.rtrim(DSL.literal(str));
    assertEquals(STRING, expression.type());
    assertEquals(str.stripTrailing(), eval(expression).stringValue());
  }

  void trimString(String str) {
    FunctionExpression expression = dsl.trim(DSL.literal(str));
    assertEquals(STRING, expression.type());
    assertEquals(str.trim(), eval(expression).stringValue());
  }

  @Test
  public void lower() {
    UPPER_LOWER_STRINGS.forEach(this::testLowerString);

    when(nullRef.type()).thenReturn(STRING);
    when(missingRef.type()).thenReturn(STRING);
    assertEquals(missingValue(), eval(dsl.lower(missingRef)));
    assertEquals(nullValue(), eval(dsl.lower(nullRef)));
  }

  @Test
  public void upper() {
    UPPER_LOWER_STRINGS.forEach(this::testUpperString);

    when(nullRef.type()).thenReturn(STRING);
    when(missingRef.type()).thenReturn(STRING);
    assertEquals(missingValue(), eval(dsl.upper(missingRef)));
    assertEquals(nullValue(), eval(dsl.upper(nullRef)));
  }

  @Test
  void concat() {
    CONCAT_STRING_LISTS.forEach(this::testConcatString);

    when(nullRef.type()).thenReturn(STRING);
    when(missingRef.type()).thenReturn(STRING);
    assertEquals(missingValue(), eval(
            dsl.concat(missingRef, DSL.literal("1"))));
    assertEquals(nullValue(), eval(
            dsl.concat(nullRef, DSL.literal("1"))));
    assertEquals(missingValue(), eval(
            dsl.concat(DSL.literal("1"), missingRef)));
    assertEquals(nullValue(), eval(
            dsl.concat(DSL.literal("1"), nullRef)));
  }

  @Test
  void concat_ws() {
    CONCAT_STRING_LISTS.forEach(s -> testConcatString(s, ","));

    when(nullRef.type()).thenReturn(STRING);
    when(missingRef.type()).thenReturn(STRING);
    assertEquals(missingValue(), eval(
        dsl.concat_ws(missingRef, DSL.literal("1"), DSL.literal("1"))));
    assertEquals(nullValue(), eval(
        dsl.concat_ws(nullRef, DSL.literal("1"), DSL.literal("1"))));
    assertEquals(missingValue(), eval(
        dsl.concat_ws(DSL.literal("1"), missingRef, DSL.literal("1"))));
    assertEquals(nullValue(), eval(
        dsl.concat_ws(DSL.literal("1"), nullRef, DSL.literal("1"))));
    assertEquals(missingValue(), eval(
        dsl.concat_ws(DSL.literal("1"), DSL.literal("1"), missingRef)));
    assertEquals(nullValue(), eval(
        dsl.concat_ws(DSL.literal("1"), DSL.literal("1"), nullRef)));
  }

  @Test
  void length() {
    UPPER_LOWER_STRINGS.forEach(this::testLengthString);

    when(nullRef.type()).thenReturn(STRING);
    when(missingRef.type()).thenReturn(STRING);
    assertEquals(missingValue(), eval(dsl.length(missingRef)));
    assertEquals(nullValue(), eval(dsl.length(nullRef)));
  }

  @Test
  void strcmp() {
    STRING_PATTERN_PAIRS.forEach(this::testStcmpString);

    when(nullRef.type()).thenReturn(STRING);
    when(missingRef.type()).thenReturn(STRING);
    assertEquals(missingValue(), eval(dsl.strcmp(missingRef, missingRef)));
    assertEquals(nullValue(), eval(dsl.strcmp(nullRef, nullRef)));
    assertEquals(missingValue(), eval(dsl.strcmp(nullRef, missingRef)));
    assertEquals(missingValue(), eval(dsl.strcmp(missingRef, nullRef)));
  }

  @Test
  void right() {
    FunctionExpression expression = dsl.right(
            DSL.literal(new ExprStringValue("foobarbar")),
            DSL.literal(new ExprIntegerValue(4)));
    assertEquals(STRING, expression.type());
    assertEquals("rbar", eval(expression).stringValue());

    when(nullRef.type()).thenReturn(STRING);
    when(missingRef.type()).thenReturn(INTEGER);
    assertEquals(missingValue(), eval(dsl.right(nullRef, missingRef)));
    assertEquals(nullValue(), eval(dsl.right(nullRef, DSL.literal(new ExprIntegerValue(1)))));

    when(nullRef.type()).thenReturn(INTEGER);
    assertEquals(nullValue(), eval(dsl.right(DSL.literal(new ExprStringValue("value")), nullRef)));
  }

  void testConcatString(List<String> strings) {
    String expected = null;
    if (strings.stream().noneMatch(Objects::isNull)) {
      expected = String.join("", strings);
    }

    FunctionExpression expression = dsl.concat(
        DSL.literal(strings.get(0)), DSL.literal(strings.get(1)));
    assertEquals(STRING, expression.type());
    assertEquals(expected, eval(expression).stringValue());
  }

  void testConcatString(List<String> strings, String delim) {
    String expected = strings.stream()
        .filter(Objects::nonNull).collect(Collectors.joining(","));

    FunctionExpression expression = dsl.concat_ws(
        DSL.literal(delim), DSL.literal(strings.get(0)), DSL.literal(strings.get(1)));
    assertEquals(STRING, expression.type());
    assertEquals(expected, eval(expression).stringValue());
  }

  void testLengthString(String str) {
    FunctionExpression expression = dsl.length(DSL.literal(new ExprStringValue(str)));
    assertEquals(INTEGER, expression.type());
    assertEquals(str.getBytes().length, eval(expression).integerValue());
  }

  void testStcmpString(StringPatternPair stringPatternPair) {
    FunctionExpression expression = dsl.strcmp(
            DSL.literal(new ExprStringValue(stringPatternPair.getStr())),
            DSL.literal(new ExprStringValue(stringPatternPair.getPatt())));
    assertEquals(INTEGER, expression.type());
    assertEquals(stringPatternPair.strCmpTest(), eval(expression).integerValue());
  }

  void testLowerString(String str) {
    FunctionExpression expression = dsl.lower(DSL.literal(new ExprStringValue(str)));
    assertEquals(STRING, expression.type());
    assertEquals(stringValue(str.toLowerCase()), eval(expression));
  }

  void testUpperString(String str) {
    FunctionExpression expression = dsl.upper(DSL.literal(new ExprStringValue(str)));
    assertEquals(STRING, expression.type());
    assertEquals(stringValue(str.toUpperCase()), eval(expression));
  }

  private ExprValue eval(Expression expression) {
    return expression.valueOf(env);
  }
}
