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
import static com.amazon.opendistroforelasticsearch.sql.expression.DSL.ref;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprIntegerValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprStringValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTupleValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
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

  @Test
  public void path_as_whole_has_highest_priority() {
    ReferenceExpression expr = new ReferenceExpression("project.year", INTEGER);
    ExprValue actualValue = expr.resolve(tuple());

    assertEquals(INTEGER, actualValue.type());
    assertEquals(1990, actualValue.integerValue());
  }

  @Test
  public void one_path_value() {
    ReferenceExpression expr = ref("name", STRING);
    ExprValue actualValue = expr.resolve(tuple());

    assertEquals(STRING, actualValue.type());
    assertEquals("bob smith", actualValue.stringValue());
  }

  @Test
  public void multiple_path_value() {
    ReferenceExpression expr = new ReferenceExpression("address.state", STRING);
    ExprValue actualValue = expr.resolve(tuple());

    assertEquals(STRING, actualValue.type());
    assertEquals("WA", actualValue.stringValue());
  }

  @Test
  public void not_exist_path() {
    ReferenceExpression expr = new ReferenceExpression("missing_field", STRING);
    ExprValue actualValue = expr.resolve(tuple());

    assertTrue(actualValue.isMissing());
  }

  @Test
  public void object_field_contain_dot() {
    ReferenceExpression expr = new ReferenceExpression("address.local.state", STRING);
    ExprValue actualValue = expr.resolve(tuple());

    assertTrue(actualValue.isMissing());
  }

  @Test
  public void innner_none_object_field_contain_dot() {
    ReferenceExpression expr = new ReferenceExpression("address.project.year", INTEGER);
    ExprValue actualValue = expr.resolve(tuple());

    assertEquals(INTEGER, actualValue.type());
    assertEquals(1990, actualValue.integerValue());
  }

  /**
   * {
   *   "name": "bob smith"
   *   "project.year": 1990,
   *   "project": {
   *     "year": 2020
   *   },
   *   "address": {
   *     "state": "WA",
   *     "city": "seattle"
   *     "project.year": 1990
   *   },
   *   "address.local": {
   *     "state": "WA",
   *   }
   * }
   */
  private ExprTupleValue tuple() {
    ExprValue address =
        ExprValueUtils.tupleValue(ImmutableMap.of("state", "WA", "city", "seattle", "project"
            + ".year", 1990));
    ExprValue project =
        ExprValueUtils.tupleValue(ImmutableMap.of("year", 2020));
    ExprValue addressLocal =
        ExprValueUtils.tupleValue(ImmutableMap.of("state", "WA"));
    ExprTupleValue tuple = ExprTupleValue.fromExprValueMap(ImmutableMap.of(
        "name", new ExprStringValue("bob smith"),
        "project.year", new ExprIntegerValue(1990),
        "project", project,
        "address", address,
        "address.local", addressLocal
        ));
    return tuple;
  }
}