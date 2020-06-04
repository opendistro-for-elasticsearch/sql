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

package com.amazon.opendistroforelasticsearch.sql.legacy.esdomain;

import com.amazon.opendistroforelasticsearch.sql.legacy.query.join.BackOffRetryStrategy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.client.Client;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ESClient {

    private static final Logger LOG = LogManager.getLogger();
    private static final int[] retryIntervals = new int[]{4, 12, 20, 20};
    private final Client client;

    public ESClient(Client client) {
        this.client = client;
    }

    public MultiSearchResponse.Item[] multiSearch(MultiSearchRequest multiSearchRequest) {
        MultiSearchResponse.Item[] responses = new MultiSearchResponse.Item[multiSearchRequest.requests().size()];
        multiSearchRetry(responses, multiSearchRequest,
                IntStream.range(0, multiSearchRequest.requests().size()).boxed().collect(Collectors.toList()), 0);

        return responses;
    }

    private void multiSearchRetry(MultiSearchResponse.Item[] responses, MultiSearchRequest multiSearchRequest,
                                  List<Integer> indices, int retry) {
        MultiSearchRequest multiSearchRequestRetry = new MultiSearchRequest();
        for (int i : indices) {
            multiSearchRequestRetry.add(multiSearchRequest.requests().get(i));
        }
        MultiSearchResponse.Item[] res = client.multiSearch(multiSearchRequestRetry).actionGet().getResponses();
        List<Integer> indicesFailure = new ArrayList<>();
        //Could get EsRejectedExecutionException and ElasticsearchException as getCause
        for (int i = 0; i < res.length; i++) {
            if (res[i].isFailure()) {
                indicesFailure.add(indices.get(i));
                if (retry == 3) {
                    responses[indices.get(i)] = res[i];
                }
            } else {
                responses[indices.get(i)] = res[i];
            }
        }
        if (!indicesFailure.isEmpty()) {
            LOG.info("ES multisearch has failures on retry {}", retry);
            if (retry < 3) {
                BackOffRetryStrategy.backOffSleep(retryIntervals[retry]);
                multiSearchRetry(responses, multiSearchRequest, indicesFailure, retry + 1);
            }
        }
    }
}
