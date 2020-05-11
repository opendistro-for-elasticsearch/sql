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
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

import static com.amazon.opendistroforelasticsearch.sql.config.TestConfig.BOOL_TYPE_MISSING_VALUE_FIELD;
import static com.amazon.opendistroforelasticsearch.sql.config.TestConfig.BOOL_TYPE_NULL_VALUE_FIELD;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_MISSING;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.LITERAL_NULL;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.booleanValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.collectionValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.doubleValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.floatValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.integerValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.longValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.stringValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.tupleValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReferenceExpressionTest extends ExpressionTestBase {

    @Test
    public void resolve_value() {
        assertEquals(integerValue(1), DSL.ref("integer_value").valueOf(valueEnv()));
        assertEquals(longValue(1L), DSL.ref("long_value").valueOf(valueEnv()));
        assertEquals(floatValue(1f), DSL.ref("float_value").valueOf(valueEnv()));
        assertEquals(doubleValue(1d), DSL.ref("double_value").valueOf(valueEnv()));
        assertEquals(booleanValue(true), DSL.ref("boolean_value").valueOf(valueEnv()));
        assertEquals(stringValue("str"), DSL.ref("string_value").valueOf(valueEnv()));
        assertEquals(tupleValue(ImmutableMap.of("str", 1)), DSL.ref("struct_value").valueOf(valueEnv()));
        assertEquals(collectionValue(ImmutableList.of(1)), DSL.ref("array_value").valueOf(valueEnv()));
        assertEquals(LITERAL_NULL, DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD).valueOf(valueEnv()));
        assertEquals(LITERAL_MISSING, DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD).valueOf(valueEnv()));
    }

    @Test
    public void resolve_type() {
        assertEquals(ExprType.INTEGER, DSL.ref("integer_value").type(typeEnv()));
        assertEquals(ExprType.LONG, DSL.ref("long_value").type(typeEnv()));
        assertEquals(ExprType.FLOAT, DSL.ref("float_value").type(typeEnv()));
        assertEquals(ExprType.DOUBLE, DSL.ref("double_value").type(typeEnv()));
        assertEquals(ExprType.BOOLEAN, DSL.ref("boolean_value").type(typeEnv()));
        assertEquals(ExprType.STRING, DSL.ref("string_value").type(typeEnv()));
        assertEquals(ExprType.STRUCT, DSL.ref("struct_value").type(typeEnv()));
        assertEquals(ExprType.ARRAY, DSL.ref("array_value").type(typeEnv()));
        assertThrows(ExpressionEvaluationException.class, () -> DSL.ref("not_exist_field").type(typeEnv()));
    }
}