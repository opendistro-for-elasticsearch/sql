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
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.value.ElasticsearchExprBinaryValue;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.value.ElasticsearchExprGeoPointValue;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.value.ElasticsearchExprIpValue;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.value.ElasticsearchExprTextKeywordValue;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.data.value.ElasticsearchExprTextValue;
import com.google.common.collect.ImmutableMap;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.common.time.DateFormatters;

@RequiredArgsConstructor
public class Parser {
  /**
   * The mapping of field and ExprType.
   */
  private final TypeMapping typeMapping;

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      new DateTimeFormatterBuilder()
          .appendOptional(SQL_LITERAL_DATE_TIME_FORMAT)
          .appendOptional(STRICT_DATE_OPTIONAL_TIME_FORMATTER)
          .appendOptional(STRICT_HOUR_MINUTE_SECOND_FORMATTER)
          .toFormatter();

  private Map<ExprType, Function<Content, ExprValue>> map =
      new ImmutableMap.Builder<ExprType, Function<Content, ExprValue>>()
          .put(INTEGER, c -> new ExprIntegerValue(c.intValue()))
          .put(LONG, c -> new ExprLongValue(c.longValue()))
          .put(SHORT, c -> new ExprShortValue(c.shortValue()))
          .put(BYTE, c -> new ExprByteValue(c.byteValue()))
          .put(FLOAT, c -> new ExprFloatValue(c.floatValue()))
          .put(DOUBLE, c -> new ExprDoubleValue(c.doubleValue()))
          .put(STRING, c -> new ExprStringValue(c.stringValue()))
          .put(BOOLEAN, c -> ExprBooleanValue.of(c.booleanValue()))
          .put(TIMESTAMP, this::parseTimestamp)
          .put(DATE, c -> new ExprDateValue(parseTimestamp(c).dateValue().toString()))
          .put(TIME, c -> new ExprTimeValue(parseTimestamp(c).timeValue().toString()))
          .put(DATETIME, c -> new ExprDatetimeValue(parseTimestamp(c).datetimeValue()))
          .put(ES_TEXT, c -> new ElasticsearchExprTextValue(c.stringValue()))
          .put(ES_TEXT_KEYWORD, c -> new ElasticsearchExprTextKeywordValue(c.stringValue()))
          .put(ES_IP, c -> new ElasticsearchExprIpValue(c.stringValue()))
          .put(ES_GEO_POINT, c -> new ElasticsearchExprGeoPointValue(c.geoValue().getLeft(),
              c.geoValue().getRight()))
          .put(ES_BINARY, c -> new ElasticsearchExprBinaryValue(c.stringValue()))
          .put(STRUCT, this::parseStruct)
          .put(ARRAY, this::parseArray)
          .build();

  public ExprValue parse(Content content, ExprType type) {
    if (map.containsKey(type)) {
      return map.get(type).apply(content);
    } else {
      throw new IllegalStateException(
          String.format(
              "Unsupported type: %s for value: %s.", type.typeName(), content.objectValue()));
    }
  }

  public ExprValue parse(Content content, String field) {
    return parse(content, typeMapping.type(field));
  }

  private ExprValue parseStruct(Content content) {
    LinkedHashMap<String, ExprValue> result = new LinkedHashMap<>();
    for (Map.Entry<String, Content> entry : content.map().entrySet()) {
      result.put(entry.getKey(),
          new Parser(typeMapping.subTypeMapping(entry.getKey())).parse(entry.getValue(),
              typeMapping.type(entry.getKey())));
    }
    return new ExprTupleValue(result);
  }

  private ExprValue parseArray(Content content) {
    List<ExprValue> result = new ArrayList<>();
    content.array().forEachRemaining(v -> result.add(parseStruct(v)));
    return new ExprCollectionValue(result);
  }

  private ExprValue parseTimestamp(Content value) {
    ExprValue exprValue;
    if (value.isNumber()) {
      return new ExprTimestampValue(Instant.ofEpochMilli(value.longValue()));
    } else if (value.isString()) {
      exprValue = constructTimestamp(value.stringValue());
    } else {
      exprValue = new ExprTimestampValue((Instant) value.objectValue());
    }
    return exprValue;
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
}
