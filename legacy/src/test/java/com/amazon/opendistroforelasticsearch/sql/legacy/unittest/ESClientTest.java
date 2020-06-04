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

package com.amazon.opendistroforelasticsearch.sql.legacy.unittest;

import com.amazon.opendistroforelasticsearch.sql.legacy.esdomain.ESClient;
import org.elasticsearch.action.ActionFuture;
import org.elasticsearch.action.search.MultiSearchRequest;
import org.elasticsearch.action.search.MultiSearchResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ESClientTest {

    @Mock
    protected Client client;

    @Before
    public void init() {
        MockitoAnnotations.initMocks(this);
        ActionFuture<MultiSearchResponse> mockFuture = mock(ActionFuture.class);
        when(client.multiSearch(any())).thenReturn(mockFuture);

        MultiSearchResponse response = mock(MultiSearchResponse.class);
        when(mockFuture.actionGet()).thenReturn(response);

        MultiSearchResponse.Item item0 = new MultiSearchResponse.Item(mock(SearchResponse.class), null);
        MultiSearchResponse.Item item1 = new MultiSearchResponse.Item(mock(SearchResponse.class), new Exception());
        MultiSearchResponse.Item[] itemsRetry0 = new MultiSearchResponse.Item[]{item0, item1};
        MultiSearchResponse.Item[] itemsRetry1 = new MultiSearchResponse.Item[]{item0};
        when(response.getResponses()).thenAnswer(new Answer<MultiSearchResponse.Item[]>() {
            private int callCnt;

            @Override
            public MultiSearchResponse.Item[] answer(InvocationOnMock invocation) {
                return callCnt++ == 0 ? itemsRetry0 : itemsRetry1;
            }
        });
    }

    @Test
    public void multiSearchRetryOneTime() {
        ESClient esClient = new ESClient(client);
        MultiSearchResponse.Item[] res = esClient.multiSearch(new MultiSearchRequest().add(new SearchRequest()).add(new SearchRequest()));
        Assert.assertEquals(res.length, 2);
        Assert.assertFalse(res[0].isFailure());
        Assert.assertFalse(res[1].isFailure());
    }

}
