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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.value;

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.booleanValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.byteValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.doubleValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.floatValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.integerValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.longValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.nullValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.shortValue;
import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.stringValue;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprCollectionValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprDateValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprDatetimeValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTimeValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTimestampValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTupleValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.utils.Content;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.utils.JsonContent;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.utils.Parser;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.utils.TypeMapping;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.junit.jupiter.api.Test;

class ElasticsearchExprValueFactoryTest {

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
  private ElasticsearchExprValueFactory exprValueFactory =
      new ElasticsearchExprValueFactory(MAPPING);



  @Test
  public void parserTest() throws Exception {
    final Parser parser = new Parser(new TypeMapping.DefaultTypeMapping(MAPPING));
    final ExprValue value = parser.parse(jsonContent("{\"intV\":1}"), STRUCT);

    assertEquals(integerValue(1), value.keyValue("intV"));
  }
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public Content jsonContent(String json) throws Exception {
    return new JsonContent(OBJECT_MAPPER.readTree(json));
  }



  @Test
  public void constructNullValue() {
    assertEquals(nullValue(), tupleValue("{\"intV\":null}").get("intV"));
    assertEquals(nullValue(), constructFromObject("intV",  null));
  }

  @Test
  public void constructByte() {
    assertEquals(byteValue((byte) 1), tupleValue("{\"byteV\":1}").get("byteV"));
  }

  @Test
  public void constructShort() {
    assertEquals(shortValue((short) 1), tupleValue("{\"shortV\":1}").get("shortV"));
  }

  @Test
  public void constructInteger() {
    assertEquals(integerValue(1), tupleValue("{\"intV\":1}").get("intV"));
    assertEquals(integerValue(1), constructFromObject("intV", 1));
  }

  @Test
  public void constructIntegerValueInStringValue() {
    assertEquals(integerValue(1), constructFromObject("intV", "1"));
  }

  @Test
  public void constructLong() {
    assertEquals(longValue(1L), tupleValue("{\"longV\":1}").get("longV"));
    assertEquals(longValue(1L), constructFromObject("longV", 1L));
  }

  @Test
  public void constructFloat() {
    assertEquals(floatValue(1f), tupleValue("{\"floatV\":1.0}").get("floatV"));
    assertEquals(floatValue(1f), constructFromObject("floatV", 1f));
  }

  @Test
  public void constructDouble() {
    assertEquals(doubleValue(1d), tupleValue("{\"doubleV\":1.0}").get("doubleV"));
    assertEquals(doubleValue(1d), constructFromObject("doubleV", 1d));
  }

  @Test
  public void constructString() {
    assertEquals(stringValue("text"), tupleValue("{\"stringV\":\"text\"}").get("stringV"));
    assertEquals(stringValue("text"), constructFromObject("stringV", "text"));
  }

  @Test
  public void constructBoolean() {
    assertEquals(booleanValue(true), tupleValue("{\"boolV\":true}").get("boolV"));
    assertEquals(booleanValue(true), constructFromObject("boolV", true));
  }

  @Test
  public void constructText() {
    assertEquals(new ElasticsearchExprTextValue("text"),
                 tupleValue("{\"textV\":\"text\"}").get("textV"));
    assertEquals(new ElasticsearchExprTextValue("text"),
                 constructFromObject("textV", "text"));

    assertEquals(new ElasticsearchExprTextKeywordValue("text"),
                 tupleValue("{\"textKeywordV\":\"text\"}").get("textKeywordV"));
    assertEquals(new ElasticsearchExprTextKeywordValue("text"),
                 constructFromObject("textKeywordV", "text"));
  }

