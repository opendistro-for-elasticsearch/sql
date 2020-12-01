/*
 *    Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License").
 *    You may not use this file except in compliance with the License.
 *    A copy of the License is located at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    or in the "license" file accompanying this file. This file is distributed
 *    on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *    express or implied. See the License for the specific language governing
 *    permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.sql.sql.domain;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import java.util.Locale;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.json.JSONObject;

/**
 * SQL query request.
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class SQLQueryRequest {

  private static final Set<String> QUERY_FIELD = ImmutableSet.of("query");
  private static final Set<String> QUERY_AND_FETCH_SIZE = ImmutableSet.of("query", "fetch_size");

  /**
   * JSON payload in REST request.
   */
  private final JSONObject jsonContent;

  /**
   * SQL query.
   */
  @Getter
  private final String query;

  /**
   * Request path.
   */
  private final String path;

  /**
   * Request format.
   */
  private final String format;

  /**
   * Response format options.
   */
  public enum Format {
    JDBC,
    CSV
  }

  /**
   * Pre-check if the request can be supported by meeting the following criteria:
   *  1.Only "query" field or "query" and "fetch_size=0" in payload. In other word,
   *  it's not a cursor request with either non-zero "fetch_size" or "cursor" field,
   *  or request with extra field such as "filter".
   *  2.Response format expected is default JDBC format.
   *
   * @return  true if supported.
   */
  public boolean isSupported() {
    return (isOnlyQueryFieldInPayload() || isOnlyQueryAndFetchSizeZeroInPayload())
        && isSupportedFormat();
  }

  /**
   * Check if request is to explain rather than execute the query.
   * @return  true if it is a explain request
   */
  public boolean isExplainRequest() {
    return path.endsWith("/_explain");
  }

  /**
   * Decide on the formatter by the requested format.
   */
  public Format format() {
    if (Strings.isNullOrEmpty(format)) {
      return Format.JDBC;
    }
    switch (format.toLowerCase()) {
      case "jdbc":
        return Format.JDBC;
      case "csv":
        return Format.CSV;

      default:
        throw new IllegalArgumentException(
            String.format(Locale.ROOT, "response in %s format is not supported.", format));
    }
  }

  private boolean isOnlyQueryFieldInPayload() {
    return QUERY_FIELD.equals(jsonContent.keySet());
  }

  private boolean isOnlyQueryAndFetchSizeZeroInPayload() {
    return QUERY_AND_FETCH_SIZE.equals(jsonContent.keySet())
        && (jsonContent.getInt("fetch_size") == 0);
  }

  private boolean isSupportedFormat() {
    return Strings.isNullOrEmpty(format) || "jdbc".equalsIgnoreCase(format)
        || "csv".equalsIgnoreCase(format);
  }

}
