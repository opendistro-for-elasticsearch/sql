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

import static com.amazon.opendistroforelasticsearch.sql.data.model.ExprValueUtils.nullValue;
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
import static com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.value.ElasticsearchDateFormatters.SQL_LITERAL_DATE_TIME_FORMAT;
import static com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.value.ElasticsearchDateFormatters.STRICT_DATE_OPTIONAL_TIME_FORMATTER;
import static com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.value.ElasticsearchDateFormatters.STRICT_HOUR_MINUTE_SECOND_FORMATTER;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprBooleanValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprByteValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprCollectionValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprDateValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprDatetimeValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprDoubleValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprFloatValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprIntegerValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprLongValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprShortValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprStringValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTimeValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTimestampValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTupleValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.utils.JsonContent;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.utils.ObjectContent;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.utils.Parser;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.utils.TypeMapping;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.Setter;
import org.elasticsearch.common.time.DateFormatters;

/** Construct ExprValue from Elasticsearch response. */
public class ElasticsearchExprValueFactory {
  /** The Mapping of Field and ExprType. */
  private Map<String, ExprType> typeMapping;

  private Parser parser;

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      new DateTimeFormatterBuilder()
          .appendOptional(SQL_LITERAL_DATE_TIME_FORMAT)
          .appendOptional(STRICT_DATE_OPTIONAL_TIME_FORMATTER)
          .appendOptional(STRICT_HOUR_MINUTE_SECOND_FORMATTER)
          .toFormatter();

  private static final String TOP_PATH = "";

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  public ElasticsearchExprValueFactory(
      Map<String, ExprType> typeMapping) {
    this.typeMapping = typeMapping;
    this.parser = new Parser(new TypeMapping.DefaultTypeMapping(typeMapping));
  }

  public void setTypeMapping(Map<String, ExprType> typeMapping) {
    parser = new Parser(new TypeMapping.DefaultTypeMapping(typeMapping));
  }

  /**
   * The struct construction has the following assumption. 1. The field has Elasticsearch Object
   * data type. https://www.elastic.co/guide/en/elasticsearch/reference/current/object.html 2. The
   * deeper field is flattened in the typeMapping. e.g. {"employ", "STRUCT"} {"employ.id",
   * "INTEGER"} {"employ.state", "STRING"}
   */
  public ExprValue construct(String jsonString) {
    try {
//      return constructStruct(OBJECT_MAPPER.readTree(jsonString), TOP_PATH);

      final JsonNode value = OBJECT_MAPPER.readTree(jsonString);

      return parser.parse(new JsonContent(value), STRUCT);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(String.format("invalid json: %s.", jsonString), e);
    }
  }

  /** Construct ExprValue from field and value pair. */
  private ExprValue construct(String field, JsonNode value) {
    if (value.isNull()) {
      return nullValue();
    }

    return parser.parse(new JsonContent(value), STRUCT);
  }

  /**
   * Construct ExprValue from field and its value object. Throw exception if trying
   * to construct from field of unsupported type.
   * Todo, add IP, GeoPoint support after we have function implementation around it.
   *
   * @param field   field name
   * @param value   value object
   * @return        ExprValue
   */
  public ExprValue construct(String field, Object value) {
    if (value == null) {
      return nullValue();
    }

    return parser.parse(new ObjectContent(value), field);
  }

  /**
   * Elastisearch could return value String value even the type is Number.
   */
  private <T> T parseNumberValue(Object value, Function<String, T> stringTFunction,
                                 Function<Number, T> numberTFunction) {
    if (value instanceof String) {
      return stringTFunction.apply((String) value);
    } else {
      return numberTFunction.apply((Number) value);
    }
  }

  private ExprType type(String field) {
    if (typeMapping.containsKey(field)) {
      return typeMapping.get(field);
    } else {
      throw new IllegalStateException(String.format("No type found for field: %s.", field));
    }
  }

  private ExprIntegerValue constructInteger(Integer value) {
    return new ExprIntegerValue(value);
  }

  private ExprLongValue constructLong(Long value) {
    return new ExprLongValue(value);
  }

  private ExprShortValue constructShort(Short value) {
    return new ExprShortValue(value);
  }

  private ExprByteValue constructByte(Byte value) {
    return new ExprByteValue(value);
  }

  private ExprFloatValue constructFloat(Float value) {
    return new ExprFloatValue(value);
  }

  private ExprDoubleValue constructDouble(Double value) {
    return new ExprDoubleValue(value);
  }

  private ExprStringValue constructString(String value) {
    return new ExprStringValue(value);
  }

  private ExprBooleanValue constructBoolean(Boolean value) {
    return ExprBooleanValue.of(value);
  }

  private ExprValue constructTimestamp(Long value) {
    return constructTimestamp(Instant.ofEpochMilli(value));
  }

  private ExprValue constructTimestamp(Instant instant) {
    return new ExprTimestampValue(instant);
  }

  /**
   * Only default strict_date_optional_time||epoch_millis is supported.
   * https://www.elastic.co/guide/en/elasticsearch/reference/current/date.html
   * The customized date_format is not supported.
   */
  private ExprValue constructTimestamp(String value) {
    try {
      return new ExprTimestampValue(
          // Using Elasticsearch DateFormatters for now.
          DateFormatters.from(DATE_TIME_FORMATTER.parse(value)).toInstant());
    } catch (DateTimeParseException e) {
      throw new IllegalStateException(
          String.format(
              "Construct ExprTimestampValue from \"%s\" failed, unsupported date format.", value),
          e);
    }
  }

  private ExprTupleValue constructStruct(JsonNode value, String path) {
    LinkedHashMap<String, ExprValue> map = new LinkedHashMap<>();
    value
        .fieldNames()
        .forEachRemaining(
            field -> map.put(field, construct(makeField(path, field), value.get(field))));
    return new ExprTupleValue(map);
  }

  /**
   * Todo. ARRAY is not support now. In Elasticsearch, there is no dedicated array data type.
   * https://www.elastic.co/guide/en/elasticsearch/reference/current/array.html. The similar data
   * type is nested, but it can only allow a list of objects.
   */
  private ExprCollectionValue constructArray(JsonNode value, String path) {
    List<ExprValue> list = new ArrayList<>();
    value
        .elements()
        .forEachRemaining(
            node -> {
              list.add(constructStruct(node, path));
            });
    return new ExprCollectionValue(list);
  }

  private String makeField(String path, String field) {
    return path.equalsIgnoreCase(TOP_PATH) ? field : String.join(".", path, field);
  }
}
