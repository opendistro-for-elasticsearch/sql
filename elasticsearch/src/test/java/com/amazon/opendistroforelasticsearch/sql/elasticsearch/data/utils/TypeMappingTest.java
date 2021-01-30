/*
 *     Copyright 2021 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License").
 *     You may not use this file except in compliance with the License.
 *     A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *     or in the "license" file accompanying this file. This file is distributed
 *     on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *     express or implied. See the License for the specific language governing
 *     permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.utils;

import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.ARRAY;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.BOOLEAN;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.BYTE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DATE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DATETIME;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.FLOAT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.LONG;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.SHORT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRUCT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.TIME;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.TIMESTAMP;
import static com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.type.ElasticsearchDataType.ES_BINARY;
import static com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.type.ElasticsearchDataType.ES_GEO_POINT;
import static com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.type.ElasticsearchDataType.ES_IP;
import static com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.type.ElasticsearchDataType.ES_TEXT;
import static com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.type.ElasticsearchDataType.ES_TEXT_KEYWORD;
import static org.junit.jupiter.api.Assertions.*;

import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TypeMappingTest {
  private static final Map<String, ExprType> MAPPING =
      new ImmutableMap.Builder<String, ExprType>()
          .put("byteV", BYTE)
          .put("shortV", SHORT)
          .put("intV", INTEGER)
          .put("longV", LONG)
          .put("floatV", FLOAT)
          .put("doubleV", DOUBLE)
          .put("stringV", STRING)
          .put("dateV", DATE)
          .put("datetimeV", DATETIME)
          .put("timeV", TIME)
          .put("timestampV", TIMESTAMP)
          .put("boolV", BOOLEAN)
          .put("structV", STRUCT)
          .put("structV.id", INTEGER)
          .put("structV.state", STRING)
          .put("arrayV", ARRAY)
          .put("arrayV.info", STRING)
          .put("arrayV.author", STRING)
          .put("textV", ES_TEXT)
          .put("textKeywordV", ES_TEXT_KEYWORD)
          .put("ipV", ES_IP)
          .put("geoV", ES_GEO_POINT)
          .put("binaryV", ES_BINARY)
          .build();

  @Test
  public void test() {
    final TypeMapping typeMapping = new TypeMapping.DefaultTypeMapping(MAPPING);
    assertEquals(STRUCT, typeMapping.type("structV"));

    TypeMapping subMapping = typeMapping.subTypeMapping("structV");
    assertEquals(INTEGER, subMapping.type("id"));
    assertEquals(STRING, subMapping.type("state"));

    assertEquals(TypeMapping.EMPTY, typeMapping.subTypeMapping("ipV"));
  }

}