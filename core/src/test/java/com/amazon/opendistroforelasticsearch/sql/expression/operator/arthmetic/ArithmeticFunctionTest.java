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

import static com.amazon.opendistroforelasticsearch.sql.config.TestConfig.INT_TYPE_MISSING_VALUE_FIELD;
import static com.amazon.opendistroforelasticsearch.sql.config.TestConfig.INT_TYPE_NULL_VALUE_FIELD;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_MISSING;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_NULL;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.integerValue;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.SHORT;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.literal;
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.ref;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprDoubleValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprFloatValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprIntegerValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprLongValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprShortValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.data.type.WideningTypeRule;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ExpressionTestBase;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ArithmeticFunctionTest extends ExpressionTestBase {

  private static Stream<Arguments> arithmeticFunctionArguments() {
    List<ExprValue> numberOp1 = Arrays.asList(new ExprShortValue(3), new ExprIntegerValue(3),
        new ExprLongValue(3L), new ExprFloatValue(3f), new ExprDoubleValue(3D));
    List<ExprValue> numberOp2 =
        Arrays.asList(new ExprShortValue(2), new ExprIntegerValue(2), new ExprLongValue(3L),
            new ExprFloatValue(2f), new ExprDoubleValue(2D));
    return Lists.cartesianProduct(numberOp1, numberOp2).stream()
        .map(list -> Arguments.of(list.get(0), list.get(1)));
  }

  private static Stream<Arguments> arithmeticOperatorArguments() {
    return Stream
        .of(BuiltinFunctionName.ADD, BuiltinFunctionName.SUBTRACT, BuiltinFunctionName.MULTIPLY,
            BuiltinFunctionName.DIVIDE, BuiltinFunctionName.DIVIDE).map(Arguments::of);
  }

  @ParameterizedTest(name = "add({1}, {2})")
  @MethodSource("arithmeticFunctionArguments")
  public void add(ExprValue op1, ExprValue op2) {
    FunctionExpression expression = dsl.add(literal(op1), literal(op2));
    ExprType expectedType = WideningTypeRule.max(op1.type(), op2.type());
    assertEquals(expectedType, expression.type());
    assertValueEqual(BuiltinFunctionName.ADD, expectedType, op1, op2, expression.valueOf(null));
    assertEquals(String.format("+(%s, %s)", op1.toString(), op2.toString()), expression.toString());
  }

  @ParameterizedTest(name = "{0}(int,null)")
  @MethodSource("arithmeticOperatorArguments")
  public void arithmetic_int_null(BuiltinFunctionName builtinFunctionName) {
    Function<
        List<Expression>, FunctionExpression> function = functionMapping(builtinFunctionName);

    FunctionExpression functionExpression =
        function.apply(Arrays.asList(literal(integerValue(1)),
            ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER)));
    assertEquals(INTEGER, functionExpression.type());
    assertEquals(LITERAL_NULL, functionExpression.valueOf(valueEnv()));

    functionExpression = function.apply(
        Arrays.asList(ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER), literal(integerValue(1))));
    assertEquals(INTEGER, functionExpression.type());
    assertEquals(LITERAL_NULL, functionExpression.valueOf(valueEnv()));
  }

  @ParameterizedTest(name = "{0}(int,missing)")
  @MethodSource("arithmeticOperatorArguments")
  public void arithmetic_int_missing(BuiltinFunctionName builtinFunctionName) {
    Function<
        List<Expression>, FunctionExpression> function = functionMapping(builtinFunctionName);
    FunctionExpression functionExpression =
        function.apply(Arrays.asList(literal(integerValue(1)),
            ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER)));
    assertEquals(INTEGER, functionExpression.type());
    assertEquals(LITERAL_MISSING, functionExpression.valueOf(valueEnv()));

    functionExpression = function.apply(Arrays.asList(ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER),
        literal(integerValue(1))));
    assertEquals(INTEGER, functionExpression.type());
    assertEquals(LITERAL_MISSING, functionExpression.valueOf(valueEnv()));
  }

  @ParameterizedTest(name = "{0}(null,missing)")
  @MethodSource("arithmeticOperatorArguments")
  public void arithmetic_null_missing(BuiltinFunctionName builtinFunctionName) {
    Function<
        List<Expression>, FunctionExpression> function = functionMapping(builtinFunctionName);
    FunctionExpression functionExpression = function.apply(
        Arrays.asList(ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER),
            ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER)));
    assertEquals(INTEGER, functionExpression.type());
    assertEquals(LITERAL_NULL, functionExpression.valueOf(valueEnv()));

    functionExpression = function.apply(
        Arrays.asList(ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER),
            ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER)));
    assertEquals(INTEGER, functionExpression.type());
    assertEquals(LITERAL_MISSING, functionExpression.valueOf(valueEnv()));

    functionExpression = function.apply(
        Arrays.asList(ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER),
            ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER)));
    assertEquals(INTEGER, functionExpression.type());
    assertEquals(LITERAL_MISSING, functionExpression.valueOf(valueEnv()));

    functionExpression = function.apply(
        Arrays.asList(ref(INT_TYPE_NULL_VALUE_FIELD, INTEGER),
            ref(INT_TYPE_MISSING_VALUE_FIELD, INTEGER)));
    assertEquals(INTEGER, functionExpression.type());
    assertEquals(LITERAL_MISSING, functionExpression.valueOf(valueEnv()));
  }

  @ParameterizedTest(name = "subtract({1}, {2})")
  @MethodSource("arithmeticFunctionArguments")
  public void subtract(ExprValue op1, ExprValue op2) {
    FunctionExpression expression = dsl.subtract(literal(op1), literal(op2));
    ExprType expectedType = WideningTypeRule.max(op1.type(), op2.type());
    assertEquals(expectedType, expression.type());
    assertValueEqual(BuiltinFunctionName.SUBTRACT, expectedType, op1, op2,
        expression.valueOf(null));
    assertEquals(String.format("-(%s, %s)", op1.toString(), op2.toString()),
        expression.toString());
  }

  @ParameterizedTest(name = "multiply({1}, {2})")
  @MethodSource("arithmeticFunctionArguments")
  public void multiply(ExprValue op1, ExprValue op2) {
    FunctionExpression expression = dsl.multiply(literal(op1), literal(op2));
    ExprType expectedType = WideningTypeRule.max(op1.type(), op2.type());
    assertEquals(expectedType, expression.type());
    assertValueEqual(BuiltinFunctionName.MULTIPLY, expectedType, op1, op2,
        expression.valueOf(null));
    assertEquals(String.format("*(%s, %s)", op1.toString(), op2.toString()),
        expression.toString());
  }

  @ParameterizedTest(name = "divide({1}, {2})")
  @MethodSource("arithmeticFunctionArguments")
  public void divide(ExprValue op1, ExprValue op2) {
    FunctionExpression expression = dsl.divide(literal(op1), literal(op2));
    ExprType expectedType = WideningTypeRule.max(op1.type(), op2.type());
    assertEquals(expectedType, expression.type());
    assertValueEqual(BuiltinFunctionName.DIVIDE, expectedType, op1, op2, expression.valueOf(null));
    assertEquals(String.format("/(%s, %s)", op1.toString(), op2.toString()),
        expression.toString());

    expression = dsl.divide(literal(op1), literal(new ExprShortValue(0)));
    expectedType = WideningTypeRule.max(op1.type(), SHORT);
    assertEquals(expectedType, expression.type());
    assertTrue(expression.valueOf(valueEnv()).isNull());
    assertEquals(String.format("/(%s, 0)", op1.toString()), expression.toString());
  }

  @ParameterizedTest(name = "module({1}, {2})")
  @MethodSource("arithmeticFunctionArguments")
  public void module(ExprValue op1, ExprValue op2) {
    FunctionExpression expression = dsl.module(literal(op1), literal(op2));
    ExprType expectedType = WideningTypeRule.max(op1.type(), op2.type());
    assertEquals(expectedType, expression.type());
    assertValueEqual(BuiltinFunctionName.MODULES, expectedType, op1, op2, expression.valueOf(null));
    assertEquals(String.format("%%(%s, %s)", op1.toString(), op2.toString()),
        expression.toString());

    expression = dsl.module(literal(op1), literal(new ExprShortValue(0)));
    expectedType = WideningTypeRule.max(op1.type(), SHORT);
    assertEquals(expectedType, expression.type());
    assertTrue(expression.valueOf(valueEnv()).isNull());
    assertEquals(String.format("%%(%s, 0)", op1.toString()), expression.toString());
  }

  protected void assertValueEqual(BuiltinFunctionName builtinFunctionName, ExprType type,
                                  ExprValue op1,
                                  ExprValue op2,
                                  ExprValue actual) {
    switch ((ExprCoreType) type) {
      case SHORT:
        Short vs1 = op1.shortValue();
        Short vs2 = op2.shortValue();
        Integer vsActual = actual.integerValue();
        switch (builtinFunctionName) {
          case ADD:
            assertEquals(vs1 + vs2, vsActual);
            return;
          case SUBTRACT:
            assertEquals(vs1 - vs2, vsActual);
            return;
          case DIVIDE:
            assertEquals(vs1 / vs2, vsActual);
            return;
          case MULTIPLY:
            assertEquals(vs1 * vs2, vsActual);
            return;
          case MODULES:
            assertEquals(vs1 % vs2, vsActual);
            return;
          default:
            throw new IllegalStateException("illegal function name " + builtinFunctionName);
        }
      case INTEGER:
        Integer vi1 = ExprValueUtils.getIntegerValue(op1);
        Integer vi2 = ExprValueUtils.getIntegerValue(op2);
        Integer viActual = ExprValueUtils.getIntegerValue(actual);
        switch (builtinFunctionName) {
          case ADD:
            assertEquals(vi1 + vi2, viActual);
            return;
          case SUBTRACT:
            assertEquals(vi1 - vi2, viActual);
            return;
          case DIVIDE:
            assertEquals(vi1 / vi2, viActual);
            return;
          case MULTIPLY:
            assertEquals(vi1 * vi2, viActual);
            return;
          case MODULES:
            assertEquals(vi1 % vi2, viActual);
            return;
          default:
            throw new IllegalStateException("illegal function name " + builtinFunctionName);
        }
      case LONG:
        Long vl1 = ExprValueUtils.getLongValue(op1);
        Long vl2 = ExprValueUtils.getLongValue(op2);
        Long vlActual = ExprValueUtils.getLongValue(actual);
        switch (builtinFunctionName) {
          case ADD:
            assertEquals(vl1 + vl2, vlActual);
            return;
          case SUBTRACT:
            assertEquals(vl1 - vl2, vlActual);
            return;
          case DIVIDE:
            assertEquals(vl1 / vl2, vlActual);
            return;
          case MULTIPLY:
            assertEquals(vl1 * vl2, vlActual);
            return;
          case MODULES:
            assertEquals(vl1 % vl2, vlActual);
            return;
          default:
            throw new IllegalStateException("illegal function name " + builtinFunctionName);
        }
      case FLOAT:
        Float vf1 = ExprValueUtils.getFloatValue(op1);
        Float vf2 = ExprValueUtils.getFloatValue(op2);
        Float vfActual = ExprValueUtils.getFloatValue(actual);
        switch (builtinFunctionName) {
          case ADD:
            assertEquals(vf1 + vf2, vfActual);
            return;
          case SUBTRACT:
            assertEquals(vf1 - vf2, vfActual);
            return;
          case DIVIDE:
            assertEquals(vf1 / vf2, vfActual);
            return;
          case MULTIPLY:
            assertEquals(vf1 * vf2, vfActual);
            return;
          case MODULES:
            assertEquals(vf1 % vf2, vfActual);
            return;
          default:
            throw new IllegalStateException("illegal function name " + builtinFunctionName);
        }
      case DOUBLE:
        Double vd1 = ExprValueUtils.getDoubleValue(op1);
        Double vd2 = ExprValueUtils.getDoubleValue(op2);
        Double vdActual = ExprValueUtils.getDoubleValue(actual);
        switch (builtinFunctionName) {
          case ADD:
            assertEquals(vd1 + vd2, vdActual);
            return;
          case SUBTRACT:
            assertEquals(vd1 - vd2, vdActual);
            return;
          case DIVIDE:
            assertEquals(vd1 / vd2, vdActual);
            return;
          case MULTIPLY:
            assertEquals(vd1 * vd2, vdActual);
            return;
          case MODULES:
            assertEquals(vd1 % vd2, vdActual);
            return;
          default:
            throw new IllegalStateException("illegal function name " + builtinFunctionName);
        }
      default:
        throw new IllegalStateException("illegal function name " + builtinFunctionName);
    }
  }
}