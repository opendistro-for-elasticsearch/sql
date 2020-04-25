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

package com.amazon.opendistroforelasticsearch.sql.analysis;

import com.amazon.opendistroforelasticsearch.sql.analysis.scheme.Namespace;
import com.amazon.opendistroforelasticsearch.sql.analysis.scheme.Symbol;
import com.amazon.opendistroforelasticsearch.sql.analysis.scheme.SymbolTable;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprType;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.amazon.opendistroforelasticsearch.sql.expression.DSL;
import com.amazon.opendistroforelasticsearch.sql.expression.Expression;
import com.amazon.opendistroforelasticsearch.sql.expression.ReferenceExpression;
import com.amazon.opendistroforelasticsearch.sql.expression.config.ExpressionConfig;
import com.amazon.opendistroforelasticsearch.sql.expression.env.Environment;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;

import static com.amazon.opendistroforelasticsearch.sql.expression.ExpressionTestBase.BOOL_TYPE_MISSING_VALUE_FIELD;
import static com.amazon.opendistroforelasticsearch.sql.expression.ExpressionTestBase.BOOL_TYPE_NULL_VALUE_FIELD;
import static com.amazon.opendistroforelasticsearch.sql.expression.ExpressionTestBase.INT_TYPE_MISSING_VALUE_FIELD;
import static com.amazon.opendistroforelasticsearch.sql.expression.ExpressionTestBase.INT_TYPE_NULL_VALUE_FIELD;

@Configuration
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {ExpressionConfig.class, AnalyzerTestBase.class})
public class AnalyzerTestBase {
    @Autowired
    protected DSL dsl;

    @Autowired
    protected AnalysisContext analysisContext;

    @Autowired
    protected ExpressionAnalyzer expressionAnalyzer;


    protected SymbolTable symbolTable() {
        SymbolTable symbolTable = new SymbolTable();
        Map<String, ExprType> map = new ImmutableMap.Builder<String, ExprType>()
                .put("integer_value", ExprType.INTEGER)
                .put(INT_TYPE_NULL_VALUE_FIELD, ExprType.INTEGER)
                .put(INT_TYPE_MISSING_VALUE_FIELD, ExprType.INTEGER)
                .put("long_value", ExprType.LONG)
                .put("float_value", ExprType.FLOAT)
                .put("double_value", ExprType.DOUBLE)
                .put("boolean_value", ExprType.BOOLEAN)
                .put("string_value", ExprType.STRING)
                .put("struct_value", ExprType.STRUCT)
                .put("array_value", ExprType.ARRAY)
                .build();
        map.entrySet()
                .forEach(
                        entry -> symbolTable.store(new Symbol(Namespace.FIELD_NAME, entry.getKey()), entry.getValue()));
        return symbolTable;
    }

    protected TypeEnvironment typeEnvironment() {
        return new TypeEnvironment(null, symbolTable());
    }

    @Bean
    protected AnalysisContext analysisContext() {
        return new AnalysisContext(typeEnvironment());
    }

    @Bean
    protected ExpressionAnalyzer expressionAnalyzer(DSL dsl) {
        return new ExpressionAnalyzer(dsl);
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
}
