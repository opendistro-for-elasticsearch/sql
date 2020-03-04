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

package com.amazon.opendistroforelasticsearch.ppl.request;

import org.elasticsearch.common.bytes.BytesArray;
import org.elasticsearch.rest.RestRequest;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class PPLRequestFactoryTest {

    @Mock
    private RestRequest restRequest;

    @Ignore("RestRequest is a final method, and Mockito 1.x cannot mock it." +
            "Ignore this test case till we can upgrade to Mockito 2.x")
    @Test
    public void testGeneratePPLRequestFromUrlParams() {
        String ppl = "{\"query\": \"search source=accounts age>30\"}";
        Mockito.when(restRequest.method()).thenReturn(RestRequest.Method.GET);
        Mockito.when(restRequest.param("ppl")).thenReturn(ppl);

        PPLRequest pplRequest = PPLRequestFactory.getPPLRequest(restRequest);

        Assert.assertFalse(pplRequest instanceof com.amazon.opendistroforelasticsearch.ppl.request.PreparedStatementRequest);
        Assert.assertEquals(ppl, pplRequest.getPpl());
    }

    @Test
    public void testGeneratePPLRequestFromPayload() {
        String ppl = "{\"query\": \"search source=accounts age>30\"}";

        Mockito.when(this.restRequest.content()).thenReturn(new BytesArray(ppl));
        Mockito.when(this.restRequest.method()).thenReturn(RestRequest.Method.POST);

        PPLRequest pplRequest = PPLRequestFactory.getPPLRequest(this.restRequest);
        Assert.assertFalse(pplRequest instanceof com.amazon.opendistroforelasticsearch.ppl.request.PreparedStatementRequest);
        Assert.assertEquals("search source=accounts age>30", pplRequest.getPpl());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateSqlRequest_unsupportedHttpMethod() {
        Mockito.when(this.restRequest.method()).thenReturn(RestRequest.Method.PUT);
        PPLRequestFactory.getPPLRequest(this.restRequest);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGenerateSqlRequestWithInvalidJson() {
        String payload = "{\n" +
                "  \"query\": \"search source=accounts logic_expression\",\n";
        Mockito.when(this.restRequest.content()).thenReturn(new BytesArray(payload));
        Mockito.when(this.restRequest.method()).thenReturn(RestRequest.Method.POST);
        PPLRequestFactory.getPPLRequest(this.restRequest);
    }
}
