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

package com.amazon.opendistroforelasticsearch.sql.plugin;

import com.amazon.opendistroforelasticsearch.sql.executor.format.ErrorMessageFactory;

import com.amazon.opendistroforelasticsearch.sql.utils.LogUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.admin.cluster.settings.ClusterUpdateSettingsRequest;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestController;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.action.RestToXContentListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.elasticsearch.rest.RestStatus.INTERNAL_SERVER_ERROR;

/**
 * Interface to manage opendistro.sql.* cluster settings
 * All non-sql settings are ignored.
 * Any non-transient and non-persistent settings are ignored.
 */
public class RestSqlSettingsAction extends BaseRestHandler {
    private static final Logger LOG = LogManager.getLogger(RestSqlSettingsAction.class);

    private static final String PERSISTENT = "persistent";
    private static final String TRANSIENT = "transient";
    private static final String SQL_SETTINGS_PREFIX = "opendistro.sql.";

    /**
     * API endpoint path
     */
    public static final String SETTINGS_API_ENDPOINT = "/_opendistro/_sql/settings";

    public RestSqlSettingsAction(Settings settings, RestController restController) {
        super();
        restController.registerHandler(RestRequest.Method.POST, SETTINGS_API_ENDPOINT, this);
    }

    @Override
    public String getName() {
        return "sql_stats_action";
    }

    /**
     * @see org.elasticsearch.rest.action.admin.cluster.RestClusterUpdateSettingsAction
     */
    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
        LogUtils.addRequestId();
        final ClusterUpdateSettingsRequest clusterUpdateSettingsRequest = Requests.clusterUpdateSettingsRequest();
        clusterUpdateSettingsRequest.timeout(request.paramAsTime("timeout", clusterUpdateSettingsRequest.timeout()));
        clusterUpdateSettingsRequest.masterNodeTimeout(
                request.paramAsTime("master_timeout", clusterUpdateSettingsRequest.masterNodeTimeout()));
        Map<String, Object> source;
        try (XContentParser parser = request.contentParser()) {
            source = parser.map();
        }

        try {
            if (source.containsKey(TRANSIENT)) {
                clusterUpdateSettingsRequest.transientSettings((Map) source.get(TRANSIENT));
            }
            if (source.containsKey(PERSISTENT)) {
                clusterUpdateSettingsRequest.persistentSettings((Map) source.get(PERSISTENT));
            }

            // filter out all non-sql settings
            clusterUpdateSettingsRequest.transientSettings(
                    filterSettings(clusterUpdateSettingsRequest.transientSettings())
            );
            clusterUpdateSettingsRequest.persistentSettings(
                    filterSettings(clusterUpdateSettingsRequest.persistentSettings())
            );

            return channel -> client.admin().cluster().updateSettings(
                    clusterUpdateSettingsRequest, new RestToXContentListener<>(channel));
        } catch (Exception e) {
            LOG.error("Error changing OpenDistro SQL plugin cluster settings", e);
            e.printStackTrace();
            return channel -> channel.sendResponse(new BytesRestResponse(INTERNAL_SERVER_ERROR,
                    ErrorMessageFactory.createErrorMessage(e, INTERNAL_SERVER_ERROR.getStatus()).toString()));
        }
    }

    @Override
    protected Set<String> responseParams() {
        Set<String> responseParams = new HashSet<>(super.responseParams());
        responseParams.addAll(Arrays.asList("sql", "flat", "separator", "_score", "_type", "_id", "newLine", "format"));
        return responseParams;
    }

    private Settings filterSettings(Settings settings) {
        Settings.Builder builder = Settings.builder().put(settings);
        builder.keys().removeIf(key -> !key.startsWith(SQL_SETTINGS_PREFIX));
        return builder.build();
    }
}