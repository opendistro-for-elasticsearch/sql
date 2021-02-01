/*
 *   Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.ppl.domain;

import com.amazon.opendistroforelasticsearch.sql.protocol.response.format.Format;
import com.google.common.base.Strings;
import java.util.Locale;
import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.json.JSONObject;

@RequiredArgsConstructor
public class PPLQueryRequest {
  public static final PPLQueryRequest NULL = new PPLQueryRequest("", null, "", "");

  private final String pplQuery;
  private final JSONObject jsonContent;
  private final String path;
  private String format = "";

  @Setter
  @Getter
  @Accessors(fluent = true)
  private boolean sanitize = true;

  /**
   * Constructor of PPLQueryRequest.
   */
  public PPLQueryRequest(String pplQuery, JSONObject jsonContent, String path, String format) {
    this.pplQuery = pplQuery;
    this.jsonContent = jsonContent;
    this.path = path;
    this.format = format;
  }

  public String getRequest() {
    return pplQuery;
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
    Optional<Format> optionalFormat = Format.of(format);
    if (optionalFormat.isPresent()) {
      return optionalFormat.get();
    } else {
      throw new IllegalArgumentException(
          String.format(Locale.ROOT,"response in %s format is not supported.", format));
    }
  }

}
