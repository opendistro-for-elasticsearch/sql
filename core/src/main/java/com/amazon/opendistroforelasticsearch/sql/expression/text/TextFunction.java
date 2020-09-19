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

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.ARRAY;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.BOOLEAN;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionDSL.define;
import static com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionDSL.impl;
import static com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionDSL.nullMissingHandling;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprBooleanValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprIntegerValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprStringValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionResolver;

import java.util.stream.Collectors;

import lombok.experimental.UtilityClass;


/**
 * The definition of text functions.
 * 1) have the clear interface for function define.
 * 2) the implementation should rely on ExprValue.
 */
@UtilityClass
public class TextFunction {
  /**
   * Register String Functions.
   *
   * @param repository {@link BuiltinFunctionRepository}.
   */
  public void register(BuiltinFunctionRepository repository) {
    repository.register(substr());
    repository.register(ltrim());
    repository.register(rtrim());
    repository.register(trim());
    repository.register(lower());
    repository.register(upper());
    repository.register(concat());
    repository.register(concat_ws());
    repository.register(length());
    repository.register(strcmp());
  }

  /**
   * Gets substring starting at given point, for optional given length.
   * Form of this function using keywords instead of comma delimited variables is not supported.
   * Supports following signatures:
   * (STRING, INTEGER)/(STRING, INTEGER, INTEGER) -> STRING
   */
  private FunctionResolver substr() {
    return define(BuiltinFunctionName.SUBSTR.getName(),
      impl(nullMissingHandling(TextFunction::exprSubstrStart),
        STRING, STRING, INTEGER),
      impl(nullMissingHandling(TextFunction::exprSubstrStartLength),
        STRING, STRING, INTEGER, INTEGER));
  }

  /**
   * Removes leading whitespace from string.
   * Supports following signatures:
   * STRING -> STRING
   */
  private FunctionResolver ltrim() {
    return define(BuiltinFunctionName.LTRIM.getName(),
            impl(nullMissingHandling((v) -> new ExprStringValue(v.stringValue().stripLeading())),
                    STRING, STRING));
  }

  /**
   * Removes trailing whitespace from string.
   * Supports following signatures:
   * STRING -> STRING
   */
  private FunctionResolver rtrim() {
    return define(BuiltinFunctionName.RTRIM.getName(),
        impl(nullMissingHandling((v) -> new ExprStringValue(v.stringValue().stripTrailing())),
                STRING, STRING));
  }

  /**
   * Removes leading and trailing whitespace from string.
   * Has option to specify a String to trim instead of whitespace but this is not yet supported.
   * Supporting String specification requires finding keywords inside TRIM command.
   * Supports following signatures:
   * STRING -> STRING
   */
  private FunctionResolver trim() {
    return define(BuiltinFunctionName.TRIM.getName(),
        impl(nullMissingHandling((v) -> new ExprStringValue(v.stringValue().trim())),
            STRING, STRING));
  }

  /**
   * Converts String to lowercase.
   * Supports following signatures:
   * STRING -> STRING
   */
  private FunctionResolver lower() {
    return define(BuiltinFunctionName.LOWER.getName(),
        impl(nullMissingHandling((v) -> new ExprStringValue((v.stringValue().toLowerCase()))),
            STRING, STRING)
    );
  }

  /**
   * Converts String to uppercase.
   * Supports following signatures:
   * STRING -> STRING
   */
  private FunctionResolver upper() {
    return define(BuiltinFunctionName.UPPER.getName(),
        impl(nullMissingHandling((v) -> new ExprStringValue((v.stringValue().toUpperCase()))),
            STRING, STRING)
    );
  }

  /**
   * TODO: https://github.com/opendistro-for-elasticsearch/sql/issues/710
   *  Extend to accept variable argument amounts.
   * Concatenates a list of Strings.
   * Supports following signatures:
   * (STRING, STRING) -> STRING
   */
  private FunctionResolver concat() {
    return define(BuiltinFunctionName.CONCAT.getName(),
        impl(nullMissingHandling((str1, str2) ->
            new ExprStringValue(str1.stringValue() + str2.stringValue())), STRING, STRING, STRING));
  }

  /**
   * TODO: https://github.com/opendistro-for-elasticsearch/sql/issues/710
   *  Extend to accept variable argument amounts.
   * Concatenates a list of Strings with a separator string.
   * Supports following signatures:
   * (STRING, STRING, STRING) -> STRING
   */
  private FunctionResolver concat_ws() {
    return define(BuiltinFunctionName.CONCAT_WS.getName(),
        impl(nullMissingHandling((sep, str1, str2) ->
            new ExprStringValue(str1.stringValue() + sep.stringValue() + str2.stringValue())),
                STRING, STRING, STRING, STRING));
  }

  /**
   * Calculates length of String in bytes.
   * Supports following signatures:
   * STRING -> INTEGER
   */
  private FunctionResolver length() {
    return define(BuiltinFunctionName.LENGTH.getName(),
        impl(nullMissingHandling((str) ->
            new ExprIntegerValue(str.stringValue().getBytes().length)), INTEGER, STRING));
  }

  /**
   * Does String comparison of two Strings and returns Integer value.
   * Supports following signatures:
   * (STRING, STRING) -> INTEGER
   */
  private FunctionResolver strcmp() {
    return define(BuiltinFunctionName.STRCMP.getName(),
        impl(nullMissingHandling((str1, str2) ->
            new ExprIntegerValue(str1.stringValue().compareTo(str2.stringValue()))),
            INTEGER, STRING, STRING));
  }

  private static ExprValue exprSubstrStart(ExprValue exprValue, ExprValue start) {
    int startIdx = start.integerValue();
    if (startIdx == 0) {
      return new ExprStringValue("");
    }
    String str = exprValue.stringValue();
    startIdx = (startIdx > 0) ? (startIdx - 1) : (str.length() + startIdx);
    if (startIdx > str.length()) {
      return new ExprStringValue("");
    }

    return new ExprStringValue(str.substring(startIdx));
  }

  private static ExprValue exprSubstrStartLength(
          ExprValue exprValue, ExprValue start, ExprValue length) {
    int startIdx = start.integerValue();
    int len = length.integerValue();
    if ((startIdx == 0) || (len == 0)) {
      return new ExprStringValue("");
    }

    String str = exprValue.stringValue();
    startIdx = (startIdx > 0) ? (startIdx - 1) : (str.length() + startIdx);
    if ((startIdx + len) > str.length()) {
      if (startIdx > str.length()) {
        return new ExprStringValue("");
      }

      // Return full String after start if start + len > string length
      return new ExprStringValue(str.substring(startIdx));
    }
    return new ExprStringValue(str.substring(startIdx, startIdx + len));
  }
}

