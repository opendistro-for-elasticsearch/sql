/*
 *   Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazon.opendistroforelasticsearch.sql.legacy.plugin;

import com.amazon.opendistroforelasticsearch.sql.legacy.executor.format.ErrorMessageFactory;
import com.amazon.opendistroforelasticsearch.sql.legacy.metrics.Metrics;
import com.amazon.opendistroforelasticsearch.sql.legacy.utils.LogUtils;
import com.google.common.collect.ImmutableList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.elasticsearch.rest.RestStatus.SERVICE_UNAVAILABLE;

/**
 * Currently this interface is for node level.
 * Cluster level is coming up soon. https://github.com/opendistro-for-elasticsearch/sql/issues/41
 */
public class RestSqlStatsAction extends BaseRestHandler {
    private static final Logger LOG = LogManager.getLogger(RestSqlStatsAction.class);

    /**
     * API endpoint path
     */
    public static final String STATS_API_ENDPOINT = "/_opendistro/_sql/stats";

    public RestSqlStatsAction(Settings settings, RestController restController) {
        super();
    }

    @Override
    public String getName() {
        return "sql_stats_action";
    }

    @Override
    public List<Route> routes() {
        return ImmutableList.of(
                new Route(RestRequest.Method.POST, STATS_API_ENDPOINT),
                new Route(RestRequest.Method.GET, STATS_API_ENDPOINT)
        );
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) {

        LogUtils.addRequestId();

        try {
            return channel -> channel.sendResponse(new BytesRestResponse(RestStatus.OK,
                    Metrics.getInstance().collectToJSON()));
        } catch (Exception e) {
            LOG.error("Failed during Query SQL STATS Action.", e);

            return channel -> channel.sendResponse(new BytesRestResponse(SERVICE_UNAVAILABLE,
                    ErrorMessageFactory.createErrorMessage(e, SERVICE_UNAVAILABLE.getStatus()).toString()));
        }
    }

    @Override
    protected Set<String> responseParams() {
        Set<String> responseParams = new HashSet<>(super.responseParams());
        responseParams.addAll(Arrays.asList("sql", "flat", "separator", "_score", "_type", "_id", "newLine", "format", "sanitize"));
        return responseParams;
    }

}