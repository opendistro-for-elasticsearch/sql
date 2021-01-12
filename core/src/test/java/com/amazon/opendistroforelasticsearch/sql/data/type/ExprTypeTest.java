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

package com.amazon.opendistroforelasticsearch.sql.data.type;

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.ARRAY;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.FLOAT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.LONG;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.SHORT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRUCT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.UNKNOWN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

class ExprTypeTest {
  @Test
  public void isCompatible() {
    assertTrue(DOUBLE.isCompatible(DOUBLE));
    assertTrue(DOUBLE.isCompatible(FLOAT));
    assertTrue(DOUBLE.isCompatible(LONG));
    assertTrue(DOUBLE.isCompatible(INTEGER));
    assertTrue(DOUBLE.isCompatible(SHORT));
    assertTrue(FLOAT.isCompatible(FLOAT));
    assertTrue(FLOAT.isCompatible(LONG));
    assertTrue(FLOAT.isCompatible(INTEGER));
    assertTrue(FLOAT.isCompatible(SHORT));
    assertFalse(INTEGER.isCompatible(DOUBLE));
    assertFalse(STRING.isCompatible(DOUBLE));
    assertFalse(INTEGER.isCompatible(UNKNOWN));
  }

  @Test
  public void getParent() {
    assertThat(((ExprType) () -> "test").getParent(), Matchers.contains(UNKNOWN));
  }

  @Test
  void legacyName() {
    assertEquals("keyword", STRING.legacyTypeName());
    assertEquals("nested", ARRAY.legacyTypeName());
    assertEquals("object", STRUCT.legacyTypeName());
    assertEquals("integer", INTEGER.legacyTypeName().toLowerCase());
  }

  // for test coverage.
  @Test
  void defaultLegacyTypeName() {
    final ExprType exprType = () -> "dummy";
    assertEquals("dummy", exprType.legacyTypeName());
  }
}