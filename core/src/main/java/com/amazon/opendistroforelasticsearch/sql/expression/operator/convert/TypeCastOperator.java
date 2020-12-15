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

package com.amazon.opendistroforelasticsearch.sql.expression.operator.convert;

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.BOOLEAN;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.BYTE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DATE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DATETIME;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.FLOAT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.LONG;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.SHORT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.TIME;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.TIMESTAMP;
import static com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionDSL.impl;
import static com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionDSL.nullMissingHandling;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprBooleanValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprDateValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprDoubleValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprFloatValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprIntegerValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprLongValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprStringValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTimeValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTimestampValue;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionDSL;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionResolver;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.experimental.UtilityClass;

@UtilityClass
public class TypeCastOperator {
  /**
   * Register Type Cast Operator.
   */
  public static void register(BuiltinFunctionRepository repository) {
    repository.register(castToString());
    repository.register(castToInt());
    repository.register(castToLong());
    repository.register(castToFloat());
    repository.register(castToDouble());
    repository.register(castToBoolean());
    repository.register(castToDate());
    repository.register(castToTime());
    repository.register(castToTimestamp());
  }


  private static FunctionResolver castToString() {
    return FunctionDSL.define(BuiltinFunctionName.CAST_TO_STRING.getName(),
        Stream.concat(
            Arrays.asList(BYTE, SHORT, INTEGER, LONG, FLOAT, DOUBLE, BOOLEAN, TIME, DATE,
                TIMESTAMP, DATETIME).stream()
                .map(type -> impl(
                    nullMissingHandling((v) -> new ExprStringValue(v.value().toString())),
                    STRING, type)),
            Stream.of(impl(nullMissingHandling((v) -> v), STRING, STRING)))
            .collect(Collectors.toList())
    );
  }

  private static FunctionResolver castToInt() {
    return FunctionDSL.define(BuiltinFunctionName.CAST_TO_INT.getName(),
        impl(nullMissingHandling(
            (v) -> new ExprIntegerValue(Integer.valueOf(v.stringValue()))), INTEGER, STRING),
        impl(nullMissingHandling(
            (v) -> new ExprIntegerValue(v.integerValue())), INTEGER, DOUBLE),
        impl(nullMissingHandling(
            (v) -> new ExprIntegerValue(v.booleanValue() ? 1 : 0)), INTEGER, BOOLEAN)
    );
  }

  private static FunctionResolver castToLong() {
    return FunctionDSL.define(BuiltinFunctionName.CAST_TO_LONG.getName(),
        impl(nullMissingHandling(
            (v) -> new ExprLongValue(Long.valueOf(v.stringValue()))), LONG, STRING),
        impl(nullMissingHandling(
            (v) -> new ExprLongValue(v.longValue())), LONG, DOUBLE),
        impl(nullMissingHandling(
            (v) -> new ExprLongValue(v.booleanValue() ? 1L : 0L)), LONG, BOOLEAN)
    );
  }

  private static FunctionResolver castToFloat() {
    return FunctionDSL.define(BuiltinFunctionName.CAST_TO_FLOAT.getName(),
        impl(nullMissingHandling(
            (v) -> new ExprFloatValue(Float.valueOf(v.stringValue()))), FLOAT, STRING),
        impl(nullMissingHandling(
            (v) -> new ExprFloatValue(v.longValue())), FLOAT, DOUBLE),
        impl(nullMissingHandling(
            (v) -> new ExprFloatValue(v.booleanValue() ? 1f : 0f)), FLOAT, BOOLEAN)
    );
  }

  private static FunctionResolver castToDouble() {
    return FunctionDSL.define(BuiltinFunctionName.CAST_TO_DOUBLE.getName(),
        impl(nullMissingHandling(
            (v) -> new ExprDoubleValue(Double.valueOf(v.stringValue()))), DOUBLE, STRING),
        impl(nullMissingHandling(
            (v) -> new ExprDoubleValue(v.doubleValue())), DOUBLE, DOUBLE),
        impl(nullMissingHandling(
            (v) -> new ExprDoubleValue(v.booleanValue() ? 1D : 0D)), DOUBLE, BOOLEAN)
    );
  }

  private static FunctionResolver castToBoolean() {
    return FunctionDSL.define(BuiltinFunctionName.CAST_TO_BOOLEAN.getName(),
        impl(nullMissingHandling(
            (v) -> ExprBooleanValue.of(Boolean.valueOf(v.stringValue()))), BOOLEAN, STRING),
        impl(nullMissingHandling(
            (v) -> ExprBooleanValue.of(v.doubleValue() != 0)), BOOLEAN, DOUBLE),
        impl(nullMissingHandling((v) -> v), BOOLEAN, BOOLEAN)
    );
  }

  private static FunctionResolver castToDate() {
    return FunctionDSL.define(BuiltinFunctionName.CAST_TO_DATE.getName(),
        impl(nullMissingHandling(
            (v) -> new ExprDateValue(v.stringValue())), DATE, STRING),
        impl(nullMissingHandling(
            (v) -> new ExprDateValue(v.dateValue())), DATE, DATETIME),
        impl(nullMissingHandling(
            (v) -> new ExprDateValue(v.dateValue())), DATE, TIMESTAMP),
        impl(nullMissingHandling((v) -> v), DATE, DATE)
    );
  }

  private static FunctionResolver castToTime() {
    return FunctionDSL.define(BuiltinFunctionName.CAST_TO_TIME.getName(),
        impl(nullMissingHandling(
            (v) -> new ExprTimeValue(v.stringValue())), TIME, STRING),
        impl(nullMissingHandling(
            (v) -> new ExprTimeValue(v.timeValue())), TIME, DATETIME),
        impl(nullMissingHandling(
            (v) -> new ExprTimeValue(v.timeValue())), TIME, TIMESTAMP),
        impl(nullMissingHandling((v) -> v), TIME, TIME)
    );
  }

  private static FunctionResolver castToTimestamp() {
    return FunctionDSL.define(BuiltinFunctionName.CAST_TO_TIMESTAMP.getName(),
        impl(nullMissingHandling(
            (v) -> new ExprTimestampValue(v.stringValue())), TIMESTAMP, STRING),
        impl(nullMissingHandling(
            (v) -> new ExprTimestampValue(v.timestampValue())), TIMESTAMP, DATETIME),
        impl(nullMissingHandling((v) -> v), TIMESTAMP, TIMESTAMP)
    );
  }
}
