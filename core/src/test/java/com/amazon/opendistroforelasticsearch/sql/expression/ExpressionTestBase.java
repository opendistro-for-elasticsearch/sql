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

package com.amazon.opendistroforelasticsearch.sql.expression;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.amazon.opendistroforelasticsearch.sql.expression.config.ExpressionConfig;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import com.amazon.opendistroforelasticsearch.sql.expression.function.BuiltinFunctionName;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.function.Function;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.booleanValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.collectionValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.doubleValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.floatValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.integerValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.longValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.missingValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.nullValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.stringValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.tupleValue;

@Configuration
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ExpressionConfig.class, ExpressionTestBase.class})
public class ExpressionTestBase {
    public static final String INT_TYPE_NULL_VALUE_FIELD = "int_null_value";
    public static final String INT_TYPE_MISSING_VALUE_FIELD = "int_missing_value";
    public static final String BOOL_TYPE_NULL_VALUE_FIELD = "null_value_boolean";
    public static final String BOOL_TYPE_MISSING_VALUE_FIELD = "missing_value_boolean";

    @Autowired
    protected DSL dsl;

    @Autowired
    protected Environment<Expression, ExprValue> valueEnv;

    @Autowired
    protected Environment<Expression, ExprType> typeEnv;

    @Bean
    protected Environment<Expression, ExprValue> valueEnv() {
        return var -> {
            if (var instanceof ReferenceExpression) {
                switch (((ReferenceExpression) var).getAttr()) {
                    case "integer_value":
                        return integerValue(1);
                    case "long_value":
                        return longValue(1L);
                    case "float_value":
                        return floatValue(1f);
                    case "double_value":
                        return doubleValue(1d);
                    case "boolean_value":
                        return booleanValue(true);
                    case "string_value":
                        return stringValue("str");
                    case "struct_value":
                        return tupleValue(ImmutableMap.of("str", 1));
                    case "array_value":
                        return collectionValue(ImmutableList.of(1));
                    case BOOL_TYPE_NULL_VALUE_FIELD:
                    case INT_TYPE_NULL_VALUE_FIELD:
                        return nullValue();
                }
            }
            return missingValue();
        };
    }

    @Bean
    protected Environment<Expression, ExprType> typeEnv() {
        return var -> {
            if (var instanceof ReferenceExpression) {
                switch (((ReferenceExpression) var).getAttr()) {
                    case "integer_value":
                    case INT_TYPE_NULL_VALUE_FIELD:
                    case INT_TYPE_MISSING_VALUE_FIELD:
                        return ExprType.INTEGER;
                    case "long_value":
                        return ExprType.LONG;
                    case "float_value":
                        return ExprType.FLOAT;
                    case "double_value":
                        return ExprType.DOUBLE;
                    case "boolean_value":
                        return ExprType.BOOLEAN;
                    case "string_value":
                        return ExprType.STRING;
                    case "struct_value":
                        return ExprType.STRUCT;
                    case "array_value":
                        return ExprType.ARRAY;
                    case BOOL_TYPE_NULL_VALUE_FIELD:
                    case BOOL_TYPE_MISSING_VALUE_FIELD:
                        return ExprType.BOOLEAN;
                }
            }
            throw new ExpressionEvaluationException("type resolved failed");
        };
    }

    protected Function<List<Expression>, FunctionExpression> functionMapping(BuiltinFunctionName builtinFunctionName) {
        switch (builtinFunctionName) {
            case ADD:
                return expressions -> dsl.add(expressions.get(0), expressions.get(1));
            case SUBTRACT:
                return expressions -> dsl.subtract(expressions.get(0), expressions.get(1));
            case MULTIPLY:
                return expressions -> dsl.multiply(expressions.get(0), expressions.get(1));
            case DIVIDE:
                return expressions -> dsl.divide(expressions.get(0), expressions.get(1));
            case MODULES:
                return expressions -> dsl.module(expressions.get(0), expressions.get(1));
            default:
                throw new RuntimeException();
        }
    }
}
