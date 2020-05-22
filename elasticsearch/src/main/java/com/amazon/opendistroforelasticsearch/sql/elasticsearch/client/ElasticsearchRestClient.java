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

package com.amazon.opendistroforelasticsearch.sql.elasticsearch.client;

import com.amazon.opendistroforelasticsearch.sql.elasticsearch.mapping.IndexMapping;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.request.ElasticsearchRequest;
import com.amazon.opendistroforelasticsearch.sql.elasticsearch.response.ElasticsearchResponse;
import lombok.RequiredArgsConstructor;
import org.elasticsearch.action.search.ClearScrollRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.GetMappingsRequest;
import org.elasticsearch.client.indices.GetMappingsResponse;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Elasticsearch REST client to support standalone mode that runs entire engine from remote.
 *
 * TODO: Support for authN and authZ with AWS Sigv4 or security plugin.
 */
@RequiredArgsConstructor
public class ElasticsearchRestClient implements ElasticsearchClient {

    /**
     * Elasticsearch high level REST client
     */
    private final RestHighLevelClient client;


    @Override
    public Map<String, IndexMapping> getIndexMappings(String indexExpression) {
        GetMappingsRequest request = new GetMappingsRequest().indices(indexExpression);
        try {
            GetMappingsResponse response = client.indices().getMapping(request, RequestOptions.DEFAULT);
            return response.mappings().
                            entrySet().
                            stream().
                            collect(Collectors.toMap(
                                Map.Entry::getKey,
                                e -> new IndexMapping(e.getValue()))
                            );
        } catch (IOException e) {
            throw new IllegalStateException(
                "Failed to get index mappings for " + indexExpression, e);
        }
    }

    @Override
    public ElasticsearchResponse search(ElasticsearchRequest request) {
        try {
            SearchResponse esResponse;
            if (request.isScrollStarted()) {
                esResponse = client.scroll(request.scrollRequest(), RequestOptions.DEFAULT);
            } else {
                esResponse = client.search(request.searchRequest(), RequestOptions.DEFAULT);
                request.setScrollId(esResponse.getScrollId());
            }

            ElasticsearchResponse response = new ElasticsearchResponse(esResponse);
            if (response.isEmpty()) {
                ClearScrollRequest clearRequest = new ClearScrollRequest();
                clearRequest.addScrollId(esResponse.getScrollId());
                client.clearScroll(clearRequest, RequestOptions.DEFAULT);
            }
            return response;
        } catch (IOException e) {
            throw new IllegalStateException(
                "Failed to perform search operation with request=" + request, e);
        }
    }

    @Override
    public void schedule(Runnable task) {
        task.run();
    }

}
