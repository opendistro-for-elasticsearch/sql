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
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.ARRAY;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.BOOLEAN;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.FLOAT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.LONG;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRUCT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.exception.ExpressionEvaluationException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;

@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class ReferenceExpressionTest extends ExpressionTestBase {

  @Test
  public void resolve_value() {
    assertEquals(integerValue(1), DSL.ref("integer_value", INTEGER).valueOf(valueEnv()));
    assertEquals(longValue(1L), DSL.ref("long_value", LONG).valueOf(valueEnv()));
    assertEquals(floatValue(1f), DSL.ref("float_value", FLOAT).valueOf(valueEnv()));
    assertEquals(doubleValue(1d), DSL.ref("double_value", DOUBLE).valueOf(valueEnv()));
    assertEquals(booleanValue(true), DSL.ref("boolean_value", BOOLEAN).valueOf(valueEnv()));
    assertEquals(stringValue("str"), DSL.ref("string_value", STRING).valueOf(valueEnv()));
    assertEquals(tupleValue(ImmutableMap.of("str", 1)),
        DSL.ref("struct_value", STRUCT).valueOf(valueEnv()));
    assertEquals(collectionValue(ImmutableList.of(1)),
        DSL.ref("array_value", ARRAY).valueOf(valueEnv()));
    assertEquals(LITERAL_NULL, DSL.ref(BOOL_TYPE_NULL_VALUE_FIELD, BOOLEAN).valueOf(valueEnv()));
    assertEquals(LITERAL_MISSING,
        DSL.ref(BOOL_TYPE_MISSING_VALUE_FIELD, BOOLEAN).valueOf(valueEnv()));
  }

  @Test
  public void resolve_type() {
    assertEquals(ExprCoreType.INTEGER, DSL.ref("integer_value", INTEGER).type());
    assertEquals(ExprCoreType.LONG, DSL.ref("long_value", LONG).type());
    assertEquals(ExprCoreType.FLOAT, DSL.ref("float_value", FLOAT).type());
    assertEquals(ExprCoreType.DOUBLE, DSL.ref("double_value", DOUBLE).type());
    assertEquals(ExprCoreType.BOOLEAN, DSL.ref("boolean_value", BOOLEAN).type());
    assertEquals(ExprCoreType.STRING, DSL.ref("string_value", STRING).type());
    assertEquals(ExprCoreType.STRUCT, DSL.ref("struct_value", STRUCT).type());
    assertEquals(ExprCoreType.ARRAY, DSL.ref("array_value", ARRAY).type());
  }
}