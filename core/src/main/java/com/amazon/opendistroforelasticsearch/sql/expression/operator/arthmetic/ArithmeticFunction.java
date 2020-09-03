/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.expression.operator.arthmetic;

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.FLOAT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.LONG;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.SHORT;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprDoubleValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprFloatValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprIntegerValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprLongValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprNullValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprShortValue;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionDSL;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionResolver;
import lombok.experimental.UtilityClass;

/**
 * The definition of arithmetic function
 * add, Accepts two numbers and produces a number.
 * subtract, Accepts two numbers and produces a number.
 * multiply, Accepts two numbers and produces a number.
 * divide, Accepts two numbers and produces a number.
 * module, Accepts two numbers and produces a number.
 */
@UtilityClass
public class ArithmeticFunction {
  /**
   * Register Arithmetic Function.
   *
   * @param repository {@link BuiltinFunctionRepository}.
   */
  public static void register(BuiltinFunctionRepository repository) {
    repository.register(add());
    repository.register(subtract());
    repository.register(multiply());
    repository.register(divide());
    repository.register(modules());
  }

  private static FunctionResolver add() {
    return FunctionDSL.define(BuiltinFunctionName.ADD.getName(),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> new ExprShortValue(v1.shortValue() + v2.shortValue())),
            SHORT, SHORT, SHORT),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> new ExprIntegerValue(Math.addExact(v1.integerValue(),
                    v2.integerValue()))),
            INTEGER, INTEGER, INTEGER),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> new ExprLongValue(Math.addExact(v1.longValue(), v2.longValue()))),
            LONG, LONG, LONG),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> new ExprFloatValue(v1.floatValue() + v2.floatValue())),
            FLOAT, FLOAT, FLOAT),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> new ExprDoubleValue(v1.doubleValue() + v2.doubleValue())),
            DOUBLE, DOUBLE, DOUBLE)
    );
  }

  private static FunctionResolver subtract() {
    return FunctionDSL.define(BuiltinFunctionName.SUBTRACT.getName(),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> new ExprShortValue(v1.shortValue() - v2.shortValue())),
            SHORT, SHORT, SHORT),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> new ExprIntegerValue(Math.subtractExact(v1.integerValue(),
                    v2.integerValue()))),
            INTEGER, INTEGER, INTEGER),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> new ExprLongValue(Math.subtractExact(v1.longValue(), v2.longValue()))),
            LONG, LONG, LONG),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> new ExprFloatValue(v1.floatValue() - v2.floatValue())),
            FLOAT, FLOAT, FLOAT),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> new ExprDoubleValue(v1.doubleValue() - v2.doubleValue())),
            DOUBLE, DOUBLE, DOUBLE)
    );
  }

  private static FunctionResolver multiply() {
    return FunctionDSL.define(BuiltinFunctionName.MULTIPLY.getName(),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> new ExprShortValue(v1.shortValue() * v2.shortValue())),
            SHORT, SHORT, SHORT),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> new ExprIntegerValue(Math.multiplyExact(v1.integerValue(),
                    v2.integerValue()))),
            INTEGER, INTEGER, INTEGER),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> new ExprLongValue(Math.multiplyExact(v1.longValue(), v2.longValue()))),
            LONG, LONG, LONG),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> new ExprFloatValue(v1.floatValue() * v2.floatValue())),
            FLOAT, FLOAT, FLOAT),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> new ExprDoubleValue(v1.doubleValue() * v2.doubleValue())),
            DOUBLE, DOUBLE, DOUBLE)
    );
  }

  private static FunctionResolver divide() {
    return FunctionDSL.define(BuiltinFunctionName.DIVIDE.getName(),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> v2.shortValue() == 0 ? ExprNullValue.of() :
                    new ExprShortValue(v1.shortValue() / v2.shortValue())),
            SHORT, SHORT, SHORT),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> v2.integerValue() == 0 ? ExprNullValue.of() :
                    new ExprIntegerValue(v1.integerValue() / v2.integerValue())),
            INTEGER, INTEGER, INTEGER),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> v2.longValue() == 0 ? ExprNullValue.of() :
                    new ExprLongValue(v1.longValue() / v2.longValue())),
            LONG, LONG, LONG),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> v2.floatValue() == 0 ? ExprNullValue.of() :
                    new ExprFloatValue(v1.floatValue() / v2.floatValue())),
            FLOAT, FLOAT, FLOAT),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> v2.doubleValue() == 0 ? ExprNullValue.of() :
                    new ExprDoubleValue(v1.doubleValue() / v2.doubleValue())),
            DOUBLE, DOUBLE, DOUBLE)
    );
  }


  private static FunctionResolver modules() {
    return FunctionDSL.define(BuiltinFunctionName.MODULES.getName(),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> v2.shortValue() == 0 ? ExprNullValue.of() :
                    new ExprShortValue(v1.shortValue() % v2.shortValue())),
            SHORT, SHORT, SHORT),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> v2.integerValue() == 0 ? ExprNullValue.of() :
                    new ExprIntegerValue(v1.integerValue() % v2.integerValue())),
            INTEGER, INTEGER, INTEGER),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> v2.longValue() == 0 ? ExprNullValue.of() :
                    new ExprLongValue(v1.longValue() % v2.longValue())),
            LONG, LONG, LONG),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> v2.floatValue() == 0 ? ExprNullValue.of() :
                    new ExprFloatValue(v1.floatValue() % v2.floatValue())),
            FLOAT, FLOAT, FLOAT),
        FunctionDSL.impl(
            FunctionDSL.nullMissingHandling(
                (v1, v2) -> v2.doubleValue() == 0 ? ExprNullValue.of() :
                    new ExprDoubleValue(v1.doubleValue() % v2.doubleValue())),
            DOUBLE, DOUBLE, DOUBLE)
    );
  }
}
