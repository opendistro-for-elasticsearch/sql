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

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.getDateValue;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DATE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName.DAYOFMONTH;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprIntegerValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.FunctionExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionRepository;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionBuilder;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionName;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionResolver;
import com.amazon.opendistroforelasticsearch.sql.expression.function.FunctionSignature;
import java.util.Collections;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.Pair;

/**
 * The definition of date and time functions.
 * todo, keep define and unaryImpl internally for now. there are two purpose for doing this
 * 1) have the clear interface for function define.
 * 2) the implementation should rely on ExprValue.
 */
@UtilityClass
public class DateTimeFunction {
  public void register(BuiltinFunctionRepository repository) {
    repository.register(dayOfMonth());
  }

  /**
   * DAYOFMONTH(DATE). return the day of the month (1-31).
   */
  private FunctionResolver dayOfMonth() {
    return define(DAYOFMONTH.getName(),
        unaryImpl(DateTimeFunction::exprDayOfMonth, INTEGER, DATE)
    );
  }

  /**
   * Define overloaded function with implementation.
   * @param functionName function name.
   * @param functions a list of function implementation.
   * @return FunctionResolver.
   */
  private FunctionResolver define(FunctionName functionName,
                                          Function<FunctionName, Pair<FunctionSignature,
                                              FunctionBuilder>>... functions) {

    FunctionResolver.FunctionResolverBuilder builder = FunctionResolver.builder();
    builder.functionName(functionName);
    for (Function<FunctionName, Pair<FunctionSignature, FunctionBuilder>> func : functions) {
      Pair<FunctionSignature, FunctionBuilder> functionBuilder = func.apply(functionName);
      builder.functionBundle(functionBuilder.getKey(), functionBuilder.getValue());
    }
    return builder.build();
  }

  /**
   * Unary Function Implementation.
   * @param function {@link ExprValue} based unary function.
   * @param returnType return type.
   * @param argsType argument type.
   *
   * @return Unary Function Implementation.
   */
  private Function<FunctionName, Pair<FunctionSignature, FunctionBuilder>> unaryImpl(
      Function<ExprValue, ExprValue> function,
      ExprType returnType,
      ExprType argsType) {

    return functionName -> {
      FunctionSignature functionSignature =
          new FunctionSignature(functionName, Collections.singletonList(argsType));
      FunctionBuilder functionBuilder =
          arguments -> new FunctionExpression(functionName, arguments) {
            @Override
            public ExprValue valueOf(Environment<Expression, ExprValue> valueEnv) {
              ExprValue value = arguments.get(0).valueOf(valueEnv);
              if (value.isMissing()) {
                return ExprValueUtils.missingValue();
              } else if (value.isNull()) {
                return ExprValueUtils.nullValue();
              } else {
                return function.apply(value);
              }
            }

            @Override
            public ExprType type() {
              return returnType;
            }

            @Override
            public String toString() {
              return String.format("%s(%s)", functionName,
                  arguments.stream()
                      .map(Object::toString)
                      .collect(Collectors.joining(", ")));
            }
          };
      return Pair.of(functionSignature, functionBuilder);
    };
  }

  /**
   * Day of Month implementation for ExprValue.
   * @param date ExprValue of Date type.
   * @return ExprValue.
   */
  private ExprValue exprDayOfMonth(ExprValue date) {
    return new ExprIntegerValue(getDateValue(date).getMonthValue());
  }
}
