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

import static java.time.temporal.ChronoField.DAY_OF_MONTH;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.MONTH_OF_YEAR;
import static java.time.temporal.ChronoField.NANO_OF_SECOND;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.time.format.SignStyle;
import java.time.temporal.ChronoField;
import java.util.Locale;
import lombok.experimental.UtilityClass;

/**
 * DateTimeFormatter.
 * Reference org.elasticsearch.common.time.DateFormatters.
 */
@UtilityClass
public class ElasticsearchDateFormatters {

  public static final DateTimeFormatter TIME_ZONE_FORMATTER_NO_COLON =
      new DateTimeFormatterBuilder()
          .appendOffset("+HHmm", "Z")
          .toFormatter(Locale.ROOT)
          .withResolverStyle(ResolverStyle.STRICT);

  public static final DateTimeFormatter STRICT_YEAR_MONTH_DAY_FORMATTER =
      new DateTimeFormatterBuilder()
          .appendValue(ChronoField.YEAR, 4, 10, SignStyle.EXCEEDS_PAD)
          .optionalStart()
          .appendLiteral("-")
          .appendValue(MONTH_OF_YEAR, 2, 2, SignStyle.NOT_NEGATIVE)
          .optionalStart()
          .appendLiteral('-')
          .appendValue(DAY_OF_MONTH, 2, 2, SignStyle.NOT_NEGATIVE)
          .optionalEnd()
          .optionalEnd()
          .toFormatter(Locale.ROOT)
          .withResolverStyle(ResolverStyle.STRICT);

  public static final DateTimeFormatter STRICT_HOUR_MINUTE_SECOND_FORMATTER =
      new DateTimeFormatterBuilder()
          .appendValue(HOUR_OF_DAY, 2, 2, SignStyle.NOT_NEGATIVE)
          .appendLiteral(':')
          .appendValue(MINUTE_OF_HOUR, 2, 2, SignStyle.NOT_NEGATIVE)
          .appendLiteral(':')
          .appendValue(SECOND_OF_MINUTE, 2, 2, SignStyle.NOT_NEGATIVE)
          .toFormatter(Locale.ROOT)
          .withResolverStyle(ResolverStyle.STRICT);

  public static final DateTimeFormatter STRICT_DATE_OPTIONAL_TIME_FORMATTER =
      new DateTimeFormatterBuilder()
          .append(STRICT_YEAR_MONTH_DAY_FORMATTER)
          .optionalStart()
          .appendLiteral('T')
          .optionalStart()
          .appendValue(HOUR_OF_DAY, 2, 2, SignStyle.NOT_NEGATIVE)
          .optionalStart()
          .appendLiteral(':')
          .appendValue(MINUTE_OF_HOUR, 2, 2, SignStyle.NOT_NEGATIVE)
          .optionalStart()
          .appendLiteral(':')
          .appendValue(SECOND_OF_MINUTE, 2, 2, SignStyle.NOT_NEGATIVE)
          .optionalStart()
          .appendFraction(NANO_OF_SECOND, 1, 9, true)
          .optionalEnd()
          .optionalStart()
          .appendLiteral(',')
          .appendFraction(NANO_OF_SECOND, 1, 9, false)
          .optionalEnd()
          .optionalEnd()
          .optionalEnd()
          .optionalStart()
          .appendZoneOrOffsetId()
          .optionalEnd()
          .optionalStart()
          .append(TIME_ZONE_FORMATTER_NO_COLON)
          .optionalEnd()
          .optionalEnd()
          .optionalEnd()
          .toFormatter(Locale.ROOT)
          .withResolverStyle(ResolverStyle.STRICT);

  public static final DateTimeFormatter SQL_LITERAL_DATE_TIME_FORMAT = DateTimeFormatter
          .ofPattern("yyyy-MM-dd HH:mm:ss");
}
