/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License").
 *   You may not use this file except in compliance with the License.
 *   A copy of the License is located at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file. This file is distributed
 *   on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *   express or implied. See the License for the specific language governing
 *   permissions and limitations under the License.
 */

package com.amazon.opendistroforelasticsearch.sql.plugin.request;

import com.amazon.opendistroforelasticsearch.sql.ppl.domain.PPLQueryRequest;
import org.elasticsearch.rest.RestRequest;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Factory of {@link PPLQueryRequest}.
 */
public class PPLQueryRequestFactory {
  private static final String PPL_URL_PARAM_KEY = "ppl";
  private static final String PPL_FIELD_NAME = "query";

  /**
   * Build {@link PPLQueryRequest} from {@link RestRequest}.
   * @param request {@link PPLQueryRequest}
   * @return {@link RestRequest}
   */
  public static PPLQueryRequest getPPLRequest(RestRequest request) {
    switch (request.method()) {
      case GET:
        return parsePPLRequestFromUrl(request);
      case POST:
        return parsePPLRequestFromPayload(request);
      default:
        throw new IllegalArgumentException(
            "ES PPL doesn't supported HTTP " + request.method().name());
    }
  }

  private static PPLQueryRequest parsePPLRequestFromUrl(RestRequest restRequest) {
    String ppl;

    ppl = restRequest.param(PPL_URL_PARAM_KEY);
    if (ppl == null) {
      throw new IllegalArgumentException("Cannot find ppl parameter from the URL");
    }
    return new PPLQueryRequest(ppl, null, restRequest.path());
  }

  private static PPLQueryRequest parsePPLRequestFromPayload(RestRequest restRequest) {
    String content = restRequest.content().utf8ToString();
    JSONObject jsonContent;
    try {
      jsonContent = new JSONObject(content);
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to parse request payload", e);
    }
    return new PPLQueryRequest(jsonContent.getString(PPL_FIELD_NAME),
        jsonContent, restRequest.path());
  }
}
