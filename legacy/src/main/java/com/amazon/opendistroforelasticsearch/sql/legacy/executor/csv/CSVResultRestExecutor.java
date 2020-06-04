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

package com.amazon.opendistroforelasticsearch.sql.legacy.executor.csv;

import com.amazon.opendistroforelasticsearch.sql.legacy.executor.QueryActionElasticExecutor;
import com.amazon.opendistroforelasticsearch.sql.legacy.executor.RestExecutor;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.QueryAction;
import com.amazon.opendistroforelasticsearch.sql.legacy.query.join.BackOffRetryStrategy;
import com.google.common.base.Joiner;
import org.elasticsearch.client.Client;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestChannel;
import org.elasticsearch.rest.RestStatus;

import java.util.List;
import java.util.Map;

/**
 * Created by Eliran on 26/12/2015.
 */
public class CSVResultRestExecutor implements RestExecutor {

    @Override
    public void execute(final Client client, final Map<String, String> params, final QueryAction queryAction,
                        final RestChannel channel) throws Exception {

        final String csvString = execute(client, params, queryAction);
        final BytesRestResponse bytesRestResponse = new BytesRestResponse(RestStatus.OK, csvString);

        if (!BackOffRetryStrategy.isHealthy(2 * bytesRestResponse.content().length(), this)) {
            throw new IllegalStateException(
                    "[CSVResultRestExecutor] Memory could be insufficient when sendResponse().");
        }

        channel.sendResponse(bytesRestResponse);
    }

    @Override
    public String execute(final Client client, final Map<String, String> params, final QueryAction queryAction)
            throws Exception {

        final Object queryResult = QueryActionElasticExecutor.executeAnyAction(client, queryAction);

        final String separator = params.getOrDefault("separator", ",");
        final String newLine = params.getOrDefault("newLine", "\n");

        final boolean flat = getBooleanOrDefault(params, "flat", false);
        final boolean includeScore = getBooleanOrDefault(params, "_score", false);
        final boolean includeType = getBooleanOrDefault(params, "_type", false);
        final boolean includeId = getBooleanOrDefault(params, "_id", false);

        final List<String> fieldNames = queryAction.getFieldNames().orElse(null);
        final CSVResult result = new CSVResultsExtractor(includeScore, includeType, includeId)
                .extractResults(queryResult, flat, separator, fieldNames);

        return buildString(separator, result, newLine);
    }

    private boolean getBooleanOrDefault(Map<String, String> params, String param, boolean defaultValue) {
        boolean flat = defaultValue;
        if (params.containsKey(param)) {
            flat = Boolean.parseBoolean(params.get(param));
        }
        return flat;
    }

    private String buildString(String separator, CSVResult result, String newLine) {
        StringBuilder csv = new StringBuilder();
        csv.append(Joiner.on(separator).join(result.getHeaders()));
        csv.append(newLine);
        csv.append(Joiner.on(newLine).join(result.getLines()));
        return csv.toString();
    }

}
