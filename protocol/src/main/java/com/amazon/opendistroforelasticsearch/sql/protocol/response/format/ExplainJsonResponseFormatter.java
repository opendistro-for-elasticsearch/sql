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

package com.amazon.opendistroforelasticsearch.sql.protocol.response.format;

import com.amazon.opendistroforelasticsearch.sql.executor.ExecutionEngine.ExplainResponse;

/**
 * JSON formatter for explain response which has completely different structure than query response.
 */
public class ExplainJsonResponseFormatter extends JsonResponseFormatter<ExplainResponse>  {

  public ExplainJsonResponseFormatter(Style style) {
    super(style);
  }

  @Override
  protected Object buildJsonObject(ExplainResponse response) {
    /*JSONObject root = new JSONObject();
    doBuild(response.getRoot(), root);
    return root;*/
    return response;
  }

  /*
  private void doBuild(ExplainResponseNode node, JSONObject parent) {
    if (node == null) {
      return;
    }

    JSONObject body = new JSONObject();
    body.put("description", new JSONObject(node.getDescription()));
    parent.put(node.getName(), body);

    doBuild(node.getChild(), body);
  }
  */

}
