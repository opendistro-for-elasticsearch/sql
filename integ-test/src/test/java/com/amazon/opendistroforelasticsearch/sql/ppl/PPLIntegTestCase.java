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

package com.amazon.opendistroforelasticsearch.sql.ppl;

import com.amazon.opendistroforelasticsearch.sql.RestIntegTestCase;
import java.io.IOException;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.junit.Assert;
import static com.amazon.opendistroforelasticsearch.sql.TestUtils.getResponseBody;
import static com.amazon.opendistroforelasticsearch.sql.plugin.rest.RestPPLQueryAction.QUERY_API_ENDPOINT;

public abstract class PPLIntegTestCase extends RestIntegTestCase {

    protected String executeQuery(String query) throws IOException {
        Response response = client().performRequest(buildRequest(query));
        Assert.assertEquals(200, response.getStatusLine().getStatusCode());
        return getResponseBody(response, true);
    }

    protected Request buildRequest(String query) {
        Request request = new Request("POST", QUERY_API_ENDPOINT);
        request.setJsonEntity(query);
        RequestOptions.Builder restOptionsBuilder = RequestOptions.DEFAULT.toBuilder();
        restOptionsBuilder.addHeader("Content-Type", "application/json");
        request.setOptions(restOptionsBuilder);
        return request;
    }
}
