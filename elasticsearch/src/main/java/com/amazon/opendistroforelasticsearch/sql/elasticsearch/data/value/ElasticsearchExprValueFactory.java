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
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.DOUBLE;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.FLOAT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.INTEGER;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.LONG;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRING;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.STRUCT;
import static com.amazon.opendistroforelasticsearch.sql.data.type.ExprCoreType.TIMESTAMP;
import static com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.type.ElasticsearchDataType.ES_TEXT;
import static com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.value.ElasticsearchDateFormatters.SQL_LITERAL_DATE_TIME_FORMAT;
import static com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.value.ElasticsearchDateFormatters.STRICT_DATE_OPTIONAL_TIME_FORMATTER;

import com.amazon.opendistroforelasticsearch.sql.data.model.ExprBooleanValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprCollectionValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprDoubleValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprFloatValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprIntegerValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprLongValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprStringValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTimestampValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprTupleValue;
import com.amazon.opendistroforelasticsearch.sql.data.model.ExprValue;
import com.amazon.opendistroforelasticsearch.sql.data.type.ExprType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.common.time.DateFormatters;

/** Construct ExprValue from Elasticsearch response. */
@RequiredArgsConstructor
public class ElasticsearchExprValueFactory {
  /** The Mapping of Field and ExprType. */
  private final Map<String, ExprType> typeMapping;

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      new DateTimeFormatterBuilder()
          .appendOptional(SQL_LITERAL_DATE_TIME_FORMAT)
          .appendOptional(STRICT_DATE_OPTIONAL_TIME_FORMATTER)
          .toFormatter();

  private static final String TOP_PATH = "";

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  /**
   * The struct construction has the following assumption. 1. The field has Elasticsearch Object
   * data type. https://www.elastic.co/guide/en/elasticsearch/reference/current/object.html 2. The
   * deeper field is flattened in the typeMapping. e.g. {"employ", "STRUCT"} {"employ.id",
   * "INTEGER"} {"employ.state", "STRING"}
   */
  public ExprTupleValue construct(String jsonString) {
    try {
      return constructStruct(OBJECT_MAPPER.readTree(jsonString), TOP_PATH);
    } catch (JsonProcessingException e) {
      throw new IllegalStateException(String.format("invalid json: %s.", jsonString), e);
    }
  }

  /** Construct ExprValue from field and value pair. */
  private ExprValue construct(String field, JsonNode value) {
    if (value.isNull()) {
      return nullValue();
    }

    ExprType type = type(field);
    if (type.equals(INTEGER)) {
      return constructInteger(value);
    } else if (type.equals(LONG)) {
      return constructLong(value);
    } else if (type.equals(FLOAT)) {
      return constructFloat(value);
    } else if (type.equals(DOUBLE)) {
      return constructDouble(value);
    } else if (type.equals(STRING)) {
      return constructString(value);
    } else if (type.equals(BOOLEAN)) {
      return constructBoolean(value);
    } else if (type.equals(STRUCT)) {
      return constructStruct(value, field);
    } else if (type.equals(ARRAY)) {
      return constructArray(value, field);
    } else if (type.equals(TIMESTAMP)) {
      return constructTimestamp(value);
    } else if (type.equals(ES_TEXT)) {
      return new ElasticsearchExprTextValue(value.asText());
    } else {
      throw new IllegalStateException(
          String.format(
              "Unsupported type: %s for field: %s, value: %s.", type.typeName(), field, value));
    }
  }

  private ExprType type(String field) {
    if (typeMapping.containsKey(field)) {
      return typeMapping.get(field);
    } else {
      throw new IllegalStateException(String.format("No type found for field: %s.", field));
    }
  }

  private ExprIntegerValue constructInteger(JsonNode value) {
    return new ExprIntegerValue(value.intValue());
  }

  private ExprLongValue constructLong(JsonNode value) {
    return new ExprLongValue(value.longValue());
  }

  private ExprFloatValue constructFloat(JsonNode value) {
    return new ExprFloatValue(value.floatValue());
  }

  private ExprDoubleValue constructDouble(JsonNode value) {
    return new ExprDoubleValue(value.doubleValue());
  }

  private ExprStringValue constructString(JsonNode value) {
    return new ExprStringValue(value.textValue());
  }

  private ExprBooleanValue constructBoolean(JsonNode value) {
    return ExprBooleanValue.of(value.booleanValue());
  }

  /**
   * Only default strict_date_optional_time||epoch_millis is supported.
   * https://www.elastic.co/guide/en/elasticsearch/reference/current/date.html
   * The customized date_format is not supported.
   */
  private ExprValue constructTimestamp(JsonNode value) {
    try {
      if (value.getNodeType().equals(JsonNodeType.NUMBER)) {
        return new ExprTimestampValue(Instant.ofEpochMilli(value.asLong()));
      } else {
        return new ExprTimestampValue(
            // Using Elasticsearch DateFormatters for now.
            DateFormatters.from(DATE_TIME_FORMATTER.parse(value.asText())).toInstant());
      }
    } catch (DateTimeParseException e) {
      throw new IllegalStateException(
          String.format(
              "Construct ExprTimestampValue from %s failed, unsupported date format.", value),
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
