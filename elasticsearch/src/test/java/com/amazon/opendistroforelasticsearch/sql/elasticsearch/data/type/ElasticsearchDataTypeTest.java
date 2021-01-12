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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.type;

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.type.ElasticsearchDataType.ES_TEXT;
import static com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.type.ElasticsearchDataType.ES_TEXT_KEYWORD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ElasticsearchDataTypeTest {
  @Test
  public void testIsCompatible() {
    assertTrue(STRING.isCompatible(ES_TEXT));
    assertFalse(ES_TEXT.isCompatible(STRING));

    assertTrue(STRING.isCompatible(ES_TEXT_KEYWORD));
    assertTrue(ES_TEXT.isCompatible(ES_TEXT_KEYWORD));
  }

  @Test
  public void testTypeName() {
    assertEquals("string", ES_TEXT.typeName());
    assertEquals("string", ES_TEXT_KEYWORD.typeName());
  }

  @Test
  public void legacyTypeName() {
    assertEquals("text", ES_TEXT.legacyTypeName());
    assertEquals("text", ES_TEXT_KEYWORD.legacyTypeName());
  }
}