  @Test
  public void constructDate() {
    assertEquals(
        new ExprTimestampValue("2015-01-01 00:00:00"),
        tupleValue("{\"timestampV\":\"2015-01-01\"}").get("timestampV"));
    assertEquals(
        new ExprTimestampValue("2015-01-01 12:10:30"),
        tupleValue("{\"timestampV\":\"2015-01-01T12:10:30Z\"}").get("timestampV"));
    assertEquals(
        new ExprTimestampValue("2015-01-01 12:10:30"),
        tupleValue("{\"timestampV\":\"2015-01-01T12:10:30\"}").get("timestampV"));
    assertEquals(
        new ExprTimestampValue("2015-01-01 12:10:30"),
        tupleValue("{\"timestampV\":\"2015-01-01 12:10:30\"}").get("timestampV"));
    assertEquals(
        new ExprTimestampValue(Instant.ofEpochMilli(1420070400001L)),
        tupleValue("{\"timestampV\":1420070400001}").get("timestampV"));
    assertEquals(
        new ExprTimeValue("19:36:22"),
        tupleValue("{\"timestampV\":\"19:36:22\"}").get("timestampV"));

    assertEquals(
        new ExprTimestampValue(Instant.ofEpochMilli(1420070400001L)),
        constructFromObject("timestampV", 1420070400001L));
    assertEquals(
        new ExprTimestampValue(Instant.ofEpochMilli(1420070400001L)),
        constructFromObject("timestampV", Instant.ofEpochMilli(1420070400001L)));
    assertEquals(
        new ExprTimestampValue("2015-01-01 12:10:30"),
        constructFromObject("timestampV", "2015-01-01 12:10:30"));
    assertEquals(
        new ExprDateValue("2015-01-01"),
        constructFromObject("dateV","2015-01-01"));
    assertEquals(
        new ExprTimeValue("12:10:30"),
        constructFromObject("timeV","12:10:30"));
    assertEquals(
        new ExprDatetimeValue("2015-01-01 12:10:30"),
        constructFromObject("datetimeV", "2015-01-01 12:10:30"));
  }

  @Test
  public void constructDateFromUnsupportedFormatThrowException() {
    IllegalStateException exception =
        assertThrows(
            IllegalStateException.class, () -> tupleValue("{\"timestampV\":\"2015-01-01 12:10\"}"));
    assertEquals(
        "Construct ExprTimestampValue from \"2015-01-01 12:10\" failed, "
            + "unsupported date format.",
        exception.getMessage());
  }

  @Test
  public void constructArray() {
    assertEquals(
        new ExprCollectionValue(ImmutableList.of(new ExprTupleValue(
            new LinkedHashMap<String, ExprValue>() {
              {
                put("info", stringValue("zz"));
                put("author", stringValue("au"));
              }
            }))),
        tupleValue("{\"arrayV\":[{\"info\":\"zz\",\"author\":\"au\"}]}").get("arrayV"));
  }

  @Test
  public void constructStruct() {
    assertEquals(
        new ExprTupleValue(
            new LinkedHashMap<String, ExprValue>() {
              {
                put("id", integerValue(1));
                put("state", stringValue("WA"));
              }
            }),
        tupleValue("{\"structV\":{\"id\":1,\"state\":\"WA\"}}").get("structV"));
  }

  @Test
  public void constructIP() {
    assertEquals(new ElasticsearchExprIpValue("192.168.0.1"),
        tupleValue("{\"ipV\":\"192.168.0.1\"}").get("ipV"));
  }

  @Test
  public void constructGeoPoint() {
    assertEquals(new ElasticsearchExprGeoPointValue(42.60355556, -97.25263889),
        tupleValue("{\"geoV\":{\"lat\":42.60355556,\"lon\":-97.25263889}}").get("geoV"));
  }

  @Test
  public void constructBinary() {
    assertEquals(new ElasticsearchExprBinaryValue("U29tZSBiaW5hcnkgYmxvYg=="),
        tupleValue("{\"binaryV\":\"U29tZSBiaW5hcnkgYmxvYg==\"}").get("binaryV"));
  }

  @Test
  public void constructFromInvalidJsonThrowException() {
    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> tupleValue("{\"invalid_json:1}"));
    assertEquals("invalid json: {\"invalid_json:1}.", exception.getMessage());
  }

  @Test
  public void noTypeFoundForMappingThrowException() {
    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> tupleValue("{\"not_exist\":1}"));
    assertEquals("No type found for field: not_exist.", exception.getMessage());
  }

  @Test
  public void constructUnsupportedTypeThrowException() {
    ElasticsearchExprValueFactory exprValueFactory =
        new ElasticsearchExprValueFactory(ImmutableMap.of("type", new TestType()));
    IllegalStateException exception =
        assertThrows(IllegalStateException.class, () -> exprValueFactory.construct("{\"type\":1}"));
    assertEquals("Unsupported type: TEST_TYPE for field: type, value: 1.", exception.getMessage());

    exception =
        assertThrows(IllegalStateException.class, () -> exprValueFactory.construct("type", 1));
    assertEquals(
        "Unsupported type TEST_TYPE to construct expression value "
            + "from object for field: type, value: 1.",
        exception.getMessage());
  }

  public Map<String, ExprValue> tupleValue(String jsonString) {
    final ExprValue construct = exprValueFactory.construct(jsonString);
    return construct.tupleValue();
  }

  private ExprValue constructFromObject(String fieldName, Object value) {
    return exprValueFactory.construct(fieldName, value);
  }

  @EqualsAndHashCode
  @ToString
  private static class TestType implements ExprType {

    @Override
    public String typeName() {
      return "TEST_TYPE";
    }
  }
}
