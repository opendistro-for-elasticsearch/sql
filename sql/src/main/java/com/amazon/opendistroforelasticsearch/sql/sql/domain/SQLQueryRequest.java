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
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.json.JSONObject;

/**
 * SQL query request.
 */
@ToString
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class SQLQueryRequest {

  /**
   * JSON payload in REST request.
   */
  private final JSONObject jsonContent;

  /**
   * SQL query.
   */
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
   * Pre-check if the request can be supported by meeting the following criteria:
   *  1.Not explain request
   *  2.Only "query" field in payload. In other word, it's not a cursor request
   *   (with either "fetch_size" or "cursor" field) or request with extra field
   *   such as "filter".
   *  3.Response format expected is default JDBC format.
   *
   * @return  true if supported.
   */
  public boolean isSupported() {
    return !isExplainRequest()
        && isOnlyQueryFieldInPayload()
        && isDefaultFormat();
  }

  private boolean isExplainRequest() {
    return path.endsWith("/_explain");
  }

  private boolean isOnlyQueryFieldInPayload() {
    return (jsonContent.keySet().size() == 1 && jsonContent.has("query"))
        || (jsonContent.keySet().size() == 2 && jsonContent.has("query")
            && jsonContent.has("fetch_size") && jsonContent.getInt("fetch_size") == 0);
  }

  private boolean isDefaultFormat() {
    return Strings.isNullOrEmpty(format) || "jdbc".equalsIgnoreCase(format);
  }

}
