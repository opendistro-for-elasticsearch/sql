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

package com.amazon.opendistroforelasticsearch.sql.plugin.rest;

import static org.elasticsearch.rest.RestStatus.SERVICE_UNAVAILABLE;

import com.amazon.opendistroforelasticsearch.sql.legacy.executor.format.ErrorMessageFactory;
import com.amazon.opendistroforelasticsearch.sql.legacy.metrics.Metrics;
import com.amazon.opendistroforelasticsearch.sql.legacy.utils.LogUtils;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;

/**
 * PPL Node level status.
 */
public class RestPPLStatsAction extends BaseRestHandler {

  private static final Logger LOG = LogManager.getLogger(RestPPLStatsAction.class);

  /**
   * API endpoint path.
   */
  public static final String PPL_STATS_API_ENDPOINT = "/_opendistro/_ppl/stats";

  public RestPPLStatsAction(Settings settings, RestController restController) {
    super();
  }

  @Override
  public String getName() {
    return "ppl_stats_action";
  }

  @Override
  public List<Route> routes() {
    return ImmutableList.of(
        new Route(RestRequest.Method.POST, PPL_STATS_API_ENDPOINT),
        new Route(RestRequest.Method.GET, PPL_STATS_API_ENDPOINT)
    );
  }

  @Override
  protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) {

    LogUtils.addRequestId();

    try {
      return channel -> channel.sendResponse(new BytesRestResponse(RestStatus.OK,
          Metrics.getInstance().collectToJSON()));
    } catch (Exception e) {
      LOG.error("Failed during Query PPL STATS Action.", e);

      return channel -> channel.sendResponse(new BytesRestResponse(SERVICE_UNAVAILABLE,
          ErrorMessageFactory.createErrorMessage(e, SERVICE_UNAVAILABLE.getStatus()).toString()));
    }
  }
}
